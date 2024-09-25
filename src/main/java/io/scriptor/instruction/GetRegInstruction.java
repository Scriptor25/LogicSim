package io.scriptor.instruction;

import io.scriptor.context.State;
import io.scriptor.function.Function;
import io.scriptor.util.IOStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public record GetRegInstruction(UUID uuid, UUID reg, int index) implements Instruction {

    public static void read(final InputStream in, final Function fn) throws IOException {
        final var uuid = IOStream.readUUID(in);
        final var reg = IOStream.readUUID(in);
        final var index = IOStream.readInt(in);
        fn.add(new GetRegInstruction(uuid, reg, index));
    }

    public GetRegInstruction(final UUID reg, final int index) {
        this(UUID.randomUUID(), reg, index);
    }

    @Override
    public void write(final OutputStream out) throws IOException {
        Instruction.super.write(out);
        IOStream.write(out, reg);
        IOStream.write(out, index);
    }

    @Override
    public boolean get(final State state) {
        return state.getReg(reg, index);
    }
}
