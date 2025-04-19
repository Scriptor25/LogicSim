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
package io.scriptor.view;

import imgui.ImGui;
import imgui.ImVec2;
import imgui.extension.imnodes.ImNodes;
import imgui.extension.imnodes.ImNodesEditorContext;
import imgui.extension.imnodes.flag.ImNodesMiniMapLocation;
import imgui.flag.*;
import imgui.type.ImBoolean;
import imgui.type.ImInt;
import imgui.type.ImString;
import io.scriptor.event.EventManager;
import io.scriptor.graph.*;
import io.scriptor.util.RTException;
import io.scriptor.util.Range;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import static io.scriptor.util.Constants.*;

public class EditorView extends View {

    private static final String STRING_DELETE = "Delete";

    private static boolean showFullAttribute(final @NotNull Attribute attribute) {
        ImGui.beginDisabled(attribute.output());
        ImGui.sliderInt("##data@%s".formatted(attribute.uuid()), attribute.data().getData(), 0, (1 << attribute.bitwidth()) - 1);
        ImGui.endDisabled();
        ImGui.sameLine();
        ImGui.selectable("%s##label@%s".formatted(attribute.label().get(), attribute.uuid()));
        return ImGui.isItemHovered();
    }

    private static boolean showAttribute(final @NotNull Attribute attribute) {
        return ImGui.selectable(attribute.label().get());
    }

    private static boolean showBlueprint(final @NotNull Blueprint blueprint) {
        return ImGui.selectable(blueprint.label().get());
    }

    private final ListView<Attribute> attributeView;
    private final ListView<Attribute> addAttributeView;
    private final ListView<Blueprint> addBlueprintView;
    private final ListView<Attribute> addDirectAttributeView;
    private final ListView<Blueprint> addDirectBlueprintView;
    private final ListView<Attribute> replaceAttributeView;
    private final ListView<Blueprint> replaceBlueprintView;
    private final TextInputView labelView;

    private final Popup attributePopup;
    private final Popup setLabelPopup;
    private final Popup nodePopup;
    private final Popup linkPopup;
    private final Popup editorPopup;
    private final Popup addPopup;
    private final Popup addDirectPopup;
    private final Popup replacePopup;
    private final Popup deletePopup;
    private final Popup addAttributePopup;

    private final ImBoolean open = new ImBoolean(true);

    private final Blueprint instance;
    private final Graph source;

    private final ImNodesEditorContext context;

    private Attribute selectedAttribute;
    private Node selectedNode;
    private Pin sourcePin;
    private Pin targetPin;
    private float mouseX;
    private float mouseY;
    private boolean first = true;

    private final ImInt addAttributeBitwidth = new ImInt();
    private final ImBoolean addAttributeOutput = new ImBoolean();
    private final ImString addAttributeLabel = new ImString();

