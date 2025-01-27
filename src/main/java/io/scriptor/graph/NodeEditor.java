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

import imgui.ImGui;
import imgui.ImVec2;
import imgui.extension.imnodes.ImNodes;
import imgui.extension.imnodes.flag.ImNodesMiniMapLocation;
import imgui.flag.ImGuiMouseButton;
import imgui.type.ImInt;
import io.scriptor.event.StringPayload;
import io.scriptor.imgui.Array;
import io.scriptor.imgui.Element;
import io.scriptor.imgui.Layout;
import io.scriptor.event.KeyPayload;
import io.scriptor.util.Range;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static io.scriptor.util.ID.ID_CLIPBOARD_GET;
import static io.scriptor.util.ID.ID_CLIPBOARD_SET;

public class NodeEditor extends Element {

    private Graph graph;

    private final Range<Attribute> attributes = new Range<>(Attribute.class);
    private final Range<Blueprint> blueprints = new Range<>(Blueprint.class);

    private INode hoveredNode;
    private Link hoveredLink;
    private Pin source;
    private Pin target;
    private float mouseX;
    private float mouseY;

    public NodeEditor(final @NotNull Layout root, final @NotNull String id) {
        super(root, id);

        attributes.sorted(Comparator.comparing(Attribute::label));
        attributes.sorted(Comparator.comparing(Attribute::output));
        blueprints.sorted(Comparator.comparing(Blueprint::label));

        getEvents().registerEvent(getParentId() + ".node-context.copy.click", this::onNodeCopy);
        getEvents().registerEvent(getParentId() + ".node-context.cut.click", this::onNodeCut);
        getEvents().registerEvent(getParentId() + ".node-context.duplicate.click", this::onNodeDuplicate);
        getEvents().registerEvent(getParentId() + ".node-context.replace.click", this::onNodeReplace);
        getEvents().registerEvent(getParentId() + ".node-context.delete.click", this::onNodeDelete);
        getEvents().registerEvent(getParentId() + ".link-context.delete.click", this::onLinkDelete);
        getEvents().registerEvent(getParentId() + ".editor-context.paste.click", this::onEditorPaste);
        getEvents().registerEvent(getParentId() + ".editor-context.add.click", this::onEditorAdd);
        getEvents().registerEvent(getParentId() + ".editor-context.clear.click", args -> graph.clear());
        getEvents().registerEvent(getParentId() + ".add-context.attributes.select", this::onAddAttribute);
        getEvents().registerEvent(getParentId() + ".add-context.blueprints.select", this::onAddBlueprint);
        getEvents().registerEvent(getParentId() + ".replace-context.attributes.select", this::onReplaceAttribute);
        getEvents().registerEvent(getParentId() + ".replace-context.blueprints.select", this::onReplaceBlueprint);
        getEvents().registerEvent(getParentId() + ".delete-context.nodes.click", args -> deleteSelectedNodes());
        getEvents().registerEvent(getParentId() + ".delete-context.links.click", args -> deleteSelectedLinks());

        getEvents().<KeyPayload>registerEvent("key.delete.press", payload -> getEvents().scheduleTask(this, this::deleteSelection));
        getEvents().<KeyPayload>registerEvent("key.a.press+control", payload -> getEvents().scheduleTask(this, this::selectAll));
        getEvents().<KeyPayload>registerEvent("key.c.press+control", payload -> getEvents().callService(ID_CLIPBOARD_SET, new StringPayload(copy())));
        getEvents().<KeyPayload>registerEvent("key.d.press+control", payload -> duplicate());
        getEvents().<KeyPayload>registerEvent("key.v.press+control", payload -> paste(getEvents().callService(ID_CLIPBOARD_GET)));
        getEvents().<KeyPayload>registerEvent("key.x.press+control", payload -> getEvents().callService(ID_CLIPBOARD_SET, new StringPayload(cut())));
    }

    public void graph(final @NotNull Graph graph) {
        this.graph = graph;
    }

    public @NotNull Range<Attribute> attributes() {
        return attributes;
    }

    public void blueprints(final @NotNull Collection<Blueprint> blueprints) {
        this.blueprints.collection(blueprints);
    }

    public @NotNull String copy() {
        final var nodeIds = new int[ImNodes.numSelectedNodes()];
        ImNodes.getSelectedNodes(nodeIds);

        final List<INode> nodes = new ArrayList<>();
        if (hoveredNode != null && hoveredNode.notSelected())
            nodes.add(hoveredNode);
        for (final var nodeId : nodeIds)
            graph
                    .findNode(nodeId)
                    .ifPresent(nodes::add);
        final var nodeArray = nodes.toArray(INode[]::new);

        final List<Link> links = new ArrayList<>();
        graph
                .links()
                .filter(link -> !link.usesNoneOf(nodeArray))
                .forEach(links::add);
        final var linkArray = links.toArray(Link[]::new);

        final var data = new StringBuilder();
        data.append("Java Logic Sim\n");
        data
                .append(nodeArray.length)
                .append('\n');
        for (final var node : nodeArray)
            data
                    .append(node.getString())
                    .append('\n');
        data
                .append(linkArray.length)
                .append('\n');
        for (final var link : linkArray)
            data
                    .append(link.getString(nodeArray))
                    .append('\n');

        return data.toString();
    }

