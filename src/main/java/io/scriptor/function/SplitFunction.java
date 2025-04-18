package io.scriptor.function;

import io.scriptor.context.State;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record SplitFunction(@NotNull UUID uuid, byte bitwidth) implements IFunction {

    @Override
    public int numInputs() {
        return 1;
    }

    @Override
    public int numOutputs() {
        return bitwidth;
    }

    @Override
    public void execute(final @NotNull State state, final int @NotNull [] inputs, final int @NotNull [] outputs) {
        for (int i = 0; i < bitwidth; ++i)
            outputs[i] = ((inputs[0] >> i) & 1);
    }
}
