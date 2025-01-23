package io.scriptor.instruction;

import io.scriptor.context.State;
import io.scriptor.function.Function;
import io.scriptor.util.IOStream;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class CallInstruction implements Instruction {

    public static void read(final @NotNull InputStream inputStream, final @NotNull Function function) throws IOException {
        final var uuid = IOStream.readUUID(inputStream);
        final var callee = IOStream.readUUID(inputStream);
        final var args = new Instruction[IOStream.readInt(inputStream)];
        for (int i = 0; i < args.length; i++) args[i] = function.find(IOStream.readUUID(inputStream));
        function.add(new CallInstruction(uuid, callee, args));
    }

    private final UUID uuid;
    private final UUID callee;
    private final Instruction[] args;

    private boolean error = false;

    public CallInstruction(final @NotNull UUID uuid,
                           final @NotNull UUID callee,
                           final @NotNull Instruction @NotNull ... args) {
        this.uuid = uuid;
        this.callee = callee;
        this.args = args;
    }

    public CallInstruction(final @NotNull UUID callee, final @NotNull Instruction @NotNull ... args) {
        this(UUID.randomUUID(), callee, args);
    }

    public boolean get(final @NotNull State state, final int index) {
        return !error && state.getResult(uuid, index);
    }

    @Override
    public @NotNull UUID uuid() {
        return uuid;
    }

    @Override
    public void write(final @NotNull OutputStream outputStream) throws IOException {
        Instruction.super.write(outputStream);
        IOStream.write(outputStream, callee);
        IOStream.write(outputStream, args.length);
        for (final var arg : args) IOStream.write(outputStream, arg.uuid());
    }

    @Override
    public void exec(final @NotNull State state) {
        if (error) return;

        final var values = new boolean[args.length];
        for (int i = 0; i < args.length; ++i)
            values[i] = args[i].get(state);

        error = state.call(uuid, callee, values);
    }
}
