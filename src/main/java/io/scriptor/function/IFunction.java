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
import io.scriptor.util.IUnique;
import org.jetbrains.annotations.NotNull;

/**
 * The function interface specifies that every function has to provide a type id, the number of inputs and outputs,
 * an endpoint to execute the function as well as one to write the function to an output stream.
 */
public interface IFunction extends IUnique {

    /**
     * Get the number of inputs.
     *
     * @return the number of inputs
     */
    int numInputs();

    /**
     * Get the number of outputs.
     *
     * @return the number of outputs
     */
    int numOutputs();

    /**
     * Execute this function using the given inputs. The results are written to the given outputs array.
     *
     * @param state   the state
     * @param inputs  the inputs
     * @param outputs the outputs
     */
    void exec(final @NotNull State state, final boolean @NotNull [] inputs, final boolean @NotNull [] outputs);
}
