package io.scriptor.node;

import imgui.type.ImBoolean;
import imgui.type.ImString;

import java.util.UUID;

public record Attribute(UUID uuid, ImString label, boolean output, ImBoolean powered) {

    @Override
    public String toString() {
        return (output ? "<< " : ">> ") + label.get();
    }

    public Attribute(final String label, final boolean output) {
        this(UUID.randomUUID(), new ImString(label), output, new ImBoolean());
    }

    public Attribute(final UUID uuid, final String label, final boolean output) {
        this(uuid, new ImString(label), output, new ImBoolean());
    }
}
