package io.scriptor.graph;

import io.scriptor.context.Registry;
import io.scriptor.context.State;
import io.scriptor.function.Function;
import io.scriptor.util.IUnique;
import io.scriptor.util.Range;

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

    public Graph(final Registry registry) {
        this(registry, UUID.randomUUID(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
    }

    public Graph(final Registry registry, final UUID uuid, final List<Attribute> attributes, final List<INode> nodes, final List<Link> links) {
        this.registry = registry;
        this.uuid = uuid;
        this.attributes = attributes;
        this.nodes = nodes;
        this.links = links;
    }

    public UUID uuid() {
        return uuid;
    }

    public void attributes(final Range<Attribute> range) {
        range.collection(attributes);
    }

    public Stream<Attribute> inputs() {
        return attributes.stream().filter(Attribute::input);
    }

    public Stream<Attribute> outputs() {
        return attributes.stream().filter(Attribute::output);
    }

    public void show() {
        nodes.forEach(node -> node.show(this));
        links.forEach(Link::show);
    }

    public void clear() {
        nodes.clear();
        links.clear();
        attributes.clear();
        function = null;
    }

    public void add(final INode node) {
        if (!nodes.contains(node)) {
            nodes.add(node);
            function = null;
        }
    }

    public void add(final Link link) {
        if (!links.contains(link)) {
            links.add(link);
            function = null;
        }
    }

    public void add(final Attribute attribute) {
        if (!attributes.contains(attribute)) {
            attributes.add(attribute);
            function = null;
        }
    }

    public void remove(final INode node) {
        if (!nodes.remove(node)) return;
        links.removeIf(link -> link.uses(node));
        function = null;
    }

    public void remove(final Link link) {
        if (!links.remove(link)) return;
        function = null;
    }

    public void remove(final Attribute attribute) {
        if (!attributes.remove(attribute)) return;
        function = null;
    }

    public Optional<INode> findNode(final int id) {
        return nodes.stream()
                .filter(node -> node.id() == id)
                .findFirst();
    }

    public Optional<Link> findLink(final int id) {
        return links.stream()
                .filter(link -> link.id() == id)
                .findFirst();
    }

    public Optional<Link> findLink(final Pin pin) {
        return links.stream()
                .filter(link -> link.uses(pin))
                .findFirst();
    }

    public List<Link> findLinks(final Pin pin) {
        return links.stream()
                .filter(link -> link.uses(pin))
                .toList();
    }

    public Optional<Pin> findPin(final int id) {
        return nodes.stream()
                .map(node -> node.pin(id))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst();
    }

    public Stream<INode> findEntryPoints() {
        return nodes.stream().filter(node -> node.noPredecessor(this));
    }

    public Stream<INode> findExitPoints() {
        return nodes.stream().filter(node -> node.noSuccessors(this));
    }

    public Graph copy() {
        final var graph = copy(nodes.toArray(INode[]::new));
        attributes.forEach(graph::add);
        return graph;
    }

    public Graph copy(final INode[] nodes) {
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
                    sourcePin.output() ? source.output(sourcePin.index()) : source.input(sourcePin.index()),
                    targetPin.output() ? target.output(targetPin.index()) : target.input(targetPin.index())
            ));
        }

        copies.values().forEach(graph::add);

        return graph;
    }

    public void paste(final Graph graph) {

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
                    sourcePin.output() ? source.output(sourcePin.index()) : source.input(sourcePin.index()),
                    targetPin.output() ? target.output(targetPin.index()) : target.input(targetPin.index())
            ));
        }

        copies.values().forEach(this::add);
    }

    public Function compile(final boolean store) {
        final var fn = new Function(
                store ? registry : null,
                uuid,
                inputs()
                        .map(Attribute::uuid)
                        .toArray(UUID[]::new),
                outputs()
                        .map(Attribute::uuid)
                        .toArray(UUID[]::new)
        );

        findExitPoints().forEach(node -> node.compile(this, fn, new HashSet<>()));
        return fn;
    }

    public void cycle() {
        if (function == null) {
            function = compile(false);
            state = new State(registry);
        }

        final var inputs = attributes.stream().filter(Attribute::input).toArray(Attribute[]::new);
        final var outputs = attributes.stream().filter(Attribute::output).toArray(Attribute[]::new);

        final var in = new boolean[inputs.length];
        for (int i = 0; i < inputs.length; i++) in[i] = inputs[i].powered().get();

        final var out = new boolean[outputs.length];
        function.exec(state, 0, in, out);

        for (int i = 0; i < outputs.length; i++) outputs[i].powered().set(out[i]);
    }
}
