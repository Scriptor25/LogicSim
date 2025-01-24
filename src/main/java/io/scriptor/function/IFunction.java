package io.scriptor.function;

import io.scriptor.context.State;
import io.scriptor.util.IOStream;
import io.scriptor.util.IUnique;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.OutputStream;

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
public interface IFunction extends IUnique {

    int typeId();

    int numInputs();

    int numOutputs();

    void exec(final @NotNull State state, final boolean @NotNull [] inputs, final boolean @NotNull [] outputs);

    default void write(final @NotNull OutputStream outputStream) throws IOException {
        IOStream.write(outputStream, typeId());
        IOStream.write(outputStream, uuid());
    }
}
