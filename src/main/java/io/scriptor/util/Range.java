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

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class Range<E> {

    private final Class<E> type;
    private Collection<E> collection;
    private int start;
    private int end;

    private final List<Comparator<E>> comparators = new ArrayList<>();
    private final List<Predicate<E>> filters = new ArrayList<>();
    private final List<Predicate<E>> tempFilters = new ArrayList<>();

    public Range(final Class<E> type) {
        this(Collections.emptyList(), type, 0, 0);
    }

    public Range(final Collection<E> collection, final Class<E> type) {
        this(collection, type, 0, 0);
    }

    public Range(final Collection<E> collection, final Class<E> type, final int start, final int end) {
        this.collection = collection;
        this.type = type;
        this.start = start;
        this.end = end;
    }

    public Collection<E> collection() {
        return collection;
    }

    public void collection(final Collection<E> collection) {
        this.collection = collection;
    }

    public int start() {
        return start;
    }

    public void start(final int start) {
        this.start = start;
    }

    public int end() {
        return end;
    }

    public void end(final int end) {
        this.end = end;
    }

    public void clear() {
        final var toDelete = stream().toList();
        collection.removeAll(toDelete);
    }

    public void sorted(final Comparator<E> comparator) {
        comparators.add(comparator);
    }

    public void filter(final Predicate<E> filter) {
        filters.add(filter);
    }

    public void tempFilter(final Predicate<E> filter) {
        tempFilters.add(filter);
    }

    public void clearTempFilters() {
        tempFilters.clear();
    }

    public Stream<E> stream() {
        var stream = collection
                .stream()
                .skip(start)
                .filter(type::isInstance);
        if (end > 0)
            stream = stream.limit((long) end - start);
        for (final var filter : filters)
            stream = stream.filter(filter);
        for (final var filter : tempFilters)
            stream = stream.filter(filter);
        for (final var comparator : comparators)
            stream = stream.sorted(comparator);
        return stream;
    }
}
