package io.scriptor.nodes;

import imgui.extension.imnodes.ImNodes;

import java.util.UUID;

public class Link {

    private final UUID id;
    private final Pin source;
    private final Pin target;

    public Link(final Pin source, final Pin target) {
        this.id = UUID.randomUUID();
        this.source = source;
        this.target = target;
    }

    public int getId() {
        return id.hashCode();
    }

    public int getSource() {
        return source.getId();
    }

    public int getTarget() {
        return target.getId();
    }

    public void show() {
        ImNodes.link(getId(), getSource(), getTarget());
    }
}
