package io.scriptor.imgui;

import imgui.ImGui;

public class Selectable extends Element {

    private final String label;
    private final String event;

    public Selectable(final Layout root, final String id, final String label, final String event) {
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
