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
package io.scriptor.context;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class State {

    private final Registry registry;

    private final Map<UUID, Boolean> attributeMap = new HashMap<>();
    private final Map<UUID, Map<Integer, Boolean>> registerMap = new HashMap<>();
    private final Map<UUID, boolean[]> resultMap = new HashMap<>();
    private final Map<UUID, State> substateMap = new HashMap<>();


    public State(final @NotNull Registry registry) {
        this.registry = registry;
    }

    public State(final @NotNull State state) {
        this.registry = state.registry;
    }

    public void setAttribute(final @NotNull UUID attribute, final boolean value) {
        attributeMap.put(attribute, value);
    }

    public boolean getAttribute(final @NotNull UUID attribute) {
        return attributeMap.computeIfAbsent(attribute, key -> false);
    }

    public void setRegister(final @NotNull UUID register, final int index, final boolean value) {
        registerMap.computeIfAbsent(register, key -> new HashMap<>()).put(index, value);
    }

    public boolean getRegister(final @NotNull UUID register, final int index) {
        return registerMap.computeIfAbsent(register, key -> new HashMap<>()).computeIfAbsent(index, key -> false);
    }

    public boolean call(final @NotNull UUID caller, final @NotNull UUID callee, final boolean @NotNull [] args) {
        return registry.get(callee).map(function -> {
            resultMap.put(caller, new boolean[function.numOutputs()]);
            final var substate = substateMap.computeIfAbsent(caller, key -> new State(this));
            function.exec(substate, args, resultMap.get(caller));
            return false;
        }).orElse(true);
    }

    public boolean getResult(final @NotNull UUID uuid, final int index) {
        return resultMap.get(uuid)[index];
    }
}
