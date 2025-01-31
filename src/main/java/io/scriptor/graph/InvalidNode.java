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
import imgui.ImVec2;
import imgui.extension.imnodes.ImNodes;
import io.scriptor.instruction.Instruction;
import io.scriptor.util.RTException;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;
import java.util.stream.Stream;

import static io.scriptor.util.Constants.NODE_ID_INVALID;
import static io.scriptor.util.IO.*;

public class InvalidNode extends Node {

    public static @NotNull Node asNode(final @NotNull String string) {
        final var split = string.split(",");
        final var node = new InvalidNode();
        final var posX = Integer.parseInt(split[1]);
        final var posY = Integer.parseInt(split[2]);
        node.editorPosition(new ImVec2(posX, posY));
        return node;
    }

    public static @NotNull InvalidNode read(final @NotNull InputStream stream) throws IOException {
        final var uuid = readUUID(stream);
        final var inputCount = readInt(stream);
        final var outputCount = readInt(stream);
        final var node = new InvalidNode(uuid, inputCount, outputCount);
        final var posX = readInt(stream);
        final var posY = readInt(stream);
        node.position(posX, posY);
        return node;
    }

    private final List<Pin> inputs = new ArrayList<>();
    private final List<Pin> outputs = new ArrayList<>();

    private boolean running = false;

    public InvalidNode() {
        this(UUID.randomUUID(), 0, 0);
    }

    public InvalidNode(final @NotNull UUID uuid) {
        this(uuid, 0, 0);
    }

    public InvalidNode(final @NotNull UUID uuid, final int inputCount, final int outputCount) {
        super(uuid);
        for (int i = 0; i < inputCount; ++i)
            inputs.add(new Pin(this, i, false));
        for (int i = 0; i < outputCount; ++i)
            outputs.add(new Pin(this, i, true));
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
    public boolean front(final @NotNull Graph graph) {
        return inputs
                .stream()
                .allMatch(x -> x
                        .predecessor(graph)
                        .isEmpty());
    }

    @Override
    public boolean back(final @NotNull Graph graph) {
        return outputs
                .stream()
                .allMatch(x -> x
                        .successors(graph)
                        .findAny()
                        .isEmpty());
    }

    @Override
    public @NotNull Stream<Node> successors(final @NotNull Graph graph) {
        return outputs
                .stream()
                .<Node>mapMulti((pin, consumer) -> graph
                        .findLinks(pin)
                        .map(link -> link.target().node())
                        .forEach(consumer));
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
            ImGui.newLine();
            ImNodes.endInputAttribute();
            ImGui.sameLine();
            ImNodes.beginOutputAttribute(outputs.get(i).id());
            ImGui.newLine();
            ImNodes.endOutputAttribute();
        }
        for (; i < inputs.size(); ++i) {
            ImNodes.beginInputAttribute(inputs.get(i).id());
            ImGui.newLine();
            ImNodes.endInputAttribute();
        }
        for (; i < outputs.size(); ++i) {
            if (!inputs.isEmpty()) {
                ImNodes.beginStaticAttribute(i);
                ImGui.newLine();
                ImNodes.endStaticAttribute();
                ImGui.sameLine();
            }
            ImNodes.beginOutputAttribute(outputs.get(i).id());
            ImGui.newLine();
            ImNodes.endOutputAttribute();
        }

        ImNodes.endNode();
    }

    @Override
    public @NotNull Node copy(final @NotNull Map<Attribute, Attribute> copies) {
        final var node = new InvalidNode();
        node.position(posX(), posY());
        return node;
    }

    @Override
    public void compile(final @NotNull Graph graph, final @NotNull Collection<Instruction> instructions) {
        if (running)
            return;
        running = true;

        for (final var input : inputs)
            input
                    .predecessor(graph)
                    .ifPresent(pre -> pre
                            .node()
                            .compile(graph, instructions));

        running = false;
    }

    @Override
    public boolean @NotNull [] exec(final @NotNull Graph graph) {
        if (running)
            return new boolean[outputs.size()];
        running = true;

        for (final var input : inputs)
            input
                    .predecessor(graph)
                    .ifPresent(pin -> pin
                            .node()
                            .exec(graph));

        running = false;
        return new boolean[outputs.size()];
    }

    @Override
    public @NotNull String string() {
        final var pos = editorPosition();
        return "%d,%d,%d".formatted(NODE_ID_INVALID, (int) pos.x, (int) pos.y);
    }

    @Override
    public void write(final @NotNull OutputStream stream) throws IOException {
        writeByte(stream, NODE_ID_INVALID);
        writeUUID(stream, uuid());
        writeInt(stream, inputs.size());
        writeInt(stream, outputs.size());
        writeInt(stream, posX());
        writeInt(stream, posY());
    }
}