    public EditorView(final @NotNull EventManager events, final @NotNull Blueprint instance) {
        super(events);

        this.instance = instance;
        this.source = instance.source();

        labelView = new TextInputView(events, this::onLabelEnter);
        attributeView = new ListView<>(
                events,
                new Range<>(source.attributes())
                        .sorted(Comparator.comparing(Attribute::output)),
                this::onAttributeSelect,
                EditorView::showFullAttribute);
        addAttributeView = new ListView<>(
                events,
                new Range<>(source.attributes())
                        .sorted(Comparator.comparing(Attribute::label))
                        .sorted(Comparator.comparing(Attribute::output)),
                this::onAddAttribute,
                EditorView::showAttribute);
        addBlueprintView = new ListView<>(
                events,
                new Range<>(source.context().blueprints())
                        .filter(blueprint -> blueprint != instance)
                        .filter(blueprint -> !blueprint.uses(instance, true))
                        .sorted(Comparator.comparing(Blueprint::label)),
                this::onAddBlueprint,
                EditorView::showBlueprint);
        addDirectAttributeView = new ListView<>(
                events,
                new Range<>(source.attributes())
                        .filter(attribute -> sourcePin != null)
                        .filter(attribute -> sourcePin.output() == attribute.output())
                        .filter(attribute -> sourcePin.bitwidth() == attribute.bitwidth())
                        .sorted(Comparator.comparing(Attribute::label))
                        .sorted(Comparator.comparing(Attribute::output)),
                this::onAddAttribute,
                EditorView::showAttribute);
        addDirectBlueprintView = new ListView<>(
                events,
                new Range<>(source.context().blueprints())
                        .filter(blueprint -> sourcePin != null)
                        .filter(blueprint -> blueprint != instance)
                        .filter(blueprint -> !blueprint.uses(instance, true))
                        .filter(blueprint -> sourcePin.output()
                                ? blueprint.numInputs() > 0
                                : blueprint.numOutputs() > 0)
                        .filter(blueprint -> (sourcePin.output()
                                ? blueprint.inputBitwidth(0)
                                : blueprint.outputBitwidth(0))
                                .map(bitwidth -> sourcePin.bitwidth() == bitwidth)
                                .orElse(false))
                        .sorted(Comparator.comparing(Blueprint::label)),
                this::onAddBlueprint,
                EditorView::showBlueprint);
        replaceAttributeView = new ListView<>(
                events,
                new Range<>(source.attributes())
                        .filter(attribute -> selectedNode != null)
                        .filter(attribute -> !selectedNode.same(attribute))
                        .sorted(Comparator.comparing(Attribute::label))
                        .sorted(Comparator.comparing(Attribute::output)),
                this::onReplaceAttribute,
                EditorView::showAttribute);
        replaceBlueprintView = new ListView<>(
                events,
                new Range<>(source.context().blueprints())
                        .filter(blueprint -> selectedNode != null)
                        .filter(blueprint -> blueprint != instance)
                        .filter(blueprint -> !blueprint.uses(instance, true))
                        .filter(blueprint -> !selectedNode.same(blueprint))
                        .sorted(Comparator.comparing(Blueprint::label)),
                this::onReplaceBlueprint,
                EditorView::showBlueprint);

        attributePopup = new Popup(events, this::showAttributeContext);
        setLabelPopup = new Popup(events, this::showLabelContext);
        nodePopup = new Popup(events, this::showNodeContext);
        linkPopup = new Popup(events, this::showLinkContext);
        editorPopup = new Popup(events, this::showEditorContext);
        addPopup = new Popup(events, this::showAddContext);
        addDirectPopup = new Popup(events, this::showAddDirectContext);
        replacePopup = new Popup(events, this::showReplaceContext);
        deletePopup = new Popup(events, this::showDeleteContext);
        addAttributePopup = new Popup(events, this::showAddAttributeContext);

        events.registerEvent("key.delete.press", this::onKeyDelete);
        events.registerEvent("key.a.press+control", this::onKeyCtrlA);
        events.registerEvent("key.c.press+control", this::onKeyCtrlC);
        events.registerEvent("key.d.press+control", this::onKeyCtrlD);
        events.registerEvent("key.v.press+control", this::onKeyCtrlV);
        events.registerEvent("key.x.press+control", this::onKeyCtrlX);

        context = ImNodes.editorContextCreate();
    }

    private @NotNull String id() {
        return "%s###%s".formatted(instance.label(), instance.uuid());
    }

    public void focus() {
        ImGui.setWindowFocus(id());
    }

    @Override
    public void show() {
        if (!open.get()) {
            events.callService(ID_BLUEPRINT_CLOSE, instance);
            return;
        }

        ImGui.setNextWindowSize(400, 300, ImGuiCond.FirstUseEver);
        if (!ImGui.begin(id(), open)) {
            ImGui.end();
            return;
        }

        ImGui.beginGroup();
        if (ImGui.button("Add Attribute"))
            addAttribute();
        if (ImGui.beginChild("attributes", 200, 0))
            attributeView.show();
        ImGui.endChild();
        ImGui.endGroup();
        ImGui.sameLine();

        ImNodes.editorContextSet(context);
        ImNodes.beginNodeEditor();

        if (first) {
            first = false;
            source.loadNodePositions();
        }

        source.compile();
        source.execute();
        source.show();

        final var hovered = ImNodes.isEditorHovered();

        ImNodes.miniMap(.2f, ImNodesMiniMapLocation.BottomRight);
        ImNodes.endNodeEditor();

        final var hoveredLink = new ImInt();
        if (ImNodes.isLinkHovered(hoveredLink))
            source
                    .findLink(hoveredLink.get())
                    .ifPresent(link -> ImGui.setTooltip("%d".formatted(link.source().data(source))));

        handleMouse(hovered);

        handleLinkDropped();
        handleLinkCreated();

        events.runTasks(this);

        ImGui.end();

        attributePopup.show();
        setLabelPopup.show();
        nodePopup.show();
        linkPopup.show();
        editorPopup.show();
        addPopup.show();
        addDirectPopup.show();
        replacePopup.show();
        deletePopup.show();
        addAttributePopup.show();
    }

    private void onLabelEnter(final @NotNull String label) {
        if (!label.isEmpty())
            selectedAttribute.label().set(label, true);
        ImGui.closeCurrentPopup();
    }

