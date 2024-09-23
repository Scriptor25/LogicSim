package io.scriptor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class Reference<E> implements IUnique {

    private final List<Consumer<E>> consumers = new ArrayList<>();
    private E value;

    public Reference() {
    }

    public Reference(final E value) {
        this.value = value;
    }

    public boolean valid() {
        return value != null;
    }

    public void get(final Consumer<E> consumer) {
        if (value != null) {
            consumer.accept(value);
            return;
        }
        consumers.add(consumer);
    }

    public E get() {
        if (!valid()) throw new IllegalStateException();
        return value;
    }

    public void set(final E value) {
        this.value = value;
        if (value != null) {
            consumers.forEach(consumer -> consumer.accept(value));
            consumers.clear();
        }
    }

    @Override
    public UUID uuid() {
        return null;
    }
}
