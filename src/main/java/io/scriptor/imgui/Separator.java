package io.scriptor.imgui;

import imgui.ImGui;
import io.scriptor.imgui.Element;
import io.scriptor.imgui.Layout;

public class Separator extends Element {

    public Separator(final Layout root, final String id) {
        super(root, id);
    }

    @Override
    public void show() {
        ImGui.separator();
    }
}
