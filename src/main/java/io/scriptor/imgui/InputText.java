package io.scriptor.imgui;

import imgui.ImGui;
import imgui.type.ImString;
import org.jetbrains.annotations.NotNull;

public class InputText extends Element {

    private final String label;
    private final int flags;
    private final String event;

    private final ImString buffer = new ImString();

    public InputText(final @NotNull Layout root,
                     final @NotNull String id,
                     final @NotNull String label,
                     final @NotNull Integer @NotNull [] flags,
                     final @NotNull String event) {
        super(root, id);
        this.label = label;
        int f = 0;
        for (final var flag : flags) f |= flag;
        this.flags = f;
        this.event = id + '.' + event;
    }

    public @NotNull String get() {
        return buffer.get();
    }

    public void set(final @NotNull String string) {
        buffer.set(string, true);
    }

    @Override
    protected void onShow() {
        ImGui.setKeyboardFocusHere();
        if (ImGui.inputText(label, buffer, flags))
            getEvents().invokeEvent(event, this, buffer.get());
        ImGui.setItemDefaultFocus();
    }
}