    private void onAttributeSelect(final @NotNull Attribute attribute) {
        if (ImGui.isMouseClicked(ImGuiMouseButton.Right) || ImGui.isKeyPressed(ImGuiKey.Menu)) {
            selectedAttribute = attribute;
            events.scheduleTask(attributePopup::open);
        }
    }

    private void showAttributeContext() {
        if (ImGui.selectable("Set Label")) {
            labelView.value(selectedAttribute.label().get());
            events.scheduleTask(setLabelPopup::open);
        }
        if (ImGui.selectable(STRING_DELETE))
            events.scheduleTask(this, this::onAttributeDelete);
    }

    private void showLabelContext() {
        labelView.show();
    }

    private void showNodeContext() {
        if (ImGui.selectable("Copy"))
            events.scheduleTask(this, this::onNodeCopy);
        if (ImGui.selectable("Cut"))
            events.scheduleTask(this, this::onNodeCut);
        if (ImGui.selectable("Duplicate"))
            events.scheduleTask(this, this::onNodeDuplicate);
        if (ImGui.selectable("Replace"))
            events.scheduleTask(this, this::onNodeReplace);
        if (ImGui.selectable(STRING_DELETE))
            events.scheduleTask(this, this::onNodeDelete);
        if (selectedNode instanceof BlueprintNode node && node.blueprint().editable()) {
            ImGui.separator();
            if (ImGui.selectable("Edit Blueprint"))
                events.callService(ID_BLUEPRINT_EDIT, node.blueprint());
        }
    }

    private void showLinkContext() {
        if (ImGui.selectable(STRING_DELETE))
            events.scheduleTask(this, this::onLinkDelete);
    }

    private void showEditorContext() {
        if (ImGui.selectable("Add"))
            events.scheduleTask(this, this::onEditorAdd);
        if (ImGui.selectable("Paste"))
            events.scheduleTask(this, this::onEditorPaste);
        if (ImGui.selectable("Clear"))
            events.scheduleTask(this, this::onEditorClear);
    }

    private void showAddContext() {
        addAttributeView.show();
        ImGui.separator();
        addBlueprintView.show();
    }

    private void showAddDirectContext() {
        addDirectAttributeView.show();
        ImGui.separator();
        addDirectBlueprintView.show();
    }

    private void showReplaceContext() {
        replaceAttributeView.show();
        ImGui.separator();
        replaceBlueprintView.show();
    }

    private void showDeleteContext() {
        if (ImGui.selectable("Nodes"))
            events.scheduleTask(this::onNodeDelete);
        if (ImGui.selectable("Links"))
            events.scheduleTask(this::onLinkDelete);
    }

    private void showAddAttributeContext() {
        ImGui.sliderInt("Bitwidth", addAttributeBitwidth.getData(), 1, 32);
        ImGui.sameLine();
        ImGui.checkbox("Output", addAttributeOutput);
        if (ImGui.inputTextWithHint("Label", "press enter to confirm", addAttributeLabel, ImGuiInputTextFlags.EnterReturnsTrue)) {
            events.scheduleTask(this, () -> source.add(new Attribute(addAttributeLabel.get(), addAttributeOutput.get(), addAttributeBitwidth.byteValue())));
            ImGui.closeCurrentPopup();
        }
    }

    private void onKeyDelete() {
        events.scheduleTask(this, () -> {
            if (ImGui.isWindowFocused(ImGuiFocusedFlags.RootAndChildWindows))
                deleteSelection();
        });
    }

    private void onKeyCtrlA() {
        events.scheduleTask(this, () -> {
            if (ImGui.isWindowFocused(ImGuiFocusedFlags.RootAndChildWindows))
                selectAll();
        });
    }

    private void onKeyCtrlC() {
        events.scheduleTask(this, () -> {
            if (ImGui.isWindowFocused(ImGuiFocusedFlags.RootAndChildWindows))
                events.callService(ID_CLIPBOARD_SET, copy());
        });
    }

    private void onKeyCtrlD() {
        events.scheduleTask(this, () -> {
            if (ImGui.isWindowFocused(ImGuiFocusedFlags.RootAndChildWindows))
                duplicate();
        });
    }

    private void onKeyCtrlV() {
        events.scheduleTask(this, () -> {
            if (ImGui.isWindowFocused(ImGuiFocusedFlags.RootAndChildWindows))
                paste(events.callService(ID_CLIPBOARD_GET));
        });
    }

