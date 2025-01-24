package io.scriptor.util;

import org.jetbrains.annotations.NotNull;

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
public class RTException extends RuntimeException {

    public RTException() {
        super();
    }

    public RTException(final @NotNull String message) {
        super(message);
    }

    public RTException(final @NotNull String format, final @NotNull Object @NotNull ... args) {
        super(format.formatted(args));
    }

    public RTException(final @NotNull Throwable cause) {
        super(cause);
    }

    public RTException(final @NotNull Throwable cause, final @NotNull String message) {
        super(message, cause);
    }

    public RTException(final @NotNull Throwable cause, final @NotNull String format, final @NotNull Object @NotNull ... args) {
        super(format.formatted(args), cause);
    }
}
