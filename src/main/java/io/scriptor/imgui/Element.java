package io.scriptor.imgui;

import io.scriptor.manager.EventManager;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public abstract class Element {

    private final Layout root;
    private final String id;

    protected Element(final @NotNull Layout root, final @NotNull String id) {
        this.root = root;
        this.id = id;
    }

    public @NotNull Layout getRoot() {
        return root;
    }

    public @NotNull String getId() {
        return id;
    }

    public @NotNull String getParentId() {
        return id.substring(0, id.lastIndexOf('.'));
    }

    public @NotNull EventManager getEvents() {
        return root.getEvents();
    }

    public void start() {
        onStart();
    }

    public void show() {
        onShow();
    }

    protected void onStart() {
    }

    protected abstract void onShow();

    public @NotNull <T extends Element> Optional<T> findElement(final @NotNull String id) {
        if (id.equals(this.id))
            return Optional.of((T) this);
        return Optional.empty();
    }
}
