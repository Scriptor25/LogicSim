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

import io.scriptor.function.IFunction;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * The registry stores and manages blueprint functions.
 */
public class Registry {

    private final Context context;
    private final Map<UUID, IFunction> functions = new HashMap<>();

    /**
     * Create an empty registry instance.
     */
    public Registry(final @NotNull Context context) {
        this.context = context;
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
