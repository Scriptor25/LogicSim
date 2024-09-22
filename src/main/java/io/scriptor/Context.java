package io.scriptor;

import io.scriptor.logic.AndLogic;
import io.scriptor.logic.ILogic;
import io.scriptor.logic.NotLogic;
import io.scriptor.node.Attribute;
import io.scriptor.node.Blueprint;

import java.io.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Context {

    private final Map<UUID, ILogic> logics = new HashMap<>();
    private final Map<UUID, Blueprint> blueprints = new HashMap<>();
    private final Map<UUID, Attribute> attributes = new HashMap<>();

    public Context() {
        final var notLogic = new NotLogic();
        logics.put(notLogic.uuid(), notLogic);

        final var andLogic = new AndLogic();
        logics.put(andLogic.uuid(), andLogic);

        final var not = new Blueprint.Builder()
                .label("Not")
                .baseColor(0x3c689f)
                .inputLabels("In")
                .outputLabels("Out")
                .logic(notLogic)
                .build();
        blueprints.put(not.uuid(), not);

        final var and = new Blueprint.Builder()
                .label("And")
                .baseColor(0xd943a2)
                .inputLabels("In A", "In B")
                .outputLabels("Out")
                .logic(andLogic)
                .build();
        blueprints.put(and.uuid(), and);
    }

    public Context(final String filename) throws IOException {
        this(new FileInputStream(filename));
    }

    public Context(final InputStream stream) throws IOException {
        try (final var in = new BufferedReader(new InputStreamReader(stream))) {
            final var count = Integer.parseInt(in.readLine());
            for (int i = 0; i < count; ++i) {
                final var blueprint = Blueprint.read(this, in);
                blueprints.put(blueprint.uuid(), blueprint);
            }
        }
    }

    public void write(final String filename) throws FileNotFoundException {
        write(new FileOutputStream(filename));
    }

    public void write(final OutputStream stream) {
        try (final var out = new PrintStream(stream)) {
            out.println(logics.size());
            logics.forEach((k, v) -> v.write(out));
            out.println(blueprints.size());
            blueprints.forEach((k, v) -> v.write(out));
        }
    }

    public Collection<Blueprint> blueprints() {
        return blueprints.values();
    }

    public Collection<Attribute> attributes() {
        return attributes.values();
    }

    public void add(final Blueprint blueprint) {
        this.blueprints.put(blueprint.uuid(), blueprint);
    }

    public void remove(final Blueprint blueprint) {
        this.blueprints.remove(blueprint.uuid());
    }

    public void add(final Attribute attribute) {
        this.attributes.put(attribute.uuid(), attribute);
    }

    public void remove(final Attribute attribute) {
        this.attributes.remove(attribute.uuid());
    }

    public ILogic logic(final UUID uuid) {
        return logics.get(uuid);
    }
}
