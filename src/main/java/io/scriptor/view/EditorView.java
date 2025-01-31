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
import imgui.flag.ImGuiMouseButton;
import imgui.type.ImInt;
import io.scriptor.event.EventManager;
import io.scriptor.event.KeyPayload;
import io.scriptor.event.StringPayload;
import io.scriptor.graph.*;
import io.scriptor.util.Range;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import static io.scriptor.util.Constants.ID_CLIPBOARD_GET;
import static io.scriptor.util.Constants.ID_CLIPBOARD_SET;

public class EditorView extends View {

    private final ListView<Attribute> attributeView;
    private final ListView<Attribute> addAttributeView;
    private final ListView<Blueprint> addBlueprintView;
    private final ListView<Attribute> addDirectAttributeView;
    private final ListView<Blueprint> addDirectBlueprintView;
    private final ListView<Attribute> replaceAttributeView;
    private final ListView<Blueprint> replaceBlueprintView;
    private final TextInputView editLabelView;

    private final PopupView attributeContext;
    private final PopupView renameContext;
    private final PopupView nodeContext;
    private final PopupView linkContext;
    private final PopupView editorContext;
    private final PopupView addContext;
    private final PopupView addDirectContext;
    private final PopupView replaceContext;
    private final PopupView deleteContext;

    private final Blueprint instance;
    private final Graph source;

    private final ImNodesEditorContext imEditorContext;

    private Attribute selectedAttribute;
    private Node selectedNode;
    private Link selectedLink;
    private Pin sourcePin;
    private Pin targetPin;
    private float mouseX;
    private float mouseY;
    private boolean first = true;

    public EditorView(final @NotNull EventManager events, final @NotNull Blueprint instance) {
        super(events);

        this.instance = instance;
        this.source = instance.source();

        editLabelView = new TextInputView(events, label -> {
            if (!label.isEmpty())
                selectedAttribute.label().set(label, true);
            ImGui.closeCurrentPopup();
        });
        renameContext = new PopupView(events, editLabelView::show);
        attributeContext = new PopupView(events, () -> {
            if (ImGui.selectable("Rename")) {
                editLabelView.hint(selectedAttribute.label().get());
                events.scheduleTask(renameContext::open);
            }
            if (ImGui.selectable("Delete"))
                events.scheduleTask(this, this::onAttributeDelete);
        });
        attributeView = new ListView<>(
                events,
                new Range<>(source.attributes())
                        .sorted(Comparator.comparing(Attribute::output)),
                attribute -> {
                    selectedAttribute = attribute;
                    events.scheduleTask(attributeContext::open);
                },
                attribute -> {
                    ImGui.beginDisabled(attribute.output());
                    ImGui.checkbox("##powered", attribute.powered());
                    ImGui.endDisabled();
                    ImGui.sameLine();
                    return ImGui.selectable(attribute.label().get());
                });
        addAttributeView = new ListView<>(
                events,
                new Range<>(source.attributes())
                        .sorted(Comparator.comparing(Attribute::label))
                        .sorted(Comparator.comparing(Attribute::output)),
                this::onAddAttribute,
                attribute -> ImGui.selectable(attribute.label().get()));
        addBlueprintView = new ListView<>(
                events,
                new Range<>(source.context().blueprints())
                        .filter(blueprint -> blueprint != instance)
                        .sorted(Comparator.comparing(Blueprint::label)),
                this::onAddBlueprint,
                blueprint -> ImGui.selectable(blueprint.label().get()));
        addDirectAttributeView = new ListView<>(
                events,
                new Range<>(source.attributes())
                        .filter(attribute -> sourcePin != null)
                        .filter(attribute -> sourcePin.output() == attribute.output())
                        .sorted(Comparator.comparing(Attribute::label))
                        .sorted(Comparator.comparing(Attribute::output)),
                this::onAddAttribute,
                attribute -> ImGui.selectable(attribute.label().get()));
        addDirectBlueprintView = new ListView<>(
                events,
                new Range<>(source.context().blueprints())
                        .filter(blueprint -> sourcePin != null)
                        .filter(blueprint -> blueprint != instance)
                        .filter(blueprint -> sourcePin.output()
                                ? blueprint.function().numInputs() > 0
                                : blueprint.function().numOutputs() > 0)
                        .sorted(Comparator.comparing(Blueprint::label)),
                this::onAddBlueprint,
                blueprint -> ImGui.selectable(blueprint.label().get()));
        replaceAttributeView = new ListView<>(
                events,
                new Range<>(source.attributes())
                        .filter(attribute -> selectedNode != null)
                        .filter(attribute -> !selectedNode.uses(attribute))
                        .sorted(Comparator.comparing(Attribute::label))
                        .sorted(Comparator.comparing(Attribute::output)),
                this::onReplaceAttribute,
                attribute -> ImGui.selectable(attribute.label().get()));
        replaceBlueprintView = new ListView<>(
                events,
                new Range<>(source.context().blueprints())
                        .filter(blueprint -> selectedNode != null)
                        .filter(blueprint -> blueprint != instance)
                        .filter(blueprint -> !selectedNode.uses(blueprint))
                        .sorted(Comparator.comparing(Blueprint::label)),
                this::onReplaceBlueprint,
                blueprint -> ImGui.selectable(blueprint.label().get()));

        nodeContext = new PopupView(events, () -> {
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
        });
        linkContext = new PopupView(events, () -> {
            if (ImGui.selectable("Delete"))
                events.scheduleTask(this, this::onLinkDelete);
        });
        editorContext = new PopupView(events, () -> {
            if (ImGui.selectable("Add"))
                events.scheduleTask(this, this::onEditorAdd);
            if (ImGui.selectable("Paste"))
                events.scheduleTask(this, this::onEditorPaste);
            if (ImGui.selectable("Clear"))
                events.scheduleTask(this, this::onEditorClear);
        });
        addContext = new PopupView(events, () -> {
            addAttributeView.show();
            ImGui.separator();
            addBlueprintView.show();
        });
        addDirectContext = new PopupView(events, () -> {
            addDirectAttributeView.show();
            ImGui.separator();
            addDirectBlueprintView.show();
        });
        replaceContext = new PopupView(events, () -> {
            replaceAttributeView.show();
            ImGui.separator();
            replaceBlueprintView.show();
        });
        deleteContext = new PopupView(events, () -> {
            if (ImGui.selectable("Nodes"))
                events.scheduleTask(this::deleteSelectedNodes);
            if (ImGui.selectable("Links"))
                events.scheduleTask(this::deleteSelectedLinks);
        });

        events.<KeyPayload>registerEvent("key.delete.press", payload -> events.scheduleTask(this, this::deleteSelection));
        events.<KeyPayload>registerEvent("key.a.press+control", payload -> events.scheduleTask(this, this::selectAll));
        events.<KeyPayload>registerEvent("key.c.press+control", payload -> events.callService(ID_CLIPBOARD_SET, new StringPayload(copy())));
        events.<KeyPayload>registerEvent("key.d.press+control", payload -> duplicate());
        events.<KeyPayload>registerEvent("key.v.press+control", payload -> paste(events.callService(ID_CLIPBOARD_GET)));
        events.<KeyPayload>registerEvent("key.x.press+control", payload -> events.callService(ID_CLIPBOARD_SET, new StringPayload(cut())));

        imEditorContext = ImNodes.editorContextCreate();
    }