    private void onKeyCtrlX() {
        events.scheduleTask(this, () -> {
            if (ImGui.isWindowFocused(ImGuiFocusedFlags.RootAndChildWindows))
                events.callService(ID_CLIPBOARD_SET, cut());
        });
    }

    private @NotNull String copy() {
        final var nodeIds = new int[ImNodes.numSelectedNodes()];
        ImNodes.getSelectedNodes(nodeIds);

        final List<Node> nodes = new ArrayList<>();
        for (final var nodeId : nodeIds)
            source
                    .findNode(nodeId)
                    .ifPresent(nodes::add);

        final List<Link> links = new ArrayList<>();
        source
                .links()
                .filter(link -> link.usesPairOf(nodes))
                .forEach(links::add);

        final var data = new StringBuilder();
        data
                .append("LS")
                .append('\n')
                .append(nodes.size())
                .append('\n');
        for (final var node : nodes)
            data
                    .append(node.string())
                    .append('\n');
        data
                .append(links.size())
                .append('\n');
        for (final var link : links)
            data
                    .append(link.string(nodes))
                    .append('\n');
        return data.toString();
    }

    private void duplicate() {
        paste(copy());
    }

    private @NotNull String cut() {
        final var data = copy();
        deleteSelectedNodes();
        return data;
    }

    private void paste(final @NotNull String data) {
        final var lines = data
                .lines()
                .toArray(String[]::new);
        int i = 0;

        final var magic = lines[i++];
        if (!magic.equals("LS"))
            return;

        final var nodeArrayLength = Integer.parseInt(lines[i++], 10);
        final var nodeArray = new Node[nodeArrayLength];
        for (int j = 0; j < nodeArrayLength; ++j) {
            final var node = Node.parse(source, lines[i++]);
            nodeArray[j] = node;
            source.add(node);
        }

        final var linkArrayLength = Integer.parseInt(lines[i++], 10);
        final var linkArray = new Link[linkArrayLength];
        for (int j = 0; j < linkArrayLength; ++j) {
            final var link = Link.parse(nodeArray, lines[i++]);
            linkArray[j] = link;
            source.add(link);
        }

        events.scheduleTask(this, () -> {
            ImNodes.clearNodeSelection();
            ImNodes.clearLinkSelection();

            for (final var node : nodeArray)
                node.select();
            for (final var link : linkArray)
                link.select();
        });
    }

    private void addAttribute() {
        addAttributeBitwidth.set(1);
        addAttributeOutput.set(false);
        addAttributeLabel.set("");
        events.scheduleTask(addAttributePopup::open);
    }

    private void onAttributeDelete() {
        source.remove(selectedAttribute);
    }

    private void onNodeCopy() {
        events.callService(ID_CLIPBOARD_SET, copy());
    }

    private void onNodeCut() {
        events.callService(ID_CLIPBOARD_SET, cut());
    }

    private void onNodeDuplicate() {
        paste(copy());
    }

    private void onNodeReplace() {
        events.scheduleTask(replacePopup::open);
    }

    private void onNodeDelete() {
        deleteSelectedNodes();
    }

    private void onLinkDelete() {
        deleteSelectedLinks();
    }

    private void onEditorPaste() {
        paste(events.callService(ID_CLIPBOARD_GET));
    }

    private void onEditorClear() {
        source.clear();
    }

    private void onEditorAdd() {
        events.scheduleTask(addPopup::open);
    }

    private void onAddAttribute(final @NotNull Attribute attribute) {
        onAddNode(attribute.output()
                ? new OutputNode(attribute)
                : new InputNode(attribute));
    }

    private void onAddBlueprint(final @NotNull Blueprint blueprint) {
        onAddNode(new BlueprintNode(blueprint));
    }

    private void onAddNode(final @NotNull Node node) {
        source.add(node);
        node.screenPosition(new ImVec2(mouseX, mouseY));

        if (sourcePin == null)
            return;

        targetPin = sourcePin.output()
                ? node.input(0)
                : node.output(0);

        if (sourcePin.bitwidth() != targetPin.bitwidth())
            throw new RTException();

        source
                .findLink(targetPin.output()
                        ? sourcePin
                        : targetPin)
                .ifPresent(source::remove);

        source.add(sourcePin.output()
                ? new Link(sourcePin, targetPin)
                : new Link(targetPin, sourcePin));

        sourcePin = null;
        targetPin = null;
    }

    private void onReplaceAttribute(final @NotNull Attribute attribute) {
        onReplaceNode(attribute.output()
                ? new OutputNode(attribute)
                : new InputNode(attribute));
    }

