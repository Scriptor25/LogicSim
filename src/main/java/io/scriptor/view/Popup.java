package io.scriptor.view;

import imgui.ImGui;
import io.scriptor.event.EventManager;
import org.jetbrains.annotations.NotNull;

public class Popup extends View {

    private final Runnable content;

    public Popup(final @NotNull EventManager events, final @NotNull Runnable content) {
        super(events);
        this.content = content;
    }

    public @NotNull String id() {
        return "popup.%d".formatted(hashCode());
    }

    public void open() {
        ImGui.openPopup(id());
    }

    @Override
    public void show() {
        if (!ImGui.beginPopup(id()))
            return;
        content.run();
        ImGui.endPopup();
    }
}
