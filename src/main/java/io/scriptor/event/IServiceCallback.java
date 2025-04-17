package io.scriptor.event;

import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface IServiceCallback<R, T> {

    @NotNull R call(final @NotNull T payload);
}
