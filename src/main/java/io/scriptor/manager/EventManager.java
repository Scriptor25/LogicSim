package io.scriptor.manager;

import io.scriptor.util.RTException;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventManager {

    @FunctionalInterface
    public interface IEventCallback {

        void invoke(final @NotNull Object @NotNull ... args);
    }

    @FunctionalInterface
    public interface IServiceCallback<R, T> {

        R call(final @NotNull T arg);
    }

    private final Map<String, List<IEventCallback>> events = new HashMap<>();
    private final Map<String, IServiceCallback<?, ?>> services = new HashMap<>();
    private final List<Runnable> tasks = new ArrayList<>();

    public void registerEvent(final @NotNull String id, final @NotNull IEventCallback callback) {
        events.computeIfAbsent(id, key -> new ArrayList<>()).add(callback);
    }

    public void invokeEvent(final @NotNull String id, final @NotNull Object @NotNull ... args) {
        events.computeIfAbsent(id, key -> new ArrayList<>()).forEach(callback -> callback.invoke(args));
    }

    public void offerService(final @NotNull String id, final @NotNull IServiceCallback<?, ?> callback) {
        if (services.containsKey(id))
            throw new RTException("overriding already existing services with id '%s'", id);
        services.put(id, callback);
    }

    public <R, T> R callService(final @NotNull String id, final @NotNull T arg) {
        if (!services.containsKey(id))
            throw new RTException("service for id '%s' does not exist", id);
        return ((IServiceCallback<R, T>) services.get(id)).call(arg);
    }

    public void schedule(final @NotNull Runnable task) {
        tasks.add(task);
    }

    public void runTasks() {
        for (final var task : tasks)
            task.run();
        tasks.clear();
    }
}
