package io.scriptor.util;

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
