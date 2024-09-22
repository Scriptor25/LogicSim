package io.scriptor.logic;

import java.io.PrintStream;
import java.util.UUID;

public interface ILogic {

    UUID uuid();

    void write(final PrintStream out);

    void cycle(final boolean[] inputs, final boolean[] outputs);
}
