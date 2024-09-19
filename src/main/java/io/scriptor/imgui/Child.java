package io.scriptor.imgui;

import imgui.ImGui;
import io.scriptor.imgui.Element;
import io.scriptor.imgui.Layout;

import java.util.Arrays;
import java.util.Objects;

public class Child extends Element {

    private final Element[] elements;

    public Child(final Layout root, final String id, final Element[] elements) {
        super(root, id);
        this.elements = elements;
    }

    @Override
    public void show() {
        if (ImGui.beginChild(getId()))
            Arrays.stream(elements).forEach(Element::show);
        ImGui.endChild();
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
