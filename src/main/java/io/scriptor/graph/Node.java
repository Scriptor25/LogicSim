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
import imgui.extension.imnodes.ImNodes;
import io.scriptor.instruction.Instruction;
import io.scriptor.util.IUnique;
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

import static io.scriptor.util.Constants.*;
import static io.scriptor.util.IO.readByte;

public abstract class Node implements IUnique {

    public static @NotNull Node parse(final @NotNull Graph graph,
                                      final @NotNull String string) {
        final var split = string.split(",");
        return switch (Byte.parseByte(split[0])) {
            case NODE_ID_UNDEFINED -> UndefinedNode.parse(graph, string);
            case NODE_ID_INPUT -> InputNode.parse(graph, string);
            case NODE_ID_OUTPUT -> OutputNode.parse(graph, string);
            case NODE_ID_BLUEPRINT -> BlueprintNode.parse(graph, string);
            default -> throw new RTException("undefined node type in '%s'", string);
        };
    }

    static @NotNull Node read(final @NotNull Graph graph,
                              final @NotNull InputStream stream) throws IOException {
        final var type = readByte(stream);
        return switch (type) {
            case NODE_ID_UNDEFINED -> UndefinedNode.read(graph, stream);
            case NODE_ID_INPUT -> InputNode.read(graph, stream);
            case NODE_ID_OUTPUT -> OutputNode.read(graph, stream);
            case NODE_ID_BLUEPRINT -> BlueprintNode.read(graph, stream);
            default -> throw new RTException("undefined node type '%d'", type);
        };
    }

    private final UUID uuid;
    private int posX;
    private int posY;

    protected Node(final @NotNull UUID uuid) {
        this.uuid = uuid;
    }

    @Override
    public @NotNull UUID uuid() {
        return uuid;
    }

    public int posX() {
        return posX;
    }

    public int posY() {
        return posY;
    }

    public void position(final int posX, final int posY) {
        this.posX = posX;
        this.posY = posY;
    }

    public int id() {
        return uuid().hashCode();
    }

    public boolean notSelected() {
        return !ImNodes.isNodeSelected(id());
    }

    public void select() {
        if (notSelected())
            ImNodes.selectNode(id());
    }

    public @NotNull ImVec2 editorPosition() {
        return ImNodes.getNodeEditorSpacePos(id());
    }

    public void editorPosition(final @NotNull ImVec2 pos) {
        ImNodes.setNodeEditorSpacePos(id(), pos);
    }

    public @NotNull ImVec2 gridPosition() {
        return ImNodes.getNodeGridSpacePos(id());
    }

    public void gridPosition(final @NotNull ImVec2 pos) {
        ImNodes.setNodeGridSpacePos(id(), pos);
    }

    public @NotNull ImVec2 screenPosition() {
        return ImNodes.getNodeScreenSpacePos(id());
    }

    public void screenPosition(final @NotNull ImVec2 pos) {
        ImNodes.setNodeScreenSpacePos(id(), pos);
    }

    public abstract @NotNull Pin input(final int index);

    public abstract @NotNull Pin output(final int index);

    public abstract int numInputs();

    public abstract int numOutputs();

    public abstract int data(final @NotNull Graph graph, final boolean output, final int index);

    public abstract @NotNull Optional<Pin> pin(final int id);

    /**
     * Check if this node has no predecessors.
     *
     * @param graph parent graph
     * @return if no predecessors
     */
    public abstract boolean front(final @NotNull Graph graph);

    /**
     * Check if this node has no successors.
     *
     * @param graph parent graph
     * @return if no successors
     */
    public abstract boolean back(final @NotNull Graph graph);

    public abstract @NotNull Stream<Node> successors(final @NotNull Graph graph);

    /**
     * Check if this node uses given attribute.
     *
     * @param attribute some attribute
     * @return if used
     */
    public boolean same(final @NotNull Attribute attribute) {
        return false;
    }

    @SuppressWarnings("java:S1172")
    public boolean same(final @NotNull Blueprint blueprint) {
        return false;
    }

    @SuppressWarnings("java:S1172")
    public boolean uses(final @NotNull Blueprint blueprint, final boolean recursive) {
        return false;
    }

    public abstract void show(final @NotNull Graph graph);

    public abstract @NotNull Node copy(final @NotNull Map<Attribute, Attribute> copies);

    public abstract void compile(final @NotNull Graph graph, final @NotNull Collection<Instruction> instructions);

    public abstract int @NotNull [] execute(final @NotNull Graph graph);

    public abstract @NotNull String string();

    public abstract void write(final @NotNull OutputStream stream) throws IOException;

    public void loadPosition() {
        gridPosition(new ImVec2(posX, posY));
    }

    public void savePosition() {
        final var pos = gridPosition();
        position((int) pos.x, (int) pos.y);
    }
}
