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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * The event manager handles events, services and tasks on an application-wide scale, enabling communication between systems at runtime.
 */
public class EventManager {

    private final Map<Object, List<IEventListener<? extends IPayload>>> eventMap = new HashMap<>();
    private final Map<Object, IServiceCallback<?, ? extends IPayload>> serviceMap = new HashMap<>();
    private final Map<Object, List<Runnable>> taskMap = new HashMap<>();

    /**
     * Register an event listener. Multiple different listeners can be registered to a single event id.
     *
     * @param id       the event id
     * @param listener the event listener
     * @param <T>      the payload type
     */
    public <T extends IPayload> void registerEvent(final @NotNull Object id, final @NotNull IEventListener<T> listener) {
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
    public <T extends IPayload> void invokeEvent(final @NotNull Object id, final @NotNull T payload) {
        eventMap
                .computeIfAbsent(id, key -> new ArrayList<>())
                .forEach(callback -> ((IEventListener<T>) callback).invoke(payload));
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
    public <R, T extends IPayload> void offerService(final @NotNull Object id, final @NotNull IServiceCallback<R, T> callback) {
        if (serviceMap.containsKey(id))
            throw new RTException("overriding already existing services with id '%s'", id);
        serviceMap.put(id, callback);
    }

    public <T extends IPayload> void offerService(final @NotNull Object id, final @NotNull Consumer<T> callback) {
        this.<Void, T>offerService(id, payload -> {
            callback.accept(payload);
            return null;
        });
    }

    public <R> void offerService(final @NotNull Object id, final @NotNull Supplier<R> callback) {
        this.<R, IPayload>offerService(id, payload -> callback.get());
    }

    public void offerService(final @NotNull Object id, final @NotNull Runnable callback) {
        this.<Void, IPayload>offerService(id, payload -> {
            callback.run();
            return null;
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
    public <R, T extends IPayload> R callService(final @NotNull Object id, final @NotNull T payload) {
        if (!serviceMap.containsKey(id))
            throw new RTException("service for id '%s' does not exist", id);
        return ((IServiceCallback<R, T>) serviceMap.get(id)).call(payload);
    }

    public <R> R callService(final @NotNull Object id) {
        return callService(id, new IPayload() {
        });
    }

    public <T extends IPayload> void callVoidService(final @NotNull Object id, final @NotNull T payload) {
        callService(id, payload);
    }

    public void callVoidService(final @NotNull Object id) {
        callService(id, new IPayload() {
        });
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
        final var taskList = taskMap.computeIfAbsent(null, key -> new ArrayList<>());
        for (final var task : taskList)
            task.run();
        taskList.clear();
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
        final var taskList = taskMap.computeIfAbsent(id, key -> new ArrayList<>());
        for (final var task : taskList)
            task.run();
        taskList.clear();
    }
}
