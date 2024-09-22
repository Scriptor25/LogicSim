package io.scriptor.logic;

import java.io.PrintStream;
import java.util.UUID;

/**
 * UUID(0, 0)
 * <ul>
 * <li>pin 0 - In</li>
 * <li>pin 1 - Out</li>
 * </ul>
 */
public class NotLogic implements ILogic {

    @Override
    public UUID uuid() {
        return new UUID(0, 0);
    }

    @Override
    public void write(final PrintStream out) {
        out.println(uuid());
    }

    @Override
    public void cycle(final boolean[] inputs, final boolean[] outputs) {
        outputs[0] = !inputs[0];
    }
}