    @Override
    public void show() {
        if (!ImGui.begin("%s##%s".formatted(instance.label(), instance.uuid()))) {
            ImGui.end();
            return;
        }

        if (ImGui.beginChild("attributes", 200, 0)) {
            if (ImGui.button("Add Input"))
                events.scheduleTask(this, this::onAddInput);
            ImGui.sameLine();
            if (ImGui.button("Add Output"))
                events.scheduleTask(this, this::onAddOutput);
            attributeView.show();
        }
        ImGui.endChild();
        ImGui.sameLine();

        ImNodes.editorContextSet(imEditorContext);
        ImNodes.beginNodeEditor();

        if (first) {
            first = false;
            source.loadNodePositions();
        }

        source.exec();
        source.show();

        final var hovered = ImNodes.isEditorHovered();

        ImNodes.miniMap(.2f, ImNodesMiniMapLocation.BottomRight);
        ImNodes.endNodeEditor();

        handleMouse(hovered);

        handleLinkDropped();
        handleLinkCreated();

        events.runTasks(this);

        ImGui.end();

        attributeContext.show();
        renameContext.show();
        nodeContext.show();
        linkContext.show();
        editorContext.show();
        addContext.show();
        addDirectContext.show();
        replaceContext.show();
        deleteContext.show();
    }

    private @NotNull String copy() {
        final var nodeIds = new int[ImNodes.numSelectedNodes()];
        ImNodes.getSelectedNodes(nodeIds);

        final List<Node> nodes = new ArrayList<>();
        if (selectedNode != null && selectedNode.notSelected())
            nodes.add(selectedNode);
        for (final var nodeId : nodeIds)
            source
                    .findNode(nodeId)
                    .ifPresent(nodes::add);
        final var nodeArray = nodes.toArray(Node[]::new);

        final List<Link> links = new ArrayList<>();
        source
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
                    .append(node.asString())
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
            source.remove(selectedNode);
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
            final var node = Node.asNode(source, lines[i++]);
            nodeArray[j] = node;
            source.add(node);
        }

