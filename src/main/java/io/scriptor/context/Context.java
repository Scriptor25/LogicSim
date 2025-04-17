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

import static io.scriptor.util.Constants.UUID_AND;
import static io.scriptor.util.Constants.UUID_NOT;
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

        final var notGraph = new Graph(this);
        notGraph.add(new Attribute("In", false, (byte) 1));
        notGraph.add(new Attribute("Out", true, (byte) 1));

        final var notBlueprint = new Blueprint.Builder()
                .uuid(UUID_NOT)
                .label("Not")
                .baseColor(0x3f579a)
                .source(notGraph)
                .editable(false)
                .build(this);
        add(notBlueprint);

        final var andGraph = new Graph(this);
        andGraph.add(new Attribute("In A", false, (byte) 1));
        andGraph.add(new Attribute("In B", false, (byte) 1));
        andGraph.add(new Attribute("Out", true, (byte) 1));

        final var andBlueprint = new Blueprint.Builder()
                .uuid(UUID_AND)
                .label("And")
                .baseColor(0x3f579a)
                .source(andGraph)
                .editable(false)
                .build(this);
        add(andBlueprint);
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
