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

import io.scriptor.function.AndFunction;
import io.scriptor.function.NotFunction;
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

/**
 * The context manages the function registry and blueprint storage.
 */
public class Context {

    private final Registry registry;
    private final List<Blueprint> blueprints = new ArrayList<>();

    public Context(final boolean addDefaults) {
        final var notFunction = new NotFunction(UUID_NOT);
        final var andFunction = new AndFunction(UUID_AND);

        registry = new Registry(this);
        registry.add(notFunction);
        registry.add(andFunction);

        if (!addDefaults)
            return;

        final var notGraph = new Graph(this);
        notGraph.add(new Attribute("In", false));
        notGraph.add(new Attribute("Out", true));

        final var notBlueprint = new Blueprint.Builder()
                .uuid(UUID_NOT)
                .label("Not")
                .baseColor(0x3f579a)
                .source(notGraph)
                .editable(false)
                .build(this);
        add(notBlueprint);

        final var andGraph = new Graph(this);
        andGraph.add(new Attribute("In A", false));
        andGraph.add(new Attribute("In B", false));
        andGraph.add(new Attribute("Out", true));

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
        for (final var blueprint : blueprints)
            blueprint.write(stream);
    }

    public static @NotNull Context read(final @NotNull File file) throws IOException {
        try (final var stream = Files.newInputStream(file.toPath())) {
            return read(stream);
        }
    }

    public static @NotNull Context read(final @NotNull InputStream stream) throws IOException {
        final var context = new Context(false);
        final var blueprintCount = readInt(stream);
        for (int i = 0; i < blueprintCount; ++i) {
            final var blueprint = Blueprint.read(context, stream);
            context.registry.add(blueprint.function());
            context.add(blueprint);
        }
        return context;
    }

    /**
     * Get the registry used by this context.
     *
     * @return the registry
     */
    public @NotNull Registry registry() {
        return registry;
    }

    /**
     * Get the blueprints stored in this context. This returns an immutable view of the original instance.
     *
     * @return the blueprints
     */
    public @NotNull Collection<Blueprint> blueprints() {
        return Collections.unmodifiableCollection(blueprints);
    }

    public @NotNull Optional<Blueprint> findBlueprint(final @NotNull UUID uuid) {
        return blueprints
                .stream()
                .filter(blueprint -> blueprint.uuid().equals(uuid))
                .findAny();
    }

    /**
     * Add a blueprint to this context.
     *
     * @param blueprint the blueprint
     */
    public void add(final @NotNull Blueprint blueprint) {
        blueprints.add(blueprint);
    }

    /**
     * Remove a blueprint from this context.
     *
     * @param blueprint the blueprint
     */
    public void remove(final @NotNull Blueprint blueprint) {
        blueprints.remove(blueprint);
    }
}
