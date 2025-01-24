package io.scriptor.util;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * This class is part of the <a href="https://github.com/Scriptor25/LogicSim">Java Logic Sim</a> project.
 * <p>
 * Copyright (C) 2025  Felix Schreiber
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <a href="https://www.gnu.org/licenses/">https://www.gnu.org/licenses/</a>.
 *
 * @author Felix Schreiber
 */
public class IOStream {

    public static void write(final @NotNull OutputStream outputStream, final @NotNull UUID uuid) throws IOException {
        write(outputStream, uuid.getMostSignificantBits());
        write(outputStream, uuid.getLeastSignificantBits());
    }

    public static void write(final @NotNull OutputStream outputStream, final @NotNull String string) throws IOException {
        final var bytes = string.getBytes();
        write(outputStream, bytes.length);
        outputStream.write(bytes);
    }

    public static void write(final @NotNull OutputStream outputStream, final boolean value) throws IOException {
        outputStream.write(value ? 1 : 0);
    }

    public static void write(final @NotNull OutputStream outputStream, final byte value) throws IOException {
        outputStream.write(value);
    }

    public static void write(final @NotNull OutputStream outputStream, final short value) throws IOException {
        outputStream.write((byte) (value >> 8));
        outputStream.write((byte) value);
    }

    public static void write(final @NotNull OutputStream outputStream, final int value) throws IOException {
        outputStream.write((byte) (value >> 24));
        outputStream.write((byte) (value >> 16));
        outputStream.write((byte) (value >> 8));
        outputStream.write((byte) value);
    }

    public static void write(final @NotNull OutputStream outputStream, final long value) throws IOException {
        outputStream.write((byte) (value >> 56));
        outputStream.write((byte) (value >> 48));
        outputStream.write((byte) (value >> 40));
        outputStream.write((byte) (value >> 32));
        outputStream.write((byte) (value >> 24));
        outputStream.write((byte) (value >> 16));
        outputStream.write((byte) (value >> 8));
        outputStream.write((byte) value);
    }

    public static UUID readUUID(final @NotNull InputStream inputStream) throws IOException {
        final var msb = readLong(inputStream);
        final var lsb = readLong(inputStream);
        return new UUID(msb, lsb);
    }

    public static @NotNull String readString(final @NotNull InputStream inputStream) throws IOException {
        final var length = readInt(inputStream);
        if (length <= 0) return "";
        final var bytes = new byte[length];
        inputStream.readNBytes(bytes, 0, length);
        return new String(bytes);
    }

    public static boolean readBoolean(final @NotNull InputStream inputStream) throws IOException {
        return inputStream.read() != 0;
    }

    public static byte readByte(final @NotNull InputStream inputStream) throws IOException {
        return (byte) inputStream.read();
    }

    public static short readShort(final @NotNull InputStream inputStream) throws IOException {
        short value = 0;
        value |= (short) (inputStream.read() << 8);
        value |= (short) (inputStream.read());
        return value;
    }

    public static int readInt(final @NotNull InputStream inputStream) throws IOException {
        int value = 0;
        value |= inputStream.read() << 24;
        value |= inputStream.read() << 16;
        value |= inputStream.read() << 8;
        value |= inputStream.read();
        return value;
    }

    public static long readLong(final @NotNull InputStream inputStream) throws IOException {
        long value = 0;
        value |= (long) inputStream.read() << 56;
        value |= (long) inputStream.read() << 48;
        value |= (long) inputStream.read() << 40;
        value |= (long) inputStream.read() << 32;
        value |= (long) inputStream.read() << 24;
        value |= (long) inputStream.read() << 16;
        value |= (long) inputStream.read() << 8;
        value |= inputStream.read();
        return value;
    }

    private IOStream() {
    }
}
