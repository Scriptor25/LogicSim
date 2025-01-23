package io.scriptor.graph;

import io.scriptor.context.State;
import io.scriptor.instruction.*;
import io.scriptor.util.RTException;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Stream;

public class Node implements INode {

    private final UUID uuid;
    private final Blueprint blueprint;

    private final Pin[] inputs;
    private final Pin[] outputs;

    private final boolean[] pinOut;
    private State state;

    public Node(final @NotNull UUID uuid, final @NotNull Blueprint blueprint) {
        this.uuid = uuid;
        this.blueprint = blueprint;

        this.inputs = new Pin[blueprint.function().numInputs()];
        for (int i = 0; i < inputs.length; ++i) this.inputs[i] = new Pin(this, i, false);

        this.outputs = new Pin[blueprint.function().numOutputs()];
        for (int i = 0; i < outputs.length; ++i) this.outputs[i] = new Pin(this, i, true);

        this.pinOut = new boolean[blueprint.function().numOutputs()];
    }

    @Override
    public @NotNull UUID uuid() {
        return uuid;
    }

    @Override
    public @NotNull Pin input(final int i) {
        return inputs[i];
    }

    @Override
    public @NotNull Pin output(final int i) {
        return outputs[i];
    }

    @Override
    public boolean powered(final @NotNull Graph graph, final boolean output, final int index) {
        if (output && index < pinOut.length) return pinOut[index];
        if (!output && index < inputs.length) {
            final var pre = inputs[index].predecessor(graph);
            return pre.map(pin -> pin.powered(graph)).orElse(false);
        }
        throw new RTException("cannot get powered state of %s pin at index '%d'", output ? "output" : "input", index);
    }

    @Override
    public @NotNull Optional<Pin> pin(final int id) {
        return Stream.concat(
                        Arrays.stream(inputs),
                        Arrays.stream(outputs)
                )
                .filter(x -> x.id() == id)
                .findFirst();
    }

    @Override
    public boolean noPredecessor(final @NotNull Graph graph) {
        return Arrays.stream(inputs).allMatch(x -> x.predecessor(graph).isEmpty());
    }

    @Override
    public boolean noSuccessors(final @NotNull Graph graph) {
        return Arrays.stream(outputs).allMatch(x -> x.successors(graph).isEmpty());
    }

    @Override
    public @NotNull List<INode> successors(final @NotNull Graph graph) {
        return Arrays.stream(outputs)
                .<INode>mapMulti((pin, consumer) -> graph.findLinks(pin).stream()
                        .map(link -> link.target().node())
                        .forEach(consumer))
                .toList();
    }

    @Override
    public void show(final @NotNull Graph graph) {
        blueprint.show(graph, this);
    }

    @Override
    public @NotNull INode copy() {
        return new Node(UUID.randomUUID(), blueprint);
    }

    @Override
    public void compile(final @NotNull Graph graph, final @NotNull Collection<Instruction> instructions, final @NotNull Set<INode> compiling) {
        if (compiling.contains(this))
            return;
        compiling.add(this);

        final var args = new Instruction[inputs.length];
        for (int i = 0; i < args.length; ++i) {
            final var pre = inputs[i].predecessor(graph);
            if (pre.isPresent()) {
                pre.get().node().compile(graph, instructions, compiling);
                args[i] = new GetRegInstruction(pre.get().node().uuid(), pre.get().index());
            } else
                args[i] = new ConstInstruction(false);
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

    @Override
    public boolean @NotNull [] exec(final @NotNull Graph graph, final @NotNull Set<INode> executing) {
        if (executing.contains(this))
            return pinOut;
        executing.add(this);

        final var args = new boolean[inputs.length];
        for (int i = 0; i < args.length; ++i) {
            final var pre = inputs[i].predecessor(graph);
            if (pre.isPresent()) {
                final var out = pre.get().node().exec(graph, executing);
                args[i] = out[pre.get().index()];
            }
        }

        if (state == null)
            state = new State(graph.state());

        blueprint.function().exec(state, args, pinOut);

        executing.remove(this);
        return pinOut;
    }
}
