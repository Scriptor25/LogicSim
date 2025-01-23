package io.scriptor.function;

import io.scriptor.context.State;
import io.scriptor.util.IOStream;
import io.scriptor.util.IUnique;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.OutputStream;

public interface IFunction extends IUnique {

    int typeId();

    int numInputs();

    int numOutputs();

    void exec(final @NotNull State state, final boolean @NotNull [] inputs, final boolean @NotNull [] outputs);

    default void write(final @NotNull OutputStream outputStream) throws IOException {
        IOStream.write(outputStream, typeId());
        IOStream.write(outputStream, uuid());
    }
}
