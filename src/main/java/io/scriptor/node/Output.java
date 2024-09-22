package io.scriptor.node;

import imgui.ImGui;
import imgui.extension.imnodes.ImNodes;

import java.util.Optional;
import java.util.Queue;

public class Output implements INode {

    private final Attribute attribute;
    private final Pin pin = new Pin(this, 0, false);

    public Output(final Attribute attribute) {
        this.attribute = attribute;
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
    public void show() {
        ImNodes.beginNode(id());
        ImNodes.beginInputAttribute(pin.id());
        ImGui.textUnformatted(attribute.label().get());
        ImGui.sameLine();
        ImGui.checkbox("##powered", attribute.powered().get());
        ImNodes.endInputAttribute();
        ImNodes.endNode();
    }

    @Override
    public Output copy() {
        return new Output(attribute);
    }

    @Override
    public void cycle(final Graph graph, final Queue<INode> callQueue) {
        pin.predecessor(graph).ifPresent(x -> attribute.powered().set(x.powered()));
    }

    public Optional<Pin> predecessor(final Graph graph) {
        return pin.predecessor(graph);
    }
}
