package io.scriptor.node;

import io.scriptor.Context;
import io.scriptor.util.ObjectIO;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * UUID(0, 1)
 * <ul>
 * <li>pin 0 - In A</li>
 * <li>pin 1 - In B</li>
 * <li>pin 2 - Out</li>
 * </ul>
 */
public class AndLogic implements ILogic {

    public static void read(final Context context, final InputStream in) throws IOException {
        final var uuid = ObjectIO.readUUID(in);
        context.getRef(uuid).set(new AndLogic(uuid));
    }

    private final UUID uuid;

    public AndLogic() {
        this(UUID.randomUUID());
    }

    public AndLogic(final UUID uuid) {
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
            case 0 -> "In A";
            case 1 -> "In B";
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

    @Override
    public void cycle(final long key, final boolean[] inputs, final boolean[] outputs) {
        outputs[0] = inputs[0] && inputs[1];
    }
}
