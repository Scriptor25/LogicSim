package io.scriptor.imgui;

import io.scriptor.manager.EventManager;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public class Layout {

    private final EventManager events;
    private final List<Element> elements;

    public Layout(final @NotNull EventManager events, final @NotNull List<Element> elements) {
        this.events = events;
        this.elements = elements;
    }

    public void start() {
        elements.forEach(Element::start);
    }

    public void show() {
        elements.forEach(Element::show);
    }

    public @NotNull EventManager getEvents() {
        return events;
    }

    public @NotNull <T extends Element> Optional<T> findElement(final @NotNull String id) {
        return elements.stream()
                .filter(e -> id.startsWith(e.getId()))
                .map(e -> e.<T>findElement(id))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findAny();
    }
}
