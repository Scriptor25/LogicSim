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
import imgui.extension.imnodes.flag.ImNodesCol;
import io.scriptor.util.Constants;
import io.scriptor.util.IUnique;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

import static io.scriptor.util.Util.indexOf;

public record Link(UUID uuid, Pin source, Pin target) implements IUnique {

    public static @NotNull Link parse(final @NotNull INode @NotNull [] nodes, final @NotNull String string) {
        final var split = string.split(",");
        final var sourceNode = Integer.parseInt(split[0], 10);
        final var sourceIndex = Integer.parseInt(split[1], 10);
        final var targetNode = Integer.parseInt(split[2], 10);
        final var targetIndex = Integer.parseInt(split[3], 10);
        return new Link(
                UUID.randomUUID(),
                nodes[sourceNode].output(sourceIndex),
                nodes[targetNode].input(targetIndex));
    }

    public int id() {
        return uuid.hashCode();
    }

    public boolean notSelected() {
        return !ImNodes.isLinkSelected(id());
    }

    public void select() {
        ImNodes.selectLink(id());
    }

    public void show(final @NotNull Graph graph) {
        final var powered = source.powered(graph);
        if (powered) ImNodes.pushColorStyle(ImNodesCol.Link, Constants.COLOR_POWERED);
        ImNodes.link(id(), source.id(), target.id());
        if (powered) ImNodes.popColorStyle();
    }

    public boolean uses(final @NotNull INode node) {
        return source.uses(node) || target.uses(node);
    }

    public boolean usesNoneOf(final @NotNull INode @NotNull [] nodes) {
        for (final var a : nodes)
            for (final var b : nodes) {
                if (a == b)
                    continue;
                if ((source.node() == a && target.node() == b)
                        || (target.node() == a && source.node() == b))
                    return false;
            }
        return true;
    }

    public boolean uses(final @NotNull Pin pin) {
        return source == pin || target == pin;
    }

    public @NotNull String getString(final @NotNull INode @NotNull [] nodes) {
        return "%d,%d,%d,%d".formatted(
                indexOf(nodes, source.node()),
                source.index(),
                indexOf(nodes, target.node()),
                target.index());
    }
}
