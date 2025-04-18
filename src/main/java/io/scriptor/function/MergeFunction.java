package io.scriptor.function;

import io.scriptor.context.State;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record MergeFunction(@NotNull UUID uuid, byte bitwidth) implements IFunction {

    @Override
    public int numInputs() {
        return bitwidth;
    }

    @Override
    public int numOutputs() {
        return 1;
    }

    @Override
    public void execute(final @NotNull State state, final int @NotNull [] inputs, final int @NotNull [] outputs) {
        outputs[0] = 0;
        for (int i = 0; i < bitwidth; ++i)
            outputs[0] |= (inputs[i] & 1) << i;
    }
}
