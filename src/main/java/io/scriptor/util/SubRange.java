package io.scriptor.util;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class SubRange<E> implements IRange<E> {

    private final Range<E> range;
    private final List<Predicate<E>> predicates = new ArrayList<>();

    public SubRange(final @NotNull Range<E> range) {
        this.range = range;
    }

    @Override
    public @NotNull SubRange<E> filter(final @NotNull Predicate<E> predicate) {
        predicates.add(predicate);
        return this;
    }

    @Override
    public @NotNull Stream<E> stream() {
        var stream = range.stream();
        for (final var predicate : predicates)
            stream = stream.filter(predicate);
        return stream;
    }
}
