package io.scriptor.imgui;

import io.scriptor.event.EventManager;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

/**
 * This class is part of the <a href="https://github.com/Scriptor25/LogicSim">Java Logic Sim</a> project.
 * <p>
 * Copyright (C) 2025  Felix Schreiber
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <a href="https://www.gnu.org/licenses/">https://www.gnu.org/licenses/</a>.
 *
 * @author Felix Schreiber
 */
public class Layout {

    private final EventManager events;
    private final List<Element> elements;

    public Layout(final @NotNull EventManager events, final @NotNull List<Element> elements) {
        this.events = events;
        this.elements = elements;
    }

    public void start() {
        elements.forEach(Element::start);
    }

    public void show() {
        elements.forEach(Element::show);
    }

    public @NotNull EventManager getEvents() {
        return events;
    }

    public @NotNull <T extends Element> Optional<T> findElement(final @NotNull String id, final @NotNull Class<T> type) {
        return elements.stream()
                .filter(e -> id.startsWith(e.getId()))
                .map(e -> e.findElement(id, type))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findAny();
    }
}
