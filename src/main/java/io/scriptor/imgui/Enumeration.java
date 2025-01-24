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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Objects;

public record Enumeration(@NotNull String id, @NotNull Entry @NotNull [] entries) {

    public record Entry(@NotNull String name, int value) {
    }

    @Override
    public boolean equals(final @Nullable Object object) {
        if (this == object)
            return true;
        if (null == object)
            return false;
        if (!(object instanceof Enumeration that))
            return false;
        return Objects.equals(id, that.id) && Objects.deepEquals(entries, that.entries);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, Arrays.hashCode(entries));
    }

    @Override
    public String toString() {
        return "Enumeration{id='%s', entries=%s}".formatted(id, Arrays.toString(entries));
    }
}
