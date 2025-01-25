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
package io.scriptor.instruction;

import io.scriptor.context.State;
import io.scriptor.function.Function;
import io.scriptor.util.IOStream;
import io.scriptor.util.RTException;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public record SetRegInstruction(
        @NotNull UUID uuid,
        @NotNull UUID reg,
        int index,
        @NotNull Instruction value
) implements Instruction {

    public static void read(final @NotNull InputStream inputStream, final @NotNull Function function) throws IOException {
        final var uuid = IOStream.readUUID(inputStream);
        final var reg = IOStream.readUUID(inputStream);
        final var index = IOStream.readInt(inputStream);
        final var value = IOStream.readUUID(inputStream);
        final var valueInstruction = function.find(value);
        if (valueInstruction == null)
            throw new RTException("invalid value instruction id %s", value);
        function.add(new SetRegInstruction(uuid, reg, index, valueInstruction));
    }

    public SetRegInstruction(final @NotNull UUID reg, final int index, final @NotNull Instruction value) {
        this(UUID.randomUUID(), reg, index, value);
    }

    @Override
    public void write(final @NotNull OutputStream outputStream) throws IOException {
        Instruction.super.write(outputStream);
        IOStream.write(outputStream, reg);
        IOStream.write(outputStream, index);
        IOStream.write(outputStream, value.uuid());
    }

    @Override
    public void exec(final @NotNull State state) {
        state.setRegister(reg, index, value.get(state));
    }
}
