package io.scriptor.instruction;

import io.scriptor.context.State;
import io.scriptor.function.Function;
import io.scriptor.util.IOStream;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public record GetResultInstruction(@NotNull UUID uuid, @NotNull CallInstruction call,
                                   int index) implements Instruction {

    public static void read(final @NotNull InputStream inputStream, final @NotNull Function function) throws IOException {
        final var uuid = IOStream.readUUID(inputStream);
        final var call = IOStream.readUUID(inputStream);
        final var index = IOStream.readInt(inputStream);
        function.add(new GetResultInstruction(uuid, function.find(call, CallInstruction.class), index));
    }

    public GetResultInstruction(final @NotNull CallInstruction call, final int index) {
        this(UUID.randomUUID(), call, index);
    }

    @Override
    public void write(final @NotNull OutputStream outputStream) throws IOException {
        Instruction.super.write(outputStream);
        IOStream.write(outputStream, call.uuid());
        IOStream.write(outputStream, index);
    }

    @Override
    public boolean get(final @NotNull State state) {
        return call.get(state, index);
    }
}
