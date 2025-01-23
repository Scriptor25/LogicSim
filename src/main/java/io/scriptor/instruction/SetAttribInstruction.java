package io.scriptor.instruction;

import io.scriptor.context.State;
import io.scriptor.function.Function;
import io.scriptor.util.IOStream;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public record SetAttribInstruction(
        @NotNull UUID uuid,
        @NotNull UUID attrib,
        @NotNull Instruction value
) implements Instruction {

    public static void read(final @NotNull InputStream inputStream, final @NotNull Function function) throws IOException {
        final var uuid = IOStream.readUUID(inputStream);
        final var attrib = IOStream.readUUID(inputStream);
        final var value = IOStream.readUUID(inputStream);
        function.add(new SetAttribInstruction(uuid, attrib, function.find(value)));
    }

    public SetAttribInstruction(final @NotNull UUID attrib, final @NotNull Instruction value) {
        this(UUID.randomUUID(), attrib, value);
    }

    @Override
    public void write(final @NotNull OutputStream outputStream) throws IOException {
        Instruction.super.write(outputStream);
        IOStream.write(outputStream, attrib);
        IOStream.write(outputStream, value.uuid());
    }

    @Override
    public void exec(final @NotNull State state) {
        state.setAttribute(attrib, value.get(state));
    }
}
