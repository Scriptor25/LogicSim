package io.scriptor.instruction;

import io.scriptor.context.State;
import io.scriptor.function.Function;
import io.scriptor.util.IOStream;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public record GetRegInstruction(@NotNull UUID uuid, @NotNull UUID reg, int index) implements Instruction {

    public static void read(final @NotNull InputStream inputStream, final @NotNull Function function) throws IOException {
        final var uuid = IOStream.readUUID(inputStream);
        final var reg = IOStream.readUUID(inputStream);
        final var index = IOStream.readInt(inputStream);
        function.add(new GetRegInstruction(uuid, reg, index));
    }

    public GetRegInstruction(final @NotNull UUID reg, final int index) {
        this(UUID.randomUUID(), reg, index);
    }

    @Override
    public void write(final @NotNull OutputStream outputStream) throws IOException {
        Instruction.super.write(outputStream);
        IOStream.write(outputStream, reg);
        IOStream.write(outputStream, index);
    }

    @Override
    public boolean get(final @NotNull State state) {
        return state.getRegister(reg, index);
    }
}
