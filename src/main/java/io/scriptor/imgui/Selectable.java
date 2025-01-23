package io.scriptor.imgui;

import imgui.ImGui;
import org.jetbrains.annotations.NotNull;

public class Selectable extends Element {

    private final String label;
    private final String event;

    public Selectable(final @NotNull Layout root,
                      final @NotNull String id,
                      final @NotNull String label,
                      final @NotNull String event) {
        super(root, id);
        this.label = label;
        this.event = id + '.' + event;
    }

    @Override
    protected void onShow() {
        if (ImGui.selectable(label))
            getEvents().invoke(event, this);
    }
}
