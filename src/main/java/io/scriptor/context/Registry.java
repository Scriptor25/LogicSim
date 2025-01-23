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
