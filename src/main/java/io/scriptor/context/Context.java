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

public class Context {

    private final Registry registry;
    private final List<Blueprint> blueprints = new ArrayList<>();

    public Context() {
        registry = new Registry();

        final var notFn = new NotFunction(registry, UUID.randomUUID());
        final var andFn = new AndFunction(registry, UUID.randomUUID());

        final var not = new Blueprint.Builder()
                .label("Not")
                .baseColor(0x3f579a)
                .inputs("In")
                .outputs("Out")
                .function(notFn)
                .build();
        add(not);

        final var and = new Blueprint.Builder()
                .label("And")
                .baseColor(0x3f579a)
                .inputs("In A", "In B")
                .outputs("Out")
                .function(andFn)
                .build();
        add(and);
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
