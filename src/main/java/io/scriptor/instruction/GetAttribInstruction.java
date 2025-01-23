package io.scriptor.instruction;

import io.scriptor.context.State;
import io.scriptor.function.Function;
import io.scriptor.util.IOStream;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public record GetAttribInstruction(@NotNull UUID uuid, @NotNull UUID attrib) implements Instruction {

    public static void read(final @NotNull InputStream inputStream, final @NotNull Function function) throws IOException {
        final var uuid = IOStream.readUUID(inputStream);
        final var attrib = IOStream.readUUID(inputStream);
        function.add(new GetAttribInstruction(uuid, attrib));
    }

    public GetAttribInstruction(final @NotNull UUID attrib) {
        this(UUID.randomUUID(), attrib);
    }

    @Override
    public void write(final @NotNull OutputStream outputStream) throws IOException {
        Instruction.super.write(outputStream);
        IOStream.write(outputStream, attrib);
    }

    @Override
    public boolean get(final @NotNull State state) {
        return state.getAttribute(attrib);
    }
}
