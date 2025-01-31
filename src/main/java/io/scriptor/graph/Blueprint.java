/*
 * This file is part of https://github.com/Scriptor25/LogicSim
 *
 * Copyright (C) 2025  Felix Schreiber
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see https://www.gnu.org/licenses/.
 */
package io.scriptor.graph;

import imgui.ImColor;
import imgui.ImGui;
import imgui.extension.imnodes.ImNodes;
import imgui.extension.imnodes.flag.ImNodesCol;
import imgui.type.ImInt;
import imgui.type.ImString;
import io.scriptor.context.Context;
import io.scriptor.context.State;
import io.scriptor.function.IFunction;
import io.scriptor.util.Constants;
import io.scriptor.util.IUnique;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import static io.scriptor.util.IO.*;

public record Blueprint(
        @NotNull UUID uuid,
        @NotNull ImString label,
        @NotNull ImInt baseColor,
        @NotNull Graph source,
        @NotNull IFunction function,
        boolean editable
) implements IUnique {

    public static class Builder {

        private UUID uuid = UUID.randomUUID();
        private String label = "";
        private int baseColor = 0x212121;
        private IFunction function;
        private Graph source;
        private boolean editable = true;

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

        public @NotNull Builder function(final @NotNull IFunction function) {
            this.function = function;
            return this;
        }

        public @NotNull Builder source(final @NotNull Graph source) {
            this.source = source;
            return this;
        }

        public @NotNull Builder editable(final boolean editable) {
            this.editable = editable;
            return this;
        }

        public @NotNull Blueprint build(final @NotNull Context context) {
            if (function == null)
                function = context
                        .registry()
                        .get(uuid)
                        .orElseGet(() -> source.compile());

            return new Blueprint(
                    uuid,
                    new ImString(label),
                    new ImInt(baseColor),
                    source,
                    function,
                    editable);
        }
    }

    public static @NotNull Blueprint read(final @NotNull Context context, final @NotNull InputStream stream) throws IOException {
        return new Builder()
                .uuid(readUUID(stream))
                .label(readString(stream))
                .baseColor(readInt(stream))
                .editable(readBool(stream))
                .source(Graph.read(context, stream))
                .build(context);
    }

    public void write(final @NotNull OutputStream stream) throws IOException {
        writeUUID(stream, uuid);
        writeString(stream, label.get());
        writeInt(stream, baseColor.get());
        writeBool(stream, editable);
        source.write(stream);
    }

    public @NotNull String input(final int index) {
        return source
                .input(index)
                .map(Attribute::label)
                .map(ImString::get)
                .orElseThrow();
    }

    public @NotNull String output(final int index) {
        return source
                .output(index)
                .map(Attribute::label)
                .map(ImString::get)
                .orElseThrow();
    }

    public void exec(final @NotNull State state, final boolean @NotNull [] inputs, final boolean @NotNull [] outputs) {
        function.exec(state, inputs, outputs);
    }

    @Override
    public String toString() {
        return label.get();
    }

    public void show(final @NotNull Graph graph, final @NotNull BlueprintNode node) {
        pushColorStyle();
        ImNodes.beginNode(node.id());

        ImNodes.beginNodeTitleBar();
        ImGui.textUnformatted(label.get());
        ImNodes.endNodeTitleBar();

        final var inputs = source
                .inputs()
                .map(Attribute::label)
                .map(ImString::get)
                .toArray(String[]::new);
        final var outputs = source
                .outputs()
                .map(Attribute::label)
                .map(ImString::get)
                .toArray(String[]::new);

        int maxInputWidth = 0;
        for (final var input : inputs)
            maxInputWidth = Math.max(maxInputWidth, input.length());

        int maxOutputWidth = 0;
        for (final var output : outputs)
            maxOutputWidth = Math.max(maxOutputWidth, output.length());

        final var labelWidth = label.getLength();
        if (labelWidth > maxInputWidth + maxOutputWidth) {
            if (maxInputWidth != 0 && maxOutputWidth != 0) {
                final var labelWidth2 = (labelWidth - maxInputWidth - maxOutputWidth) / 2;
                maxInputWidth += labelWidth2;
                maxOutputWidth += labelWidth2;
            } else if (maxInputWidth != 0) {
                maxInputWidth = labelWidth;
            } else if (maxOutputWidth != 0) {
                maxOutputWidth = labelWidth;
            }
        }

        final var inputFormat = "%%-%ds".formatted(maxInputWidth);
        final var outputFormat = "%%%ds".formatted(maxOutputWidth);

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
        ImGui.textUnformatted(format.formatted(input(pin.index())));
        ImNodes.endInputAttribute();
        if (powered)
            ImNodes.popColorStyle();
    }

    private void showOutput(final String format, final @NotNull Graph graph, final @NotNull Pin pin) {
        final var powered = pin.powered(graph);
        if (powered)
            ImNodes.pushColorStyle(ImNodesCol.Pin, Constants.COLOR_POWERED);
        ImNodes.beginOutputAttribute(pin.id());
        ImGui.textUnformatted(format.formatted(output(pin.index())));
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