    public void duplicate() {
        paste(copy());
    }

    public @NotNull String cut() {
        final var data = copy();

        if (hoveredNode != null && hoveredNode.notSelected())
            graph.remove(hoveredNode);
        deleteSelectedNodes();

        return data;
    }

    public void paste(final @NotNull String data) {
        final var lines = data
                .lines()
                .toArray(String[]::new);
        int i = 0;

        final var magic = lines[i++];
        if (!magic.equals("Java Logic Sim"))
            return;

        final var nodeArrayLength = Integer.parseInt(lines[i++], 10);
        final var nodeArray = new INode[nodeArrayLength];
        for (int j = 0; j < nodeArrayLength; ++j) {
            final var node = INode.parse(graph, lines[i++]);
            nodeArray[j] = node;
            graph.add(node);
        }

        final var linkArrayLength = Integer.parseInt(lines[i++], 10);
        final var linkArray = new Link[linkArrayLength];
        for (int j = 0; j < linkArrayLength; ++j) {
            final var link = Link.parse(nodeArray, lines[i++]);
            linkArray[j] = link;
            graph.add(link);
        }

        getEvents().scheduleTask(this, () -> {
            ImNodes.clearNodeSelection();
            ImNodes.clearLinkSelection();

            for (final var node : nodeArray)
                node.select();
            for (final var link : linkArray)
                link.select();
        });
    }

    private void onNodeCopy() {
        getEvents().callService(ID_CLIPBOARD_SET, new StringPayload(copy()));
    }

    private void onNodeCut() {
        getEvents().callService(ID_CLIPBOARD_SET, new StringPayload(cut()));
    }

    private void onNodeDuplicate() {
        paste(copy());
    }

    private void onNodeReplace() {
        getEvents().scheduleTask(() -> ImGui.openPopup(getParentId() + ".replace-context"));

        attributes.clearTempFilters();
        blueprints.clearTempFilters();

        attributes.tempFilter(attribute -> !hoveredNode.uses(attribute));
        blueprints.tempFilter(blueprint -> !hoveredNode.uses(blueprint));
    }

    private void onNodeDelete() {
        deleteSelectedNodes();

        if (hoveredNode.notSelected())
            graph.remove(hoveredNode);
    }

    private void onLinkDelete() {
        deleteSelectedLinks();

        if (hoveredLink.notSelected())
            graph.remove(hoveredLink);
    }

    private void onEditorPaste() {
        paste(getEvents().callService(ID_CLIPBOARD_GET));
    }

    private void onEditorAdd() {
        getEvents().scheduleTask(() -> ImGui.openPopup(getParentId() + ".add-context"));

        attributes.clearTempFilters();
        blueprints.clearTempFilters();
    }

    private void onAddAttribute(final @NotNull Array.Payload<Attribute> payload) {
        final var attribute = payload.value();

        final INode node;
        if (attribute.output())
            node = new Output(UUID.randomUUID(), attribute);
        else
            node = new Input(UUID.randomUUID(), attribute);

        onAddNode(node);
    }

    private void onAddBlueprint(final @NotNull Array.Payload<Blueprint> payload) {
        final var blueprint = payload.value();
        final var node = new Node(UUID.randomUUID(), blueprint);

        onAddNode(node);
    }

    private void onAddNode(final @NotNull INode node) {
        graph.add(node);
        node.screenPosition(new ImVec2(mouseX, mouseY));

        if (source != null) {
            target = source.output()
                    ? node.input(0)
                    : node.output(0);

            if (target.output())
                graph
                        .findLink(source)
                        .ifPresent(graph::remove);
            else
                graph
                        .findLink(target)
                        .ifPresent(graph::remove);

            final Link link;
            if (source.output())
                link = new Link(UUID.randomUUID(), source, target);
            else
                link = new Link(UUID.randomUUID(), target, source);
            graph.add(link);

            source = null;
            target = null;
        }

        attributes.clearTempFilters();
        blueprints.clearTempFilters();
    }

    private void onReplaceAttribute(final @NotNull Array.Payload<Attribute> payload) {
        final var attribute = payload.value();

        final INode node;
        if (attribute.output())
            node = new Output(UUID.randomUUID(), attribute);
        else
            node = new Input(UUID.randomUUID(), attribute);

        onReplaceNode(node);
    }

