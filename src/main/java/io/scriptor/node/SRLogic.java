package io.scriptor.node;

import io.scriptor.Context;
import io.scriptor.util.ObjectIO;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SRLogic implements ILogic {

    public static void read(final Context context, final InputStream in) throws IOException {
        final var uuid = ObjectIO.readUUID(in);
        context.getRef(uuid).set(new SRLogic(uuid));
    }

    private final UUID uuid;

    public SRLogic() {
        this(UUID.randomUUID());
    }

    public SRLogic(final UUID uuid) {
        this.uuid = uuid;
    }

    @Override
    public UUID uuid() {
        return uuid;
    }

    @Override
    public int inputs() {
        return 2;
    }

    @Override
    public int outputs() {
        return 1;
    }

    @Override
    public String input(final int i) {
        return switch (i) {
            case 0 -> "Set";
            case 1 -> "Reset";
            default -> throw new IllegalStateException();
        };
    }

    @Override
    public String output(final int i) {
        if (i == 0) return "Out";
        throw new IllegalStateException();
    }

    @Override
    public void write(final Context context, final OutputStream out) throws IOException {
        ObjectIO.write(out, uuid());
    }

    private final Map<Long, Boolean> states = new HashMap<>();

    @Override
    public void cycle(final long key, final boolean[] inputs, final boolean[] outputs) {
        if (inputs[0]) states.put(key, true);
        if (inputs[1]) states.put(key, false);
        outputs[0] = states.getOrDefault(key, false);
    }
}
