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
import io.scriptor.util.IUnique;
import io.scriptor.util.RTException;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.OutputStream;

import static io.scriptor.util.IO.writeUUID;

public interface Instruction extends IUnique {

    default void write(final @NotNull OutputStream stream) throws IOException {
        writeUUID(stream, uuid());
    }

    default boolean get(final @NotNull State state) {
        throw new RTException();
    }

    default void exec(final @NotNull State state) {
    }
}
