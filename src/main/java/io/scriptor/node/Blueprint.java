package io.scriptor.node;

import imgui.ImColor;
import imgui.ImGui;
import imgui.extension.imnodes.ImNodes;
import imgui.extension.imnodes.flag.ImNodesCol;
import imgui.type.ImString;
import io.scriptor.Context;
import io.scriptor.logic.ILogic;
import io.scriptor.util.IUnique;
import io.scriptor.util.ObjectIO;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public record Blueprint(
        UUID uuid,
        ImString label,
        int baseColor,
        ILogic logic) implements IUnique {

    public static void read(final Context context, final InputStream in) throws IOException {
        final var builder = new Builder();

        final var uuid = ObjectIO.readUUID(in);
        builder.uuid(uuid);
        builder.label(ObjectIO.readString(in));
        builder.baseColor(ObjectIO.readInt(in));

        context.<ILogic>getRef(ObjectIO.readUUID(in))
                .get(x -> context.getRef(uuid).set(builder.logic(x).build()));
    }

    public void write(final Context context, final OutputStream out) throws IOException {
        ObjectIO.write(out, uuid);
        ObjectIO.write(out, label.get());
        ObjectIO.write(out, baseColor);
        ObjectIO.write(out, logic.uuid());

        context.next(logic);
    }

    public static class Builder {

        private UUID uuid = UUID.randomUUID();
        private String label = "";
        private int baseColor = 0x212121;
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

        public Builder logic(final ILogic logic) {
            this.logic = logic;
            return this;
        }

        public Blueprint build() {
            return new Blueprint(
                    uuid,
                    new ImString(label),
                    baseColor,
                    logic);
        }
    }

    @Override
    public String toString() {
        return label.get();
    }

    public boolean hasInput() {
        return logic.inputs() > 0;
    }

    public boolean hasOutput() {
        return logic.outputs() > 0;
    }

    public void show(final Node node) {
        pushColorStyle();
        ImNodes.beginNode(node.id());

        ImNodes.beginNodeTitleBar();
        ImGui.textUnformatted(label.get());
        ImNodes.endNodeTitleBar();

        int i = 0;
        for (; i < Math.min(logic.inputs(), logic.outputs()); i++) {
            showInput(node.input(i));
            ImGui.sameLine();
            showOutput(node.output(i));
        }
        for (; i < logic.inputs(); i++) showInput(node.input(i));
        for (; i < logic.outputs(); i++) showOutput(node.output(i));

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
        ImGui.textUnformatted(logic.input(pin.index()));
        ImNodes.endInputAttribute();
    }

    private void showOutput(final Pin pin) {
        ImNodes.beginOutputAttribute(pin.id());
        ImGui.textUnformatted(logic.output(pin.index()));
        ImNodes.endOutputAttribute();
    }
}
