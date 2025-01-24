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

import imgui.extension.imnodes.ImNodes;
import org.jetbrains.annotations.NotNull;

public class Minimap extends Element {

    public final boolean enable;
    public final float size;
    public final int location;

    public Minimap(final @NotNull Layout root,
                   final @NotNull String id,
                   final @NotNull Boolean enable,
                   final @NotNull Float size,
                   final @NotNull Integer location) {
        super(root, id);
        this.enable = enable;
        this.size = size;
        this.location = location;
    }

    @Override
    protected void onShow() {
        if (enable)
            ImNodes.miniMap(size, location);
    }
}
