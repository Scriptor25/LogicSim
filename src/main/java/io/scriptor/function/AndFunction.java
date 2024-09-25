package io.scriptor.function;

import io.scriptor.context.Registry;
import io.scriptor.context.State;
import io.scriptor.util.IOStream;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

public record AndFunction(Registry registry, UUID uuid) implements IFunction {

    public static void read(final InputStream in, final Registry registry) throws IOException {
        final var uuid = IOStream.readUUID(in);
        registry.add(new AndFunction(registry, uuid));
    }

    public AndFunction(final Registry registry, final UUID uuid) {
        this.registry = registry;
        this.uuid = uuid;

        if (registry != null) registry.add(this);
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
    public void exec(final State state, final int hash, final boolean[] in, final boolean[] out) {
        out[0] = in[0] && in[1];
    }
}
