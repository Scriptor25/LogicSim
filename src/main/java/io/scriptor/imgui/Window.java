package io.scriptor.imgui;

import imgui.ImGui;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Optional;

public class Window extends Element {

    public final String title;
    public final Element[] elements;

    public Window(final @NotNull Layout root,
                  final @NotNull String id,
                  final @NotNull String title,
                  final @NotNull Element @NotNull [] elements) {
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
    public @NotNull <T extends Element> Optional<T> findElement(final @NotNull String id, final @NotNull Class<T> type) {
        return Arrays.stream(elements)
                .filter(e -> id.startsWith(e.getId()))
                .map(e -> e.findElement(id, type))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findAny();
    }
}
