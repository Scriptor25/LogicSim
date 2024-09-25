package io.scriptor.context;

import io.scriptor.function.AndFunction;
import io.scriptor.function.NotFunction;
import io.scriptor.graph.Blueprint;
import io.scriptor.util.IOStream;
import io.scriptor.util.Task;

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

    public Context(final String filename) throws IOException {
        this(new File(filename));
    }

    public Context(final File file) throws IOException {
        this(Files.newInputStream(file.toPath()));
    }

    public Context(final InputStream in) throws IOException {
        try (in) {
            registry = new Registry(in);

            final var count = IOStream.readInt(in);
            for (int i = 0; i < count; ++i) Blueprint.read(in, this);
        }
    }

    public void write(final String filename) throws IOException {
        write(new File(filename));
    }

    public void write(final File file) throws IOException {
        write(Files.newOutputStream(file.toPath()));
    }

    public void write(final OutputStream out) throws IOException {
        try (out) {
            registry.write(out);

            IOStream.write(out, blueprints.size());
            blueprints.forEach(blueprint -> Task.handleVoid(() -> blueprint.write(out)));
        }
    }

    public Registry registry() {
        return registry;
    }

    public Collection<Blueprint> blueprints() {
        return blueprints;
    }

    public void add(final Blueprint blueprint) {
        blueprints.add(blueprint);
    }

    public void remove(final Blueprint blueprint) {
        blueprints.remove(blueprint);
    }
}
