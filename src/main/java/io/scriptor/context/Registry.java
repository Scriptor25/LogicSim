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
package io.scriptor.context;

import io.scriptor.function.AndFunction;
import io.scriptor.function.Function;
import io.scriptor.function.IFunction;
import io.scriptor.function.NotFunction;
import io.scriptor.util.IOStream;
import io.scriptor.util.RTException;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * The registry stores and manages blueprint functions.
 */
public class Registry {

    private final Map<UUID, IFunction> functions = new HashMap<>();

    /**
     * Create an empty registry instance.
     */
    public Registry() {
    }

    /**
     * Load a registry from an input stream.
     *
     * @param inputStream the input stream
     * @throws IOException if any
     */
    public Registry(final @NotNull InputStream inputStream) throws IOException {
        final var functionCount = IOStream.readInt(inputStream);
        for (int i = 0; i < functionCount; ++i) {
            final var functionType = IOStream.readInt(inputStream);
            final var function = switch (functionType) {
                case 0 -> NotFunction.read(inputStream);
                case 1 -> AndFunction.read(inputStream);
                case 2 -> Function.read(inputStream);
                default -> throw new RTException("invalid function type '%d'", functionType);
            };
            add(function);
        }
    }

    /**
     * Write this registry to an output stream.
     *
     * @param outputStream the output stream
     * @throws IOException if any
     */
    public void write(final @NotNull OutputStream outputStream) throws IOException {
        IOStream.write(outputStream, functions.size());
        for (final var function : functions.values())
            function.write(outputStream);
    }

    /**
     * Get a function by its uuid.
     *
     * @param uuid the function uuid
     * @return the function if existing, else empty
     */
    public @NotNull Optional<IFunction> get(final @NotNull UUID uuid) {
        if (functions.containsKey(uuid))
            return Optional.of(functions.get(uuid));
        return Optional.empty();
    }

    /**
     * Add a function to this registry.
     *
     * @param function the function
     */
    public void add(final @NotNull IFunction function) {
        functions.put(function.uuid(), function);
    }

    /**
     * Remove a function from this registry.
     *
     * @param function the function
     */
    public void remove(final @NotNull IFunction function) {
        functions.remove(function.uuid());
    }
}
