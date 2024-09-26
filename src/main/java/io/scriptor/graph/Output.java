package io.scriptor.graph;

import imgui.ImGui;
import imgui.extension.imnodes.ImNodes;
import imgui.extension.imnodes.flag.ImNodesCol;
import io.scriptor.Constants;
import io.scriptor.instruction.ConstInstruction;
import io.scriptor.instruction.GetRegInstruction;
import io.scriptor.instruction.Instruction;
import io.scriptor.instruction.SetAttribInstruction;

import java.util.*;

public class Output implements INode {

    private final UUID uuid;
    private final Attribute attribute;
    private final Pin pin = new Pin(this, 0, false);

    public Output(final UUID uuid, final Attribute attribute) {
        this.uuid = uuid;
        this.attribute = attribute;
    }

    public boolean powered() {
        return attribute.powered().get();
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
    public boolean powered(final Graph graph, final boolean output, final int index) {
        if (!output && index == 0) return powered();
        throw new IllegalStateException();
    }

    @Override
    public Optional<Pin> pin(final int id) {
        if (pin.id() == id) return Optional.of(pin);
        return Optional.empty();
    }

    @Override
    public boolean noPredecessor(final Graph graph) {
        return pin.predecessor(graph).isEmpty();
    }

    @Override
    public boolean noSuccessors(final Graph graph) {
        return true;
    }

    @Override
    public List<INode> successors(final Graph graph) {
        return List.of();
    }

    @Override
    public void show(final Graph graph) {
        ImNodes.beginNode(id());

        final var powered = powered();
        if (powered) ImNodes.pushColorStyle(ImNodesCol.Pin, Constants.COLOR_POWERED);
        ImNodes.beginInputAttribute(pin.id());
        ImGui.textUnformatted(attribute.label().get());
        ImGui.sameLine();
        ImGui.checkbox("##powered", powered);
        ImNodes.endInputAttribute();
        if (powered) ImNodes.popColorStyle();

        ImNodes.endNode();
    }

    @Override
    public Output copy() {
        return new Output(UUID.randomUUID(), attribute);
    }

    @Override
    public void compile(final Graph graph, final Collection<Instruction> instructions, Set<INode> compiling) {
        final var pre = pin.predecessor(graph);
        final Instruction get;
        if (pre.isPresent()) {
            pre.get().node().compile(graph, instructions, compiling);
            get = new GetRegInstruction(pre.get().node().uuid(), pre.get().index());
        } else get = new ConstInstruction(false);
        final var set = new SetAttribInstruction(attribute.uuid(), get);
        instructions.add(get);
        instructions.add(set);
    }

    @Override
    public boolean[] exec(final Graph graph, final Set<INode> executing) {
        final var pre = pin.predecessor(graph);
        if (pre.isPresent()) {
            final var out = pre.get().node().exec(graph, executing);
            attribute.powered().set(out[pre.get().index()]);
        } else attribute.powered().set(false);
        return new boolean[0];
    }
}
