package io.scriptor.function;

import io.scriptor.context.State;
import io.scriptor.util.IOStream;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

public record NotFunction(@NotNull UUID uuid) implements IFunction {

    public static IFunction read(final @NotNull InputStream inputStream) throws IOException {
        final var uuid = IOStream.readUUID(inputStream);
        return new NotFunction(uuid);
    }

    @Override
    public int typeId() {
        return 0;
    }

    @Override
    public int numInputs() {
        return 1;
    }

    @Override
    public int numOutputs() {
        return 1;
    }

    @Override
    public void exec(final @NotNull State state, final int hash, final boolean @NotNull [] inputs, final boolean @NotNull [] outputs) {
        outputs[0] = !inputs[0];
    }
}
