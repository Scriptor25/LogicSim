package io.scriptor.imgui;

import imgui.ImGui;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Optional;

public class Popup extends Element {

    private final Element[] elements;

    public Popup(final @NotNull Layout root, final @NotNull String id, final @NotNull Element @NotNull [] elements) {
        super(root, id);
        this.elements = elements;
    }

    @Override
    protected void onStart() {
        Arrays.stream(elements).forEach(Element::start);
    }

    @Override
    protected void onShow() {
        if (ImGui.beginPopup(getId())) {
            Arrays.stream(elements).forEach(Element::show);
            ImGui.endPopup();
        }
    }

    @Override
    public @NotNull <T extends Element> Optional<T> findElement(final @NotNull String id, final @NotNull Class<T> type) {
        return Arrays.stream(elements)
                .filter(e -> id.startsWith(e.getId()))
                .map(e -> e.findElement(id, type))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findAny();
    }
}
