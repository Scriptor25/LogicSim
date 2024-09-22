package io.scriptor.node;

import imgui.ImColor;
import imgui.ImGui;
import imgui.extension.imnodes.ImNodes;
import imgui.extension.imnodes.flag.ImNodesCol;
import imgui.type.ImString;
import io.scriptor.Context;
import io.scriptor.logic.ILogic;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.*;

public record Blueprint(
        UUID uuid,
        ImString label,
        int baseColor,
        String[] inputLabels,
        String[] outputLabels,
        ILogic logic) {

    public static Blueprint read(final Context context, final BufferedReader in) throws IOException {
        final var builder = new Builder();

        builder.uuid(UUID.fromString(in.readLine()));
        builder.label(in.readLine());
        builder.baseColor = Integer.parseInt(in.readLine());

        final var inputLabelCount = Integer.parseInt(in.readLine());
        final var inputLabels = new String[inputLabelCount];
        for (int i = 0; i < inputLabelCount; ++i) inputLabels[i] = in.readLine();
        builder.inputLabels(inputLabels);

        final var outputLabelCount = Integer.parseInt(in.readLine());
        final var outputLabels = new String[outputLabelCount];
        for (int i = 0; i < outputLabelCount; ++i) outputLabels[i] = in.readLine();
        builder.outputLabels(outputLabels);

        final var logicUUID = UUID.fromString(in.readLine());
        builder.logic(context.logic(logicUUID));

        return builder.build();
    }

    public void write(final PrintStream out) {
        out.println(uuid);
        out.println(label);
        out.println(baseColor);
        out.println(inputLabels.length);
        Arrays.stream(inputLabels).forEach(out::println);
        out.println(outputLabels.length);
        Arrays.stream(outputLabels).forEach(out::println);
        out.println(logic.uuid());
    }

    public static class Builder {

        private UUID uuid = UUID.randomUUID();
        private String label = "";
        private int baseColor = 0x212121;
        private final List<String> inputLabels = new ArrayList<>();
        private final List<String> outputLabels = new ArrayList<>();
        private ILogic logic;

        public Builder uuid(final UUID uuid) {
            this.uuid = uuid;
            return this;
        }

        public Builder label(final String label) {
            this.label = label;
            return this;
        }

        public Builder baseColor(final int baseColor) {
            this.baseColor = baseColor;
            return this;
        }

        public Builder inputLabels(final String... inputLabels) {
            this.inputLabels.addAll(Arrays.asList(inputLabels));
            return this;
        }

        public Builder outputLabels(final String... outputLabels) {
            this.outputLabels.addAll(Arrays.asList(outputLabels));
            return this;
        }

        public Builder logic(final ILogic logic) {
            this.logic = logic;
            return this;
        }

        public Blueprint build() {
            return new Blueprint(
                    uuid,
                    new ImString(label),
                    baseColor,
                    inputLabels.toArray(String[]::new),
                    outputLabels.toArray(String[]::new),
                    logic);
        }
    }

    @Override
    public String toString() {
        return label.get();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (!(obj instanceof Blueprint b)) return false;
        return b.label.equals(label)
                && b.baseColor == baseColor
                && Arrays.equals(b.inputLabels, inputLabels)
                && Arrays.equals(b.outputLabels, outputLabels)
                && b.logic == logic;
    }

    @Override
    public int hashCode() {
        return Objects.hash(label, baseColor, Arrays.hashCode(inputLabels), Arrays.hashCode(outputLabels), logic);
    }

    public boolean hasInput() {
        return inputLabels.length != 0;
    }

    public boolean hasOutput() {
        return outputLabels.length != 0;
    }

    public void show(final Node node) {
        pushColorStyle();
        ImNodes.beginNode(node.id());

        ImNodes.beginNodeTitleBar();
        ImGui.textUnformatted(label.get());
        ImNodes.endNodeTitleBar();

        int i = 0;
        for (; i < Math.min(inputLabels.length, outputLabels.length); i++) {
            showInput(node.input(i));
            ImGui.sameLine();
            showOutput(node.output(i));
        }
        for (; i < inputLabels.length; i++) showInput(node.input(i));
        for (; i < outputLabels.length; i++) showOutput(node.output(i));

        ImNodes.endNode();
        popColorStyle();
    }

    private void pushColorStyle() {
        final var scale = 1.f / 255.f;
        final var r = (baseColor >> 16 & 0xff) * scale;
        final var g = (baseColor >> 8 & 0xff) * scale;
        final var b = (baseColor & 0xff) * scale;

        ImNodes.pushColorStyle(ImNodesCol.NodeBackground, ImColor.rgb(r, g, b));
        ImNodes.pushColorStyle(ImNodesCol.NodeBackgroundHovered, ImColor.rgb(r * 0.9f, g * 0.9f, b * 0.9f));
        ImNodes.pushColorStyle(ImNodesCol.NodeBackgroundSelected, ImColor.rgb(r * 0.7f, g * 0.8f, b * 0.95f));
        ImNodes.pushColorStyle(ImNodesCol.TitleBar, ImColor.rgb(r * 0.8f, g * 0.8f, b * 0.8f));
        ImNodes.pushColorStyle(ImNodesCol.TitleBarHovered, ImColor.rgb(r * 0.75f, g * 0.75f, b * 0.75f));
        ImNodes.pushColorStyle(ImNodesCol.TitleBarSelected, ImColor.rgb(r * 0.6f, g * 0.7f, b * 0.85f));
    }

    private void popColorStyle() {
        ImNodes.popColorStyle();
        ImNodes.popColorStyle();
        ImNodes.popColorStyle();
        ImNodes.popColorStyle();
        ImNodes.popColorStyle();
        ImNodes.popColorStyle();
    }

    private void showInput(final Pin pin) {
        ImNodes.beginInputAttribute(pin.id());
        ImGui.textUnformatted(inputLabels[pin.index()]);
        ImNodes.endInputAttribute();
    }

    private void showOutput(final Pin pin) {
        ImNodes.beginOutputAttribute(pin.id());
        ImGui.textUnformatted(outputLabels[pin.index()]);
        ImNodes.endOutputAttribute();
    }
}
