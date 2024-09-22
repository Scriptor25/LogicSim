package io.scriptor.imgui;

import io.scriptor.manager.EventManager;

import java.util.List;
import java.util.Objects;

public class Layout {

    private final EventManager events;
    private final List<Element> elements;

    public Layout(final EventManager events, final List<Element> elements) {
        this.events = events;
        this.elements = elements;
    }

    public void start() {
        elements.forEach(Element::start);
    }

    public void show() {
        elements.forEach(Element::show);
    }

    public EventManager getEvents() {
        return events;
    }

    public <T extends Element> T findElement(final String id) {
        return elements.stream()
                .filter(e -> id.startsWith(e.getId()))
                .map(e -> e.<T>findElement(id))
                .filter(Objects::nonNull)
                .findAny()
                .orElse(null);
    }
}
