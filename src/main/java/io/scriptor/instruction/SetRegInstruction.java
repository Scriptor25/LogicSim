package io.scriptor.instruction;

import io.scriptor.context.State;
import io.scriptor.function.Function;
import io.scriptor.util.IOStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public record SetRegInstruction(UUID uuid, UUID reg, int index, Instruction value) implements Instruction {

    public static void read(final InputStream in, final Function fn) throws IOException {
        final var uuid = IOStream.readUUID(in);
        final var reg = IOStream.readUUID(in);
        final var index = IOStream.readInt(in);
        final var value = IOStream.readUUID(in);
        fn.add(new SetRegInstruction(uuid, reg, index, fn.find(value)));
    }

    public SetRegInstruction(final UUID reg, final int index, final Instruction value) {
        this(UUID.randomUUID(), reg, index, value);
    }

    @Override
    public void writeData(final OutputStream out) throws IOException {
        IOStream.write(out, reg);
        IOStream.write(out, index);
        IOStream.write(out, value.uuid());
    }

    @Override
    public void exec(final State state) {
        state.setReg(reg, index, value.get(state));
    }
}
