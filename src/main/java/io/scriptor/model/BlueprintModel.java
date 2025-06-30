package io.scriptor.model;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public record BlueprintModel() {

    public static @NotNull BlueprintModel read(final @NotNull InputStream stream) throws IOException {
        return new BlueprintModel();
    }

    public void write(final @NotNull OutputStream stream) throws IOException {
    }
}
