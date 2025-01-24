package io.scriptor.manager;

import io.scriptor.util.RTException;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
