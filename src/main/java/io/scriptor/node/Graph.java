package io.scriptor.node;

import io.scriptor.Context;
import io.scriptor.IUnique;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class Graph implements IUnique {

    public static void read(final Context context, final BufferedReader in) throws IOException {
        final var uuid = UUID.fromString(in.readLine());
        final var nodeCount = Integer.parseInt(in.readLine());
        final var nodes = new ArrayList<INode>();
        for (int i = 0; i < nodeCount; ++i) {
            final var nodeUUID = UUID.fromString(in.readLine());
            context.<INode>getRef(nodeUUID).get(nodes::add);
        }
        final var linkCount = Integer.parseInt(in.readLine());
        final var links = new ArrayList<Link>();
        for (int i = 0; i < linkCount; ++i) {
            final var linkUUID = UUID.fromString(in.readLine());
            context.<Link>getRef(linkUUID).get(links::add);
        }
        context.getRef(uuid).set(new Graph(uuid, nodes, links));
    }

    private final UUID uuid;
    private final List<INode> nodes;
    private final List<Link> links;

    public Graph() {
        uuid = UUID.randomUUID();
        nodes = new ArrayList<>();
        links = new ArrayList<>();
    }

    public Graph(final UUID uuid, final List<INode> nodes, final List<Link> links) {
        this.uuid = uuid;
        this.nodes = nodes;
        this.links = links;
    }

    public UUID uuid() {
        return uuid;
    }

    public void show() {
        nodes.forEach(INode::show);
        links.forEach(Link::show);
    }

    public void clear() {
        nodes.clear();
        links.clear();
    }

    public void add(final INode node) {
        if (!nodes.contains(node))
            nodes.add(node);
    }

    public void add(final Link link) {
        if (!links.contains(link))
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

    public long cycle() {
        final var start = System.currentTimeMillis();
        final Queue<INode> callQueue = new ArrayDeque<>();

        for (final var node : nodes)
            if (node.noPredecessor(this))
                callQueue.add(node);

        final Set<INode> cycled = new HashSet<>();
        while (!callQueue.isEmpty()) {
            final var next = callQueue.poll();
            cycled.add(next);
            next.cycle(this, callQueue);

            for (final var node : cycled)
                callQueue.remove(node);
        }
        return System.currentTimeMillis() - start;
    }

    public void write(final Context context, final PrintWriter out) {
        out.println(uuid);
        out.println(nodes.size());
        for (final var node : nodes) {
            out.println(node.uuid());
            context.next(node);
        }
        out.println(links.size());
        for (final var link : links) {
            out.println(link.uuid());
            context.next(link);
        }
    }
}
