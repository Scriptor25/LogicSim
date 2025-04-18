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
package io.scriptor.context;

import io.scriptor.graph.Attribute;
import io.scriptor.graph.Blueprint;
import io.scriptor.graph.Graph;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.*;

import static io.scriptor.util.Constants.*;
import static io.scriptor.util.IO.readInt;
import static io.scriptor.util.IO.writeInt;
import static io.scriptor.util.Task.handleVoid;

/**
 * The context manages the function registry and blueprint storage.
 */
public class Context {

    public static @NotNull Context read(final @NotNull File file) throws IOException {
        try (final var stream = Files.newInputStream(file.toPath())) {
            return read(stream);
        }
    }

    public static @NotNull Context read(final @NotNull InputStream stream) throws IOException {
        final var context = new Context(false);
        final var blueprintCount = readInt(stream);
        for (int i = 0; i < blueprintCount; ++i)
            context.add(Blueprint.read(context, stream));
        return context;
    }

    private final Map<UUID, Blueprint> blueprints = new HashMap<>();

    public Context(final boolean addDefaults) {
        if (!addDefaults)
            return;

        {
            final var graph = new Graph(this);
            graph.add(new Attribute("IN", false, (byte) 1));
            graph.add(new Attribute("OUT", true, (byte) 1));

            final var blueprint = new Blueprint.Builder()
                    .uuid(UUID_NOT)
                    .label("NOT-01")
                    .baseColor(0x3f579a)
                    .source(graph)
                    .editable(false)
                    .build(this);
            add(blueprint);
        }

        {
            final var graph = new Graph(this);
            graph.add(new Attribute("IN A", false, (byte) 1));
            graph.add(new Attribute("IN B", false, (byte) 1));
            graph.add(new Attribute("OUT", true, (byte) 1));

            final var blueprint = new Blueprint.Builder()
                    .uuid(UUID_AND)
                    .label("AND-01")
                    .baseColor(0x3f579a)
                    .source(graph)
                    .editable(false)
                    .build(this);
            add(blueprint);
        }

        for (int i = 0; i < 5; ++i) {
            final var graph = new Graph(this);
            for (int j = 0; j < (2 << i); ++j)
                graph.add(new Attribute("IN %d".formatted(j), false, (byte) 1));
            graph.add(new Attribute("OUT", true, (byte) (2 << i)));

            final var blueprint = new Blueprint.Builder()
                    .uuid(UUID_MERGE[i])
                    .label("MERGE-%02d".formatted(2 << i))
                    .baseColor(0x3f579a)
                    .source(graph)
                    .editable(false)
                    .build(this);
            add(blueprint);
        }

        for (int i = 0; i < 5; ++i) {
            final var graph = new Graph(this);
            graph.add(new Attribute("IN", false, (byte) (2 << i)));
            for (int j = 0; j < (2 << i); ++j)
                graph.add(new Attribute("OUT %d".formatted(j), true, (byte) 1));

            final var blueprint = new Blueprint.Builder()
                    .uuid(UUID_SPLIT[i])
                    .label("SPLIT-%02d".formatted(2 << i))
                    .baseColor(0x3f579a)
                    .source(graph)
                    .editable(false)
                    .build(this);
            add(blueprint);
        }
    }

    /**
     * Write this context to a file.
     *
     * @param filename the filename
     * @throws IOException if any
     */
    public void write(final @NotNull String filename) throws IOException {
        write(new File(filename));
    }

    /**
     * Write this context to a file.
     *
     * @param file the file
     * @throws IOException if any
     */
    public void write(final @NotNull File file) throws IOException {
        try (final var stream = Files.newOutputStream(file.toPath())) {
            write(stream);
        }
    }

    /**
     * Write this context to an output stream.
     *
     * @param stream the output stream
     * @throws IOException if any
     */
    public void write(final @NotNull OutputStream stream) throws IOException {
        writeInt(stream, blueprints.size());
        blueprints
                .values()
                .stream()
                .sorted((a, b) -> {
                    final var x = b.uses(a) ? -1 : 0;
                    return a.uses(b) ? 1 : x;
                })
                .forEach(blueprint -> handleVoid(() -> blueprint.write(stream)));
    }

    /**
     * Get the blueprints stored in this context. This returns an immutable view of the original instance.
     *
     * @return the blueprints
     */
    public @NotNull Collection<Blueprint> blueprints() {
        return Collections.unmodifiableCollection(blueprints.values());
    }

    public @NotNull Optional<Blueprint> get(final @NotNull UUID uuid) {
        if (blueprints.containsKey(uuid))
            return Optional.of(blueprints.get(uuid));
        return Optional.empty();
    }

    /**
     * Add a blueprint to this context.
     *
     * @param blueprint the blueprint
     */
    public void add(final @NotNull Blueprint blueprint) {
        blueprints.put(blueprint.uuid(), blueprint);
    }

    /**
     * Remove a blueprint from this context.
     *
     * @param blueprint the blueprint
     */
    public void remove(final @NotNull Blueprint blueprint) {
        blueprints.remove(blueprint.uuid());
    }

    public boolean uses(final @NotNull Blueprint blueprint) {
        return blueprints
                .values()
                .stream()
                .anyMatch(b -> b.uses(blueprint));
    }
}
