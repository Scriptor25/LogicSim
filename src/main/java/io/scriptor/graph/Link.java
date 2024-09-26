package io.scriptor.graph;

import imgui.extension.imnodes.ImNodes;
import imgui.extension.imnodes.flag.ImNodesCol;
import io.scriptor.Constants;
import io.scriptor.util.IUnique;

import java.util.UUID;

public record Link(UUID uuid, Pin source, Pin target) implements IUnique {

    public int id() {
        return uuid.hashCode();
    }

    public void show(final Graph graph) {
        final var powered = source.powered(graph);
        if (powered) ImNodes.pushColorStyle(ImNodesCol.Link, Constants.COLOR_POWERED);
        ImNodes.link(id(), source.id(), target.id());
        if (powered) ImNodes.popColorStyle();
    }

    public boolean uses(final INode node) {
        return source.uses(node) || target.uses(node);
    }

    public boolean uses(final INode[] nodes) {
        for (final var a : nodes) {
            for (final var b : nodes) {
                if (a == b) continue;
                if (source.node() == a && target.node() == b) return true;
                if (target.node() == a && source.node() == b) return true;
            }
        }
        return false;
    }

    public boolean uses(final Pin pin) {
        return source == pin || target == pin;
    }
}
