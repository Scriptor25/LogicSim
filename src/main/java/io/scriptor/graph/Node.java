package io.scriptor.graph;

import io.scriptor.instruction.*;

import java.util.*;
import java.util.stream.Stream;

public class Node implements INode {

    private final UUID uuid;
    private final Blueprint blueprint;

    private final Pin[] inputs;
    private final Pin[] outputs;

    public Node(final UUID uuid, final Blueprint blueprint) {
        this.uuid = uuid;
        this.blueprint = blueprint;

        this.inputs = new Pin[blueprint.function().numInputs()];
        this.outputs = new Pin[blueprint.function().numOutputs()];
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
    public boolean noSuccessors(final Graph graph) {
        return Arrays.stream(outputs).allMatch(x -> x.successors(graph).isEmpty());
    }

    @Override
    public List<INode> successors(final Graph graph) {
        return Arrays.stream(outputs)
                .<INode>mapMulti((pin, consumer) -> graph.findLinks(pin).stream()
                        .map(link -> link.target().node())
                        .forEach(consumer))
                .toList();
    }

    @Override
    public void show(final Graph graph) {
        blueprint.show(this);
    }

    @Override
    public Node copy() {
        return new Node(UUID.randomUUID(), blueprint);
    }

    @Override
    public void compile(final Graph graph, final Collection<Instruction> instructions, final Set<INode> compiling) {
        if (compiling.contains(this)) return;
        compiling.add(this);

        final var args = new Instruction[inputs.length];
        for (int i = 0; i < args.length; ++i) {
            final var pre = inputs[i].predecessor(graph);
            if (pre.isPresent()) {
                pre.get().node().compile(graph, instructions, compiling);
                args[i] = new GetRegInstruction(pre.get().node().uuid(), pre.get().index());
            } else args[i] = new ConstInstruction(false);
            instructions.add(args[i]);
        }

        final var call = new CallInstruction(blueprint.function().uuid(), args);
        instructions.add(call);

        for (int i = 0; i < outputs.length; ++i) {
            final var get = new GetResultInstruction(call, i);
            final var set = new SetRegInstruction(uuid, i, get);
            instructions.add(get);
            instructions.add(set);
        }

        compiling.remove(this);
    }
}
