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
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Optional;

public class Window extends Element {

    public final String title;
    public final Element[] elements;

    public Window(final @NotNull Layout root,
                  final @NotNull String id,
                  final @NotNull String title,
                  final @NotNull Element @NotNull [] elements) {
        super(root, id);
        this.title = title;
        this.elements = elements;
    }

    @Override
    protected void onStart() {
        Arrays
                .stream(elements)
                .forEach(Element::start);
    }

    @Override
    protected void onShow() {
        if (ImGui.begin(title)) {
            Arrays
                    .stream(elements)
                    .forEach(Element::show);
            getEvents().runTasks();
        }
        ImGui.end();
    }

    @Override
    public @NotNull <T extends Element> Optional<T> findElement(final @NotNull String id, final @NotNull Class<T> type) {
        return Arrays.stream(elements)
                .filter(e -> id.startsWith(e.getId()))
                .map(e -> e.findElement(id, type))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findAny();
    }
}
