package io.scriptor.model;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static io.scriptor.util.Constants.MODEL_SIGNATURE;
import static io.scriptor.util.Constants.MODEL_VERSION;
import static io.scriptor.util.IO.writeBytes;
import static io.scriptor.util.IO.writeInt;

public record ProjectModel(@NotNull BlueprintModel @NotNull [] blueprints) {

    public static @NotNull ProjectModel read(final @NotNull InputStream stream) throws IOException {
    }

    public void write(final @NotNull OutputStream stream) throws IOException {
        writeBytes(stream, MODEL_SIGNATURE);
        writeInt(stream, MODEL_VERSION);
    }
}
