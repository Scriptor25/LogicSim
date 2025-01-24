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

import io.scriptor.util.IYamlNode;
import io.scriptor.util.RTException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Objects;

public record Component(
        @NotNull String id,
        @NotNull Class<?> clazz,
        @NotNull Field @NotNull [] fields,
        @NotNull IYamlNode elementsYaml
) {

    public record Field(
            @NotNull String name,
            @NotNull String type,
            boolean array,
            boolean hasDefault,
            @NotNull Object defaultValue
    ) {

        public Field index(final int i) {
            if (!array)
                throw new RTException("cannot index into non-array field value");
            return new Field(name + '[' + i + ']', type, false, false, new Object());
        }
    }

    @Override
    public boolean equals(final @Nullable Object object) {
        if (this == object)
            return true;
        if (null == object)
            return false;
        if (!(object instanceof Component component))
            return false;
        return Objects.equals(id, component.id)
                && Objects.equals(clazz, component.clazz)
                && Objects.equals(elementsYaml, component.elementsYaml)
                && Objects.deepEquals(fields, component.fields);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, clazz, Arrays.hashCode(fields), elementsYaml);
    }

    @Override
    public String toString() {
        return "Component{id='%s', clazz=%s, fields=%s, elementsYaml=%s}".formatted(
                id,
                clazz,
                Arrays.toString(fields),
                elementsYaml);
    }
}
