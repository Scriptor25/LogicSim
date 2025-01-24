package io.scriptor.imgui;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Objects;

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
public record Enumeration(@NotNull String id, @NotNull Entry @NotNull [] entries) {

    public record Entry(@NotNull String name, int value) {
    }

    @Override
    public boolean equals(final @Nullable Object object) {
        if (this == object) return true;
        if (null == object) return false;
        if (!(object instanceof Enumeration that)) return false;
        return Objects.equals(id, that.id) && Objects.deepEquals(entries, that.entries);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, Arrays.hashCode(entries));
    }

    @Override
    public String toString() {
        return "Enumeration{" +
                "id='" + id + '\'' +
                ", entries=" + Arrays.toString(entries) +
                '}';
    }
}
