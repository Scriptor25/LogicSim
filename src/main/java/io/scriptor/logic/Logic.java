package io.scriptor.logic;

import io.scriptor.Context;
import io.scriptor.node.Attribute;
import io.scriptor.node.Graph;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;
import java.util.function.Consumer;

public class Logic implements ILogic {

    public static void read(final Context context, final BufferedReader in) throws IOException {
        final var uuid = UUID.fromString(in.readLine());
        final var attributeCount = Integer.parseInt(in.readLine());
        final var attributeUUIDs = new UUID[attributeCount];
        for (int i = 0; i < attributeCount; ++i)
            attributeUUIDs[i] = UUID.fromString(in.readLine());
        final var graphUUID = UUID.fromString(in.readLine());

        final var attributes = new ArrayList<Attribute>();
        final var consumers = new ArrayList<Consumer<Attribute>>();
        for (int i = 0; i < attributeCount; ++i)
            consumers.add(attr -> {
                attributes.add(attr);
                if (consumers.isEmpty()) {
                    context.<Graph>getRef(graphUUID)
                            .get(graph -> context.getRef(uuid).set(new Logic(uuid, attributes.toArray(Attribute[]::new), graph)));
                } else {
                    context.<Attribute>getRef(attributeUUIDs[attributes.size()]).get(consumers.remove(0));
                }
            });

        if (attributeCount != 0) context.<Attribute>getRef(attributeUUIDs[0]).get(consumers.remove(0));
        else context.<Graph>getRef(graphUUID)
                .get(graph -> context.getRef(uuid).set(new Logic(uuid, new Attribute[0], graph)));
    }

    private final UUID uuid;
    private final Attribute[] inputs;
    private final Attribute[] outputs;
    private final Graph graph;

    public Logic(final UUID uuid, final Attribute[] attributes, final Graph graph) {
        this.uuid = uuid;
        this.inputs = Arrays.stream(attributes).filter(Attribute::input).toArray(Attribute[]::new);
        this.outputs = Arrays.stream(attributes).filter(Attribute::output).toArray(Attribute[]::new);
        this.graph = graph;
    }

    public Logic(final UUID uuid, final Attribute[] inputs, final Attribute[] outputs, final Graph graph) {
        this.uuid = uuid;
        this.inputs = inputs;
        this.outputs = outputs;
        this.graph = graph;
    }

    @Override
    public UUID uuid() {
        return uuid;
    }

    @Override
    public int inputs() {
        return inputs.length;
    }

    @Override
    public int outputs() {
        return outputs.length;
    }

    @Override
    public String input(final int i) {
        return inputs[i].label().get();
    }

    @Override
    public String output(final int i) {
        return outputs[i].label().get();
    }

    @Override
    public void write(final Context context, final PrintWriter out) {
        out.println(uuid());
        out.println(inputs.length + outputs.length);
        for (final var attribute : inputs) {
            out.println(attribute.uuid());
            context.next(attribute);
        }
        for (final var attribute : outputs) {
            out.println(attribute.uuid());
            context.next(attribute);
        }
        out.println(graph.uuid());

        context.next(graph);
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
