package io.scriptor.view;

import io.scriptor.event.EventManager;
import org.jetbrains.annotations.NotNull;

public abstract class View {

    protected final EventManager events;

    protected View(final @NotNull EventManager events) {
        this.events = events;
    }

    public abstract void show();
}