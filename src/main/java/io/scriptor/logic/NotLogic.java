package io.scriptor.logic;

/**
 * <ul>
 * <li>pin 0 - In</li>
 * <li>pin 1 - Out</li>
 * </ul>
 */
public class NotLogic implements ILogicBase {

    @Override
    public void cycle(final boolean[] inputs, final boolean[] outputs) {
        outputs[0] = !inputs[0];
    }
}
