package io.scriptor.node;

import imgui.ImGui;
import imgui.extension.imnodes.ImNodes;
import io.scriptor.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.UUID;

public class Input implements INode {

    public static void read(final Context context, final BufferedReader in) throws IOException {
        final var uuid = UUID.fromString(in.readLine());
        context.<Attribute>getRef(UUID.fromString(in.readLine()))
                .get(x -> context.getRef(uuid).set(new Input(uuid, x)));
    }

    private final UUID uuid;
    private final Attribute attribute;
    private final Pin pin = new Pin(this, 0, true);

    public Input(final UUID uuid, final Attribute attribute) {
        this.uuid = uuid;
        this.attribute = attribute;
    }

    @Override
    public UUID uuid() {
        return uuid;
    }

    @Override
    public Pin input(final int i) {
        throw new IllegalStateException();
    }

    @Override
    public Pin output(final int i) {
        if (i == 0) return pin;
        throw new IllegalStateException();
    }

    @Override
    public boolean powered(final int i) {
        if (i == 0) return attribute.powered().get();
        throw new IllegalStateException();
    }

    @Override
    public Optional<Pin> pin(final int id) {
        if (pin.id() == id) return Optional.of(pin);
        return Optional.empty();
    }

    @Override
    public boolean noPredecessor(final Graph graph) {
        return true;
    }

    @Override
    public void show() {
        ImNodes.beginNode(id());
        ImNodes.beginOutputAttribute(pin.id());
        ImGui.checkbox("##powered", attribute.powered());
        ImGui.sameLine();
        ImGui.textUnformatted(attribute.label().get());
        ImNodes.endOutputAttribute();
        ImNodes.endNode();
    }

    @Override
    public Input copy() {
        return new Input(UUID.randomUUID(), attribute);
    }

    @Override
    public void write(final Context context, final PrintWriter out) {
        out.println(uuid);
        out.println(attribute.uuid());

        context.next(attribute);
    }

    @Override
    public void cycle(final Graph graph, final Queue<INode> callQueue) {
        graph.findLinks(pin)
                .stream()
                .map(link -> link.target().node())
                .forEach(callQueue::add);
    }

    public List<INode> successors(final Graph graph) {
        return pin.successors(graph).stream().map(Pin::node).toList();
    }
}
