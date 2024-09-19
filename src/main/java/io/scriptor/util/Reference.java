package io.scriptor.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class Reference<E> {

    private E value;
    private final List<Consumer<E>> onValue = new ArrayList<>();

    public Reference() {
    }

    public Reference(final E value) {
        this.value = value;
    }

    public E get() {
        return value;
    }

    public void set(final E value) {
        this.value = value;
        if (value != null) {
            onValue.forEach(c -> c.accept(value));
            onValue.clear();
        }
    }

    public void onValue(final Consumer<E> consumer) {
        if (value != null) {
            consumer.accept(value);
            return;
        }
        onValue.add(consumer);
    }
}
