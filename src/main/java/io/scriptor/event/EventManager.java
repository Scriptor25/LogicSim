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
package io.scriptor.event;

import io.scriptor.util.RTException;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * The event manager handles events, services and tasks on an application-wide scale, enabling communication between systems at runtime.
 */
public class EventManager {

    private final Map<Object, List<IEventListener<?>>> eventMap = new HashMap<>();
    private final Map<Object, IServiceCallback<?, ?>> serviceMap = new HashMap<>();
    private final Map<Object, List<Runnable>> taskMap = new HashMap<>();
    private final Map<Object, Timer> timerMap = new HashMap<>();

    /**
     * Register an event listener. Multiple different listeners can be registered to a single event id.
     *
     * @param id       the event id
     * @param listener the event listener
     * @param <T>      the payload type
     */
    public <T> void registerEvent(final @NotNull Object id, final @NotNull IEventListener<T> listener) {
        eventMap
                .computeIfAbsent(id, key -> new ArrayList<>())
                .add(listener);
    }

    public void registerEvent(final @NotNull Object id, final @NotNull Runnable listener) {
        registerEvent(id, payload -> listener.run());
    }

    /**
     * Invoke an event. Calls invoke with the given payload on all event listeners registered for the event id.
     *
     * @param id      the event id
     * @param payload the event payload
     * @param <T>     the payload type
     */
    @SuppressWarnings("unchecked")
    public <T> void invokeEvent(final @NotNull Object id, final @NotNull T payload) {
        eventMap
                .computeIfAbsent(id, key -> new ArrayList<>())
                .forEach(callback -> ((IEventListener<T>) callback).invoke(payload));
    }

    public void invokeEvent(final @NotNull Object id) {
        this.invokeEvent(id, new Object());
    }

    /**
     * Offer a service callback. Service ids are unique, and overriding one is illegal and will throw an exception at runtime.
     *
     * @param id       the service id
     * @param callback the service callback
     * @param <R>      the result type
     * @param <T>      the payload type
     * @throws RTException if a service is already registered for the id
     */
    public <R, T> void offerService(final @NotNull Object id, final @NotNull IServiceCallback<R, T> callback) {
        if (serviceMap.containsKey(id))
            throw new RTException("overriding already existing services with id '%s'", id);
        serviceMap.put(id, callback);
    }

    public <T> void offerService(final @NotNull Object id, final @NotNull Consumer<T> callback) {
        this.<Object, T>offerService(id, payload -> {
            callback.accept(payload);
            return new Object();
        });
    }

    public <R> void offerService(final @NotNull Object id, final @NotNull Supplier<R> callback) {
        this.<R, Object>offerService(id, payload -> callback.get());
    }

    public void offerService(final @NotNull Object id, final @NotNull Runnable callback) {
        offerService(id, payload -> {
            callback.run();
            return new Object();
        });
    }

    /**
     * Call a service and return the result. If no service is registered for the id, an exception is thrown.
     *
     * @param id      the service id
     * @param payload the service payload
     * @param <R>     the result type
     * @param <T>     the payload type
     * @return the service result
     * @throws RTException if no service is registered for the id
     */
    @SuppressWarnings("unchecked")
    public <R, T> R callService(final @NotNull Object id, final @NotNull T payload) {
        if (!serviceMap.containsKey(id))
            throw new RTException("service for id '%s' does not exist", id);
        return ((IServiceCallback<R, T>) serviceMap.get(id)).call(payload);
    }

    public <R> R callService(final @NotNull Object id) {
        return callService(id, new Object());
    }

    public <T> void callVoidService(final @NotNull Object id, final @NotNull T payload) {
        callService(id, payload);
    }

    public void callVoidService(final @NotNull Object id) {
        callService(id, new Object());
    }

    /**
     * Schedule a task for frame-synced unscoped execution. The task will be executed once the next time runTasks is called.
     *
     * @param task the task to be run
     */
    public void scheduleTask(final @NotNull Runnable task) {
        taskMap
                .computeIfAbsent(null, key -> new ArrayList<>())
                .add(task);
    }

    /**
     * Run all scheduled unscoped tasks.
     */
    public void runTasks() {
        final var tasks = taskMap.computeIfAbsent(null, key -> new ArrayList<>());
        final var copy = new ArrayList<>(tasks);
        tasks.clear();
        for (final var task : copy)
            task.run();
    }

    /**
     * Schedule a task for frame-synced scoped execution. The task will be executed once the next time runTasks is called with the same id.
     *
     * @param id   the frame id
     * @param task the task to be run
     */
    public void scheduleTask(final @NotNull Object id, final @NotNull Runnable task) {
        taskMap
                .computeIfAbsent(id, key -> new ArrayList<>())
                .add(task);
    }

    /**
     * Run all scheduled scoped tasks for the id.
     *
     * @param id the frame id
     */
    public void runTasks(final @NotNull Object id) {
        final var tasks = taskMap.computeIfAbsent(id, key -> new ArrayList<>());
        final var copy = new ArrayList<>(tasks);
        tasks.clear();
        for (final var task : copy)
            task.run();
    }

    public void registerTimer(final @NotNull Object id,
                              final long interval,
                              final boolean wait,
                              final @NotNull Runnable task) {
        final var timer = timerMap.computeIfAbsent(id, key -> new Timer());
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                task.run();
            }
        }, wait ? interval : 0, interval);
    }

    public void removeTimer(final @NotNull Object id) {
        if (!timerMap.containsKey(id))
            return;
        final var timer = timerMap.get(id);
        timer.cancel();
        timer.purge();
        timerMap.remove(id);
    }
}