    private void onReplaceBlueprint(final @NotNull Blueprint blueprint) {
        onReplaceNode(new BlueprintNode(blueprint));
    }

    private void onReplaceNode(final @NotNull Node node) {
        node.screenPosition(selectedNode.screenPosition());

        final var links = source
                .links()
                .filter(link -> link.uses(selectedNode))
                .map(link -> {
                    if (link.source().uses(selectedNode) && link.source().index() < node.numOutputs() && (link.source().bitwidth() == 0 || link.source().bitwidth() == node.output(link.source().index()).bitwidth()))
                        return new Link(node.output(link.source().index()), link.target());
                    if (link.target().uses(selectedNode) && link.target().index() < node.numInputs() && (link.target().bitwidth() == 0 || link.target().bitwidth() == node.input(link.target().index()).bitwidth()))
                        return new Link(link.source(), node.input(link.target().index()));
                    return null;
                })
                .filter(Objects::nonNull)
                .toList();

        source.remove(selectedNode);
        source.add(node);
        links.forEach(source::add);
    }

    private void deleteSelectedNodes() {
        final var nodeIds = new int[ImNodes.numSelectedNodes()];
        ImNodes.getSelectedNodes(nodeIds);

        for (final var nodeId : nodeIds)
            source
                    .findNode(nodeId)
                    .ifPresent(source::remove);

        ImNodes.clearNodeSelection();
    }

    private void deleteSelectedLinks() {
        final var linkIds = new int[ImNodes.numSelectedLinks()];
        ImNodes.getSelectedLinks(linkIds);

        for (final var linkId : linkIds)
            source
                    .findLink(linkId)
                    .ifPresent(source::remove);

        ImNodes.clearLinkSelection();
    }

    private void handleMouse(final boolean isEditorHovered) {
        if (!ImGui.isMouseClicked(ImGuiMouseButton.Right))
            return;

        final var hoveredNodeId = ImNodes.getHoveredNode();
        final var hoveredLinkId = ImNodes.getHoveredLink();

        mouseX = ImGui.getMousePosX();
        mouseY = ImGui.getMousePosY();
        sourcePin = null;
        targetPin = null;

        if (hoveredNodeId != -1) {
            events.scheduleTask(nodePopup::open);
            source
                    .findNode(hoveredNodeId)
                    .ifPresent(node -> {
                        selectedNode = node;
                        selectedNode.select();
                    });
            return;
        }
        if (hoveredLinkId != -1) {
            events.scheduleTask(linkPopup::open);
            source
                    .findLink(hoveredLinkId)
                    .ifPresent(Link::select);
            return;
        }
        if (isEditorHovered)
            events.scheduleTask(editorPopup::open);
    }

    private void handleLinkDropped() {
        final var pinId = new ImInt();

        if (!ImNodes.isLinkDropped(pinId))
            return;

        events.scheduleTask(addDirectPopup::open);

        mouseX = ImGui.getMousePosX();
        mouseY = ImGui.getMousePosY();
        sourcePin = null;
        targetPin = null;

        source
                .findPin(pinId.get())
                .ifPresent(pin -> sourcePin = pin);
    }

    private void handleLinkCreated() {
        final var sourceId = new ImInt();
        final var targetId = new ImInt();

        if (!ImNodes.isLinkCreated(sourceId, targetId))
            return;

        source
                .findPin(sourceId.get())
                .ifPresent(pin -> sourcePin = pin);
        source
                .findPin(targetId.get())
                .ifPresent(pin -> targetPin = pin);

        if (sourcePin == null || targetPin == null)
            throw new RTException();

        if (sourcePin.bitwidth() != targetPin.bitwidth())
            return;

        source
                .findLink(targetPin.output()
                        ? sourcePin
                        : targetPin)
                .ifPresent(source::remove);

        source.add(sourcePin.output()
                ? new Link(sourcePin, targetPin)
                : new Link(targetPin, sourcePin));

        sourcePin = null;
        targetPin = null;
    }

    private void selectAll() {
        source
                .nodes()
                .filter(Node::notSelected)
                .forEach(Node::select);
        source
                .links()
                .filter(Link::notSelected)
                .forEach(Link::select);
    }

    private void deleteSelection() {
        if (ImNodes.numSelectedNodes() == 0 && ImNodes.numSelectedLinks() == 0)
            return;

        if (ImNodes.numSelectedLinks() == 0) {
            deleteSelectedNodes();
            return;
        }

        if (ImNodes.numSelectedNodes() == 0) {
            deleteSelectedLinks();
            return;
        }

        events.scheduleTask(deletePopup::open);
    }
}
