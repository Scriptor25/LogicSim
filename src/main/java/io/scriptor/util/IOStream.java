package io.scriptor.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class IOStream {

    public static void write(final OutputStream out, final UUID uuid) throws IOException {
        write(out, uuid.getMostSignificantBits());
        write(out, uuid.getLeastSignificantBits());
    }

    public static void write(final OutputStream out, final String string) throws IOException {
        final var bytes = string.getBytes();
        write(out, bytes.length);
        out.write(bytes);
        out.write(0);
    }

    public static void write(final OutputStream out, final boolean b) throws IOException {
        out.write(b ? 1 : 0);
    }

    public static void write(final OutputStream out, final byte b) throws IOException {
        out.write(b);
    }

    public static void write(final OutputStream out, final short s) throws IOException {
        out.write((byte) (s >> 8));
        out.write((byte) s);
    }

    public static void write(final OutputStream out, final int i) throws IOException {
        out.write((byte) (i >> 24));
        out.write((byte) (i >> 16));
        out.write((byte) (i >> 8));
        out.write((byte) i);
    }

    public static void write(final OutputStream out, final long l) throws IOException {
        out.write((byte) (l >> 56));
        out.write((byte) (l >> 48));
        out.write((byte) (l >> 40));
        out.write((byte) (l >> 32));
        out.write((byte) (l >> 24));
        out.write((byte) (l >> 16));
        out.write((byte) (l >> 8));
        out.write((byte) l);
    }

    public static UUID readUUID(final InputStream in) throws IOException {
        final var msb = readLong(in);
        final var lsb = readLong(in);
        return new UUID(msb, lsb);
    }

    public static String readString(final InputStream in) throws IOException {
        final var length = readInt(in);
        if (length < 0) return null;
        final var bytes = new byte[length];
        in.readNBytes(bytes, 0, length);
        in.read();
        return new String(bytes);
    }

    public static boolean readBoolean(final InputStream in) throws IOException {
        return in.read() != 0;
    }

    public static byte readByte(final InputStream in) throws IOException {
        return (byte) in.read();
    }

    public static short readShort(final InputStream in) throws IOException {
        short s = 0;
        s |= (short) (in.read() << 8);
        s |= (short) (in.read());
        return s;
    }

    public static int readInt(final InputStream in) throws IOException {
        int i = 0;
        i |= in.read() << 24;
        i |= in.read() << 16;
        i |= in.read() << 8;
        i |= in.read();
        return i;
    }

    public static long readLong(final InputStream in) throws IOException {
        long l = 0;
        l |= (long) in.read() << 56;
        l |= (long) in.read() << 48;
        l |= (long) in.read() << 40;
        l |= (long) in.read() << 32;
        l |= (long) in.read() << 24;
        l |= (long) in.read() << 16;
        l |= (long) in.read() << 8;
        l |= in.read();
        return l;
    }

    private IOStream() {
    }
}
