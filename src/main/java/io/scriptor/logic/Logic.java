package io.scriptor.logic;

import io.scriptor.node.Attribute;
import io.scriptor.node.Graph;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.UUID;

public class Logic implements ILogic {

    private final UUID uuid;
    private final Attribute[] inputs;
    private final Attribute[] outputs;
    private final Graph graph;

    public Logic(final UUID uuid, final Attribute[] attributes, final Graph graph) {
        this.uuid = uuid;
        this.inputs = Arrays.stream(attributes).filter(x -> !x.output()).toArray(Attribute[]::new);
        this.outputs = Arrays.stream(attributes).filter(Attribute::output).toArray(Attribute[]::new);
        this.graph = graph;
    }

    @Override
    public UUID uuid() {
        return uuid;
    }

    @Override
    public void write(PrintStream out) {

    }

    @Override
    public void cycle(final boolean[] in, final boolean[] out) {
        for (int i = 0; i < inputs.length; ++i)
            inputs[i].powered().set(in[i]);

        graph.cycle();

        for (int i = 0; i < outputs.length; ++i)
            out[i] = outputs[i].powered().get();
    }
}
