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
import io.scriptor.graph.Blueprint;
import io.scriptor.util.IOStream;
import io.scriptor.util.Task;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.*;

/**
 * The context manages the function registry and blueprint storage.
 */
public class Context {

    private final Registry registry;
    private final List<Blueprint> blueprints = new ArrayList<>();

    /**
     * Create a new context, containing only the 'not' and 'and' blueprints and functions.
     */
    public Context() {
        registry = new Registry(this);

        final var notFunction = new NotFunction(UUID.randomUUID());
        registry.add(notFunction);

        final var notBlueprint = new Blueprint.Builder()
                .label("Not")
                .baseColor(0x3f579a)
                .inputs("In")
                .outputs("Out")
                .function(notFunction)
                .build();
        add(notBlueprint);

        final var andFunction = new AndFunction(UUID.randomUUID());
        registry.add(andFunction);

        final var andBlueprint = new Blueprint.Builder()
                .label("And")
                .baseColor(0x3f579a)
                .inputs("In A", "In B")
                .outputs("Out")
                .function(andFunction)
                .build();
        add(andBlueprint);
    }

    /**
     * Load a context from a file.
     *
     * @param filename the filename
     * @throws IOException if any
     */
    public Context(final @NotNull String filename) throws IOException {
        this(new File(filename));
    }

    /**
     * Load a context from a file.
     *
     * @param file the file
     * @throws IOException if any
     */
    public Context(final @NotNull File file) throws IOException {
        this(Files.newInputStream(file.toPath()), true);
    }

    /**
     * Load a context from an input stream.
     *
     * @param inputStream the input stream
     * @param closeStream if the input stream should be closed after the operation has finished
     * @throws IOException if any
     */
    public Context(final @NotNull InputStream inputStream, final boolean closeStream) throws IOException {
        registry = new Registry(this, inputStream);

        final var count = IOStream.readInt(inputStream);
        for (int i = 0; i < count; ++i)
            Blueprint.read(inputStream, this);

        if (closeStream)
            inputStream.close();
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
        write(Files.newOutputStream(file.toPath()), true);
    }

    /**
     * Write this context to an output stream.
     *
     * @param outputStream the output stream
     * @param closeStream  if the output stream should be closed after the operation has finished
     * @throws IOException if any
     */
    public void write(final @NotNull OutputStream outputStream, final boolean closeStream) throws IOException {
        registry.write(outputStream);

        IOStream.write(outputStream, blueprints.size());
        blueprints.forEach(blueprint -> Task.handleVoid(() -> blueprint.write(outputStream)));

        if (closeStream)
            outputStream.close();
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
