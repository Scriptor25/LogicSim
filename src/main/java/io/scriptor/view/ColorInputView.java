package io.scriptor.view;

import imgui.ImColor;
import imgui.ImGui;
import imgui.type.ImInt;
import io.scriptor.event.EventManager;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class ColorInputView extends View {

    private final float[] color = new float[3];
    private final Consumer<Integer> select;

    public ColorInputView(final @NotNull EventManager events, final @NotNull Consumer<Integer> select) {
        super(events);
        this.select = select;
    }

    public void value(final @NotNull ImInt color) {
        final var s = 1.f / 255.f;
        this.color[0] = s * (color.get() >> 16 & 0xff);
        this.color[1] = s * (color.get() >> 8 & 0xff);
        this.color[2] = s * (color.get() & 0xff);
    }

    @Override
    public void show() {
        if (ImGui.colorEdit3("##color", color))
            select.accept(ImColor.rgb(color[2], color[1], color[0]));
    }
}
