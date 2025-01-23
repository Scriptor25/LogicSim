package io.scriptor.imgui;

import imgui.ImGui;
import org.jetbrains.annotations.NotNull;

public class SameLine extends Element {

    public SameLine(final @NotNull Layout root, final @NotNull String id) {
        super(root, id);
    }

    @Override
    protected void onShow() {
        ImGui.sameLine();
    }
}
