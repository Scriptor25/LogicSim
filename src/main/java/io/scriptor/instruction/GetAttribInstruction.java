package io.scriptor.instruction;

import io.scriptor.context.State;
import io.scriptor.function.Function;
import io.scriptor.util.IOStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public record GetAttribInstruction(UUID uuid, UUID attrib) implements Instruction {

    public static void read(final InputStream in, final Function fn) throws IOException {
        final var uuid = IOStream.readUUID(in);
        final var attrib = IOStream.readUUID(in);
        fn.add(new GetAttribInstruction(uuid, attrib));
    }

    public GetAttribInstruction(final UUID attrib) {
        this(UUID.randomUUID(), attrib);
    }

    @Override
    public void write(final OutputStream out) throws IOException {
        Instruction.super.write(out);
        IOStream.write(out, attrib);
    }

    @Override
    public boolean get(final State state) {
        return state.getAttrib(attrib);
    }
}
