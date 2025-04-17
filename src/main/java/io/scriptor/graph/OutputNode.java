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
import imgui.extension.imnodes.flag.ImNodesCol;
import io.scriptor.instruction.ConstInstruction;
import io.scriptor.instruction.GetRegInstruction;
import io.scriptor.instruction.Instruction;
import io.scriptor.instruction.SetAttribInstruction;
import io.scriptor.util.Constants;
import io.scriptor.util.RTException;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static io.scriptor.util.Constants.NODE_ID_OUTPUT;
import static io.scriptor.util.IO.*;

public class OutputNode extends Node {

    public static @NotNull Node parse(final @NotNull Graph graph, final @NotNull String string) {
        final var split = string.split(",");
        final var node = graph
                .findAttribute(UUID.fromString(split[1]))
                .<Node>map(OutputNode::new)
                .orElseGet(InvalidNode::new);
        final var posX = Integer.parseInt(split[2]);
        final var posY = Integer.parseInt(split[3]);
        node.editorPosition(new ImVec2(posX, posY));
        return node;
    }

    public static @NotNull Node read(final @NotNull Graph graph, final @NotNull InputStream stream) throws IOException {
        final var uuid = readUUID(stream);
        final var attributeUUID = readUUID(stream);
        final var node = graph
                .findAttribute(attributeUUID)
                .<Node>map(attribute -> new OutputNode(uuid, attribute))
                .orElseGet(() -> new InvalidNode(uuid));
        final var posX = readInt(stream);
        final var posY = readInt(stream);
        node.position(posX, posY);
        return node;
    }

    private final Attribute attribute;
    private final Pin pin;

    public OutputNode(final @NotNull UUID uuid, final @NotNull Attribute attribute) {
        super(uuid);
        this.attribute = attribute;
        this.pin = new Pin(this, 0, false, (byte) 1); // TODO: bitwidth
    }

    public OutputNode(final @NotNull Attribute attribute) {
        this(UUID.randomUUID(), attribute);
    }

    public int data() {
        return attribute.data().get();
    }

    @Override
    public @NotNull Pin input(final int i, final byte bitwidth) {
        if (i == 0) return pin;
        throw new RTException("no input pin at index '%d'", i);
    }

    @Override
    public @NotNull Pin output(final int i, final byte bitwidth) {
        throw new RTException("no output pin at index '%d'", i);
    }

    @Override
    public @NotNull Optional<Pin> input(final int i) {
        if (i == 0) return Optional.of(pin);
        return Optional.empty();
    }

    @Override
    public @NotNull Optional<Pin> output(final int i) {
        return Optional.empty();
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
    public int data(final @NotNull Graph graph, final boolean output, final int index) {
        if (!output && index == 0)
            return data();
        throw new RTException("cannot get data state of %s pin at index '%d'", output ? "output" : "input", index);
    }

    @Override
    public @NotNull Optional<Pin> pin(final int id) {
        if (pin.id() == id)
            return Optional.of(pin);
        return Optional.empty();
    }

    @Override
    public boolean front(final @NotNull Graph graph) {
        return pin.predecessor(graph).isEmpty();
    }

    @Override
    public boolean back(final @NotNull Graph graph) {
        return true;
    }

    @Override
    public @NotNull Stream<Node> successors(final @NotNull Graph graph) {
        return Stream.empty();
    }

    @Override
    public boolean same(final @NotNull Attribute attribute) {
        return this.attribute == attribute;
    }

    @Override
    public void show(final @NotNull Graph graph) {
        ImNodes.beginNode(id());

        final var d = data();
        final var powered = d != 0;
        if (powered) ImNodes.pushColorStyle(ImNodesCol.Pin, Constants.COLOR_POWERED);
        ImNodes.beginInputAttribute(pin.id());
        ImGui.textUnformatted(attribute.label().get());
        ImGui.sameLine();
        ImGui.textUnformatted("%08X".formatted(d));
        ImNodes.endInputAttribute();
        if (powered) ImNodes.popColorStyle();

        ImNodes.endNode();
    }

    @Override
    public @NotNull Node copy(final @NotNull Map<Attribute, Attribute> copies) {
        final var node = new OutputNode(copies.get(attribute));
        node.position(posX(), posY());
        return node;
    }

    @Override
    public void compile(final @NotNull Graph graph, final @NotNull Collection<Instruction> instructions) {
        final var get = pin
                .predecessor(graph)
                .<Instruction>map(pre -> {
                    pre.node().compile(graph, instructions);
                    return new GetRegInstruction(pre.node().uuid(), pre.index());
                })
                .orElseGet(() -> new ConstInstruction(0));
        instructions.add(new SetAttribInstruction(attribute.uuid(), get));
    }

    @Override
    public int @NotNull [] execute(final @NotNull Graph graph) {
        attribute.data().set(pin
                .predecessor(graph)
                .map(pre -> pre.node().execute(graph)[pre.index()])
                .orElse(0));
        return new int[]{};
    }

    @Override
    public @NotNull String string() {
        final var pos = editorPosition();
        return "%d,%s,%d,%d".formatted(NODE_ID_OUTPUT, attribute.uuid(), (int) pos.x, (int) pos.y);
    }

    @Override
    public void write(final @NotNull OutputStream stream) throws IOException {
        writeByte(stream, NODE_ID_OUTPUT);
        writeUUID(stream, uuid());
        writeUUID(stream, attribute.uuid());
        writeInt(stream, posX());
        writeInt(stream, posY());
    }
}
