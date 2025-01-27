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
import io.scriptor.instruction.GetAttribInstruction;
import io.scriptor.instruction.Instruction;
import io.scriptor.instruction.SetRegInstruction;
import io.scriptor.util.RTException;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class Input implements INode {

    public static @NotNull Optional<INode> parse(final @NotNull Graph graph, final @NotNull String string) {
        final var split = string.split(",");
        return graph
                .findAttribute(UUID.fromString(split[1]))
                .map(attribute -> new Input(UUID.randomUUID(), attribute));
    }

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
    public int numInputs() {
        return 0;
    }

    @Override
    public int numOutputs() {
        return 1;
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
    public boolean isBegin(final @NotNull Graph graph) {
        return true;
    }

    @Override
    public boolean isEnd(final @NotNull Graph graph) {
        return pin
                .successors(graph)
                .findAny()
                .isEmpty();
    }

    @Override
    public @NotNull List<INode> successors(final @NotNull Graph graph) {
        return graph
                .findLinks(pin)
                .map(link -> link.target().node())
                .toList();
    }

    @Override
    public boolean uses(final @NotNull Attribute attribute) {
        return this.attribute == attribute;
    }

    @Override
    public void show(final @NotNull Graph graph) {
        ImNodes.beginNode(id());

        final var powered = powered();
        if (powered)
            ImNodes.pushColorStyle(ImNodesCol.Pin, Constants.COLOR_POWERED);
        ImNodes.beginOutputAttribute(pin.id());
        ImGui.checkbox("##powered", attribute.powered());
        ImGui.sameLine();
        ImGui.textUnformatted(label());
        ImNodes.endOutputAttribute();
        if (powered)
            ImNodes.popColorStyle();

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

    @Override
    public @NotNull String getPvtString() {
        return "0,%s".formatted(attribute.uuid());
    }
}
