package io.scriptor.logic;

import io.scriptor.Context;
import io.scriptor.util.IUnique;

import java.io.PrintWriter;

public interface ILogic extends IUnique {

    int inputs();

    int outputs();

    String input(final int i);

    String output(final int i);

    void write(final Context context, final PrintWriter out);

    void cycle(final boolean[] inputs, final boolean[] outputs);
}
