package io.scriptor.function;

import io.scriptor.context.Registry;
import io.scriptor.context.State;
import io.scriptor.util.IOStream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

public record AndFunction(@Nullable Registry registry, @NotNull UUID uuid) implements IFunction {

    public static void read(final @NotNull InputStream inputStream, final @Nullable Registry registry) throws IOException {
        final var uuid = IOStream.readUUID(inputStream);
        new AndFunction(registry, uuid);
    }

    public AndFunction(final @Nullable Registry registry, final @NotNull UUID uuid) {
        this.registry = registry;
        this.uuid = uuid;

        if (registry != null)
            registry.add(this);
    }

    @Override
    public int typeId() {
        return 1;
    }

    @Override
    public int numInputs() {
        return 2;
    }

    @Override
    public int numOutputs() {
        return 1;
    }

    @Override
    public void exec(final @NotNull State state, final int hash, final boolean @NotNull [] inputs, final boolean @NotNull [] outputs) {
        outputs[0] = inputs[0] && inputs[1];
    }
}
