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
import imgui.flag.ImGuiCond;
import imgui.flag.ImGuiFocusedFlags;
import imgui.flag.ImGuiMouseButton;
import imgui.type.ImBoolean;
import imgui.type.ImInt;
import io.scriptor.context.Context;
import io.scriptor.event.EventManager;
import io.scriptor.event.StringPayload;
import io.scriptor.graph.*;
import io.scriptor.util.RTException;
import io.scriptor.util.Range;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static io.scriptor.util.Constants.ID_CLIPBOARD_GET;
import static io.scriptor.util.Constants.ID_CLIPBOARD_SET;

public class EditorView extends View {

    private static boolean showFullAttribute(final @NotNull Attribute attribute) {
        ImGui.beginDisabled(attribute.output());
        ImGui.checkbox("##powered", attribute.powered());
        ImGui.endDisabled();
        ImGui.sameLine();
        return ImGui.selectable(attribute.label().get());
    }

    private static boolean showAttribute(final @NotNull Attribute attribute) {
        return ImGui.selectable(attribute.label().get());
    }

    private static boolean showBlueprint(final @NotNull Blueprint blueprint) {
        return ImGui.selectable(blueprint.label().get());
    }

    private final ListView<Attribute> attributeListView;
    private final ListView<Attribute> addAttributeListView;
    private final ListView<Blueprint> addBlueprintListView;
    private final ListView<Attribute> addDirectAttributeListView;
    private final ListView<Blueprint> addDirectBlueprintListView;
    private final ListView<Attribute> replaceAttributeListView;
    private final ListView<Blueprint> replaceBlueprintListView;
    private final TextInputView labelTextInputView;

    private final PopupView attributePopupView;
    private final PopupView setLabelPopupView;
    private final PopupView nodePopupView;
    private final PopupView linkPopupView;
    private final PopupView editorPopupView;
    private final PopupView addPopupView;
    private final PopupView addDirectPopupView;
    private final PopupView replacePopupView;
    private final PopupView deletePopupView;

    private final ImBoolean open = new ImBoolean(true);

    private final Context context;
    private final Blueprint instance;
    private final Graph graph;

    private final ImNodesEditorContext imEditorContext;

    private Attribute selectedAttribute;
    private Node selectedNode;
    private Link selectedLink;
    private Pin sourcePin;
    private Pin targetPin;
    private float mouseX;
    private float mouseY;
    private boolean first = true;

