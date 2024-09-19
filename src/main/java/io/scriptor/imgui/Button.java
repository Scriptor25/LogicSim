package io.scriptor.imgui;

import imgui.ImGui;
import io.scriptor.imgui.Element;
import io.scriptor.imgui.Layout;

public class Button extends Element {

    private final String label;
    private final String event;

    public Button(final Layout root, final String id, final String label, final String event) {
        super(root, id);
        this.label = label;
        this.event = id + '.' + event;
    }

    @Override
    public void show() {
        if (ImGui.button(label))
            getEvents().invoke(event, this);
    }
}
