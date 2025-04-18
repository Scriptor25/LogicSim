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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Optional;
import java.util.stream.Stream;

import static io.scriptor.util.IO.*;

public record Pin(@NotNull Node node, int index, boolean output, byte bitwidth) {

    public static @NotNull Pin read(final @NotNull Graph graph, final @NotNull InputStream stream) throws IOException {
        final var nodeUUID = readUUID(stream);
        final var index = readInt(stream);
        final var output = readBool(stream);
        return graph
                .findNode(nodeUUID)
                .map(node -> output
                        ? node.output(index)
                        : node.input(index))
                .orElseThrow(() -> new RTException("no node with uuid '%s'", nodeUUID));
    }

    public int id() {
        return hashCode();
    }

    public int data(final @NotNull Graph graph) {
        return node.data(graph, output, index);
    }

    public boolean uses(final @NotNull Node node) {
        return this.node == node;
    }

    public @NotNull Optional<Pin> predecessor(final @NotNull Graph graph) {
        if (output)
            throw new RTException("pin is in output mode, does not have predecessor");
        return graph
                .findLink(this)
                .map(Link::source);
    }

    public @NotNull Stream<Pin> successors(final @NotNull Graph graph) {
        if (!output)
            throw new RTException("pin is in input mode, does not have successors");
        return graph
                .findLinks(this)
                .map(Link::target);
    }

    public void write(final @NotNull OutputStream stream) throws IOException {
        writeUUID(stream, node.uuid());
        writeInt(stream, index);
        writeBool(stream, output);
    }
}
