package io.scriptor.node;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.stream.Stream;

public class Node implements INode {

    private final Blueprint blueprint;
    private final Pin[] inputs;
    private final Pin[] outputs;

    private final boolean[] powered;

    public Node(final Blueprint blueprint) {
        this.blueprint = blueprint;

        inputs = new Pin[blueprint.inputLabels().length];
        outputs = new Pin[blueprint.outputLabels().length];
        powered = new boolean[outputs.length];

        for (int i = 0; i < inputs.length; ++i)
            inputs[i] = new Pin(this, i, false);
        for (int i = 0; i < outputs.length; ++i)
            outputs[i] = new Pin(this, i, true);
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
    public boolean noPredecessor(Graph graph) {
        return Arrays.stream(inputs).allMatch(x -> x.predecessor(graph).isEmpty());
    }

    @Override
    public void show() {
        blueprint.show(this);
    }

    @Override
    public Node copy() {
        return new Node(blueprint);
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
