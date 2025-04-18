package io.scriptor.function;

import io.scriptor.context.State;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

import static io.scriptor.util.Constants.*;

public record ClockFunction(@NotNull UUID uuid) implements IFunction {

    @Override
    public int numInputs() {
        return 0;
    }

    @Override
    public int numOutputs() {
        return 4;
    }

    @Override
    public void execute(final @NotNull State state, final int @NotNull [] inputs, final int @NotNull [] outputs) {
        outputs[0] = ((TICK * TICKS_PER_FRAME) % TICKS_PER_CLOCK) <= TICK_THRESHOLD ? 1 : 0;
        outputs[1] = ((TICK * TICKS_PER_FRAME) % (TICKS_PER_CLOCK * 2)) <= TICK_THRESHOLD ? 1 : 0;
        outputs[2] = ((TICK * TICKS_PER_FRAME) % (TICKS_PER_CLOCK * 4)) <= TICK_THRESHOLD ? 1 : 0;
        outputs[3] = ((TICK * TICKS_PER_FRAME) % (TICKS_PER_CLOCK * 8)) <= TICK_THRESHOLD ? 1 : 0;
    }
}
