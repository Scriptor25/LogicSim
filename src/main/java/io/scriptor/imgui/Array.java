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
package io.scriptor.imgui;

import imgui.ImGui;
import imgui.type.ImInt;
import io.scriptor.event.IPayload;
import io.scriptor.util.Range;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Array extends Element {

    public record Payload<T>(@NotNull Array self, @NotNull T value) implements IPayload {
    }

    private final String event;
    private Range<?> range;
    private ICustomView<?> element;

    public Array(final @NotNull Layout root, final @NotNull String id, final @NotNull String event) {
        super(root, id);
        this.event = id + '.' + event;
    }

    public <T> @NotNull Array range(final @Nullable Range<T> range) {
        this.range = range;
        return this;
    }

    public <T> @NotNull Array element(final @Nullable ICustomView<T> element) {
        this.element = element;
        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void onShow() {
        if (range == null)
            return;

        final var i = new ImInt();
        range.stream().forEach(value -> {
            ImGui.pushID(i.get());
            i.set(i.get() + 1);
            if ((element != null && ((ICustomView<Object>) element).show(value))
                    || (element == null && ImGui.selectable(value.toString())))
                getEvents().invokeEvent(event, new Payload<>(this, value));
            ImGui.popID();
        });
    }
}
