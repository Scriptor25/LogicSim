package io.scriptor.graph;

import imgui.type.ImBoolean;
import imgui.type.ImString;
import io.scriptor.util.IUnique;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record Attribute(
        @NotNull UUID uuid,
        @NotNull ImString label,
        boolean output,
        @NotNull ImBoolean powered
) implements IUnique {

    public Attribute(final @NotNull String label, final boolean output) {
        this(UUID.randomUUID(), new ImString(label), output, new ImBoolean());
    }

    public boolean input() {
        return !output;
    }

    @Override
    public String toString() {
        return (output ? "<< " : ">> ") + label.get();
    }
}
