package io.scriptor.node;

import imgui.type.ImBoolean;
import imgui.type.ImString;
import io.scriptor.Context;
import io.scriptor.util.IUnique;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.UUID;

public record Attribute(UUID uuid, ImString label, boolean output, ImBoolean powered) implements IUnique {

    public static void read(final Context context, final BufferedReader in) throws IOException {
        final var uuid = UUID.fromString(in.readLine());
        final var label = in.readLine();
        final var output = Boolean.parseBoolean(in.readLine());
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

    public void write(final Context context, final PrintWriter out) {
        out.println(uuid);
        out.println(label);
        out.println(output);
    }
}
