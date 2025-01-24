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
package io.scriptor.function;

import io.scriptor.context.State;
import io.scriptor.util.IOStream;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

public record AndFunction(@NotNull UUID uuid) implements IFunction {

    public static IFunction read(final @NotNull InputStream inputStream) throws IOException {
        final var uuid = IOStream.readUUID(inputStream);
        return new AndFunction(uuid);
    }

    @Override
    public int typeId() {
        return 1;
    }

    @Override
    public int numInputs() {
        return 2;
    }

    @Override
    public int numOutputs() {
        return 1;
    }

    @Override
    public void exec(final @NotNull State state, final boolean @NotNull [] inputs, final boolean @NotNull [] outputs) {
        outputs[0] = inputs[0] && inputs[1];
    }
}