        final var linkArrayLength = Integer.parseInt(lines[i++], 10);
        final var linkArray = new Link[linkArrayLength];
        for (int j = 0; j < linkArrayLength; ++j) {
            final var link = Link.parseString(nodeArray, lines[i++]);
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

    private void onAddInput() {
        source.add(new Attribute("New Input", false));
    }

    private void onAddOutput() {
        source.add(new Attribute("New Output", true));
    }

    private void onAttributeDelete() {
        source.remove(selectedAttribute);
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
        events.scheduleTask(replaceContext::open);
    }

    private void onNodeDelete() {
        deleteSelectedNodes();

        if (selectedNode.notSelected())
            source.remove(selectedNode);
    }

    private void onLinkDelete() {
        deleteSelectedLinks();

        if (selectedLink.notSelected())
            source.remove(selectedLink);
    }

    private void onEditorPaste() {
        paste(events.callService(ID_CLIPBOARD_GET));
    }

    private void onEditorClear() {
        source.clear();
    }

    private void onEditorAdd() {
        events.scheduleTask(addContext::open);
    }

    private void onAddAttribute(final @NotNull Attribute attribute) {
        onAddNode(attribute.output()
                ? new OutputNode(UUID.randomUUID(), attribute)
                : new InputNode(UUID.randomUUID(), attribute));
    }

    private void onAddBlueprint(final @NotNull Blueprint blueprint) {
        onAddNode(new BlueprintNode(UUID.randomUUID(), blueprint));
    }

    private void onAddNode(final @NotNull Node node) {
        source.add(node);
        node.screenPosition(new ImVec2(mouseX, mouseY));

        if (sourcePin == null)
            return;

        targetPin = sourcePin.output()
                ? node.input(0)
                : node.output(0);

        source
                .findLink(targetPin.output()
                        ? sourcePin
                        : targetPin)
                .ifPresent(source::remove);

        source.add(sourcePin.output()
                ? new Link(UUID.randomUUID(), sourcePin, targetPin)
                : new Link(UUID.randomUUID(), targetPin, sourcePin));

        sourcePin = null;
        targetPin = null;
    }

    private void onReplaceAttribute(final @NotNull Attribute attribute) {
        onReplaceNode(attribute.output()
                ? new OutputNode(UUID.randomUUID(), attribute)
                : new InputNode(UUID.randomUUID(), attribute));
    }

    private void onReplaceBlueprint(final @NotNull Blueprint blueprint) {
        onReplaceNode(new BlueprintNode(UUID.randomUUID(), blueprint));
    }

    private void onReplaceNode(final @NotNull Node node) {
        node.screenPosition(selectedNode.screenPosition());

        final List<Link> newLinks = new ArrayList<>();
        source
                .links()
                .filter(link -> link.uses(selectedNode))
                .forEach(link -> {
                    if (link.source().node() == selectedNode && link.source().index() < node.numOutputs()) {
                        newLinks.add(new Link(UUID.randomUUID(), node.output(link.source().index()), link.target()));
                    } else if (link.target().node() == selectedNode && link.target().index() < node.numInputs()) {
                        newLinks.add(new Link(UUID.randomUUID(), link.source(), node.input(link.target().index())));
                    }
                });

        source.remove(selectedNode);
        source.add(node);
        newLinks.forEach(source::add);
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
            events.scheduleTask(nodeContext::open);
            source
                    .findNode(hoveredNodeId)
                    .ifPresent(node -> selectedNode = node);
        } else if (hoveredLinkId != -1) {
            events.scheduleTask(linkContext::open);
            source
                    .findLink(hoveredLinkId)
                    .ifPresent(link -> selectedLink = link);
        } else if (isEditorHovered) {
            events.scheduleTask(editorContext::open);
        }
    }

    private void handleLinkDropped() {
        final var pinId = new ImInt();

        if (!ImNodes.isLinkDropped(pinId))
            return;

        events.scheduleTask(addDirectContext::open);

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

        source
                .findLink(targetPin.output()
                        ? sourcePin
                        : targetPin)
                .ifPresent(source::remove);

        source.add(sourcePin.output()
                ? new Link(UUID.randomUUID(), sourcePin, targetPin)
                : new Link(UUID.randomUUID(), targetPin, sourcePin));

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

        events.scheduleTask(deleteContext::open);
    }
}
