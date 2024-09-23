package io.scriptor.node;

import imgui.extension.imnodes.ImNodes;
import io.scriptor.Context;
import io.scriptor.util.IUnique;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.UUID;

public record Link(UUID uuid, Pin source, Pin target) implements IUnique {

    public static void read(final Context context, final BufferedReader in) throws IOException {
        final var uuid = UUID.fromString(in.readLine());
        final var sourceUUID = UUID.fromString(in.readLine());
        final var sourceIndex = Integer.parseInt(in.readLine());
        final var targetUUID = UUID.fromString(in.readLine());
        final var targetIndex = Integer.parseInt(in.readLine());

        context.<INode>getRef(sourceUUID)
                .get(source -> context.<INode>getRef(targetUUID)
                        .get(target -> context.getRef(uuid).set(new Link(uuid, source.output(sourceIndex), target.input(targetIndex)))));
    }

    public int id() {
        return uuid.hashCode();
    }

    public void show() {
        ImNodes.link(id(), source.id(), target.id());
    }

    public boolean uses(final INode node) {
        return source.uses(node) || target.uses(node);
    }

    public boolean uses(final INode[] nodes) {
        for (final var a : nodes) {
            for (final var b : nodes) {
                if (a == b) continue;
                if (source.node() == a && target.node() == b) return true;
                if (target.node() == a && source.node() == b) return true;
            }
        }
        return false;
    }

    public boolean uses(final Pin pin) {
        return source == pin || target == pin;
    }

    public void write(final Context context, final PrintWriter out) {
        out.println(uuid);
        out.println(source.node().uuid());
        out.println(source.index());
        out.println(target.node().uuid());
        out.println(target.index());

        context.next(source.node());
        context.next(target.node());
    }
}
