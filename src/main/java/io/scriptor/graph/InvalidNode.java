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
import io.scriptor.instruction.Instruction;
import io.scriptor.util.RTException;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Stream;

public class InvalidNode implements INode {

    public static @NotNull Optional<INode> parse() {
        return Optional.of(new InvalidNode());
    }

    private final UUID uuid = UUID.randomUUID();
    private final List<Pin> inputs = new ArrayList<>();
    private final List<Pin> outputs = new ArrayList<>();

    @Override
    public @NotNull UUID uuid() {
        return uuid;
    }

    @Override
    public @NotNull Pin input(final int i) {
        while (i >= inputs.size())
            inputs.add(new Pin(this, inputs.size(), false));
        return inputs.get(i);
    }

    @Override
    public @NotNull Pin output(final int i) {
        while (i >= outputs.size())
            outputs.add(new Pin(this, outputs.size(), true));
        return outputs.get(i);
    }

    @Override
    public int numInputs() {
        return inputs.size();
    }

    @Override
    public int numOutputs() {
        return outputs.size();
    }

    @Override
    public boolean powered(final @NotNull Graph graph, final boolean output, final int index) {
        if (output)
            return false;
        if (index < inputs.size())
            return inputs
                    .get(index)
                    .predecessor(graph)
                    .map(pin -> pin.powered(graph))
                    .orElse(false);
        throw new RTException("cannot get powered state of input pin at index '%d'", index);
    }

    @Override
    public @NotNull Optional<Pin> pin(final int id) {
        return Stream.concat(
                        inputs.stream(),
                        outputs.stream())
                .filter(x -> x.id() == id)
                .findFirst();
    }

    @Override
    public boolean isBegin(final @NotNull Graph graph) {
        return inputs
                .stream()
                .allMatch(x -> x
                        .predecessor(graph)
                        .isEmpty());
    }

    @Override
    public boolean isEnd(final @NotNull Graph graph) {
        return outputs
                .stream()
                .allMatch(x -> x
                        .successors(graph)
                        .findAny()
                        .isEmpty());
    }

    @Override
    public @NotNull List<INode> successors(final @NotNull Graph graph) {
        return outputs
                .stream()
                .<INode>mapMulti((pin, consumer) -> graph
                        .findLinks(pin)
                        .map(link -> link.target().node())
                        .forEach(consumer))
                .toList();
    }

    @Override
    public void show(final @NotNull Graph graph) {
        ImNodes.beginNode(id());

        ImNodes.beginNodeTitleBar();
        ImGui.textUnformatted("Invalid Node");
        ImNodes.endNodeTitleBar();

        int i = 0;
        for (; i < Math.min(inputs.size(), outputs.size()); ++i) {
            ImNodes.beginInputAttribute(inputs.get(i).id());
            ImNodes.endInputAttribute();
            ImGui.sameLine();
            ImNodes.beginOutputAttribute(outputs.get(i).id());
            ImNodes.endOutputAttribute();
        }
        for (; i < inputs.size(); ++i) {
            ImNodes.beginInputAttribute(inputs.get(i).id());
            ImNodes.endInputAttribute();
        }
        for (; i < outputs.size(); ++i) {
            if (!inputs.isEmpty()) {
                ImNodes.beginStaticAttribute(i);
                ImNodes.endStaticAttribute();
                ImGui.sameLine();
            }
            ImNodes.beginOutputAttribute(outputs.get(i).id());
            ImNodes.endOutputAttribute();
        }

        ImNodes.endNode();
    }

    @Override
    public @NotNull INode copy() {
        return new InvalidNode();
    }

    @Override
    public void compile(final @NotNull Graph graph, final @NotNull Collection<Instruction> instructions, final @NotNull Set<INode> compiling) {
    }

    @Override
    public boolean @NotNull [] exec(final @NotNull Graph graph, final @NotNull Set<INode> executing) {
        return new boolean[]{false};
    }

    @Override
    public @NotNull String getPvtString() {
        return "-1,none";
    }
}
