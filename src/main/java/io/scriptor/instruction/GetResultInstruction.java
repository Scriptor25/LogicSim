package io.scriptor.instruction;

import io.scriptor.context.State;
import io.scriptor.function.Function;
import io.scriptor.util.IOStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public record GetResultInstruction(UUID uuid, CallInstruction call, int index) implements Instruction {

    public static void read(final InputStream in, final Function fn) throws IOException {
        final var uuid = IOStream.readUUID(in);
        final var call = IOStream.readUUID(in);
        final var index = IOStream.readInt(in);
        fn.add(new GetResultInstruction(uuid, fn.find(call), index));
    }

    public GetResultInstruction(final CallInstruction call, final int index) {
        this(UUID.randomUUID(), call, index);
    }

    @Override
    public void writeData(final OutputStream out) throws IOException {
        IOStream.write(out, call.uuid());
        IOStream.write(out, index);
    }

    @Override
    public boolean get(final State state) {
        return call.get(state, index);
    }
}
