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

import io.scriptor.util.RTException;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public record Pin(@NotNull INode node, int index, boolean output) {

    public int id() {
        return hashCode();
    }

    public boolean powered(final @NotNull Graph graph) {
        return node.powered(graph, output, index);
    }

    public boolean uses(final @NotNull INode node) {
        return node == this.node;
    }

    public @NotNull Optional<Pin> predecessor(final @NotNull Graph graph) {
        if (output) throw new RTException("pin is in output mode, does not have predecessor");
        return graph.findLink(this).map(Link::source);
    }

    public @NotNull List<Pin> successors(final @NotNull Graph graph) {
        if (!output) throw new RTException("pin is in input mode, does not have successors");
        return graph.findLinks(this).stream().map(Link::target).toList();
    }
}
