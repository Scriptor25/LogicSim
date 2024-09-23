package io.scriptor.util;

import io.scriptor.Context;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.UUID;

public class ObjectIO {

    public static void write(final Context context, final OutputStream out, final Object object)
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, IOException {
        write(out, object.getClass().getName());
        object.getClass().getMethod("write", Context.class, OutputStream.class).invoke(object, context, out);
    }

    public static void write(final OutputStream out, final UUID uuid) throws IOException {
        final var msb = uuid.getMostSignificantBits();
        final var lsb = uuid.getLeastSignificantBits();

        final var bytes = new byte[16];
        bytes[0] = (byte) (msb >> 56 & 0xff);
        bytes[1] = (byte) (msb >> 48 & 0xff);
        bytes[2] = (byte) (msb >> 40 & 0xff);
        bytes[3] = (byte) (msb >> 32 & 0xff);
        bytes[4] = (byte) (msb >> 24 & 0xff);
        bytes[5] = (byte) (msb >> 16 & 0xff);
        bytes[6] = (byte) (msb >> 8 & 0xff);
        bytes[7] = (byte) (msb & 0xff);
        bytes[8] = (byte) (lsb >> 56 & 0xff);
        bytes[9] = (byte) (lsb >> 48 & 0xff);
        bytes[10] = (byte) (lsb >> 40 & 0xff);
        bytes[11] = (byte) (lsb >> 32 & 0xff);
        bytes[12] = (byte) (lsb >> 24 & 0xff);
        bytes[13] = (byte) (lsb >> 16 & 0xff);
        bytes[14] = (byte) (lsb >> 8 & 0xff);
        bytes[15] = (byte) (lsb & 0xff);
        out.write(bytes);
    }

    public static void write(final OutputStream out, final String string) throws IOException {
        final var bytes = string.getBytes();
        write(out, bytes.length);
        out.write(bytes);
    }

    public static void write(final OutputStream out, final int i) throws IOException {
        final var bytes = new byte[4];
        bytes[0] = (byte) (i >> 24 & 0xff);
        bytes[1] = (byte) (i >> 16 & 0xff);
        bytes[2] = (byte) (i >> 8 & 0xff);
        bytes[3] = (byte) (i & 0xff);
        out.write(bytes);
    }

    public static void write(final OutputStream out, final boolean b) throws IOException {
        out.write(b ? 1 : 0);
    }

    public static boolean read(final Context context, final InputStream in)
            throws IOException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        final var className = readString(in);
        if (className == null) return false;
        final var type = ClassLoader.getSystemClassLoader().loadClass(className);
        type.getMethod("read", Context.class, InputStream.class).invoke(null, context, in);
        return true;
    }

    public static UUID readUUID(final InputStream in) throws IOException {
        final var bytes = in.readNBytes(16);

        final var msb = (long) bytes[0] << 56 | (long) bytes[1] << 48 | (long) bytes[2] << 40 | (long) bytes[3] << 32 | (long) bytes[4] << 24 | (long) bytes[5] << 16 | (long) bytes[6] << 8 | (long) bytes[7];
        final var lsb = (long) bytes[8] << 56 | (long) bytes[9] << 48 | (long) bytes[10] << 40 | (long) bytes[11] << 32 | (long) bytes[12] << 24 | (long) bytes[13] << 16 | (long) bytes[14] << 8 | (long) bytes[15];

        return new UUID(msb, lsb);
    }

    public static String readString(final InputStream in) throws IOException {
        final var length = readInt(in);
        if (length < 0) return null;
        final var bytes = in.readNBytes(length);
        return new String(bytes);
    }

    public static int readInt(final InputStream in) throws IOException {
        final var bytes = in.readNBytes(4);
        if (bytes.length < 4) return -1;
        return bytes[0] << 24 | bytes[1] << 16 | bytes[2] << 8 | bytes[3];
    }

    public static boolean readBool(final InputStream in) throws IOException {
        return in.read() != 0;
    }

    private ObjectIO() {
    }
}
