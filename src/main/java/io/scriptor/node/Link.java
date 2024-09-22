package io.scriptor.node;

import imgui.extension.imnodes.ImNodes;

public record Link(Pin source, Pin target) {

    public int id() {
        return hashCode();
    }

    public void show() {
        ImNodes.link(hashCode(), source.id(), target.id());
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
