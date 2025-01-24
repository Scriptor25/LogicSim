/*
 * This file is part of https://github.com/Scriptor25/LogicSim
 * 
 * Copyright (C) 2025  Felix Schreiber
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see https://www.gnu.org/licenses/.
 */
package io.scriptor.graph;

import io.scriptor.context.Registry;
import io.scriptor.context.State;
import io.scriptor.function.Function;
import io.scriptor.util.IUnique;
import io.scriptor.util.Range;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Stream;

public class Graph implements IUnique {

    private final Registry registry;

    private final UUID uuid;
    private final List<Attribute> attributes;
    private final List<INode> nodes;
    private final List<Link> links;

    private Function function;
    private State state;

    public Graph(final @NotNull Registry registry) {
        this(registry, UUID.randomUUID(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
    }

    public Graph(final @NotNull Registry registry,
                 final @NotNull UUID uuid,
                 final @NotNull List<Attribute> attributes,
                 final @NotNull List<INode> nodes,
                 final @NotNull List<Link> links) {
        this.registry = registry;
        this.uuid = uuid;
        this.attributes = attributes;
        this.nodes = nodes;
        this.links = links;

        this.state = new State(registry);
    }

    public @NotNull UUID uuid() {
        return uuid;
    }

    public void attributes(final @NotNull Range<Attribute> range) {
        range.collection(attributes);
    }

    public @NotNull Stream<INode> nodes() {
        return nodes.stream();
    }

    public @NotNull Stream<Link> links() {
        return links.stream();
    }

    public @NotNull Stream<Attribute> inputs() {
        return attributes.stream().filter(Attribute::input);
    }

    public @NotNull Stream<Attribute> outputs() {
        return attributes.stream().filter(Attribute::output);
    }

    public @NotNull State state() {
        return state;
    }

    public void show() {
        nodes.forEach(node -> node.show(this));
        links.forEach(link -> link.show(this));
    }

    public void clear() {
        nodes.clear();
        links.clear();
        attributes.clear();
        function = null;
    }

    public void add(final @NotNull INode node) {
        if (!nodes.contains(node)) {
            nodes.add(node);
            function = null;
        }
    }

    public void add(final @NotNull Link link) {
        if (!links.contains(link)) {
            links.add(link);
            function = null;
        }
    }

    public void add(final @NotNull Attribute attribute) {
        if (!attributes.contains(attribute)) {
            attributes.add(attribute);
            function = null;
        }
    }

    public void remove(final @NotNull INode node) {
        if (!nodes.remove(node)) return;
        links.removeIf(link -> link.uses(node));
        function = null;
    }

    public void remove(final @NotNull Link link) {
        if (!links.remove(link)) return;
        function = null;
    }

    public void remove(final @NotNull Attribute attribute) {
        if (!attributes.remove(attribute)) return;
        function = null;
    }

    public @NotNull Optional<INode> findNode(final int id) {
        return nodes.stream()
                .filter(node -> node.id() == id)
                .findFirst();
    }

    public @NotNull Optional<Link> findLink(final int id) {
        return links.stream()
                .filter(link -> link.id() == id)
                .findFirst();
    }

    public @NotNull Optional<Link> findLink(final @NotNull Pin pin) {
        return links.stream()
                .filter(link -> link.uses(pin))
                .findFirst();
    }

    public @NotNull List<Link> findLinks(final @NotNull Pin pin) {
        return links.stream()
                .filter(link -> link.uses(pin))
                .toList();
    }

    public @NotNull Optional<Pin> findPin(final int id) {
        return nodes.stream()
                .map(node -> node.pin(id))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst();
    }

    public @NotNull Stream<INode> findEntryPoints() {
        return nodes.stream().filter(node -> node.isBegin(this));
    }

    public @NotNull Stream<INode> findExitPoints() {
        return nodes.stream().filter(node -> node.isEnd(this));
    }

    public @NotNull Graph copy() {
        final var graph = copy(nodes.toArray(INode[]::new));
        attributes.forEach(graph::add);
        return graph;
    }

    public @NotNull Graph copy(final @NotNull INode @NotNull [] nodes) {
        final var graph = new Graph(registry);

        final Map<INode, INode> copies = new HashMap<>();
        for (final var node : nodes) copies.put(node, node.copy());

        for (final var link : links) {
            if (!link.uses(nodes)) continue;
            final var sourcePin = link.source();
            final var targetPin = link.target();
            final var source = copies.get(sourcePin.node());
            final var target = copies.get(targetPin.node());
            graph.add(new Link(
                    UUID.randomUUID(),
                    sourcePin.output()
                            ? source.output(sourcePin.index())
                            : source.input(sourcePin.index()),
                    targetPin.output()
                            ? target.output(targetPin.index())
                            : target.input(targetPin.index())
            ));
        }

        copies.values().forEach(graph::add);

        return graph;
    }

    public void paste(final @NotNull Graph graph) {

        final Map<INode, INode> copies = new HashMap<>();
        for (final var node : graph.nodes)
            copies.put(node, node.copy());

        for (final var link : graph.links) {
            if (!link.uses(graph.nodes.toArray(INode[]::new))) continue;
            final var sourcePin = link.source();
            final var targetPin = link.target();
            final var source = copies.get(sourcePin.node());
            final var target = copies.get(targetPin.node());
            add(new Link(
                    UUID.randomUUID(),
                    sourcePin.output()
                            ? source.output(sourcePin.index())
                            : source.input(sourcePin.index()),
                    targetPin.output()
                            ? target.output(targetPin.index())
                            : target.input(targetPin.index())
            ));
        }

        copies.values().forEach(this::add);
    }

    public @NotNull Function compile(final boolean store) {
        final var fn = new Function(
                uuid,
                inputs()
                        .map(Attribute::uuid)
                        .toArray(UUID[]::new),
                outputs()
                        .map(Attribute::uuid)
                        .toArray(UUID[]::new)
        );

        if (store)
            registry.add(fn);

        findExitPoints().forEach(node -> node.compile(this, fn, new HashSet<>()));
        return fn;
    }

    public void execJIT() {
        if (function == null) {
            function = compile(false);
            state = new State(registry);
        }

        final var inputs = attributes
                .stream()
                .filter(Attribute::input)
                .toArray(Attribute[]::new);
        final var outputs = attributes
                .stream()
                .filter(Attribute::output)
                .toArray(Attribute[]::new);

        final var in = new boolean[inputs.length];
        for (int i = 0; i < inputs.length; i++)
            in[i] = inputs[i]
                    .powered()
                    .get();

        final var out = new boolean[outputs.length];
        function.exec(state, in, out);

        for (int i = 0; i < outputs.length; i++)
            outputs[i]
                    .powered()
                    .set(out[i]);
    }

    public void exec() {
        findExitPoints().forEach(node -> node.exec(this, new HashSet<>()));
    }
}
