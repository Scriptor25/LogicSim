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

import imgui.ImColor;
import imgui.ImGui;
import io.scriptor.event.IPayload;
import org.jetbrains.annotations.NotNull;

public class ColorEdit extends Element {

    public record Payload(@NotNull ColorEdit self, int value) implements IPayload {
    }

    private final String label;
    private final String event;
    private final float[] col = new float[3];

    public ColorEdit(final @NotNull Layout root, final @NotNull String id, final @NotNull String label, final @NotNull String event) {
        super(root, id);
        this.label = label;
        this.event = id + '.' + event;
    }

    public void color(final float r, final float g, final float b) {
        col[0] = r;
        col[1] = g;
        col[2] = b;
    }

    public void color(final int rgb) {
        final var s = 1.f / 255.f;
        col[0] = s * (rgb >> 16 & 0xff);
        col[1] = s * (rgb >> 8 & 0xff);
        col[2] = s * (rgb & 0xff);
    }

    public int color() {
        return ImColor.rgb(col[2], col[1], col[0]);
    }

    @Override
    protected void onShow() {
        if (ImGui.colorEdit3(label, col))
            getEvents().invokeEvent(event, new Payload(this, color()));
    }
}
