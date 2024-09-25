package io.scriptor.instruction;

import io.scriptor.context.State;
import io.scriptor.function.Function;
import io.scriptor.util.IOStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public record SetAttribInstruction(UUID uuid, UUID attrib, Instruction value) implements Instruction {

    public static void read(final InputStream in, final Function fn) throws IOException {
        final var uuid = IOStream.readUUID(in);
        final var attrib = IOStream.readUUID(in);
        final var value = IOStream.readUUID(in);
        fn.add(new SetAttribInstruction(uuid, attrib, fn.find(value)));
    }

    public SetAttribInstruction(final UUID attrib, final Instruction value) {
        this(UUID.randomUUID(), attrib, value);
    }

    @Override
    public void writeData(final OutputStream out) throws IOException {
        IOStream.write(out, attrib);
        IOStream.write(out, value.uuid());
    }

    @Override
    public void exec(final State state) {
        state.setAttrib(attrib, value.get(state));
    }
}
