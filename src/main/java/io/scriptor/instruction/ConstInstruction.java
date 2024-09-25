package io.scriptor.instruction;

import io.scriptor.context.State;
import io.scriptor.function.Function;
import io.scriptor.util.IOStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public record ConstInstruction(UUID uuid, boolean value) implements Instruction {

    public static void read(final InputStream in, final Function fn) throws IOException {
        final var uuid = IOStream.readUUID(in);
        final var value = IOStream.readBoolean(in);
        fn.add(new ConstInstruction(uuid, value));
    }

    public ConstInstruction(final boolean value) {
        this(UUID.randomUUID(), value);
    }

    @Override
    public boolean get(final State state) {
        return value;
    }

    @Override
    public void writeData(final OutputStream out) throws IOException {
        IOStream.write(out, value);
    }
}
