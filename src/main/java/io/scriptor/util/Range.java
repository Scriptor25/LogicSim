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
package io.scriptor.util;

import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class Range<E> implements IRange<E> {

    private Collection<E> collection;
    private int start;
    private int end;

    private final List<Comparator<E>> comparators = new ArrayList<>();
    private final List<Predicate<E>> predicates = new ArrayList<>();

    public Range() {
        this(Collections.emptyList(), 0, 0);
    }

    public Range(final @NotNull Collection<E> collection) {
        this(collection, 0, 0);
    }

    public Range(final @NotNull Collection<E> collection,
                 final int start,
                 final int end) {
        this.collection = collection;
        this.start = start;
        this.end = end;
    }

    public @NotNull Collection<E> collection() {
        return collection;
    }

    public int start() {
        return start;
    }

    public int end() {
        return end;
    }

    public @NotNull Range<E> collection(final @NotNull Collection<E> collection) {
        this.collection = collection;
        return this;
    }

    public @NotNull Range<E> start(final int start) {
        this.start = start;
        return this;
    }

    public @NotNull Range<E> end(final int end) {
        this.end = end;
        return this;
    }

    public @NotNull Range<E> clear() {
        final var delete = stream().toList();
        collection.removeAll(delete);
        return this;
    }

    public @NotNull Range<E> sorted(final @NotNull Comparator<E> comparator) {
        comparators.add(comparator);
        return this;
    }

    @Override
    public @NotNull Range<E> filter(final @NotNull Predicate<E> predicate) {
        predicates.add(predicate);
        return this;
    }

    @Override
    public @NotNull Stream<E> stream() {
        var stream = collection
                .stream()
                .skip(start);
        if (end > 0)
            stream = stream.limit((long) end - start);
        for (final var predicate : predicates)
            stream = stream.filter(predicate);
        for (final var comparator : comparators)
            stream = stream.sorted(comparator);
        return stream;
    }
}
