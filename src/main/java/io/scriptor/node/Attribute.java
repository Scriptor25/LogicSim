package io.scriptor.node;

import imgui.type.ImBoolean;
import imgui.type.ImString;
import io.scriptor.Context;
import io.scriptor.util.IUnique;
import io.scriptor.util.ObjectIO;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public record Attribute(UUID uuid, ImString label, boolean output, ImBoolean powered) implements IUnique {

    public static void read(final Context context, final InputStream in) throws IOException {
        final var uuid = ObjectIO.readUUID(in);
        final var label = ObjectIO.readString(in);
        final var output = ObjectIO.readBoolean(in);
        context.getRef(uuid).set(new Attribute(uuid, new ImString(label), output, new ImBoolean()));
    }

    public Attribute(final String label, final boolean output) {
        this(UUID.randomUUID(), new ImString(label), output, new ImBoolean());
    }

    public Attribute(final UUID uuid, final String label, final boolean output) {
        this(uuid, new ImString(label), output, new ImBoolean());
    }

    @Override
    public String toString() {
        return (output ? "<< " : ">> ") + label.get();
    }

    public boolean input() {
        return !output;
    }

    public void write(final Context context, final OutputStream out) throws IOException {
        ObjectIO.write(out, uuid);
        ObjectIO.write(out, label.get());
        ObjectIO.write(out, output);
    }
}
