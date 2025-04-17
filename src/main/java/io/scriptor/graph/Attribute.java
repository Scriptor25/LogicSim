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
package io.scriptor.graph;

import imgui.type.ImInt;
import imgui.type.ImString;
import io.scriptor.util.IUnique;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import static io.scriptor.util.IO.*;

public record Attribute(
        @NotNull UUID uuid,
        @NotNull ImString label,
        boolean output,
        byte bitwidth,
        @NotNull ImInt data
) implements IUnique {

    public Attribute(final @NotNull String label, final boolean output, final byte bitwidth) {
        this(UUID.randomUUID(), new ImString(label), output, bitwidth, new ImInt());
    }

    public Attribute(final @NotNull UUID uuid, final @NotNull String label, final boolean output, final byte bitwidth) {
        this(uuid, new ImString(label), output, bitwidth, new ImInt());
    }

    public boolean input() {
        return !output;
    }

    public @NotNull Attribute copy() {
        return new Attribute(UUID.randomUUID(), new ImString(label.get()), output, bitwidth, new ImInt());
    }

    public void write(final @NotNull OutputStream stream) throws IOException {
        writeUUID(stream, uuid);
        writeString(stream, label.get());
        writeBool(stream, output);
        writeByte(stream, bitwidth);
    }

    public static @NotNull Attribute read(final @NotNull InputStream stream) throws IOException {
        final var uuid = readUUID(stream);
        final var label = readString(stream);
        final var output = readBool(stream);
        final var bitwidth = readByte(stream);
        return new Attribute(uuid, label, output, bitwidth);
    }

    @Override
    public @NotNull String toString() {
        return label.get();
    }
}
