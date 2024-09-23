package io.scriptor.node;

import io.scriptor.Context;
import io.scriptor.util.ObjectIO;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;
import java.util.stream.Stream;

public class Node implements INode {

    public static void read(final Context context, final InputStream in) throws IOException {
        final var uuid = ObjectIO.readUUID(in);
        context.<Blueprint>getRef(ObjectIO.readUUID(in))
                .get(x -> context.getRef(uuid).set(new Node(uuid, x)));
    }

    private final UUID uuid;
    private final Blueprint blueprint;

    private final Pin[] inputs;
    private final Pin[] outputs;
    private final boolean[] powered;

    public Node(final UUID uuid, final Blueprint blueprint) {
        this.uuid = uuid;
        this.blueprint = blueprint;

        this.inputs = new Pin[blueprint.logic().inputs()];
        this.outputs = new Pin[blueprint.logic().outputs()];
        this.powered = new boolean[outputs.length];
        for (int i = 0; i < inputs.length; ++i) this.inputs[i] = new Pin(this, i, false);
        for (int i = 0; i < outputs.length; ++i) this.outputs[i] = new Pin(this, i, true);
    }

    @Override
    public UUID uuid() {
        return uuid;
    }

    @Override
    public Pin input(final int i) {
        return inputs[i];
    }

    @Override
    public Pin output(final int i) {
        return outputs[i];
    }

    @Override
    public boolean powered(final int i) {
        return powered[i];
    }

    @Override
    public Optional<Pin> pin(final int id) {
        return Stream.concat(
                        Arrays.stream(inputs),
                        Arrays.stream(outputs)
                )
                .filter(x -> x.id() == id)
                .findFirst();
    }

    @Override
    public boolean noPredecessor(final Graph graph) {
        return Arrays.stream(inputs).allMatch(x -> x.predecessor(graph).isEmpty());
    }

    @Override
    public void show(final Graph graph) {
        blueprint.show(graph, this);
    }

    @Override
    public Node copy() {
        return new Node(UUID.randomUUID(), blueprint);
    }

    @Override
    public void write(final Context context, final OutputStream out) throws IOException {
        ObjectIO.write(out, uuid);
        ObjectIO.write(out, blueprint.uuid());

        context.next(blueprint);
    }

    @Override
    public void cycle(final Graph graph, final Queue<INode> callQueue) {
        final var in = new boolean[inputs.length];
        for (int i = 0; i < inputs.length; ++i) {
            final var j = i;
            inputs[i].predecessor(graph).ifPresent(pin -> in[j] = pin.powered());
        }
        blueprint.logic().cycle(in, powered);

        for (final var output : outputs)
            output.successors(graph).stream().map(Pin::node).filter(x -> !callQueue.contains(x)).forEach(callQueue::add);
    }

    public Blueprint blueprint() {
        return blueprint;
    }

    public List<INode> successors(final Graph graph) {
        return Arrays.stream(outputs)
                .map(x -> x.successors(graph))
                .mapMulti(Iterable<Pin>::forEach)
                .map(Pin::node)
                .distinct()
                .toList();
    }
}
