/*
 * This file is part of https://github.com/Scriptor25/LogicSim
 *
 * Copyright (C) 2025  Felix Schreiber
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see https://www.gnu.org/licenses/.
 */
package io.scriptor.util;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class IO {

    public static void writeUUID(final @NotNull OutputStream stream, final @NotNull UUID uuid) throws IOException {
        writeLong(stream, uuid.getMostSignificantBits());
        writeLong(stream, uuid.getLeastSignificantBits());
    }

    public static void writeString(final @NotNull OutputStream stream, final @NotNull String string) throws IOException {
        final var bytes = string.getBytes();
        writeInt(stream, bytes.length);
        stream.write(bytes);
    }

    public static void writeBool(final @NotNull OutputStream stream, final boolean value) throws IOException {
        stream.write(value ? 1 : 0);
    }

    public static void writeByte(final @NotNull OutputStream stream, final byte value) throws IOException {
        stream.write(value);
    }

    public static void writeShort(final @NotNull OutputStream stream, final short value) throws IOException {
        stream.write((byte) (value >> 8));
        stream.write((byte) value);
    }

    public static void writeInt(final @NotNull OutputStream stream, final int value) throws IOException {
        stream.write((byte) (value >> 24));
        stream.write((byte) (value >> 16));
        stream.write((byte) (value >> 8));
        stream.write((byte) value);
    }

    public static void writeLong(final @NotNull OutputStream stream, final long value) throws IOException {
        stream.write((byte) (value >> 56));
        stream.write((byte) (value >> 48));
        stream.write((byte) (value >> 40));
        stream.write((byte) (value >> 32));
        stream.write((byte) (value >> 24));
        stream.write((byte) (value >> 16));
        stream.write((byte) (value >> 8));
        stream.write((byte) value);
    }

    public static @NotNull UUID readUUID(final @NotNull InputStream stream) throws IOException {
        final var msb = readLong(stream);
        final var lsb = readLong(stream);
        return new UUID(msb, lsb);
    }

    public static @NotNull String readString(final @NotNull InputStream stream) throws IOException {
        final var length = readInt(stream);
        if (length <= 0) return "";
        final var bytes = new byte[length];
        stream.readNBytes(bytes, 0, length);
        return new String(bytes);
    }

    public static boolean readBool(final @NotNull InputStream stream) throws IOException {
        return stream.read() != 0;
    }

    public static byte readByte(final @NotNull InputStream stream) throws IOException {
        return (byte) stream.read();
    }

    public static short readShort(final @NotNull InputStream stream) throws IOException {
        short value = 0;
        value |= (short) (stream.read() << 8);
        value |= (short) (stream.read());
        return value;
    }

    public static int readInt(final @NotNull InputStream stream) throws IOException {
        int value = 0;
        value |= stream.read() << 24;
        value |= stream.read() << 16;
        value |= stream.read() << 8;
        value |= stream.read();
        return value;
    }

    public static long readLong(final @NotNull InputStream stream) throws IOException {
        long value = 0;
        value |= (long) stream.read() << 56;
        value |= (long) stream.read() << 48;
        value |= (long) stream.read() << 40;
        value |= (long) stream.read() << 32;
        value |= (long) stream.read() << 24;
        value |= (long) stream.read() << 16;
        value |= (long) stream.read() << 8;
        value |= stream.read();
        return value;
    }

    private IO() {
    }
}
