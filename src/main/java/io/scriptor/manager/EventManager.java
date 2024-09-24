package io.scriptor.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventManager {

    @FunctionalInterface
    public interface IEventCallback {

        void invoke(final Object... args);
    }

    private final Map<String, List<IEventCallback>> events = new HashMap<>();
    private final List<Runnable> tasks = new ArrayList<>();

    public void register(final String id, final IEventCallback callback) {
        events.computeIfAbsent(id, key -> new ArrayList<>()).add(callback);
    }

    public void invoke(final String id, final Object... args) {
        events.computeIfAbsent(id, key -> new ArrayList<>()).forEach(callback -> callback.invoke(args));
    }

    public void schedule(final Runnable task) {
        tasks.add(task);
    }

    public void runTasks() {
        for (final var task : tasks)
            task.run();
        tasks.clear();
    }
}
