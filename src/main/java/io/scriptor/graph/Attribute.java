package io.scriptor.graph;

import imgui.type.ImBoolean;
import imgui.type.ImString;
import io.scriptor.util.IUnique;

import java.util.UUID;

public record Attribute(UUID uuid, ImString label, boolean output, ImBoolean powered) implements IUnique {

    public Attribute(final String label, final boolean output) {
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
