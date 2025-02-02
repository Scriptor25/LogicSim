package io.scriptor.event;

import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface IEventListener<T> {

    void invoke(final @NotNull T payload);
}
