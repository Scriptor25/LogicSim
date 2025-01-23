package io.scriptor.imgui;

import imgui.ImGui;
import imgui.type.ImInt;
import io.scriptor.util.Range;
import org.jetbrains.annotations.NotNull;

public class Array extends Element {

    private final String event;
    private Range<?> range;

    public Array(final @NotNull Layout root, final @NotNull String id, final @NotNull String event) {
        super(root, id);
        this.event = id + '.' + event;
    }

    public void setRange(final @NotNull Range<?> range) {
        this.range = range;
    }

    @Override
    protected void onShow() {
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
