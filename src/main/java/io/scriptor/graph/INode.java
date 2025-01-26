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

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface INode extends IUnique {

    static @NotNull INode parse(final @NotNull Graph graph, final @NotNull String string) {
        final var split = string.split(",");
        final var node = (switch (split[0]) {
            case "-1" -> InvalidNode.parse();
            case "0" -> Input.parse(graph, string);
            case "1" -> Output.parse(graph, string);
            case "2" -> Node.parse(graph, string);
            default -> throw new RTException("undefined node type in '%s'", string);
        }).orElseGet(InvalidNode::new);
        final var posX = Integer.parseInt(split[2]);
        final var posY = Integer.parseInt(split[3]);
        node.screenPosition(new ImVec2(posX, posY));
        return node;
    }

    default int id() {
        return uuid().hashCode();
    }

    default boolean notSelected() {
        return !ImNodes.isNodeSelected(id());
    }

    default void select() {
        ImNodes.selectNode(id());
    }

    default @NotNull ImVec2 editorPosition() {
        return ImNodes.getNodeEditorSpacePos(id());
    }

    default void editorPosition(final @NotNull ImVec2 pos) {
        ImNodes.setNodeEditorSpacePos(id(), pos);
    }

    default @NotNull ImVec2 gridPosition() {
        return ImNodes.getNodeGridSpacePos(id());
    }

    default void gridPosition(final @NotNull ImVec2 pos) {
        ImNodes.setNodeGridSpacePos(id(), pos);
    }

    default @NotNull ImVec2 screenPosition() {
        return ImNodes.getNodeScreenSpacePos(id());
    }

    default void screenPosition(final @NotNull ImVec2 pos) {
        ImNodes.setNodeScreenSpacePos(id(), pos);
    }

    @NotNull Pin input(final int i);

    @NotNull Pin output(final int i);

    boolean powered(final @NotNull Graph graph, final boolean output, final int index);

    @NotNull Optional<Pin> pin(final int id);

    boolean isBegin(final @NotNull Graph graph);

    boolean isEnd(final @NotNull Graph graph);

    @NotNull List<INode> successors(final @NotNull Graph graph);

    void show(final @NotNull Graph graph);

    @NotNull INode copy();

    void compile(final @NotNull Graph graph, final @NotNull Collection<Instruction> instructions, final @NotNull Set<INode> compiling);

    boolean @NotNull [] exec(final @NotNull Graph graph, final @NotNull Set<INode> executing);

    default @NotNull String getString() {
        final var pos = screenPosition();
        return "%s,%d,%d".formatted(getPvtString(), (int) pos.x, (int) pos.y);
    }

    @NotNull String getPvtString();
}
