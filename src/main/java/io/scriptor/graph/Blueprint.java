package io.scriptor.graph;

import imgui.ImColor;
import imgui.ImGui;
import imgui.extension.imnodes.ImNodes;
import imgui.extension.imnodes.flag.ImNodesCol;
import imgui.type.ImInt;
import imgui.type.ImString;
import io.scriptor.context.Context;
import io.scriptor.function.IFunction;
import io.scriptor.util.IOStream;
import io.scriptor.util.IUnique;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record Blueprint(
        UUID uuid,
        ImString label,
        ImInt baseColor,
        String[] inputs,
        String[] outputs,
        IFunction function) implements IUnique {

    public static class Builder {

        private UUID uuid = UUID.randomUUID();
        private String label = "";
        private int baseColor = 0x212121;
        private final List<String> inputs = new ArrayList<>();
        private final List<String> outputs = new ArrayList<>();
        private IFunction function;

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

        public Builder inputs(final String... inputs) {
            this.inputs.addAll(List.of(inputs));
            return this;
        }

        public Builder outputs(final String... outputs) {
            this.outputs.addAll(List.of(outputs));
            return this;
        }

        public Builder function(final IFunction function) {
            this.function = function;
            return this;
        }

        public Blueprint build() {
            return new Blueprint(
                    uuid,
                    new ImString(label),
                    new ImInt(baseColor),
                    inputs.toArray(String[]::new),
                    outputs.toArray(String[]::new),
                    function);
        }
    }

    public static void read(final InputStream in, final Context context) throws IOException {
        final var uuid = IOStream.readUUID(in);

        final var builder = new Builder()
                .uuid(uuid)
                .label(IOStream.readString(in))
                .baseColor(IOStream.readInt(in));

        final var inputs = new String[IOStream.readInt(in)];
        for (int i = 0; i < inputs.length; ++i) inputs[i] = IOStream.readString(in);
        builder.inputs(inputs);

        final var outputs = new String[IOStream.readInt(in)];
        for (int i = 0; i < outputs.length; ++i) outputs[i] = IOStream.readString(in);
        builder.outputs(outputs);

        context.registry()
                .get(IOStream.readUUID(in))
                .ifPresent(fn -> context.add(builder.function(fn).build()));
    }

    public void write(final OutputStream out) throws IOException {
        IOStream.write(out, uuid);
        IOStream.write(out, label.get());
        IOStream.write(out, baseColor.get());
        IOStream.write(out, inputs.length);
        for (final var input : inputs) IOStream.write(out, input);
        IOStream.write(out, outputs.length);
        for (final var output : outputs) IOStream.write(out, output);
        IOStream.write(out, function.uuid());
    }

    @Override
    public String toString() {
        return label.get();
    }

    public void show(final Node node) {
        pushColorStyle();
        ImNodes.beginNode(node.id());

        ImNodes.beginNodeTitleBar();
        ImGui.textUnformatted(label.get());
        ImNodes.endNodeTitleBar();

        int i = 0;
        for (; i < Math.min(inputs.length, outputs.length); i++) {
            showInput(node.input(i));
            ImGui.sameLine();
            showOutput(node.output(i));
        }
        for (; i < inputs.length; i++) showInput(node.input(i));
        for (; i < outputs.length; i++) showOutput(node.output(i));

        ImNodes.endNode();
        popColorStyle();
    }

    private void pushColorStyle() {
        final var scale = 1.f / 255.f;
        final var r = (baseColor.get() >> 16 & 0xff) * scale;
        final var g = (baseColor.get() >> 8 & 0xff) * scale;
        final var b = (baseColor.get() & 0xff) * scale;

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
        ImGui.textUnformatted(inputs[pin.index()]);
        ImNodes.endInputAttribute();
    }

    private void showOutput(final Pin pin) {
        ImNodes.beginOutputAttribute(pin.id());
        ImGui.textUnformatted(outputs[pin.index()]);
        ImNodes.endOutputAttribute();
    }
}
