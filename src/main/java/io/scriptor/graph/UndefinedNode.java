package io.scriptor.graph;

import imgui.ImGui;
import imgui.ImVec2;
import imgui.extension.imnodes.ImNodes;
import imgui.extension.imnodes.flag.ImNodesCol;
import io.scriptor.instruction.ConstInstruction;
import io.scriptor.instruction.Instruction;
import io.scriptor.instruction.SetRegInstruction;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;
import java.util.stream.Stream;

import static io.scriptor.util.Constants.NODE_ID_UNDEFINED;
import static io.scriptor.util.Constants.getPowerLevel;
import static io.scriptor.util.IO.*;

public class UndefinedNode extends Node {

    public static @NotNull Node parse(final @NotNull Graph graph, final @NotNull String string) {
        final var split = string.split(",");
        final var node = new UndefinedNode();
        final var posX = Integer.parseInt(split[1]);
        final var posY = Integer.parseInt(split[2]);
        node.editorPosition(new ImVec2(posX, posY));
        return node;
    }

    public static @NotNull Node read(final @NotNull Graph graph, final @NotNull InputStream stream) throws IOException {
        final var uuid = readUUID(stream);
        final var node = new UndefinedNode(uuid);
        final var posX = readInt(stream);
        final var posY = readInt(stream);
        node.position(posX, posY);
        return node;
    }

    private final Map<Integer, Pin> inputs = new HashMap<>();
    private final Map<Integer, Pin> outputs = new HashMap<>();

    public UndefinedNode() {
        this(UUID.randomUUID());
    }

    public UndefinedNode(final @NotNull UUID uuid) {
        super(uuid);
    }

    @Override
    public @NotNull Pin input(final int index) {
        return inputs.computeIfAbsent(index, key -> new Pin(this, index, false, (byte) 0));
    }

    @Override
    public @NotNull Pin output(final int index) {
        return outputs.computeIfAbsent(index, key -> new Pin(this, index, true, (byte) 0));
    }

    @Override
    public int numInputs() {
        return ~0;
    }

    @Override
    public int numOutputs() {
        return ~0;
    }

    @Override
    public int data(final @NotNull Graph graph, final boolean output, final int index) {
        if (output)
            return 0;
        return input(index)
                .predecessor(graph)
                .map(pin -> pin.data(graph))
                .orElse(0);
    }

    @Override
    public @NotNull Optional<Pin> pin(final int id) {
        return Stream
                .concat(inputs.values().stream(), outputs.values().stream())
                .filter(pin -> pin.id() == id)
                .findFirst();
    }

    @Override
    public boolean front(final @NotNull Graph graph) {
        return inputs
                .values()
                .stream()
                .allMatch(pin -> pin
                        .predecessor(graph)
                        .isEmpty());
    }

    @Override
    public boolean back(final @NotNull Graph graph) {
        return outputs
                .values()
                .stream()
                .allMatch(pin -> pin
                        .successors(graph)
                        .findAny()
                        .isEmpty());
    }

    @Override
    public @NotNull Stream<Node> successors(final @NotNull Graph graph) {
        return outputs
                .values()
                .stream()
                .mapMulti((pin, consumer) -> graph
                        .findLinks(pin)
                        .map(link -> link
                                .target()
                                .node())
                        .forEach(consumer));
    }

    @Override
    public void show(final @NotNull Graph graph) {
        ImNodes.beginNode(id());

        ImNodes.beginNodeTitleBar();
        ImGui.textUnformatted("UNDEFINED");
        ImNodes.endNodeTitleBar();

        final var inputCount = inputs.size();
        final var outputCount = outputs.size();

        int i = 0;
        for (; i < Math.min(inputCount, outputCount); ++i) {
            showInput(graph, input(i));
            ImGui.sameLine();
            showOutput(graph, output(i));
        }
        for (; i < inputCount; ++i)
            showInput(graph, input(i));
        for (; i < outputCount; ++i) {
            if (inputCount > 0) {
                showStatic(i);
                ImGui.sameLine();
            }
            showOutput(graph, output(i));
        }

        ImNodes.endNode();
    }

    private void showInput(final @NotNull Graph graph, final @NotNull Pin pin) {
        final var data = pin.data(graph);
        ImNodes.pushColorStyle(ImNodesCol.Pin, getPowerLevel(data, pin.bitwidth()));
        ImNodes.beginInputAttribute(pin.id());
        ImGui.textUnformatted("PIN");
        ImNodes.endInputAttribute();
        ImNodes.popColorStyle();
    }

    private void showOutput(final @NotNull Graph graph, final @NotNull Pin pin) {
        final var data = pin.data(graph);
        ImNodes.pushColorStyle(ImNodesCol.Pin, getPowerLevel(data, pin.bitwidth()));
        ImNodes.beginOutputAttribute(pin.id());
        ImGui.textUnformatted("PIN");
        ImNodes.endOutputAttribute();
        ImNodes.popColorStyle();
    }

    private void showStatic(final int id) {
        ImNodes.beginStaticAttribute(id);
        ImGui.textUnformatted("   ");
        ImNodes.endStaticAttribute();
    }

    @Override
    public @NotNull Node copy(final @NotNull Map<Attribute, Attribute> copies) {
        final var node = new UndefinedNode();
        node.position(posX(), posY());
        return node;
    }

    @Override
    public void compile(final @NotNull Graph graph, final @NotNull Collection<Instruction> instructions) {
        inputs.forEach((i, pin) -> pin
                .predecessor(graph)
                .map(Pin::node)
                .ifPresent(node -> node.compile(graph, instructions)));

        outputs.forEach((i, pin) -> instructions.add(new SetRegInstruction(uuid(), i, new ConstInstruction(0))));
    }

    @Override
    public int @NotNull [] execute(final @NotNull Graph graph) {
        inputs.forEach((i, pin) -> pin
                .predecessor(graph)
                .map(Pin::node)
                .ifPresent(node -> node.execute(graph)));

        final List<Integer> output = new ArrayList<>();
        outputs.forEach((i, pin) -> {
            while (output.size() <= i)
                output.add(0);
        });

        return output
                .stream()
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .toArray();
    }

    @Override
    public @NotNull String string() {
        final var pos = editorPosition();
        return "%d,%d,%d".formatted(NODE_ID_UNDEFINED, (int) pos.x, (int) pos.y);
    }

    @Override
    public void write(final @NotNull OutputStream stream) throws IOException {
        writeByte(stream, NODE_ID_UNDEFINED);
        writeUUID(stream, uuid());
        writeInt(stream, posX());
        writeInt(stream, posY());
    }
}
