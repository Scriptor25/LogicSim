package io.scriptor.graph;

import io.scriptor.util.RTException;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public record Pin(@NotNull INode node, int index, boolean output) {

    public int id() {
        return hashCode();
    }

    public boolean powered(final @NotNull Graph graph) {
        return node.powered(graph, output, index);
    }

    public boolean uses(final @NotNull INode node) {
        return node == this.node;
    }

    public @NotNull Optional<Pin> predecessor(final @NotNull Graph graph) {
        if (output) throw new RTException("pin is in output mode, does not have predecessor");
        return graph.findLink(this).map(Link::source);
    }

    public @NotNull List<Pin> successors(final @NotNull Graph graph) {
        if (!output) throw new RTException("pin is in input mode, does not have successors");
        return graph.findLinks(this).stream().map(Link::target).toList();
    }
}
