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
package io.scriptor.function;

import io.scriptor.context.State;
import io.scriptor.graph.Attribute;
import io.scriptor.graph.Graph;
import io.scriptor.instruction.Instruction;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Array;
import java.util.*;

/**
 * A function represents a linear collection of instructions executed in sequence to produce a result. It consists of inputs, outputs and said instruction list.
 */
public class Function implements IFunction, Collection<Instruction> {

    private final UUID uuid;
    private final Graph source;

    private Instruction[] data = new Instruction[8];
    private int size = 0;

    public Function(final @NotNull Graph source) {
        this(UUID.randomUUID(), source);
    }

    public Function(final @NotNull UUID uuid, final @NotNull Graph source) {
        this.uuid = uuid;
        this.source = source;
    }

    /**
     * Find an instruction of the given type with the given uuid.
     *
     * @param uuid the uuid
     * @param type the type class
     * @param <T>  the type
     * @return the instruction, or null if not found
     */
    public <T extends Instruction> @NotNull Optional<T> get(final @NotNull UUID uuid, final @NotNull Class<T> type) {
        for (final var instruction : data)
            if (instruction.same(uuid))
                return Optional.of(type.cast(instruction));
        return Optional.empty();
    }

    /**
     * Find an instruction with the given uuid.
     *
     * @param uuid the uuid
     * @return the instruction, or null if not found
     */
    public @NotNull Optional<Instruction> get(final @NotNull UUID uuid) {
        for (final var instruction : data)
            if (instruction.same(uuid))
                return Optional.of(instruction);
        return Optional.empty();
    }

    @Override
    public @NotNull UUID uuid() {
        return uuid;
    }

    @Override
    public int numInputs() {
        return source.numInputs();
    }

    @Override
    public int numOutputs() {
        return source.numOutputs();
    }

    private @NotNull Optional<UUID> input(final int index) {
        return source
                .input(index)
                .map(Attribute::uuid);
    }

    private @NotNull Optional<UUID> output(final int index) {
        return source
                .output(index)
                .map(Attribute::uuid);
    }

    @Override
    public void execute(final @NotNull State state, final int @NotNull [] inputs, final int @NotNull [] outputs) {
        final var inputCount = numInputs();
        for (int i = 0; i < inputCount; ++i) {
            final var fi = i;
            input(i).ifPresent(uuid -> state.setAttribute(uuid, inputs[fi]));
        }

        for (final var instruction : this)
            instruction.execute(state);

        final var outputCount = numOutputs();
        for (int i = 0; i < outputCount; ++i) {
            final var fi = i;
            output(i).ifPresent(uuid -> outputs[fi] = state.getAttribute(uuid));
        }
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public boolean contains(final @NotNull Object object) {
        return Arrays
                .stream(data)
                .limit(size)
                .anyMatch(instruction -> Objects.equals(instruction, object));
    }

    @Override
    public @NotNull Iterator<Instruction> iterator() {
        return new Iterator<>() {

            int i = 0;

            @Override
            public boolean hasNext() {
                return i < size;
            }

            @Override
            public Instruction next() {
                if (i >= size)
                    throw new NoSuchElementException();
                return data[i++];
            }
        };
    }

    @Override
    public @NotNull Object @NotNull [] toArray() {
        return Arrays.copyOf(data, size);
    }

    @Override
    public <T> @NotNull T @NotNull [] toArray(T @NotNull [] a) {
        if (a.length < size)
            a = Arrays.copyOf(a, size);
        for (int i = 0; i < size; ++i)
            Array.set(a, i, data[i]);
        if (a.length > size)
            a[size] = null;
        return a;
    }

    @Override
    public boolean add(final @NotNull Instruction instruction) {
        if (size == data.length)
            data = Arrays.copyOf(data, data.length * 2);
        data[size++] = instruction;
        return true;
    }

    @Override
    public boolean remove(final @NotNull Object object) {
        int pos = -1;
        for (int i = 0; i < size; ++i)
            if (Objects.equals(data[i], object)) {
                pos = i;
                break;
            }

        if (pos < 0)
            return false;

        for (int i = pos; i < size - 1; ++i)
            data[i] = data[i + 1];
        size--;
        return true;
    }

    @Override
    public boolean containsAll(final @NotNull Collection<?> collection) {
        return collection.stream().allMatch(this::contains);
    }

    @Override
    public boolean addAll(final @NotNull Collection<? extends Instruction> collection) {
        while (size + collection.size() >= data.length)
            data = Arrays.copyOf(data, data.length * 2);
        for (final var i : collection)
            data[size++] = i;
        return !collection.isEmpty();
    }

    @Override
    public boolean removeAll(final @NotNull Collection<?> collection) {
        boolean changed = false;
        for (final var instruction : collection)
            changed |= remove(instruction);
        return changed;
    }

    @Override
    public boolean retainAll(final @NotNull Collection<?> collection) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        size = 0;
        Arrays.fill(data, null);
    }
}
