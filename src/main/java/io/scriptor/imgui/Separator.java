package io.scriptor.imgui;

import imgui.ImGui;
import org.jetbrains.annotations.NotNull;

public class Separator extends Element {

    public Separator(final @NotNull Layout root, final @NotNull String id) {
        super(root, id);
    }

    @Override
    protected void onShow() {
        ImGui.separator();
    }
}
