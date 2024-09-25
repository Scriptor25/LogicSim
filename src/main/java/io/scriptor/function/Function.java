package io.scriptor.function;

import io.scriptor.context.Registry;
import io.scriptor.context.State;
import io.scriptor.instruction.Instruction;
import io.scriptor.instruction.TypeID;
import io.scriptor.util.IOStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.util.*;

import static io.scriptor.util.Task.handleVoid;

public class Function implements IFunction, Collection<Instruction> {

    public static void read(final InputStream in, final Registry registry) throws IOException {
        final var uuid = IOStream.readUUID(in);
        final var inputs = new UUID[IOStream.readInt(in)];
        for (int j = 0; j < inputs.length; ++j) inputs[j] = IOStream.readUUID(in);
        final var outputs = new UUID[IOStream.readInt(in)];
        for (int j = 0; j < outputs.length; ++j) outputs[j] = IOStream.readUUID(in);
        final var fn = new Function(registry, uuid, inputs, outputs);
        final var instructions = IOStream.readInt(in);
        for (int j = 0; j < instructions; ++j) {
            final var typeId = IOStream.readInt(in);
            handleVoid(() -> TypeID.toClass(typeId)
                    .getMethod("read", InputStream.class, Function.class)
                    .invoke(null, in, fn));
        }
    }

    private final Registry registry;
    private final UUID uuid;
    private final UUID[] inputs;
    private final UUID[] outputs;

    private Instruction[] data = new Instruction[10];
    private int size = 0;

    public Function(final Registry registry, final UUID[] inputs, final UUID[] outputs) {
        this(registry, UUID.randomUUID(), inputs, outputs);
    }

    public Function(final Registry registry, final UUID uuid, final UUID[] inputs, final UUID[] outputs) {
        super();

        this.registry = registry;
        this.uuid = uuid;
        this.inputs = inputs;
        this.outputs = outputs;

        if (registry != null) registry.add(this);
    }

    public Registry registry() {
        return registry;
    }

    public <T extends Instruction> T find(final UUID uuid) {
        for (final var i : data)
            if (Objects.equals(i.uuid(), uuid))
                return (T) i;
        return null;
    }

    @Override
    public UUID uuid() {
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
    public void exec(final State state, final boolean[] in, final boolean[] out) {
        for (int i = 0; i < inputs.length; ++i) state.setAttrib(inputs[i], in[i]);
        for (final var i : this) i.exec(state);
        for (int i = 0; i < outputs.length; ++i) out[i] = state.getAttrib(outputs[i]);
    }

    @Override
    public void writeData(final OutputStream out) throws IOException {
        IOStream.write(out, numInputs());
        for (final var input : inputs) IOStream.write(out, input);
        IOStream.write(out, numOutputs());
        for (final var output : outputs) IOStream.write(out, output);
        IOStream.write(out, size);
        for (int j = 0; j < size; ++j) {
            final var i = data[j];
            IOStream.write(out, TypeID.fromClass(i.getClass()));
            i.write(out);
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
    public boolean contains(final Object o) {
        return Arrays.stream(data).limit(size).anyMatch(i -> Objects.equals(i, o));
    }

    @Override
    public Iterator<Instruction> iterator() {
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
    public Object[] toArray() {
        return Arrays.copyOf(data, size);
    }

    @Override
    public <T> T[] toArray(T[] a) {
        if (a.length < size) a = Arrays.copyOf(a, size);
        for (int i = 0; i < data.length; ++i)
            Array.set(a, i, data[i]);
        if (a.length > size) a[size] = null;
        return a;
    }

    @Override
    public boolean add(final Instruction i) {
        if (size == data.length)
            data = Arrays.copyOf(data, size * 2);
        data[size++] = i;
        return true;
    }

    @Override
    public boolean remove(final Object o) {
        int pos = -1;
        for (int i = 0; i < size; ++i)
            if (Objects.equals(data[i], o)) {
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
    public boolean containsAll(final Collection<?> c) {
        return c.stream().allMatch(this::contains);
    }

    @Override
    public boolean addAll(final Collection<? extends Instruction> c) {
        while (size + c.size() >= data.length)
            data = Arrays.copyOf(data, size * 2);
        for (final var i : c)
            data[size++] = i;
        return !c.isEmpty();
    }

    @Override
    public boolean removeAll(final Collection<?> c) {
        boolean changed = false;
        for (final var i : c)
            changed |= remove(i);
        return changed;
    }

    @Override
    public boolean retainAll(final Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        size = 0;
        Arrays.fill(data, null);
    }
}
