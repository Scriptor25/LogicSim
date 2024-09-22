package io.scriptor.node;

import imgui.ImGui;
import imgui.extension.imnodes.ImNodes;

import java.util.List;
import java.util.Optional;
import java.util.Queue;

public class Input implements INode {

    private final Attribute attribute;
    private final Pin pin = new Pin(this, 0, true);

    public Input(final Attribute attribute) {
        this.attribute = attribute;
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
        return new Input(attribute);
    }

    @Override
    public void cycle(final Graph graph, final Queue<INode> callQueue) {
        graph.findLinks(pin)
                .stream()
                .map(link -> link.target().node())
                .filter(node -> !callQueue.contains(node))
                .forEach(callQueue::add);
    }

    public List<INode> successors(final Graph graph) {
        return pin.successors(graph).stream().map(Pin::node).toList();
    }
}
