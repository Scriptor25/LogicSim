package io.scriptor.graph;

import imgui.ImGui;
import imgui.extension.imnodes.ImNodes;
import io.scriptor.instruction.GetAttribInstruction;
import io.scriptor.instruction.Instruction;
import io.scriptor.instruction.SetRegInstruction;

import java.util.*;

public class Input implements INode {

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
    public Optional<Pin> pin(final int id) {
        if (pin.id() == id) return Optional.of(pin);
        return Optional.empty();
    }

    @Override
    public boolean noPredecessor(final Graph graph) {
        return true;
    }

    @Override
    public boolean noSuccessors(final Graph graph) {
        return pin.successors(graph).isEmpty();
    }

    @Override
    public List<INode> successors(final Graph graph) {
        return graph.findLinks(pin)
                .stream()
                .map(link -> link.target().node())
                .toList();
    }

    @Override
    public void show(final Graph graph) {
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
    public void compile(final Graph graph, final Collection<Instruction> instructions, final Set<INode> compiling) {
        final var get = new GetAttribInstruction(attribute.uuid());
        final var set = new SetRegInstruction(uuid, 0, get);
        instructions.add(get);
        instructions.add(set);
    }
}
