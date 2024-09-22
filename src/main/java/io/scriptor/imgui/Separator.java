package io.scriptor.imgui;

import imgui.ImGui;

public class Separator extends Element {

    public Separator(final Layout root, final String id) {
        super(root, id);
    }

    @Override
    protected void onShow() {
        ImGui.separator();
    }
}
