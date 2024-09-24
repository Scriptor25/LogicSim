package io.scriptor.node;

import imgui.ImGui;
import imgui.extension.imnodes.ImNodes;
import imgui.extension.imnodes.flag.ImNodesCol;
import io.scriptor.Context;
import io.scriptor.util.ObjectIO;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Optional;
import java.util.Queue;
import java.util.UUID;

public class Output implements INode {

    public static void read(final Context context, final InputStream in) throws IOException {
        final var uuid = ObjectIO.readUUID(in);
        context.<Attribute>getRef(ObjectIO.readUUID(in))
                .get(x -> context.getRef(uuid).set(new Output(uuid, x)));
    }

    private final UUID uuid;
    private final Attribute attribute;
    private final Pin pin = new Pin(this, 0, false);

    public Output(final UUID uuid, final Attribute attribute) {
        this.uuid = uuid;
        this.attribute = attribute;
    }

    @Override
    public UUID uuid() {
        return uuid;
    }

    @Override
    public Pin input(final int i) {
        if (i == 0) return pin;
        throw new IllegalStateException();
    }

    @Override
    public Pin output(final int i) {
        throw new IllegalStateException();
    }

    @Override
    public boolean powered(final int i) {
        throw new IllegalStateException();
    }

    @Override
    public Optional<Pin> pin(final int id) {
        if (pin.id() == id) return Optional.of(pin);
        return Optional.empty();
    }

    @Override
    public boolean noPredecessor(final Graph graph) {
        return predecessor(graph).isEmpty();
    }

    @Override
    public void show(Graph graph) {
        ImNodes.beginNode(id());
        if (attribute.powered().get()) ImNodes.pushColorStyle(ImNodesCol.Pin, 0x770000ff);
        ImNodes.beginInputAttribute(pin.id());
        ImGui.textUnformatted(attribute.label().get());
        ImGui.sameLine();
        ImGui.checkbox("##powered", attribute.powered().get());
        ImNodes.endInputAttribute();
        if (attribute.powered().get()) ImNodes.popColorStyle();
        ImNodes.endNode();
    }

    @Override
    public Output copy() {
        return new Output(UUID.randomUUID(), attribute);
    }

    @Override
    public void write(final Context context, final OutputStream out) throws IOException {
        ObjectIO.write(out, uuid);
        ObjectIO.write(out, attribute.uuid());

        context.next(attribute);
    }

    @Override
    public void cycle(final long key, final Graph graph, final Queue<INode> callQueue) {
        pin.predecessor(graph).ifPresent(x -> attribute.powered().set(x.powered()));
    }

    public Optional<Pin> predecessor(final Graph graph) {
        return pin.predecessor(graph);
    }
}
