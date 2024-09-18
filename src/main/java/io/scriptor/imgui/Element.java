package io.scriptor.imgui;

import io.scriptor.EventManager;

public abstract class Element {

    private final Layout root;
    private final String id;

    protected Element(final Layout root, final String id) {
        this.root = root;
        this.id = id;
    }

    public Layout getRoot() {
        return root;
    }

    public String getId() {
        return id;
    }

    public String getParentId() {
        return id.substring(0, id.lastIndexOf('.'));
    }

    public EventManager getEvents() {
        return root.getEvents();
    }

    public abstract void show();

    public <T extends Element> T findElement(final String id) {
        if (id.equals(this.id))
            return (T) this;
        return null;
    }
}
