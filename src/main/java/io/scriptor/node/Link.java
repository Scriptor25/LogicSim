package io.scriptor.node;

import imgui.extension.imnodes.ImNodes;
import io.scriptor.Context;
import io.scriptor.util.IUnique;
import io.scriptor.util.ObjectIO;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public record Link(UUID uuid, Pin source, Pin target) implements IUnique {

    public static void read(final Context context, final InputStream in) throws IOException {
        final var uuid = ObjectIO.readUUID(in);
        final var sourceUUID = ObjectIO.readUUID(in);
        final var sourceIndex = ObjectIO.readInt(in);
        final var targetUUID = ObjectIO.readUUID(in);
        final var targetIndex = ObjectIO.readInt(in);

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

    public void write(final Context context, final OutputStream out) throws IOException {
        ObjectIO.write(out, uuid);
        ObjectIO.write(out, source.node().uuid());
        ObjectIO.write(out, source.index());
        ObjectIO.write(out, target.node().uuid());
        ObjectIO.write(out, target.index());

        context.next(source.node());
        context.next(target.node());
    }
}
