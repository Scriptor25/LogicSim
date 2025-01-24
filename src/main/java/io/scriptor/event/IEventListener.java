package io.scriptor.event;

import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface IEventListener<T extends IPayload> {

    void invoke(final @NotNull T payload);
}
