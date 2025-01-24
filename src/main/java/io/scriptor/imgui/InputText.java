/*
 * This file is part of https://github.com/Scriptor25/LogicSim
 * 
 * Copyright (C) 2025  Felix Schreiber
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see https://www.gnu.org/licenses/.
 */
package io.scriptor.imgui;

import imgui.ImGui;
import imgui.type.ImString;
import io.scriptor.event.IPayload;
import org.jetbrains.annotations.NotNull;

public class InputText extends Element {

    public record Payload(@NotNull InputText self, @NotNull String value) implements IPayload {
    }

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
            getEvents().invokeEvent(event, new Payload(this, buffer.get()));
        ImGui.setItemDefaultFocus();
    }
}
