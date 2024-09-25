package io.scriptor.context;

import io.scriptor.function.AndFunction;
import io.scriptor.function.Function;
import io.scriptor.function.IFunction;
import io.scriptor.function.NotFunction;
import io.scriptor.util.IOStream;

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

    public Registry(final InputStream in) throws IOException {
        final var functionCount = IOStream.readInt(in);
        for (int i = 0; i < functionCount; ++i) {
            switch (IOStream.readInt(in)) {
                case 0 -> NotFunction.read(in, this);
                case 1 -> AndFunction.read(in, this);
                case 2 -> Function.read(in, this);
                default -> throw new IllegalStateException();
            }
        }
    }

    public void write(final OutputStream out) throws IOException {
        IOStream.write(out, functions.size());
        for (final var fn : functions.values()) fn.write(out);
    }

    public Optional<IFunction> get(final UUID uuid) {
        return Optional.ofNullable(functions.get(uuid));
    }

    public void add(final IFunction function) {
        functions.put(function.uuid(), function);
    }

    public void remove(final IFunction function) {
        functions.remove(function.uuid());
    }
}
