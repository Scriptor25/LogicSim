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

import io.scriptor.context.Context;
import io.scriptor.context.State;
import io.scriptor.function.Function;
import io.scriptor.function.IFunction;
import io.scriptor.util.IUnique;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;
import java.util.stream.Stream;

import static io.scriptor.util.IO.*;

public class Graph implements IUnique {

    public static @NotNull Graph read(final @NotNull Context context, final @NotNull InputStream stream) throws IOException {
        final var uuid = readUUID(stream);
        final var graph = new Graph(context, uuid);

        final var attributeCount = readInt(stream);
        for (int i = 0; i < attributeCount; ++i)
            graph.add(Attribute.read(stream));

        final var nodeCount = readInt(stream);
        for (int i = 0; i < nodeCount; ++i)
            graph.add(Node.read(graph, stream));

        final var linkCount = readInt(stream);
        for (int i = 0; i < linkCount; ++i)
            graph.add(Link.read(graph, stream));

        return graph;
    }

    private final Context context;

    private final UUID uuid;
    private final List<Attribute> attributes;
    private final List<Node> nodes;
    private final List<Link> links;

    private IFunction function;
    private State state;

    public Graph(final @NotNull Context context) {
        this(context, UUID.randomUUID(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
    }

    public Graph(final @NotNull Context context, final @NotNull UUID uuid) {
        this(context, uuid, new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
    }

    public Graph(final @NotNull Context context,
                 final @NotNull UUID uuid,
                 final @NotNull List<Attribute> attributes,
                 final @NotNull List<Node> nodes,
                 final @NotNull List<Link> links) {
        this.context = context;
        this.uuid = uuid;
        this.attributes = attributes;
        this.nodes = nodes;
        this.links = links;

        this.state = new State(context);
    }

    public @NotNull Context context() {
        return context;
    }

    public @NotNull UUID uuid() {
        return uuid;
    }

    public @NotNull Collection<Attribute> attributes() {
        return attributes;
    }

    public @NotNull Stream<Node> nodes() {
        return nodes.stream();
    }

    public @NotNull Stream<Link> links() {
        return links.stream();
    }

    public @NotNull Stream<Attribute> inputs() {
        return attributes
                .stream()
                .filter(Attribute::input);
    }

    public @NotNull Optional<Attribute> input(final int index) {
        return inputs()
                .skip(index)
                .findFirst();
    }

    public @NotNull Stream<Attribute> outputs() {
        return attributes
                .stream()
                .filter(Attribute::output);
    }

    public @NotNull Optional<Attribute> output(final int index) {
        return outputs()
                .skip(index)
                .findFirst();
    }

    public @NotNull State state() {
        return state;
    }

    public void show() {
        nodes.forEach(node -> node.show(this));
        links.forEach(link -> link.show(this));

        saveNodePositions();
    }

    public void clear() {
        nodes.clear();
        links.clear();
        attributes.clear();
        function = null;
    }

    public void add(final @NotNull Node node) {
        if (nodes.contains(node))
            return;
        nodes.add(node);
        function = null;
    }

    public void add(final @NotNull Link link) {
        if (links.contains(link))
            return;
        links.add(link);
        function = null;
    }

    public void add(final @NotNull Attribute attribute) {
        if (attributes.contains(attribute))
            return;
        attributes.add(attribute);
        function = null;
    }

    public void remove(final @NotNull Node node) {
        if (!nodes.remove(node))
            return;
        links.removeIf(link -> link.uses(node));
        function = null;
    }

    public void remove(final @NotNull Link link) {
        if (!links.remove(link))
            return;
        function = null;
    }

    public void remove(final @NotNull Attribute attribute) {
        if (!attributes.remove(attribute))
            return;
        function = null;
    }

    public @NotNull Optional<Node> findNode(final int id) {
        return nodes
                .stream()
                .filter(node -> node.id() == id)
                .findAny();
    }

    public @NotNull Optional<Node> findNode(final @NotNull UUID uuid) {
        return nodes
                .stream()
                .filter(node -> node.same(uuid))
                .findAny();
    }

    public @NotNull Optional<Link> findLink(final int id) {
        return links
                .stream()
                .filter(link -> link.id() == id)
                .findAny();
    }

    public @NotNull Optional<Link> findLink(final @NotNull Pin pin) {
        return links
                .stream()
                .filter(link -> link.uses(pin))
                .findAny();
    }

    public @NotNull Stream<Link> findLinks(final @NotNull Pin pin) {
        return links
                .stream()
                .filter(link -> link.uses(pin));
    }

    public @NotNull Optional<Pin> findPin(final int id) {
        return nodes
                .stream()
                .map(node -> node.pin(id))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findAny();
    }

    public @NotNull Optional<Attribute> findAttribute(final @NotNull UUID uuid) {
        return attributes
                .stream()
                .filter(attribute -> attribute.same(uuid))
                .findAny();
    }

    public @NotNull Stream<Node> findEntryPoints() {
        return nodes
                .stream()
                .filter(node -> node.front(this));
    }

    public @NotNull Stream<Node> findExitPoints() {
        return nodes
                .stream()
                .filter(node -> node.back(this));
    }

    public @NotNull Graph copy() {
        final var graph = new Graph(context);

        final Map<Attribute, Attribute> attributeCopies = new HashMap<>();
        for (final var attribute : attributes) {
            final var copy = attribute.copy();
            graph.add(copy);
            attributeCopies.put(attribute, copy);
        }

        final Map<Node, Node> nodeCopies = new HashMap<>();
        for (final var node : nodes) {
            final var copy = node.copy(attributeCopies);
            graph.add(copy);
            nodeCopies.put(node, copy);
        }

        for (final var link : links)
            graph.add(link.copy(nodeCopies));

        return graph;
    }

    public void paste(final @NotNull Graph graph) {
        final var copy = graph.copy();
        copy
                .attributes
                .forEach(this::add);
        copy
                .nodes
                .forEach(this::add);
        copy
                .links
                .forEach(this::add);
    }

    public @NotNull IFunction compile() {
        final var fn = new Function(
                uuid,
                inputs()
                        .map(Attribute::uuid)
                        .toArray(UUID[]::new),
                outputs()
                        .map(Attribute::uuid)
                        .toArray(UUID[]::new));

        findExitPoints().forEach(node -> node.compile(this, fn));
        return fn;
    }

    public void execFn() {
        if (function == null) {
            function = compile();
            state = new State(context);
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
        findExitPoints().forEach(node -> node.exec(this));
    }

    public void write(final @NotNull OutputStream stream) throws IOException {
        writeUUID(stream, uuid);
        writeInt(stream, attributes.size());
        for (final var attribute : attributes)
            attribute.write(stream);
        writeInt(stream, nodes.size());
        for (final var node : nodes)
            node.write(stream);
        writeInt(stream, links.size());
        for (final var link : links)
            link.write(stream);
    }

    public void loadNodePositions() {
        nodes.forEach(Node::loadPosition);
    }

    public void saveNodePositions() {
        nodes.forEach(Node::savePosition);
    }
}
