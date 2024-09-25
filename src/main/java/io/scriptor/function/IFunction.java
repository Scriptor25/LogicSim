package io.scriptor.function;

import io.scriptor.context.State;
import io.scriptor.util.IOStream;
import io.scriptor.util.IUnique;

import java.io.IOException;
import java.io.OutputStream;

public interface IFunction extends IUnique {

    int typeId();

    int numInputs();

    int numOutputs();

    void exec(final State parent, final int hash, final boolean[] in, final boolean[] out);

    default void write(final OutputStream out) throws IOException {
        IOStream.write(out, typeId());
        IOStream.write(out, uuid());
    }
}
