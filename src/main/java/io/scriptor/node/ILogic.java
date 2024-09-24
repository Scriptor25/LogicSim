package io.scriptor.node;

import io.scriptor.Context;
import io.scriptor.util.IUnique;

import java.io.IOException;
import java.io.OutputStream;

public interface ILogic extends IUnique {

    int inputs();

    int outputs();

    String input(final int i);

    String output(final int i);

    void write(final Context context, final OutputStream out) throws IOException;

    void cycle(final INode parent, final boolean[] inputs, final boolean[] outputs);
}
