package io.scriptor.util;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class Range<E> {

    private final Class<E> type;
    private Collection<?> collection;
    private int start;
    private int end;

    private final List<Comparator<E>> comparators = new ArrayList<>();
    private final List<Predicate<E>> filters = new ArrayList<>();
    private final List<Predicate<E>> tempFilters = new ArrayList<>();

    public Range(final Class<E> type) {
        this(Collections.emptyList(), type, 0, 0);
    }

    public Range(final Collection<?> collection, final Class<E> type) {
        this(collection, type, 0, 0);
    }

    public Range(final Collection<?> collection, final Class<E> type, final int start, final int end) {
        this.collection = collection;
        this.type = type;
        this.start = start;
        this.end = end;
    }

    public Collection<?> collection() {
        return collection;
    }

    public void collection(final Collection<?> collection) {
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
                .filter(type::isInstance)
                .map(type::cast);
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