    public EditorView(final @NotNull EventManager events,
                      final @NotNull Context context,
                      final @NotNull Blueprint instance) {
        super(events);

        this.context = context;
        this.instance = instance;
        this.graph = instance.source();

        labelTextInputView = new TextInputView(events, this::onLabelEnter);
        attributeListView = new ListView<>(
                events,
                new Range<>(graph.attributes())
                        .sorted(Comparator.comparing(Attribute::output)),
                this::onAttributeSelect,
                EditorView::showFullAttribute);
        addAttributeListView = new ListView<>(
                events,
                new Range<>(graph.attributes())
                        .sorted(Comparator.comparing(Attribute::label))
                        .sorted(Comparator.comparing(Attribute::output)),
                this::onAddAttribute,
                EditorView::showAttribute);
        addBlueprintListView = new ListView<>(
                events,
                new Range<>(graph.context().blueprints())
                        .filter(blueprint -> blueprint != instance)
                        .filter(blueprint -> !blueprint.usesRecursive(instance))
                        .sorted(Comparator.comparing(Blueprint::label)),
                this::onAddBlueprint,
                EditorView::showBlueprint);
        addDirectAttributeListView = new ListView<>(
                events,
                new Range<>(graph.attributes())
                        .filter(attribute -> sourcePin != null)
                        .filter(attribute -> sourcePin.output() == attribute.output())
                        .sorted(Comparator.comparing(Attribute::label))
                        .sorted(Comparator.comparing(Attribute::output)),
                this::onAddAttribute,
                EditorView::showAttribute);
        addDirectBlueprintListView = new ListView<>(
                events,
                new Range<>(graph.context().blueprints())
                        .filter(blueprint -> sourcePin != null)
                        .filter(blueprint -> blueprint != instance)
                        .filter(blueprint -> !blueprint.usesRecursive(instance))
                        .filter(blueprint -> sourcePin.output()
                                ? blueprint.numInputs() > 0
                                : blueprint.numOutputs() > 0)
                        .sorted(Comparator.comparing(Blueprint::label)),
                this::onAddBlueprint,
                EditorView::showBlueprint);
        replaceAttributeListView = new ListView<>(
                events,
                new Range<>(graph.attributes())
                        .filter(attribute -> selectedNode != null)
                        .filter(attribute -> !selectedNode.uses(attribute))
                        .sorted(Comparator.comparing(Attribute::label))
                        .sorted(Comparator.comparing(Attribute::output)),
                this::onReplaceAttribute,
                EditorView::showAttribute);
        replaceBlueprintListView = new ListView<>(
                events,
                new Range<>(graph.context().blueprints())
                        .filter(blueprint -> selectedNode != null)
                        .filter(blueprint -> blueprint != instance)
                        .filter(blueprint -> !blueprint.usesRecursive(instance))
                        .filter(blueprint -> !selectedNode.uses(blueprint))
                        .sorted(Comparator.comparing(Blueprint::label)),
                this::onReplaceBlueprint,
                EditorView::showBlueprint);

        attributePopupView = new PopupView(events, this::showAttributeContext);
        setLabelPopupView = new PopupView(events, this::showLabelContext);
        nodePopupView = new PopupView(events, this::showNodeContext);
        linkPopupView = new PopupView(events, this::showLinkContext);
        editorPopupView = new PopupView(events, this::showEditorContext);
        addPopupView = new PopupView(events, this::showAddContext);
        addDirectPopupView = new PopupView(events, this::showAddDirectContext);
        replacePopupView = new PopupView(events, this::showReplaceContext);
        deletePopupView = new PopupView(events, this::showDeleteContext);

        events.registerEvent("key.delete.press", this::onKeyDelete);
        events.registerEvent("key.a.press+control", this::onKeyCtrlA);
        events.registerEvent("key.c.press+control", this::onKeyCtrlC);
        events.registerEvent("key.d.press+control", this::onKeyCtrlD);
        events.registerEvent("key.v.press+control", this::onKeyCtrlV);
        events.registerEvent("key.x.press+control", this::onKeyCtrlX);

        imEditorContext = ImNodes.editorContextCreate();
    }

    @Override
    public void show() {
        if (!open.get()) {
            events.callVoidService("blueprint.close", new BlueprintView.Payload(instance));
            return;
        }

        ImGui.setNextWindowSize(400, 300, ImGuiCond.FirstUseEver);
        if (!ImGui.begin("%s###%s".formatted(instance.label(), instance.uuid()), open)) {
            ImGui.end();
            return;
        }

        if (ImGui.beginChild("attributes", 200, 0)) {
            if (ImGui.button("Add Input"))
                events.scheduleTask(this, this::onAddInput);
            ImGui.sameLine();
            if (ImGui.button("Add Output"))
                events.scheduleTask(this, this::onAddOutput);
            attributeListView.show();
        }
        ImGui.endChild();
        ImGui.sameLine();

        ImNodes.editorContextSet(imEditorContext);
        ImNodes.beginNodeEditor();

        if (first) {
            first = false;
            graph.loadNodePositions();
        }

        graph.compile();
        graph.exec();
        graph.show();

        final var hovered = ImNodes.isEditorHovered();

        ImNodes.miniMap(.2f, ImNodesMiniMapLocation.BottomRight);
        ImNodes.endNodeEditor();

        handleMouse(hovered);

        handleLinkDropped();
        handleLinkCreated();

        events.runTasks(this);

        ImGui.end();

        attributePopupView.show();
        setLabelPopupView.show();
        nodePopupView.show();
        linkPopupView.show();
        editorPopupView.show();
        addPopupView.show();
        addDirectPopupView.show();
        replacePopupView.show();
        deletePopupView.show();
    }

    private void onLabelEnter(final @NotNull String label) {
        if (!label.isEmpty())
            selectedAttribute.label().set(label, true);
        ImGui.closeCurrentPopup();
    }

