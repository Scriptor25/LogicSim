package io.scriptor.util;

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
    public interface ITask<T> {

        T run() throws Exception;
    }

    public static void handleVoid(final ITaskVoid task) {
        try {
            task.run();
        } catch (final Exception e) {
            throw new RTException(e);
        }
    }

    public static boolean handleBoolean(final ITaskBoolean task) {
        try {
            return task.run();
        } catch (final Exception e) {
            throw new RTException(e);
        }
    }

    public static <T> T handle(final ITask<T> task) {
        try {
            return task.run();
        } catch (final Exception e) {
            throw new RTException(e);
        }
    }

    private Task() {
    }
}
