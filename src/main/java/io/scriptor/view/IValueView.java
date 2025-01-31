package io.scriptor.view;

import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface IValueView<T> {

    boolean show(final @NotNull T value);
}
