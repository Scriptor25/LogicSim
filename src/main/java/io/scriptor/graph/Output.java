/*
 * This file is part of https://github.com/Scriptor25/LogicSim
 *
 * Copyright (C) 2025  Felix Schreiber
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see https://www.gnu.org/licenses/.
 */
package io.scriptor.graph;

import imgui.ImGui;
import imgui.extension.imnodes.ImNodes;
import imgui.extension.imnodes.flag.ImNodesCol;
import io.scriptor.util.Constants;
import io.scriptor.instruction.ConstInstruction;
import io.scriptor.instruction.GetRegInstruction;
import io.scriptor.instruction.Instruction;
import io.scriptor.instruction.SetAttribInstruction;
import io.scriptor.util.RTException;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class Output implements INode {

    public static @NotNull Optional<INode> parse(final @NotNull Graph graph, final @NotNull String string) {
        final var split = string.split(",");
        return graph
                .findAttribute(UUID.fromString(split[1]))
                .map(attribute -> new Output(UUID.randomUUID(), attribute));
    }

    private final UUID uuid;
    private final Attribute attribute;
    private final Pin pin = new Pin(this, 0, false);

    public Output(final @NotNull UUID uuid, final @NotNull Attribute attribute) {
        this.uuid = uuid;
        this.attribute = attribute;
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
        if (i == 0)
            return pin;
        throw new RTException("no input pin at index '%d'", i);
    }

    @Override
    public @NotNull Pin output(final int i) {
        throw new RTException("no output pin at index '%d'", i);
    }

    @Override
    public int numInputs() {
        return 1;
    }

    @Override
    public int numOutputs() {
        return 0;
    }

    @Override
    public boolean powered(final @NotNull Graph graph, final boolean output, final int index) {
        if (!output && index == 0)
            return powered();
        throw new RTException("cannot get powered state of %s pin at index '%d'", output ? "output" : "input", index);
    }

    @Override
    public @NotNull Optional<Pin> pin(final int id) {
        if (pin.id() == id)
            return Optional.of(pin);
        return Optional.empty();
    }

    @Override
    public boolean isBegin(final @NotNull Graph graph) {
        return pin.predecessor(graph).isEmpty();
    }

    @Override
    public boolean isEnd(final @NotNull Graph graph) {
        return true;
    }

    @Override
    public @NotNull List<INode> successors(final @NotNull Graph graph) {
        return List.of();
    }

    @Override
    public boolean uses(final @NotNull Attribute attribute) {
        return this.attribute == attribute;
    }

    @Override
    public void show(final @NotNull Graph graph) {
        ImNodes.beginNode(id());

        final var powered = powered();
        if (powered) ImNodes.pushColorStyle(ImNodesCol.Pin, Constants.COLOR_POWERED);
        ImNodes.beginInputAttribute(pin.id());
        ImGui.textUnformatted(attribute.label().get());
        ImGui.sameLine();
        ImGui.beginDisabled();
        ImGui.checkbox("##powered", powered);
        ImGui.endDisabled();
        ImNodes.endInputAttribute();
        if (powered) ImNodes.popColorStyle();

        ImNodes.endNode();
    }

    @Override
    public @NotNull INode copy() {
        return new Output(UUID.randomUUID(), attribute);
    }

    @Override
    public void compile(final @NotNull Graph graph, final @NotNull Collection<Instruction> instructions, final @NotNull Set<INode> compiling) {
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
    public boolean @NotNull [] exec(final @NotNull Graph graph, final @NotNull Set<INode> executing) {
        final var pre = pin.predecessor(graph);
        if (pre.isPresent()) {
            final var out = pre.get().node().exec(graph, executing);
            attribute.powered().set(out[pre.get().index()]);
        } else attribute.powered().set(false);
        return new boolean[0];
    }

    @Override
    public @NotNull String getPvtString() {
        return "1,%s".formatted(attribute.uuid());
    }
}
