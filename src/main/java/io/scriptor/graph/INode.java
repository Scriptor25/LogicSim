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

import imgui.extension.imnodes.ImNodes;
import io.scriptor.instruction.Instruction;
import io.scriptor.util.IUnique;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface INode extends IUnique {

    default int id() {
        return uuid().hashCode();
    }

    default boolean notSelected() {
        return !ImNodes.isNodeSelected(id());
    }

    default void select() {
        ImNodes.selectNode(id());
    }

    @NotNull Pin input(final int i);

    @NotNull Pin output(final int i);

    boolean powered(final @NotNull Graph graph, final boolean output, final int index);

    @NotNull Optional<Pin> pin(final int id);

    boolean noPredecessor(final @NotNull Graph graph);

    boolean noSuccessors(final @NotNull Graph graph);

    @NotNull List<INode> successors(final @NotNull Graph graph);

    void show(final @NotNull Graph graph);

    @NotNull INode copy();

    void compile(final @NotNull Graph graph, final @NotNull Collection<Instruction> instructions, final @NotNull Set<INode> compiled);

    boolean @NotNull [] exec(final @NotNull Graph graph, final @NotNull Set<INode> executing);
}
