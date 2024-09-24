package io.scriptor.node;

import io.scriptor.Context;
import io.scriptor.util.ObjectIO;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * UUID(0, 0)
 * <ul>
 * <li>pin 0 - In</li>
 * <li>pin 1 - Out</li>
 * </ul>
 */
public class NotLogic implements ILogic {

    public static void read(final Context context, final InputStream in) throws IOException {
        final var uuid = ObjectIO.readUUID(in);
        context.getRef(uuid).set(new NotLogic(uuid));
    }

    private final UUID uuid;

    public NotLogic() {
        this(UUID.randomUUID());
    }

    public NotLogic(final UUID uuid) {
        this.uuid = uuid;
    }

    @Override
    public UUID uuid() {
        return uuid;
    }

    @Override
    public int inputs() {
        return 1;
    }

    @Override
    public int outputs() {
        return 1;
    }

    @Override
    public String input(final int i) {
        if (i == 0) return "In";
        throw new IllegalStateException();
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

    @Override
    public void cycle(final long key, final boolean[] inputs, final boolean[] outputs) {
        outputs[0] = !inputs[0];
    }
}
