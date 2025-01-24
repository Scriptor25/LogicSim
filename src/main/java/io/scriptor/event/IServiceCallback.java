package io.scriptor.event;

import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface IServiceCallback<R, T extends IPayload> {

    R call(final @NotNull T payload);
}
