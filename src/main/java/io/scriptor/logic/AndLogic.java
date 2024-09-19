package io.scriptor.logic;

/**
 * <ul>
 * <li>pin 0 - In A</li>
 * <li>pin 1 - In B</li>
 * <li>pin 2 - Out</li>
 * </ul>
 */
public class AndLogic implements ILogicBase {

    @Override
    public void cycle(final boolean[] inputs, final boolean[] outputs) {
        outputs[0] = inputs[0] && inputs[1];
    }
}
