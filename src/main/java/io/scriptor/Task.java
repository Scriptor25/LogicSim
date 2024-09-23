package io.scriptor;

import static io.scriptor.MainApp.getLogger;

public class Task {

    @FunctionalInterface
    public interface ITaskVoid {

        void run() throws Exception;
    }

    @FunctionalInterface
    public interface ITask<T> {

        T run() throws Exception;
    }

    public static void handle(final ITaskVoid task) {
        try {
            task.run();
        } catch (final Exception e) {
            getLogger().warning(e::toString);
            throw new RuntimeException(e);
        }
    }

    public static <T> T handle(final ITask<T> task) {
        try {
            return task.run();
        } catch (final Exception e) {
            getLogger().warning(e::toString);
            throw new RuntimeException(e);
        }
    }

    private Task() {
    }
}
