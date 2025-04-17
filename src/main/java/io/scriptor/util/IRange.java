package io.scriptor.util;

import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;
import java.util.stream.Stream;

public interface IRange<T> {

    @NotNull IRange<T> filter(@NotNull Predicate<T> predicate);

    @NotNull Stream<T> stream();
}
