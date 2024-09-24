package io.scriptor;

import io.scriptor.node.AndLogic;
import io.scriptor.node.Blueprint;
import io.scriptor.node.NotLogic;
import io.scriptor.node.SRLogic;
import io.scriptor.util.IUnique;
import io.scriptor.util.ObjectIO;
import io.scriptor.util.Reference;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.*;

import static io.scriptor.util.Task.handle;

public class Context {

    private final Map<UUID, IUnique> storage = new HashMap<>();
    private final List<IUnique> next = new ArrayList<>();

    public Context() {
        final var notLogic = new NotLogic();
        put(notLogic.uuid(), notLogic);

        final var andLogic = new AndLogic();
        put(andLogic.uuid(), andLogic);

        final var srLogic = new SRLogic();
        put(srLogic.uuid(), srLogic);

        final var not = new Blueprint.Builder()
                .label("Not")
                .baseColor(0x3c689f)
                .logic(notLogic)
                .build();
        put(not.uuid(), not);

        final var and = new Blueprint.Builder()
                .label("And")
                .baseColor(0xd943a2)
                .logic(andLogic)
                .build();
        put(and.uuid(), and);

        final var sr = new Blueprint.Builder()
                .label("SR Latch")
                .baseColor(0xa2943a)
                .logic(srLogic)
                .build();
        put(sr.uuid(), sr);
    }

    public Context(final String filename) throws IOException {
        this(new File(filename));
    }

    public Context(final File file) throws IOException {
        this(Files.newInputStream(file.toPath()));
    }

    public Context(final InputStream in) throws IOException {
        try (in) {
            while (true) if (!handle(() -> ObjectIO.read(this, in))) break;

            for (final var entry : storage.entrySet()) {
                if (entry.getValue() instanceof Reference<?> ref) {
                    if (!ref.valid())
                        throw new IllegalStateException();
                    storage.put(entry.getKey(), ((Reference<IUnique>) ref).get());
                }
            }
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
            next.clear();
            storage.values().forEach(object -> handle(() -> ObjectIO.write(this, out, object)));

            for (int i = 0; i < next.size(); ++i) {
                final var j = i;
                handle(() -> ObjectIO.write(this, out, next.get(j)));
            }
        }
    }

    public Collection<IUnique> storage() {
        return storage.values();
    }

    public <T extends IUnique> T get(final UUID key) {
        return (T) storage.get(key);
    }

    public <T extends IUnique> Reference<T> getRef(final UUID key) {
        return (Reference<T>) storage.computeIfAbsent(key, uuid -> new Reference<T>());
    }

    public void put(final UUID key, final IUnique unique) {
        storage.put(key, unique);
    }

    public void remove(final UUID key) {
        storage.remove(key);
    }

    public void next(final IUnique unique) {
        if (storage.containsValue(unique) || next.contains(unique))
            return;
        next.add(unique);
    }
}
