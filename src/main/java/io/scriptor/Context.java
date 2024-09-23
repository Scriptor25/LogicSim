package io.scriptor;

import io.scriptor.logic.AndLogic;
import io.scriptor.logic.NotLogic;
import io.scriptor.node.Blueprint;

import java.io.*;
import java.util.*;

import static io.scriptor.Task.handle;

public class Context {

    private final Map<UUID, IUnique> storage = new HashMap<>();
    private final List<IUnique> next = new ArrayList<>();

    public Context() {
        final var notLogic = new NotLogic();
        put(notLogic.uuid(), notLogic);

        final var andLogic = new AndLogic();
        put(andLogic.uuid(), andLogic);

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
    }

    public Context(final String filename) throws IOException {
        this(new FileInputStream(filename));
    }

    public Context(final File file) throws IOException {
        this(new FileInputStream(file));
    }

    public Context(final InputStream stream) throws IOException {
        try (final var in = new BufferedReader(new InputStreamReader(stream))) {
            while (true) if (!handle(() -> ObjectIO.read(this, in))) break;
        }

        for (final var entry : storage.entrySet()) {
            if (entry.getValue() instanceof Reference<?> ref) {
                if (!ref.valid())
                    throw new IllegalStateException();
                storage.put(entry.getKey(), ((Reference<IUnique>) ref).get());
            }
        }
    }

    public void write(final String filename) throws FileNotFoundException {
        write(new FileOutputStream(filename));
    }

    public void write(final File file) throws FileNotFoundException {
        write(new FileOutputStream(file));
    }

    public void write(final OutputStream stream) {
        try (final var out = new PrintWriter(stream)) {
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
