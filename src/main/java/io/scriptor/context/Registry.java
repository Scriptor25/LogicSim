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
public class Registry {

    private final Map<UUID, IFunction> functions = new HashMap<>();

    public Registry() {
    }

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

    public void write(final @NotNull OutputStream outputStream) throws IOException {
        IOStream.write(outputStream, functions.size());
        for (final var function : functions.values())
            function.write(outputStream);
    }

    public @NotNull Optional<IFunction> get(final @NotNull UUID uuid) {
        if (functions.containsKey(uuid))
            return Optional.of(functions.get(uuid));
        return Optional.empty();
    }

    public void add(final @NotNull IFunction function) {
        functions.put(function.uuid(), function);
    }

    public void remove(final @NotNull IFunction function) {
        functions.remove(function.uuid());
    }
}
