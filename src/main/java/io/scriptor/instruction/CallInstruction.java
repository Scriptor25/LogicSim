package io.scriptor.instruction;

import io.scriptor.context.State;
import io.scriptor.function.Function;
import io.scriptor.util.IOStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class CallInstruction implements Instruction {

    public static void read(final InputStream in, final Function fn) throws IOException {
        final var uuid = IOStream.readUUID(in);
        final var callee = IOStream.readUUID(in);
        final var args = new Instruction[IOStream.readInt(in)];
        for (int i = 0; i < args.length; i++) args[i] = fn.find(IOStream.readUUID(in));
        fn.add(new CallInstruction(uuid, callee, args));
    }

    private final UUID uuid;
    private final UUID callee;
    private final Instruction[] args;

    private boolean error = false;

    public CallInstruction(final UUID uuid, final UUID callee, final Instruction... args) {
        this.uuid = uuid;
        this.callee = callee;
        this.args = args;
    }

    public CallInstruction(final UUID callee, final Instruction... args) {
        this(UUID.randomUUID(), callee, args);
    }

    public boolean get(final State state, final int index) {
        return !error && state.getResult(uuid, index);
    }

    @Override
    public UUID uuid() {
        return uuid;
    }

    @Override
    public void write(final OutputStream out) throws IOException {
        Instruction.super.write(out);
        IOStream.write(out, callee);
        IOStream.write(out, args.length);
        for (final var arg : args) IOStream.write(out, arg.uuid());
    }

    @Override
    public void exec(final State state, final int hash) {
        if (error) return;

        final var values = new boolean[args.length];
        for (int i = 0; i < args.length; ++i)
            if (args[i] != null)
                values[i] = args[i].get(state);

        if (!state.call(uuid, hash + hashCode(), callee, values))
            error = true;
    }
}
