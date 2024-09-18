package io.scriptor.imgui.component;

import imgui.ImGui;
import imgui.type.ImString;
import io.scriptor.imgui.Element;
import io.scriptor.imgui.Layout;

public class InputText extends Element {

    private final String label;
    private final int flags;
    private final String event;

    private final ImString buffer = new ImString();

    public InputText(final Layout root, final String id, final String label, final Integer[] flags, final String event) {
        super(root, id);
        this.label = label;
        int f = 0;
        for (final var flag : flags) f |= flag;
        this.flags = f;
        this.event = event;
    }

    public String get() {
        return buffer.get();
    }

    @Override
    public void show() {
        if (ImGui.inputText(label, buffer, flags))
            getEvents().invoke(getParentId() + '.' + event);
    }
}
