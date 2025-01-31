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

public class CallInstruction implements Instruction {

    public static void read(final @NotNull InputStream stream, final @NotNull Function function) throws IOException {
        final var uuid = readUUID(stream);
        final var callee = readUUID(stream);
        final var args = new Instruction[readInt(stream)];
        for (int i = 0; i < args.length; ++i)
            args[i] = function.find(readUUID(stream));
        function.add(new CallInstruction(uuid, callee, args));
    }

    private final UUID uuid;
    private final UUID callee;
    private final Instruction[] args;

    private boolean error = false;

    public CallInstruction(final @NotNull UUID uuid,
                           final @NotNull UUID callee,
                           final @NotNull Instruction @NotNull ... args) {
        this.uuid = uuid;
        this.callee = callee;
        this.args = args;
    }

    public CallInstruction(final @NotNull UUID callee, final @NotNull Instruction @NotNull ... args) {
        this(UUID.randomUUID(), callee, args);
    }

    public boolean get(final @NotNull State state, final int index) {
        return !error && state.getResult(uuid, index);
    }

    @Override
    public @NotNull UUID uuid() {
        return uuid;
    }

    @Override
    public void write(final @NotNull OutputStream stream) throws IOException {
        Instruction.super.write(stream);
        writeUUID(stream, callee);
        writeInt(stream, args.length);
        for (final var arg : args)
            writeUUID(stream, arg.uuid());
    }

    @Override
    public void exec(final @NotNull State state) {
        if (error) return;

        final var values = new boolean[args.length];
        for (int i = 0; i < args.length; ++i)
            values[i] = args[i].get(state);

        error = state.call(uuid, callee, values);
    }
}
