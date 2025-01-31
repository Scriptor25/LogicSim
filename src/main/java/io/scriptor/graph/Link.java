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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.UUID;

import static io.scriptor.util.IO.readUUID;
import static io.scriptor.util.IO.writeUUID;
import static io.scriptor.util.Util.indexOf;

public record Link(@NotNull UUID uuid, @NotNull Pin source, @NotNull Pin target) implements IUnique {

    public static @NotNull Link parseString(final @NotNull Node @NotNull [] nodes, final @NotNull String string) {
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

    public static @NotNull Link read(final @NotNull Graph graph, final @NotNull InputStream stream) throws IOException {
        final var uuid = readUUID(stream);
        final var source = Pin.read(graph, stream);
        final var target = Pin.read(graph, stream);
        return new Link(uuid, source, target);
    }

    public Link(final @NotNull Pin source, final @NotNull Pin target) {
        this(UUID.randomUUID(), source, target);
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
        if (powered)
            ImNodes.pushColorStyle(ImNodesCol.Link, Constants.COLOR_POWERED);
        ImNodes.link(id(), source.id(), target.id());
        if (powered)
            ImNodes.popColorStyle();
    }

    public boolean uses(final @NotNull Node node) {
        return source.uses(node) || target.uses(node);
    }

    public boolean usesNoneOf(final @NotNull Node @NotNull [] nodes) {
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

    public @NotNull Link copy(final @NotNull Map<Node, Node> copies) {
        final var newSource = copies.get(source.node());
        final var newTarget = copies.get(target.node());
        return new Link(
                UUID.randomUUID(),
                newSource.output(source.index()),
                newTarget.input(target.index()));
    }

    public @NotNull String string(final @NotNull Node @NotNull [] nodes) {
        return "%d,%d,%d,%d".formatted(
                indexOf(nodes, source.node()),
                source.index(),
                indexOf(nodes, target.node()),
                target.index());
    }

    public void write(final @NotNull OutputStream stream) throws IOException {
        writeUUID(stream, uuid);
        source.write(stream);
        target.write(stream);
    }
}
