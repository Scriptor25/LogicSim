package io.scriptor.graph;

import java.util.List;
import java.util.Optional;

public record Pin(INode node, int index, boolean output) {

    public int id() {
        return hashCode();
    }

    public boolean uses(final INode node) {
        return node == this.node;
    }

    public Optional<Pin> predecessor(final Graph graph) {
        if (output) throw new IllegalStateException();
        return graph.findLink(this).map(Link::source);
    }

    public List<Pin> successors(final Graph graph) {
        if (!output) throw new IllegalStateException();
        return graph.findLinks(this).stream().map(Link::target).toList();
    }
}
