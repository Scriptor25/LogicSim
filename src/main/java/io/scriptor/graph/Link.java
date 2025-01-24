package io.scriptor.graph;

import imgui.extension.imnodes.ImNodes;
import imgui.extension.imnodes.flag.ImNodesCol;
import io.scriptor.Constants;
import io.scriptor.util.IUnique;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

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
public record Link(UUID uuid, Pin source, Pin target) implements IUnique {

    public int id() {
        return uuid.hashCode();
    }

    public boolean notSelected() {
        return !ImNodes.isLinkSelected(id());
    }

    public void select() {
        ImNodes.selectLink(id());
    }

    public void show(final @NotNull Graph graph) {
        final var powered = source.powered(graph);
        if (powered) ImNodes.pushColorStyle(ImNodesCol.Link, Constants.COLOR_POWERED);
        ImNodes.link(id(), source.id(), target.id());
        if (powered) ImNodes.popColorStyle();
    }

    public boolean uses(final @NotNull INode node) {
        return source.uses(node) || target.uses(node);
    }

    public boolean uses(final @NotNull INode @NotNull [] nodes) {
        for (final var a : nodes) {
            for (final var b : nodes) {
                if (a == b) continue;
                if (source.node() == a && target.node() == b) return true;
                if (target.node() == a && source.node() == b) return true;
            }
        }
        return false;
    }

    public boolean uses(final @NotNull Pin pin) {
        return source == pin || target == pin;
    }
}
