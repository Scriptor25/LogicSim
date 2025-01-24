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

import io.scriptor.event.EventManager;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public abstract class Element {

    private final Layout root;
    private final String id;

    protected Element(final @NotNull Layout root, final @NotNull String id) {
        this.root = root;
        this.id = id;
    }

    public @NotNull Layout getRoot() {
        return root;
    }

    public @NotNull String getId() {
        return id;
    }

    public @NotNull String getParentId() {
        return id.substring(0, id.lastIndexOf('.'));
    }

    public @NotNull EventManager getEvents() {
        return root.getEvents();
    }

    public void start() {
        onStart();
    }

    public void show() {
        onShow();
    }

    protected void onStart() {
    }

    protected abstract void onShow();

    public @NotNull <T extends Element> Optional<T> findElement(final @NotNull String id, final @NotNull Class<T> type) {
        if (id.equals(this.id))
            return Optional.of(type.cast(this));
        return Optional.empty();
    }
}
