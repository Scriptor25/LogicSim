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
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import static io.scriptor.util.IO.*;

public record SetRegInstruction(
        @NotNull UUID uuid,
        @NotNull UUID reg,
        int index,
        @NotNull Instruction value
) implements Instruction {

    public static void read(final @NotNull InputStream stream, final @NotNull Function function) throws IOException {
        final var uuid = readUUID(stream);
        final var register = readUUID(stream);
        final var index = readInt(stream);
        final var valueUUID = readUUID(stream);
        final var value = function
                .get(valueUUID)
                .orElseGet(() -> new ConstInstruction(valueUUID, false));
        function.add(new SetRegInstruction(uuid, register, index, value));
    }

    public SetRegInstruction(final @NotNull UUID reg, final int index, final @NotNull Instruction value) {
        this(UUID.randomUUID(), reg, index, value);
    }

    @Override
    public void write(final @NotNull OutputStream stream) throws IOException {
        Instruction.super.write(stream);
        writeUUID(stream, reg);
        writeInt(stream, index);
        writeUUID(stream, value.uuid());
    }

    @Override
    public void exec(final @NotNull State state) {
        state.setRegister(reg, index, value.get(state));
    }
}
