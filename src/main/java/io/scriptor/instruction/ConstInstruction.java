package io.scriptor.instruction;

import io.scriptor.context.State;
import io.scriptor.function.Function;
import io.scriptor.util.IOStream;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public record ConstInstruction(@NotNull UUID uuid, boolean value) implements Instruction {

    public static void read(final @NotNull InputStream inputStream, final @NotNull Function function) throws IOException {
        final var uuid = IOStream.readUUID(inputStream);
        final var value = IOStream.readBoolean(inputStream);
        function.add(new ConstInstruction(uuid, value));
    }

    public ConstInstruction(final boolean value) {
        this(UUID.randomUUID(), value);
    }

    @Override
    public boolean get(final @NotNull State state) {
        return value;
    }

    @Override
    public void write(final @NotNull OutputStream outputStream) throws IOException {
        Instruction.super.write(outputStream);
        IOStream.write(outputStream, value);
    }
}
