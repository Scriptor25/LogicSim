package io.scriptor.function;

import io.scriptor.context.State;
import io.scriptor.instruction.Instruction;
import io.scriptor.instruction.TypeID;
import io.scriptor.util.IOStream;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.util.*;

import static io.scriptor.util.Task.handleVoid;

public class Function implements IFunction, Collection<Instruction> {

    public static IFunction read(final @NotNull InputStream inputStream) throws IOException {
        final var uuid = IOStream.readUUID(inputStream);
        final var inputs = new UUID[IOStream.readInt(inputStream)];
        for (int i = 0; i < inputs.length; ++i)
            inputs[i] = IOStream.readUUID(inputStream);
        final var outputs = new UUID[IOStream.readInt(inputStream)];
        for (int i = 0; i < outputs.length; ++i)
            outputs[i] = IOStream.readUUID(inputStream);
        final var function = new Function(uuid, inputs, outputs);
        final var instructions = IOStream.readInt(inputStream);
        for (int i = 0; i < instructions; ++i) {
            final var typeId = IOStream.readInt(inputStream);
            handleVoid(() -> TypeID
                    .toClass(typeId)
                    .getMethod("read", InputStream.class, Function.class)
                    .invoke(null, inputStream, function));
        }
        return function;
    }

    private final UUID uuid;
    private final UUID[] inputs;
    private final UUID[] outputs;

    private Instruction[] data = new Instruction[8];
    private int size = 0;

    public Function(final @NotNull UUID @NotNull [] inputs,
                    final @NotNull UUID @NotNull [] outputs) {
        this(UUID.randomUUID(), inputs, outputs);
    }

    public Function(final @NotNull UUID uuid,
                    final @NotNull UUID @NotNull [] inputs,
                    final @NotNull UUID @NotNull [] outputs) {
        super();

        this.uuid = uuid;
        this.inputs = inputs;
        this.outputs = outputs;
    }

    public <T extends Instruction> T find(final UUID uuid, final Class<T> type) {
        for (final var instruction : data)
            if (Objects.equals(instruction.uuid(), uuid))
                return type.cast(instruction);
        return null;
    }

    public Instruction find(final UUID uuid) {
        for (final var instruction : data)
            if (Objects.equals(instruction.uuid(), uuid))
                return instruction;
        return null;
    }

    @Override
    public @NotNull UUID uuid() {
        return uuid;
    }

    @Override
    public int typeId() {
        return 2;
    }

    @Override
    public int numInputs() {
        return inputs.length;
    }

    @Override
    public int numOutputs() {
        return outputs.length;
    }

    @Override
    public void exec(final @NotNull State state, final int hash, final boolean @NotNull [] inputs, final boolean @NotNull [] outputs) {
        for (int i = 0; i < this.inputs.length; ++i)
            state.setAttrib(this.inputs[i], inputs[i]);
        for (final var instruction : this)
            instruction.exec(state, hash + hashCode());
        for (int i = 0; i < this.outputs.length; ++i)
            outputs[i] = state.getAttrib(this.outputs[i]);
    }

    @Override
    public void write(final @NotNull OutputStream outputStream) throws IOException {
        IFunction.super.write(outputStream);
        IOStream.write(outputStream, numInputs());
        for (final var input : inputs)
            IOStream.write(outputStream, input);
        IOStream.write(outputStream, numOutputs());
        for (final var output : outputs)
            IOStream.write(outputStream, output);
        IOStream.write(outputStream, size);
        for (final var instruction : this) {
            IOStream.write(outputStream, TypeID.fromClass(instruction.getClass()));
            instruction.write(outputStream);
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
                if (i >= size) throw new NoSuchElementException();
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
