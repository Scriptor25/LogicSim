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

import imgui.ImVec2;
import io.scriptor.context.State;
import io.scriptor.instruction.*;
import io.scriptor.util.RTException;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;
import java.util.stream.Stream;

import static io.scriptor.util.Constants.NODE_ID_BLUEPRINT;
import static io.scriptor.util.IO.*;

public class BlueprintNode extends Node {

    public static @NotNull Node asNode(final @NotNull Graph graph, final @NotNull String string) {
        final var split = string.split(",");
        final var blueprintUUID = UUID.fromString(split[1]);
        final var node = graph
                .context()
                .get(blueprintUUID)
                .<Node>map(BlueprintNode::new)
                .orElseGet(InvalidNode::new);
        final var posX = Integer.parseInt(split[2]);
        final var posY = Integer.parseInt(split[3]);
        node.editorPosition(new ImVec2(posX, posY));
        return node;
    }

    public static @NotNull Node read(final @NotNull Graph graph, final @NotNull InputStream stream) throws IOException {
        final var uuid = readUUID(stream);
        final var blueprintUUID = readUUID(stream);
        final var node = graph
                .context()
                .get(blueprintUUID)
                .<Node>map(blueprint -> new BlueprintNode(uuid, blueprint))
                .orElseGet(() -> new InvalidNode(uuid));
        final var posX = readInt(stream);
        final var posY = readInt(stream);
        node.position(posX, posY);
        return node;
    }

    private final Blueprint blueprint;

    private final Map<Integer, Pin> inputs = new HashMap<>();
    private final Map<Integer, Pin> outputs = new HashMap<>();

    private boolean running = false;
    private boolean[] output;
    private State state;

    public BlueprintNode(final @NotNull Blueprint blueprint) {
        this(UUID.randomUUID(), blueprint);
    }

    public BlueprintNode(final @NotNull UUID uuid, final @NotNull Blueprint blueprint) {
        super(uuid);
        this.blueprint = blueprint;
    }

    @Override
    public @NotNull Pin input(final int i) {
        if (i < 0 || i >= numInputs())
            throw new RTException();
        return inputs.computeIfAbsent(i, key -> new Pin(this, key, false));
    }

    @Override
    public @NotNull Pin output(final int i) {
        if (i < 0 || i >= numOutputs())
            throw new RTException();
        return outputs.computeIfAbsent(i, key -> new Pin(this, key, true));
    }

    @Override
    public int numInputs() {
        return blueprint.numInputs();
    }

    @Override
    public int numOutputs() {
        return blueprint.numOutputs();
    }

    @Override
    public boolean powered(final @NotNull Graph graph, final boolean output, final int index) {
        if (output && index >= 0 && index < numOutputs())
            return this.output != null && this.output[index];
        if (!output && index >= 0 && index < numInputs())
            return input(index)
                    .predecessor(graph)
                    .map(pin -> pin.powered(graph))
                    .orElse(false);
        throw new RTException("cannot get powered state of %s pin at index '%d'", output ? "output" : "input", index);
    }

    @Override
    public @NotNull Optional<Pin> pin(final int id) {
        return Stream
                .concat(
                        inputs
                                .values()
                                .stream(),
                        outputs
                                .values()
                                .stream())
                .filter(x -> x.id() == id)
                .findFirst();
    }

    @Override
    public boolean front(final @NotNull Graph graph) {
        return inputs
                .values()
                .stream()
                .allMatch(pin -> pin
                        .predecessor(graph)
                        .isEmpty());
    }

    @Override
    public boolean back(final @NotNull Graph graph) {
        return outputs
                .values()
                .stream()
                .allMatch(x -> x
                        .successors(graph)
                        .findAny()
                        .isEmpty());
    }

    @Override
    public @NotNull Stream<Node> successors(final @NotNull Graph graph) {
        return outputs
                .values()
                .stream()
                .mapMulti((pin, consumer) -> graph
                        .findLinks(pin)
                        .map(link -> link
                                .target()
                                .node())
                        .forEach(consumer));
    }

    @Override
    public boolean uses(final @NotNull Blueprint blueprint) {
        return this.blueprint == blueprint || this.blueprint.uses(blueprint);
    }

    @Override
    public void show(final @NotNull Graph graph) {
        blueprint.show(graph, this);
    }

    @Override
    public @NotNull Node copy(final @NotNull Map<Attribute, Attribute> copies) {
        final var node = new BlueprintNode(blueprint);
        node.position(posX(), posY());
        return node;
    }

    @Override
    public void compile(final @NotNull Graph graph, final @NotNull Collection<Instruction> instructions) {
        if (running)
            return;
        running = true;

        final var input = new Instruction[numInputs()];
        for (int i = 0; i < input.length; ++i)
            input[i] = input(i)
                    .predecessor(graph)
                    .<Instruction>map(pre -> {
                        pre
                                .node()
                                .compile(graph, instructions);
                        return new GetRegInstruction(pre.node().uuid(), pre.index());
                    })
                    .orElseGet(() -> new ConstInstruction(false));

        final var call = new CallInstruction(blueprint.uuid(), input);
        instructions.add(call);

        final var outputCount = numOutputs();
        for (int i = 0; i < outputCount; ++i)
            instructions.add(new SetRegInstruction(uuid(), i, new GetResultInstruction(call, i)));

        running = false;
    }

    @Override
    public boolean @NotNull [] exec(final @NotNull Graph graph) {
        if (running)
            return output;
        running = true;

        final var input = new boolean[numInputs()];
        for (int i = 0; i < input.length; ++i)
            input[i] = input(i)
                    .predecessor(graph)
                    .map(pre -> pre
                            .node()
                            .exec(graph)[pre.index()])
                    .orElse(false);

        if (state == null)
            state = new State(graph.state());

        output = new boolean[numOutputs()];
        blueprint.exec(state, input, output);

        running = false;
        return output;
    }

    @Override
    public @NotNull String asString() {
        final var pos = editorPosition();
        return "%d,%s,%d,%d".formatted(NODE_ID_BLUEPRINT, blueprint, (int) pos.x, (int) pos.y);
    }

    @Override
    public void write(@NotNull OutputStream stream) throws IOException {
        writeByte(stream, NODE_ID_BLUEPRINT);
        writeUUID(stream, uuid());
        writeUUID(stream, blueprint.uuid());
        writeInt(stream, posX());
        writeInt(stream, posY());
    }
}
