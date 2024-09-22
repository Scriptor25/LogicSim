package io.scriptor.imgui;

import imgui.ImGui;

public class SameLine extends Element {

    public SameLine(final Layout root, final String id) {
        super(root, id);
    }

    @Override
    protected void onShow() {
        ImGui.sameLine();
    }
}
