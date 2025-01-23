package io.scriptor.instruction;

import io.scriptor.context.State;
import io.scriptor.util.IOStream;
import io.scriptor.util.RTException;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

public interface Instruction {

    @NotNull UUID uuid();

    default void write(final @NotNull OutputStream outputStream) throws IOException {
        IOStream.write(outputStream, uuid());
    }

    default boolean get(final @NotNull State state) {
        throw new RTException();
    }

    default void exec(final @NotNull State state) {
    }
}
