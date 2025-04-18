package io.scriptor.view;

import io.scriptor.event.EventManager;
import io.scriptor.util.IRange;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class ListView<T> extends View {

    private final IRange<T> range;
    private final Consumer<T> select;
    private final IValueView<T> view;

    public ListView(final @NotNull EventManager events,
                    final @NotNull IRange<T> range,
                    final @NotNull Consumer<T> select,
                    final @NotNull IValueView<T> view) {
        super(events);
        this.range = range;
        this.select = select;
        this.view = view;
    }

    @Override
    public void show() {
        range.stream().forEach(value -> {
            // ImGui.pushID(value.hashCode());
            if (view.show(value))
                select.accept(value);
            // ImGui.popID();
        });
    }
}
