package io.scriptor.graph;

import io.scriptor.util.RTException;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

/**
 * This class is part of the <a href="https://github.com/Scriptor25/LogicSim">Java Logic Sim</a> project.
 * <p>
 * Copyright (C) 2025  Felix Schreiber
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <a href="https://www.gnu.org/licenses/">https://www.gnu.org/licenses/</a>.
 *
 * @author Felix Schreiber
 */
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
