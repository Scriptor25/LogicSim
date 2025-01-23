package io.scriptor.instruction;

import io.scriptor.context.State;
import io.scriptor.function.Function;
import io.scriptor.util.IOStream;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public record SetRegInstruction(
        @NotNull UUID uuid,
        @NotNull UUID reg,
        int index,
        @NotNull Instruction value
) implements Instruction {

    public static void read(final @NotNull InputStream inputStream, final @NotNull Function function) throws IOException {
        final var uuid = IOStream.readUUID(inputStream);
        final var reg = IOStream.readUUID(inputStream);
        final var index = IOStream.readInt(inputStream);
        final var value = IOStream.readUUID(inputStream);
        function.add(new SetRegInstruction(uuid, reg, index, function.find(value)));
    }

    public SetRegInstruction(final @NotNull UUID reg, final int index, final @NotNull Instruction value) {
        this(UUID.randomUUID(), reg, index, value);
    }

    @Override
    public void write(final @NotNull OutputStream outputStream) throws IOException {
        Instruction.super.write(outputStream);
        IOStream.write(outputStream, reg);
        IOStream.write(outputStream, index);
        IOStream.write(outputStream, value.uuid());
    }

    @Override
    public void exec(final @NotNull State state) {
        state.setRegister(reg, index, value.get(state));
    }
}
