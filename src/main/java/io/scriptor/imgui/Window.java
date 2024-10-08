package io.scriptor.imgui;

import imgui.ImGui;

import java.util.Arrays;
import java.util.Objects;

public class Window extends Element {

    public final String title;
    public final Element[] elements;

    public Window(final Layout root, final String id, final String title, final Element[] elements) {
        super(root, id);
        this.title = title;
        this.elements = elements;
    }

    @Override
    protected void onStart() {
        Arrays.stream(elements).forEach(Element::start);
    }

    @Override
    protected void onShow() {
        if (ImGui.begin(title)) {
            Arrays.stream(elements).forEach(Element::show);
            getEvents().runTasks();
        }
        ImGui.end();
    }

    @Override
    public <T extends Element> T findElement(final String id) {
        return Arrays.stream(elements)
                .filter(e -> id.startsWith(e.getId()))
                .map(e -> e.<T>findElement(id))
                .filter(Objects::nonNull)
                .findAny()
                .orElse(null);
    }
}
