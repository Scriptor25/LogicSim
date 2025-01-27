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

public class Task {

    @FunctionalInterface
    public interface ITaskVoid {

        void run() throws Exception;
    }

    @FunctionalInterface
    public interface ITaskBoolean {

        boolean run() throws Exception;
    }

    @FunctionalInterface
    public interface ITaskByte {

        byte run() throws Exception;
    }

    @FunctionalInterface
    public interface ITaskChar {

        char run() throws Exception;
    }

    @FunctionalInterface
    public interface ITaskShort {

        short run() throws Exception;
    }

    @FunctionalInterface
    public interface ITaskInt {

        int run() throws Exception;
    }

    @FunctionalInterface
    public interface ITaskLong {

        long run() throws Exception;
    }

    @FunctionalInterface
    public interface ITaskFloat {

        float run() throws Exception;
    }

    @FunctionalInterface
    public interface ITaskDouble {

        double run() throws Exception;
    }

    @FunctionalInterface
    public interface ITask<T> {

        @NotNull T run() throws Exception;
    }

    public static void handleVoid(final @NotNull ITaskVoid task) {
        try {
            task.run();
        } catch (final Exception e) {
            throw new RTException(e);
        }
    }

    public static boolean handleBoolean(final @NotNull ITaskBoolean task) {
        try {
            return task.run();
        } catch (final Exception e) {
            throw new RTException(e);
        }
    }

    public static byte handleByte(final @NotNull ITaskByte task) {
        try {
            return task.run();
        } catch (final Exception e) {
            throw new RTException(e);
        }
    }

    public static char handleChar(final @NotNull ITaskChar task) {
        try {
            return task.run();
        } catch (final Exception e) {
            throw new RTException(e);
        }
    }

    public static short handleShort(final @NotNull ITaskShort task) {
        try {
            return task.run();
        } catch (final Exception e) {
            throw new RTException(e);
        }
    }

    public static int handleInt(final @NotNull ITaskInt task) {
        try {
            return task.run();
        } catch (final Exception e) {
            throw new RTException(e);
        }
    }

    public static long handleLong(final @NotNull ITaskLong task) {
        try {
            return task.run();
        } catch (final Exception e) {
            throw new RTException(e);
        }
    }

    public static float handleFloat(final @NotNull ITaskFloat task) {
        try {
            return task.run();
        } catch (final Exception e) {
            throw new RTException(e);
        }
    }

    public static double handleDouble(final @NotNull ITaskDouble task) {
        try {
            return task.run();
        } catch (final Exception e) {
            throw new RTException(e);
        }
    }

    public static <T> @NotNull T handle(final @NotNull ITask<T> task) {
        try {
            return task.run();
        } catch (final Exception e) {
            throw new RTException(e);
        }
    }

    private Task() {
    }
}
