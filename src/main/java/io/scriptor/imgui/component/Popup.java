package io.scriptor.imgui.component;

import imgui.ImGui;
import io.scriptor.imgui.Element;
import io.scriptor.imgui.Layout;

import java.util.Arrays;
import java.util.Objects;

public class Popup extends Element {

    private final Element[] elements;

    public Popup(final Layout root, final String id, final Element[] elements) {
        super(root, id);
        this.elements = elements;
    }

    @Override
    public void show() {
        if (ImGui.beginPopup(getId())) {
            for (final var element : elements)
                element.show();
            ImGui.endPopup();
        }
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
