package io.scriptor.function;

import io.scriptor.context.State;
import io.scriptor.util.IOStream;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

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
public record NotFunction(@NotNull UUID uuid) implements IFunction {

    public static IFunction read(final @NotNull InputStream inputStream) throws IOException {
        final var uuid = IOStream.readUUID(inputStream);
        return new NotFunction(uuid);
    }

    @Override
    public int typeId() {
        return 0;
    }

    @Override
    public int numInputs() {
        return 1;
    }

    @Override
    public int numOutputs() {
        return 1;
    }

    @Override
    public void exec(final @NotNull State state, final boolean @NotNull [] inputs, final boolean @NotNull [] outputs) {
        outputs[0] = !inputs[0];
    }
}
