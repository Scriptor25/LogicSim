package io.scriptor.graph;

import imgui.ImColor;
import imgui.ImGui;
import imgui.extension.imnodes.ImNodes;
import imgui.extension.imnodes.flag.ImNodesCol;
import imgui.type.ImInt;
import imgui.type.ImString;
import io.scriptor.Constants;
import io.scriptor.context.Context;
import io.scriptor.function.IFunction;
import io.scriptor.util.IOStream;
import io.scriptor.util.IUnique;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;

/**
 * This class is part of the <a href="https://github.com/Scriptor25/LogicSim">Java Logic Sim</a> project.
 * <p>
 * Copyright (C) 2025  Felix Schreiber
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <a href="https://www.gnu.org/licenses/">https://www.gnu.org/licenses/</a>.
 *
 * @author Felix Schreiber
 */
public record Blueprint(
        @NotNull UUID uuid,
        @NotNull ImString label,
        @NotNull ImInt baseColor,
        @NotNull String @NotNull [] inputs,
        @NotNull String @NotNull [] outputs,
        @NotNull IFunction function
) implements IUnique {

    public static class Builder {

        private UUID uuid = UUID.randomUUID();
        private String label = "";
        private int baseColor = 0x212121;
        private final List<String> inputs = new ArrayList<>();
        private final List<String> outputs = new ArrayList<>();
        private IFunction function;

        public @NotNull Builder uuid(final @NotNull UUID uuid) {
            this.uuid = uuid;
            return this;
        }

        public @NotNull Builder label(final @NotNull String label) {
            this.label = label;
            return this;
        }

        public @NotNull Builder baseColor(final int baseColor) {
            this.baseColor = baseColor;
            return this;
        }

        public @NotNull Builder inputs(final @NotNull String @NotNull ... inputs) {
            this.inputs.addAll(List.of(inputs));
            return this;
        }

        public @NotNull Builder outputs(final @NotNull String @NotNull ... outputs) {
            this.outputs.addAll(List.of(outputs));
            return this;
        }

        public @NotNull Builder function(final @NotNull IFunction function) {
            this.function = function;
            return this;
        }

        public @NotNull Blueprint build() {
            return new Blueprint(
                    uuid,
                    new ImString(label),
                    new ImInt(baseColor),
                    inputs.toArray(String[]::new),
                    outputs.toArray(String[]::new),
                    function);
        }
    }

    public static void read(final @NotNull InputStream in, final @NotNull Context context) throws IOException {
        final var uuid = IOStream.readUUID(in);

        final var builder = new Builder()
                .uuid(uuid)
                .label(IOStream.readString(in))
                .baseColor(IOStream.readInt(in));

        final var inputs = new String[IOStream.readInt(in)];
        for (int i = 0; i < inputs.length; ++i)
            inputs[i] = IOStream.readString(in);
        builder.inputs(inputs);

        final var outputs = new String[IOStream.readInt(in)];
        for (int i = 0; i < outputs.length; ++i)
            outputs[i] = IOStream.readString(in);
        builder.outputs(outputs);

        context.registry()
                .get(IOStream.readUUID(in))
                .ifPresent(fn -> context.add(builder.function(fn).build()));
    }

    public void write(final @NotNull OutputStream outputStream) throws IOException {
        IOStream.write(outputStream, uuid);
        IOStream.write(outputStream, label.get());
        IOStream.write(outputStream, baseColor.get());
        IOStream.write(outputStream, inputs.length);
        for (final var input : inputs)
            IOStream.write(outputStream, input);
        IOStream.write(outputStream, outputs.length);
        for (final var output : outputs)
            IOStream.write(outputStream, output);
        IOStream.write(outputStream, function.uuid());
    }

    @Override
    public boolean equals(final @Nullable Object object) {
        if (this == object) return true;
        if (null == object) return false;
        if (!(object instanceof Blueprint blueprint)) return false;
        return Objects.equals(uuid, blueprint.uuid)
                && Objects.equals(label, blueprint.label)
                && Objects.equals(baseColor, blueprint.baseColor)
                && Objects.deepEquals(inputs, blueprint.inputs)
                && Objects.deepEquals(outputs, blueprint.outputs)
                && Objects.equals(function, blueprint.function);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid, label, baseColor, Arrays.hashCode(inputs), Arrays.hashCode(outputs), function);
    }

    @Override
    public String toString() {
        return label.get();
    }

    public void show(final @NotNull Graph graph, final @NotNull Node node) {
        pushColorStyle();
        ImNodes.beginNode(node.id());

        ImNodes.beginNodeTitleBar();
        ImGui.textUnformatted(label.get());
        ImNodes.endNodeTitleBar();

        int maxInputWidth = 0;
        for (final var input : inputs)
            maxInputWidth = Math.max(maxInputWidth, input.length());

        int maxOutputWidth = 0;
        for (final var output : outputs)
            maxOutputWidth = Math.max(maxOutputWidth, output.length());

        final var inputFormat = "%-" + maxInputWidth + "s";
        final var outputFormat = "%" + maxOutputWidth + "s";

        int i = 0;
        for (; i < Math.min(inputs.length, outputs.length); ++i) {
            showInput(inputFormat, graph, node.input(i));
            ImGui.sameLine();
            showOutput(outputFormat, graph, node.output(i));
        }
        for (; i < inputs.length; ++i)
            showInput(inputFormat, graph, node.input(i));
        for (; i < outputs.length; ++i) {
            if (inputs.length > 0) {
                showStatic(inputFormat, i);
                ImGui.sameLine();
            }
            showOutput(outputFormat, graph, node.output(i));
        }

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

    private void showInput(final String format, final @NotNull Graph graph, final @NotNull Pin pin) {
        final var powered = pin.powered(graph);
        if (powered)
            ImNodes.pushColorStyle(ImNodesCol.Pin, Constants.COLOR_POWERED);
        ImNodes.beginInputAttribute(pin.id());
        ImGui.textUnformatted(format.formatted(inputs[pin.index()]));
        ImNodes.endInputAttribute();
        if (powered)
            ImNodes.popColorStyle();
    }

    private void showOutput(final String format, final @NotNull Graph graph, final @NotNull Pin pin) {
        final var powered = pin.powered(graph);
        if (powered)
            ImNodes.pushColorStyle(ImNodesCol.Pin, Constants.COLOR_POWERED);
        ImNodes.beginOutputAttribute(pin.id());
        ImGui.textUnformatted(format.formatted(outputs[pin.index()]));
        ImNodes.endOutputAttribute();
        if (powered)
            ImNodes.popColorStyle();
    }

    private void showStatic(final String format, final int id) {
        ImNodes.beginStaticAttribute(id);
        ImGui.textUnformatted(format.formatted(""));
        ImNodes.endStaticAttribute();
    }
}
