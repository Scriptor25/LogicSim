package io.scriptor.logic;

import java.io.PrintStream;
import java.util.UUID;

/**
 * UUID(0, 1)
 * <ul>
 * <li>pin 0 - In A</li>
 * <li>pin 1 - In B</li>
 * <li>pin 2 - Out</li>
 * </ul>
 */
public class AndLogic implements ILogic {

    @Override
    public UUID uuid() {
        return new UUID(0, 1);
    }

    @Override
    public void write(PrintStream out) {

    }

    @Override
    public void cycle(final boolean[] inputs, final boolean[] outputs) {
        outputs[0] = inputs[0] && inputs[1];
    }
}
