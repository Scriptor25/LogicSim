package io.scriptor.graph;

import imgui.ImGui;
import imgui.extension.imnodes.ImNodes;
import imgui.extension.imnodes.flag.ImNodesCol;
import io.scriptor.Constants;
import io.scriptor.instruction.GetAttribInstruction;
import io.scriptor.instruction.Instruction;
import io.scriptor.instruction.SetRegInstruction;
import io.scriptor.util.RTException;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class Input implements INode {

    private final UUID uuid;
    private final Attribute attribute;
    private final Pin pin = new Pin(this, 0, true);

    public Input(final @NotNull UUID uuid, final @NotNull Attribute attribute) {
        this.uuid = uuid;
        this.attribute = attribute;
    }

    public @NotNull String label() {
        return attribute.label().get();
    }

    public boolean powered() {
        return attribute.powered().get();
    }

    @Override
    public @NotNull UUID uuid() {
        return uuid;
    }

    @Override
    public @NotNull Pin input(final int i) {
        throw new RTException("no input pin at index '%d'", i);
    }

    @Override
    public @NotNull Pin output(final int i) {
        if (i == 0) return pin;
        throw new RTException("no output pin at index '%d'", i);
    }

    @Override
    public boolean powered(final @NotNull Graph graph, final boolean output, final int index) {
        if (output && index == 0) return powered();
        throw new RTException("cannot get powered state of %s pin at index '%d'", output ? "output" : "input", index);
    }

    @Override
    public @NotNull Optional<Pin> pin(final int id) {
        if (pin.id() == id) return Optional.of(pin);
        return Optional.empty();
    }

    @Override
    public boolean noPredecessor(final @NotNull Graph graph) {
        return true;
    }

    @Override
    public boolean noSuccessors(final @NotNull Graph graph) {
        return pin.successors(graph).isEmpty();
    }

    @Override
    public @NotNull List<INode> successors(final @NotNull Graph graph) {
        return graph.findLinks(pin)
                .stream()
                .map(link -> link.target().node())
                .toList();
    }

    @Override
    public void show(final @NotNull Graph graph) {
        ImNodes.beginNode(id());

        final var powered = powered();
        if (powered) ImNodes.pushColorStyle(ImNodesCol.Pin, Constants.COLOR_POWERED);
        ImNodes.beginOutputAttribute(pin.id());
        ImGui.checkbox("##powered", attribute.powered());
        ImGui.sameLine();
        ImGui.textUnformatted(label());
        ImNodes.endOutputAttribute();
        if (powered) ImNodes.popColorStyle();

        ImNodes.endNode();
    }

    @Override
    public @NotNull INode copy() {
        return new Input(UUID.randomUUID(), attribute);
    }

    @Override
    public void compile(final @NotNull Graph graph, final @NotNull Collection<Instruction> instructions, final @NotNull Set<INode> compiling) {
        final var get = new GetAttribInstruction(attribute.uuid());
        final var set = new SetRegInstruction(uuid, 0, get);
        instructions.add(get);
        instructions.add(set);
    }

    @Override
    public boolean @NotNull [] exec(final @NotNull Graph graph, final @NotNull Set<INode> executing) {
        return new boolean[]{powered()};
    }
}
