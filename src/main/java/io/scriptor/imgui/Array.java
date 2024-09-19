package io.scriptor.imgui;

import imgui.ImGui;
import imgui.type.ImInt;
import io.scriptor.imgui.Element;
import io.scriptor.imgui.Layout;
import io.scriptor.util.Range;

public class Array extends Element {

    private final String event;
    private Range<?> range;

    public Array(final Layout root, final String id, final String event) {
        super(root, id);
        this.event = id + '.' + event;
    }

    public void setRange(final Range<?> range) {
        this.range = range;
    }

    @Override
    public void show() {
        if (range != null) {
            final var i = new ImInt();
            range.stream().forEach(value -> {
                ImGui.pushID(i.get());
                i.set(i.get() + 1);
                if (ImGui.selectable(value.toString()))
                    getEvents().invoke(event, this, value);
                ImGui.popID();
            });
        }
    }
}
