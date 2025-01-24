package io.scriptor.event;

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
    public interface IEventCallback<T extends IPayload> {

        void invoke(final @NotNull T arg);
    }

    @FunctionalInterface
    public interface IServiceCallback<R, T extends IPayload> {

        R call(final @NotNull T arg);
    }

    private final Map<Object, List<IEventCallback<?>>> eventMap = new HashMap<>();
    private final Map<Object, IServiceCallback<?, ?>> serviceMap = new HashMap<>();
    private final List<Runnable> generalTaskList = new ArrayList<>();
    private final Map<Object, List<Runnable>> taskMap = new HashMap<>();

    public <T extends IPayload> void registerEvent(final @NotNull Object id, final @NotNull IEventCallback<T> callback) {
        eventMap
                .computeIfAbsent(id, key -> new ArrayList<>())
                .add(callback);
    }

    public <T extends IPayload> void invokeEvent(final @NotNull Object id, final @NotNull T arg) {
        eventMap
                .computeIfAbsent(id, key -> new ArrayList<>())
                .forEach(callback -> ((IEventCallback<T>) callback).invoke(arg));
    }

    public <R, T extends IPayload> void offerService(final @NotNull Object id, final @NotNull IServiceCallback<R, T> callback) {
        if (serviceMap.containsKey(id))
            throw new RTException("overriding already existing services with id '%s'", id);
        serviceMap.put(id, callback);
    }

    public <R, T extends IPayload> R callService(final @NotNull Object id, final @NotNull T arg) {
        if (!serviceMap.containsKey(id))
            throw new RTException("service for id '%s' does not exist", id);
        return ((IServiceCallback<R, T>) serviceMap.get(id)).call(arg);
    }

    public void schedule(final @NotNull Runnable task) {
        generalTaskList.add(task);
    }

    public void runTasks() {
        for (final var task : generalTaskList)
            task.run();
        generalTaskList.clear();
    }

    public void schedule(final @NotNull Object id, final @NotNull Runnable task) {
        taskMap
                .computeIfAbsent(id, key -> new ArrayList<>())
                .add(task);
    }

    public void runTasks(final @NotNull Object id) {
        final var taskList = taskMap.computeIfAbsent(id, key -> new ArrayList<>());
        for (final var task : taskList)
            task.run();
        taskList.clear();
    }
}