    private void onReplaceBlueprint(final @NotNull Array.Payload<Blueprint> payload) {
        final var blueprint = payload.value();
        final var node = new Node(UUID.randomUUID(), blueprint);

        onReplaceNode(node);
    }

    private void onReplaceNode(final @NotNull INode node) {
        node.screenPosition(hoveredNode.screenPosition());

        final List<Link> newLinks = new ArrayList<>();

        graph
                .links()
                .filter(link -> link.uses(hoveredNode))
                .forEach(link -> {
                    if (link.source().node() == hoveredNode && link.source().index() < node.numOutputs()) {
                        newLinks.add(new Link(UUID.randomUUID(), node.output(link.source().index()), link.target()));
                    } else if (link.target().node() == hoveredNode && link.target().index() < node.numInputs()) {
                        newLinks.add(new Link(UUID.randomUUID(), link.source(), node.input(link.target().index())));
                    }
                });

        graph.remove(hoveredNode);
        graph.add(node);
        newLinks.forEach(graph::add);

        attributes.clearTempFilters();
        blueprints.clearTempFilters();
    }

    @Override
    protected void onStart() {
        getRoot()
                .findElement(getParentId() + ".add-context.attributes", Array.class)
                .ifPresent(array -> array.range(attributes));
        getRoot()
                .findElement(getParentId() + ".add-context.blueprints", Array.class)
                .ifPresent(array -> array.range(blueprints));
        getRoot()
                .findElement(getParentId() + ".replace-context.attributes", Array.class)
                .ifPresent(array -> array.range(attributes));
        getRoot()
                .findElement(getParentId() + ".replace-context.blueprints", Array.class)
                .ifPresent(array -> array.range(blueprints));
    }

    @Override
    protected void onShow() {
        ImNodes.beginNodeEditor();

        graph.exec();
        graph.show();

        final var isEditorHovered = ImNodes.isEditorHovered();

        ImNodes.miniMap(.2f, ImNodesMiniMapLocation.BottomRight);
        ImNodes.endNodeEditor();

        handleMouse(isEditorHovered);

        getEvents().runTasks(this);

        handleLinkDropped();
        handleLinkCreated();
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
        if (ImGui.isMouseClicked(ImGuiMouseButton.Right)) {
            final var hoveredNodeId = ImNodes.getHoveredNode();
            final var hoveredLinkId = ImNodes.getHoveredLink();

            mouseX = ImGui.getMousePosX();
            mouseY = ImGui.getMousePosY();
            source = null;
            target = null;

            if (hoveredNodeId != -1) {
                getEvents().scheduleTask(() -> ImGui.openPopup(getParentId() + ".node-context"));
                graph
                        .findNode(hoveredNodeId)
                        .ifPresent(node -> hoveredNode = node);
            } else if (hoveredLinkId != -1) {
                getEvents().scheduleTask(() -> ImGui.openPopup(getParentId() + ".link-context"));
                graph
                        .findLink(hoveredLinkId)
                        .ifPresent(link -> hoveredLink = link);
            } else if (isEditorHovered) {
                getEvents().scheduleTask(this, () -> ImGui.openPopup(getParentId() + ".editor-context"));
            }
        }
    }

    private void handleLinkDropped() {
        final var pinId = new ImInt();

        if (ImNodes.isLinkDropped(pinId)) {
            getEvents().scheduleTask(() -> ImGui.openPopup(getParentId() + ".add-context"));

            mouseX = ImGui.getMousePosX();
            mouseY = ImGui.getMousePosY();
            source = null;
            target = null;

            graph.findPin(pinId.get()).ifPresent(pin -> {
                source = pin;
                attributes.clearTempFilters();
                attributes.tempFilter(attribute -> pin.output() == attribute.output());
                blueprints.clearTempFilters();
                blueprints.tempFilter(blueprint -> pin.output() ? blueprint.function().numInputs() > 0 : blueprint.function().numOutputs() > 0);
            });
        }
    }

    private void handleLinkCreated() {
        final var sourceId = new ImInt();
        final var targetId = new ImInt();

        if (ImNodes.isLinkCreated(sourceId, targetId)) {
            graph
                    .findPin(sourceId.get())
                    .ifPresent(pin -> source = pin);
            graph
                    .findPin(targetId.get())
                    .ifPresent(pin -> target = pin);

            if (target.output())
                graph
                        .findLink(source)
                        .ifPresent(graph::remove);
            else
                graph
                        .findLink(target)
                        .ifPresent(graph::remove);

            final Link link;
            if (source.output())
                link = new Link(UUID.randomUUID(), source, target);
            else
                link = new Link(UUID.randomUUID(), target, source);
            graph.add(link);

            source = null;
            target = null;
        }
    }

    private void selectAll() {
        graph
                .nodes()
                .filter(INode::notSelected)
                .forEach(INode::select);
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

        getEvents().scheduleTask(() -> ImGui.openPopup(getParentId() + ".delete-context"));
    }
}
