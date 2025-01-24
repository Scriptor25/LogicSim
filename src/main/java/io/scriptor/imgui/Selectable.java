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
import io.scriptor.event.IPayload;
import org.jetbrains.annotations.NotNull;

public class Selectable extends Element {

    public record Payload(@NotNull Selectable self) implements IPayload {
    }

    private final String label;
    private final String event;

    public Selectable(final @NotNull Layout root,
                      final @NotNull String id,
                      final @NotNull String label,
                      final @NotNull String event) {
        super(root, id);
        this.label = label;
        this.event = id + '.' + event;
    }

    @Override
    protected void onShow() {
        if (ImGui.selectable(label))
            getEvents().invokeEvent(event, new Payload(this));
    }
}
