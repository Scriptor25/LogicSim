package io.scriptor.imgui;

import imgui.ImGui;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Optional;

public class Child extends Element {

    private final Element[] elements;

    public Child(final @NotNull Layout root, final @NotNull String id, final @NotNull Element @NotNull [] elements) {
        super(root, id);
        this.elements = elements;
    }

    @Override
    protected void onStart() {
        Arrays.stream(elements).forEach(Element::start);
    }

    @Override
    protected void onShow() {
        if (ImGui.beginChild(getId()))
            Arrays.stream(elements).forEach(Element::show);
        ImGui.endChild();
    }

    @Override
    public @NotNull <T extends Element> Optional<T> findElement(final @NotNull String id) {
        return Arrays.stream(elements)
                .filter(e -> id.startsWith(e.getId()))
                .map(e -> e.<T>findElement(id))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findAny();
    }
}
