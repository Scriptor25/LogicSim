package io.scriptor.instruction;

import io.scriptor.context.State;
import io.scriptor.util.IOStream;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

public interface Instruction {

    UUID uuid();

    void writeData(final OutputStream out) throws IOException;

    default void write(final OutputStream out) throws IOException {
        IOStream.write(out, uuid());
        writeData(out);
    }

    default boolean get(final State state) {
        throw new IllegalStateException();
    }

    default void exec(final State state) {
    }
}
