package io.scriptor.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.scriptor.MainApp.getLogger;

public class EventManager {

    @FunctionalInterface
    public interface IEventCallback {

        void invoke(final Object... args);
    }

    private final Map<String, IEventCallback> events = new HashMap<>();
    private final List<Runnable> tasks = new ArrayList<>();

    public void register(final String id, final IEventCallback callback) {
        if (events.containsKey(id))
            getLogger().warning(() -> "overwriting event with id '%s'".formatted(id));
        events.put(id, callback);
    }

    public void invoke(final String id, final Object... args) {
        if (!events.containsKey(id)) {
            getLogger().warning(() -> "no event with id '%s'".formatted(id));
            return;
        }
        events.get(id).invoke(args);
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
