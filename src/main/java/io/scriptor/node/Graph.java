package io.scriptor.node;

import java.util.*;

public class Graph {

    private final List<INode> nodes = new ArrayList<>();
    private final List<Link> links = new ArrayList<>();

    public void show() {
        nodes.forEach(INode::show);
        links.forEach(Link::show);
    }

    public void clear() {
        nodes.clear();
        links.clear();
    }

    public void add(final INode node) {
        nodes.add(node);
    }

    public void add(final Link link) {
        links.add(link);
    }

    public void remove(final INode node) {
        nodes.remove(node);
        links.removeIf(link -> link.uses(node));
    }

    public void remove(final Link link) {
        links.remove(link);
    }

    public Optional<INode> findNode(final int id) {
        return nodes.stream()
                .filter(x -> x.id() == id)
                .findFirst();
    }

    public Optional<Link> findLink(final int id) {
        return links.stream()
                .filter(x -> x.id() == id)
                .findFirst();
    }

    public Optional<Link> findLink(final Pin pin) {
        return links.stream()
                .filter(x -> x.uses(pin))
                .findFirst();
    }

    public List<Link> findLinks(final Pin pin) {
        return links.stream()
                .filter(x -> x.uses(pin))
                .toList();
    }

    public Optional<Pin> findPin(final int id) {
        return nodes.stream()
                .map(x -> x.pin(id))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst();
    }

    public Graph copy(final INode[] nodes) {
        final var graph = new Graph();

        final Map<INode, INode> copies = new HashMap<>();
        for (final var node : nodes)
            copies.put(node, node.copy());

        for (final var link : links) {
            if (!link.uses(nodes)) continue;
            final var sourcePin = link.source();
            final var targetPin = link.target();
            final var source = copies.get(sourcePin.node());
            final var target = copies.get(targetPin.node());
            graph.add(new Link(
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
                    sourcePin.output() ? source.output(sourcePin.index()) : source.input(sourcePin.index()),
                    targetPin.output() ? target.output(targetPin.index()) : target.input(targetPin.index())
            ));
        }

        copies.values().forEach(this::add);
    }

    public void cycle() {
        final Queue<INode> callQueue = new ArrayDeque<>();

        for (final var node : nodes)
            if (node.noPredecessor(this))
                callQueue.add(node);

        while (!callQueue.isEmpty()) {
            final var next = callQueue.poll();
            next.cycle(this, callQueue);
        }
    }
}
