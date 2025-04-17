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

/**
 * A state contains information about the current runtime state of a node, function, etc.
 */
public class State {

    private final Context context;

    private final Map<UUID, Integer> attributeMap = new HashMap<>();
    private final Map<UUID, Map<Integer, Integer>> registerMap = new HashMap<>();
    private final Map<UUID, int[]> resultMap = new HashMap<>();
    private final Map<UUID, State> substateMap = new HashMap<>();

    /**
     * Create a new state using the given context.
     *
     * @param context the context
     */
    public State(final @NotNull Context context) {
        this.context = context;
    }

    /**
     * Create a new state using the context of the given state.
     *
     * @param state the state
     */
    public State(final @NotNull State state) {
        this.context = state.context;
    }

    /**
     * Set a global attribute in this state.
     *
     * @param attribute the attribute uuid
     * @param data      the value
     */
    public void setAttribute(final @NotNull UUID attribute, final int data) {
        attributeMap.put(attribute, data);
    }

    /**
     * Get a global attribute in this state.
     *
     * @param attribute the attribute uuid
     * @return the value
     */
    public int getAttribute(final @NotNull UUID attribute) {
        return attributeMap.computeIfAbsent(attribute, key -> 0);
    }

    /**
     * Set a local register value at the index.
     *
     * @param register the register uuid
     * @param index    the index
     * @param data     the value
     */
    public void setRegister(final @NotNull UUID register, final int index, final int data) {
        registerMap.computeIfAbsent(register, key -> new HashMap<>()).put(index, data);
    }

    /**
     * Get a local register value at the index.
     *
     * @param register the register uuid
     * @param index    the index
     * @return the value
     */
    public int getRegister(final @NotNull UUID register, final int index) {
        return registerMap.computeIfAbsent(register, key -> new HashMap<>()).computeIfAbsent(index, key -> 0);
    }

    /**
     * Call a blueprint function using the given args.
     *
     * @param caller the caller uuid
     * @param callee the callee uuid
     * @param args   the args
     * @return true if the callee does not exist
     */
    public boolean call(final @NotNull UUID caller, final @NotNull UUID callee, final int @NotNull [] args) {
        return context
                .get(callee)
                .map(blueprint -> {
                    resultMap.put(caller, new int[blueprint.numOutputs()]);
                    final var substate = substateMap.computeIfAbsent(caller, key -> new State(this));
                    blueprint.execute(substate, args, resultMap.get(caller));
                    return false;
                })
                .orElse(true);
    }

    /**
     * Get the result of a function call at the index.
     *
     * @param caller the caller uuid
     * @param index  the index
     * @return the result value
     */
    public int getResult(final @NotNull UUID caller, final int index) {
        return resultMap.get(caller)[index];
    }
}
