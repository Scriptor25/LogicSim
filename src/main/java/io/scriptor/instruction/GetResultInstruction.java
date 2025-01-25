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

public record GetResultInstruction(
        @NotNull UUID uuid,
        @NotNull CallInstruction call,
        int index
) implements Instruction {

    public static void read(final @NotNull InputStream inputStream, final @NotNull Function function) throws IOException {
        final var uuid = IOStream.readUUID(inputStream);
        final var call = IOStream.readUUID(inputStream);
        final var index = IOStream.readInt(inputStream);
        final var callInstruction = function.find(call, CallInstruction.class);
        if (callInstruction == null)
            throw new RTException("invalid call instruction id %s", call);
        function.add(new GetResultInstruction(uuid, callInstruction, index));
    }

    public GetResultInstruction(final @NotNull CallInstruction call, final int index) {
        this(UUID.randomUUID(), call, index);
    }

    @Override
    public void write(final @NotNull OutputStream outputStream) throws IOException {
        Instruction.super.write(outputStream);
        IOStream.write(outputStream, call.uuid());
        IOStream.write(outputStream, index);
    }

    @Override
    public boolean get(final @NotNull State state) {
        return call.get(state, index);
    }
}
