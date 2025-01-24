package io.scriptor.imgui;

import imgui.ImGui;
import imgui.ImGuiViewport;
import io.scriptor.util.RTException;
import org.jetbrains.annotations.NotNull;

/**
 * This class is part of the <a href="https://github.com/Scriptor25/LogicSim">Java Logic Sim</a> project.
 * <p>
 * Copyright (C) 2025  Felix Schreiber
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <a href="https://www.gnu.org/licenses/">https://www.gnu.org/licenses/</a>.
 *
 * @author Felix Schreiber
 */
public class Dockspace extends Element {

    public final int viewport;
    public final int flags;

    public Dockspace(final @NotNull Layout root, final @NotNull String id, final @NotNull Integer viewport, final @NotNull Integer @NotNull [] flags) {
        super(root, id);
        this.viewport = viewport;
        int f = 0;
        for (final var flag : flags) f |= flag;
        this.flags = f;
    }

    @Override
    protected void onShow() {
        final ImGuiViewport v;
        if (viewport == 0) v = ImGui.getMainViewport();
        else if (viewport == 1) v = ImGui.getWindowViewport();
        else throw new RTException("invalid viewport id '%d'", viewport);

        ImGui.dockSpaceOverViewport(v, flags);
    }
}
