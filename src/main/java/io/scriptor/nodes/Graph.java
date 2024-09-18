package io.scriptor.nodes;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class Graph {

    private final List<Node> nodes = new ArrayList<>();
    private final List<Link> links = new ArrayList<>();

    public Stream<Node> getNodes() {
        return nodes.stream();
    }

    public Stream<Link> getLinks() {
        return links.stream();
    }

    public Node createNode(final String label) {
        final var node = new Node(label);
        nodes.add(node);
        return node;
    }

    public void createLink(final Pin source, final Pin target) {
        final var link = new Link(source, target);
        links.add(link);
    }

    public void createLink(final int sourceNodeId, final int sourcePinId, final int targetNodeId, final int targetPinId) {
        final var sourceNode = findNode(sourceNodeId);
        final var targetNode = findNode(targetNodeId);
        if (sourceNode.isPresent() && targetNode.isPresent()) {
            final var sourcePin = sourceNode.get().findPin(sourcePinId);
            final var targetPin = targetNode.get().findPin(targetPinId);
            if (sourcePin.isPresent() && targetPin.isPresent())
                createLink(sourcePin.get(), targetPin.get());
        }
    }

    public void deleteNode(final Node node) {
        nodes.remove(node);
        final var toDelete = getLinks()
                .filter(link -> node.getPins().anyMatch(pin -> pin.getId() == link.getSource() || pin.getId() == link.getTarget()))
                .toArray(Link[]::new);
        for (final var link : toDelete) deleteLink(link);
    }

    public void deleteLink(final Link link) {
        links.remove(link);
    }

    public void deletePin(final Pin pin) {
        getNodes()
                .filter(node -> node.findPin(pin.getId()).isPresent())
                .findAny()
                .ifPresent(node -> node.deletePin(pin));
        final var toDelete = getLinks()
                .filter(link -> pin.getId() == link.getSource() || pin.getId() == link.getTarget())
                .toArray(Link[]::new);
        for (final var link : toDelete) deleteLink(link);
    }

    public Optional<Node> findNode(final int id) {
        return getNodes().filter(node -> node.getId() == id).findAny();
    }

    public Optional<Link> findLink(final int id) {
        return getLinks().filter(link -> link.getId() == id).findAny();
    }

    public Optional<Pin> findPin(final int id) {
        return getNodes()
                .filter(node -> node.getPins().anyMatch(pin -> pin.getId() == id))
                .map(node -> node.getPins().filter(pin -> pin.getId() == id).findAny())
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findAny();
    }

    public void show() {
        getNodes().forEach(Node::show);
        getLinks().forEach(Link::show);
    }
}
