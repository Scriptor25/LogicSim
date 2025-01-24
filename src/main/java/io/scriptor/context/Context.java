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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

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
public class Context {

    private final Registry registry;
    private final List<Blueprint> blueprints = new ArrayList<>();

    public Context() {
        registry = new Registry();

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

    public Context(final @NotNull String filename) throws IOException {
        this(new File(filename));
    }

    public Context(final @NotNull File file) throws IOException {
        this(Files.newInputStream(file.toPath()), true);
    }

    public Context(final @NotNull InputStream inputStream, final boolean closeStream) throws IOException {
        registry = new Registry(inputStream);

        final var count = IOStream.readInt(inputStream);
        for (int i = 0; i < count; ++i)
            Blueprint.read(inputStream, this);

        if (closeStream)
            inputStream.close();
    }

    public void write(final @NotNull String filename) throws IOException {
        write(new File(filename));
    }

    public void write(final @NotNull File file) throws IOException {
        write(Files.newOutputStream(file.toPath()), true);
    }

    public void write(final @NotNull OutputStream outputStream, final boolean closeStream) throws IOException {
        registry.write(outputStream);

        IOStream.write(outputStream, blueprints.size());
        blueprints.forEach(blueprint -> Task.handleVoid(() -> blueprint.write(outputStream)));

        if (closeStream)
            outputStream.close();
    }

    public @NotNull Registry registry() {
        return registry;
    }

    public @NotNull Collection<Blueprint> blueprints() {
        return blueprints;
    }

    public void add(final @NotNull Blueprint blueprint) {
        blueprints.add(blueprint);
    }

    public void remove(final @NotNull Blueprint blueprint) {
        blueprints.remove(blueprint);
    }
}