    private void onAttributeSelect(final @NotNull Attribute attribute) {
        selectedAttribute = attribute;
        events.scheduleTask(attributePopupView::open);
    }

    private void showAttributeContext() {
        if (ImGui.selectable("Set Label")) {
            labelTextInputView.value(selectedAttribute.label().get());
            events.scheduleTask(setLabelPopupView::open);
        }
        if (ImGui.selectable("Delete"))
            events.scheduleTask(this, this::onAttributeDelete);
    }

    private void showLabelContext() {
        labelTextInputView.show();
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
        if (ImGui.selectable("Delete"))
            events.scheduleTask(this, this::onNodeDelete);
    }

    private void showLinkContext() {
        if (ImGui.selectable("Delete"))
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
        addAttributeListView.show();
        ImGui.separator();
        addBlueprintListView.show();
    }

    private void showAddDirectContext() {
        addDirectAttributeListView.show();
        ImGui.separator();
        addDirectBlueprintListView.show();
    }

    private void showReplaceContext() {
        replaceAttributeListView.show();
        ImGui.separator();
        replaceBlueprintListView.show();
    }

    private void showDeleteContext() {
        if (ImGui.selectable("Nodes"))
            events.scheduleTask(this::onNodeDelete);
        if (ImGui.selectable("Links"))
            events.scheduleTask(this::onLinkDelete);
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
                events.callService(ID_CLIPBOARD_SET, new StringPayload(copy()));
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
                events.callService(ID_CLIPBOARD_SET, new StringPayload(cut()));
        });
    }

    private @NotNull String copy() {
        final var nodeIds = new int[ImNodes.numSelectedNodes()];
        ImNodes.getSelectedNodes(nodeIds);

        final List<Node> nodes = new ArrayList<>();
        if (selectedNode != null && selectedNode.notSelected())
            nodes.add(selectedNode);
        for (final var nodeId : nodeIds)
            graph
                    .findNode(nodeId)
                    .ifPresent(nodes::add);
        final var nodeArray = nodes.toArray(Node[]::new);

        final List<Link> links = new ArrayList<>();
        graph
                .links()
                .filter(link -> !link.usesNoneOf(nodeArray))
                .forEach(links::add);
        final var linkArray = links.toArray(Link[]::new);

        final var data = new StringBuilder();
        data
                .append("JLS")
                .append('\n')
                .append(nodeArray.length)
                .append('\n');
        for (final var node : nodeArray)
            data
                    .append(node.string())
                    .append('\n');
        data
                .append(linkArray.length)
                .append('\n');
        for (final var link : linkArray)
            data
                    .append(link.string(nodeArray))
                    .append('\n');
        return data.toString();
    }

    private void duplicate() {
        paste(copy());
    }

    private @NotNull String cut() {
        final var data = copy();

        if (selectedNode != null && selectedNode.notSelected())
            graph.remove(selectedNode);
        deleteSelectedNodes();

        return data;
    }

    private void paste(final @NotNull String data) {
        final var lines = data
                .lines()
                .toArray(String[]::new);
        int i = 0;

        final var magic = lines[i++];
        if (!magic.equals("JLS"))
            return;

        final var nodeArrayLength = Integer.parseInt(lines[i++], 10);
        final var nodeArray = new Node[nodeArrayLength];
        for (int j = 0; j < nodeArrayLength; ++j) {
            final var node = Node.asNode(graph, lines[i++]);
            nodeArray[j] = node;
            graph.add(node);
        }

        final var linkArrayLength = Integer.parseInt(lines[i++], 10);
        final var linkArray = new Link[linkArrayLength];
        for (int j = 0; j < linkArrayLength; ++j) {
            final var link = Link.parseString(nodeArray, lines[i++]);
            linkArray[j] = link;
            graph.add(link);
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

    private void onAddInput() {
        graph.add(new Attribute("New Input", false));
    }

    private void onAddOutput() {
        graph.add(new Attribute("New Output", true));
    }

    private void onAttributeDelete() {
        graph.remove(selectedAttribute);
    }

    private void onNodeCopy() {
        events.callService(ID_CLIPBOARD_SET, new StringPayload(copy()));
    }

    private void onNodeCut() {
        events.callService(ID_CLIPBOARD_SET, new StringPayload(cut()));
    }

    private void onNodeDuplicate() {
        paste(copy());
    }

    private void onNodeReplace() {
        events.scheduleTask(replacePopupView::open);
    }

    private void onNodeDelete() {
        deleteSelectedNodes();

        if (selectedNode != null && selectedNode.notSelected())
            graph.remove(selectedNode);
    }

    private void onLinkDelete() {
        deleteSelectedLinks();

        if (selectedLink != null && selectedLink.notSelected())
            graph.remove(selectedLink);
    }

    private void onEditorPaste() {
        paste(events.callService(ID_CLIPBOARD_GET));
    }

    private void onEditorClear() {
        graph.clear();
    }

    private void onEditorAdd() {
        events.scheduleTask(addPopupView::open);
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
        graph.add(node);
        node.screenPosition(new ImVec2(mouseX, mouseY));

        if (sourcePin == null)
            return;

        targetPin = sourcePin.output()
                ? node.input(0)
                : node.output(0);

        graph
                .findLink(targetPin.output()
                        ? sourcePin
                        : targetPin)
                .ifPresent(graph::remove);

        graph.add(sourcePin.output()
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

        final var links = graph
                .links()
                .filter(link -> link.uses(selectedNode))
                .map(link -> {
                    if (link.source().node() == selectedNode && link.source().index() < node.numOutputs()) {
                        return new Link(node.output(link.source().index()), link.target());
                    } else if (link.target().node() == selectedNode && link.target().index() < node.numInputs()) {
                        return new Link(link.source(), node.input(link.target().index()));
                    }
                    throw new RTException();
                })
                .toList();

        graph.remove(selectedNode);
        graph.add(node);
        links.forEach(graph::add);
    }

    private void deleteSelectedNodes() {
        final var nodeIds = new int[ImNodes.numSelectedNodes()];
        ImNodes.getSelectedNodes(nodeIds);

        for (final var nodeId : nodeIds)
            graph
                    .findNode(nodeId)
                    .ifPresent(graph::remove);

        ImNodes.clearNodeSelection();
    }

    private void deleteSelectedLinks() {
        final var linkIds = new int[ImNodes.numSelectedLinks()];
        ImNodes.getSelectedLinks(linkIds);

        for (final var linkId : linkIds)
            graph
                    .findLink(linkId)
                    .ifPresent(graph::remove);

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
            events.scheduleTask(nodePopupView::open);
            graph
                    .findNode(hoveredNodeId)
                    .ifPresent(node -> selectedNode = node);
        } else if (hoveredLinkId != -1) {
            events.scheduleTask(linkPopupView::open);
            graph
                    .findLink(hoveredLinkId)
                    .ifPresent(link -> selectedLink = link);
        } else if (isEditorHovered) {
            events.scheduleTask(editorPopupView::open);
        }
    }

    private void handleLinkDropped() {
        final var pinId = new ImInt();

        if (!ImNodes.isLinkDropped(pinId))
            return;

        events.scheduleTask(addDirectPopupView::open);

        mouseX = ImGui.getMousePosX();
        mouseY = ImGui.getMousePosY();
        sourcePin = null;
        targetPin = null;

        graph
                .findPin(pinId.get())
                .ifPresent(pin -> sourcePin = pin);
    }

    private void handleLinkCreated() {
        final var sourceId = new ImInt();
        final var targetId = new ImInt();

        if (!ImNodes.isLinkCreated(sourceId, targetId))
            return;

        graph
                .findPin(sourceId.get())
                .ifPresent(pin -> sourcePin = pin);
        graph
                .findPin(targetId.get())
                .ifPresent(pin -> targetPin = pin);

        graph
                .findLink(targetPin.output()
                        ? sourcePin
                        : targetPin)
                .ifPresent(graph::remove);

        graph.add(sourcePin.output()
                ? new Link(sourcePin, targetPin)
                : new Link(targetPin, sourcePin));

        sourcePin = null;
        targetPin = null;
    }

    private void selectAll() {
        graph
                .nodes()
                .filter(Node::notSelected)
                .forEach(Node::select);
        graph
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

        events.scheduleTask(deletePopupView::open);
    }
}
