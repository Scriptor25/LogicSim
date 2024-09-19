package io.scriptor.imgui;

import io.scriptor.manager.EventManager;

import java.util.Arrays;
import java.util.Objects;

public class Layout {

    private final EventManager events;
    private final Element[] elements;

    public Layout(final EventManager events, final Element[] elements) {
        this.events = events;
        this.elements = elements;
    }

    public void show() {
        Arrays.stream(elements).forEach(Element::show);
    }

    public EventManager getEvents() {
        return events;
    }

    public <T extends Element> T findElement(final String id) {
        return Arrays.stream(elements)
                .filter(e -> id.startsWith(e.getId()))
                .map(e -> e.<T>findElement(id))
                .filter(Objects::nonNull)
                .findAny()
                .orElse(null);
    }
}
