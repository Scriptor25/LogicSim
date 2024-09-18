package io.scriptor.imgui.component;

import imgui.ImGui;
import io.scriptor.imgui.Element;
import io.scriptor.imgui.Layout;

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
    public void show() {
        if (ImGui.begin(title))
            for (final var element : elements)
                element.show();
        getEvents().runTasks();
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
