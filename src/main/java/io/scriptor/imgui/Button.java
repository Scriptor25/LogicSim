package io.scriptor.imgui;

import imgui.ImGui;

public class Button extends Element {

    private final String label;
    private final String event;

    public Button(final Layout root, final String id, final String label, final String event) {
        super(root, id);
        this.label = label;
        this.event = id + '.' + event;
    }

    @Override
    protected void onShow() {
        if (ImGui.button(label))
            getEvents().invoke(event, this);
    }
}
