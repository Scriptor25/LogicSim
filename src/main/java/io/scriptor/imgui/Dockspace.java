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
import io.scriptor.util.RTException;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class Dockspace extends Element {

    public final int viewport;
    public final int flags;

    public Dockspace(final @NotNull Layout root, final @NotNull String id, final @NotNull Integer viewport, final @NotNull Integer @NotNull [] flags) {
        super(root, id);
        this.viewport = viewport;
        this.flags = Arrays
                .stream(flags)
                .mapToInt(Integer::intValue)
                .reduce(0, (a, b) -> a | b);
    }

    @Override
    protected void onShow() {
        ImGui.dockSpaceOverViewport(switch (viewport) {
            case 0 -> ImGui.getMainViewport();
            case 1 -> ImGui.getWindowViewport();
            default -> throw new RTException("invalid viewport id '%d'", viewport);
        }, flags);
    }
}
