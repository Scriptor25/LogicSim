package io.scriptor.imgui.component;

import imgui.ImGui;
import imgui.extension.imnodes.ImNodes;
import imgui.flag.ImGuiKey;
import imgui.flag.ImGuiMouseButton;
import imgui.type.ImInt;
import io.scriptor.imgui.Element;
import io.scriptor.imgui.Layout;
import io.scriptor.nodes.Graph;
import io.scriptor.nodes.Pin;

import java.util.Arrays;
import java.util.Optional;

public class NodeEditor extends Element {

    private final Minimap minimap;
    private final Graph graph = new Graph();

    private int hoveredPinId;
    private int hoveredNodeId;
    private int hoveredLinkId;
    private int renameType;

    public NodeEditor(final Layout root, final String id, final Minimap minimap) {
        super(root, id);
        this.minimap = minimap;

        getEvents().register(getParentId() + ".pin-context.rename", this::onPinContextRename);
        getEvents().register(getParentId() + ".pin-context.delete", this::onPinContextDelete);
        getEvents().register(getParentId() + ".node-context.rename", this::onNodeContextRename);
        getEvents().register(getParentId() + ".node-context.input", this::onNodeContextInput);
        getEvents().register(getParentId() + ".node-context.output", this::onNodeContextOutput);
        getEvents().register(getParentId() + ".node-context.delete", this::onNodeContextDelete);
        getEvents().register(getParentId() + ".link-context.delete", this::onLinkContextDelete);
        getEvents().register(getParentId() + ".editor-context.create", this::onEditorContextCreate);
        getEvents().register(getParentId() + ".create-link-context.create", this::onCreateLinkContextCreate);
        getEvents().register(getParentId() + ".rename-context.enter", this::onRenameContextEnter);
        getEvents().register(getParentId() + ".delete-context.nodes", this::onDeleteContextNodes);
        getEvents().register(getParentId() + ".delete-context.links", this::onDeleteContextLinks);
    }

    public Minimap getMinimap() {
        return minimap;
    }

    public Graph getGraph() {
        return graph;
    }

    @Override
    public void show() {
        ImNodes.beginNodeEditor();
        graph.show();
        minimap.show();
        final var isEditorHovered = ImNodes.isEditorHovered();
        ImNodes.endNodeEditor();

        handleLinkCreated();
        handleLinkDropped();

        handleMouseClickRight(isEditorHovered);
        handleDeletePressed();
    }

    private void handleLinkCreated() {
        final var sourceNodeId = new ImInt();
        final var sourcePinId = new ImInt();
        final var targetNodeId = new ImInt();
        final var targetPinId = new ImInt();

        if (ImNodes.isLinkCreated(sourceNodeId, sourcePinId, targetNodeId, targetPinId))
            graph.createLink(sourceNodeId.get(), sourcePinId.get(), targetNodeId.get(), targetPinId.get());
    }

    private void handleLinkDropped() {
        final var pinId = new ImInt();

        if (ImNodes.isLinkDropped(pinId)) {
            hoveredPinId = pinId.get();
            ImGui.openPopup(getParentId() + ".create-link-context");
        }
    }

    private void handleMouseClickRight(final boolean isEditorHovered) {
        if (ImGui.isMouseClicked(ImGuiMouseButton.Right)) {
            hoveredPinId = ImNodes.getHoveredPin();
            hoveredNodeId = ImNodes.getHoveredNode();
            hoveredLinkId = ImNodes.getHoveredLink();

            if (hoveredPinId != -1) {
                ImGui.openPopup(getParentId() + ".pin-context");
            } else if (hoveredNodeId != -1) {
                ImGui.openPopup(getParentId() + ".node-context");
            } else if (hoveredLinkId != -1) {
                ImGui.openPopup(getParentId() + ".link-context");
            } else if (isEditorHovered) {
                ImGui.openPopup(getParentId() + ".editor-context");
            }
        }
    }

    private void handleDeletePressed() {
        if (ImGui.isKeyPressed(ImGuiKey.Delete)) {
            if (ImNodes.numSelectedLinks() == 0) {
                final var nodeIds = new int[ImNodes.numSelectedNodes()];
                ImNodes.getSelectedNodes(nodeIds);

                Arrays.stream(nodeIds)
                        .mapToObj(graph::findNode)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .forEach(graph::deleteNode);

                ImNodes.clearNodeSelection();
            } else if (ImNodes.numSelectedNodes() == 0) {
                final var linkIds = new int[ImNodes.numSelectedLinks()];
                ImNodes.getSelectedLinks(linkIds);

                Arrays.stream(linkIds)
                        .mapToObj(graph::findLink)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .forEach(graph::deleteLink);

                ImNodes.clearLinkSelection();
            } else {
                ImGui.openPopup(getParentId() + ".delete-context");
            }
        }
    }

    private void onPinContextRename(final Object... args) {
        getEvents().schedule(() -> {
            ImGui.openPopup(getParentId() + ".rename-context");
            renameType = 1;
        });

        ImGui.closeCurrentPopup();
    }

    private void onPinContextDelete(final Object... args) {
        graph.findPin(hoveredPinId).ifPresent(graph::deletePin);
        ImGui.closeCurrentPopup();
    }

    private void onNodeContextRename(final Object... args) {
        getEvents().schedule(() -> {
            ImGui.openPopup(getParentId() + ".rename-context");
            renameType = 2;
        });

        ImNodes.clearNodeSelection();
        ImGui.closeCurrentPopup();
    }

    private void onNodeContextInput(final Object... args) {
        final var hoveredNode = graph.findNode(hoveredNodeId);
        hoveredNode.ifPresent(node -> node.createIn("Input"));

        ImNodes.clearNodeSelection();
        ImGui.closeCurrentPopup();
    }

    private void onNodeContextOutput(final Object... args) {
        final var hoveredNode = graph.findNode(hoveredNodeId);
        hoveredNode.ifPresent(node -> node.createOut("Output"));

        ImNodes.clearNodeSelection();
        ImGui.closeCurrentPopup();
    }

    private void onNodeContextDelete(final Object... args) {
        final var hoveredNode = graph.findNode(hoveredNodeId);

        final var nodeIds = new int[ImNodes.numSelectedNodes()];
        ImNodes.getSelectedNodes(nodeIds);

        for (final var nodeId : nodeIds)
            graph.findNode(nodeId).ifPresent(graph::deleteNode);

        if (!ImNodes.isNodeSelected(hoveredNodeId) && hoveredNode.isPresent())
            graph.deleteNode(hoveredNode.get());

        ImNodes.clearNodeSelection();
        ImGui.closeCurrentPopup();
    }

    private void onLinkContextDelete(final Object... args) {
        final var hoveredLink = graph.findLink(hoveredLinkId);

        final var linkIds = new int[ImNodes.numSelectedLinks()];
        ImNodes.getSelectedLinks(linkIds);

        for (final var linkId : linkIds)
            graph.findLink(linkId).ifPresent(graph::deleteLink);

        if (!ImNodes.isLinkSelected(hoveredLinkId) && hoveredLink.isPresent())
            graph.deleteLink(hoveredLink.get());

        ImNodes.clearLinkSelection();
        ImGui.closeCurrentPopup();
    }

    private void onEditorContextCreate(final Object... args) {
        final var node = graph.createNode("Node");
        ImNodes.setNodeScreenSpacePos(node.getId(), ImGui.getMousePosX(), ImGui.getMousePosY());
        ImGui.closeCurrentPopup();
    }

    private void onCreateLinkContextCreate(final Object... args) {
        final var pin = graph.findPin(hoveredPinId);

        final var node = graph.createNode("Node");
        ImNodes.setNodeScreenSpacePos(node.getId(), ImGui.getMousePosX(), ImGui.getMousePosY());

        final Pin target;
        if (pin.get().isOutput()) target = node.createIn("Input");
        else target = node.createOut("Output");
        graph.createLink(pin.get(), target);

        ImGui.closeCurrentPopup();
    }

    private void onRenameContextEnter(final Object... args) {
        final InputText label = getRoot().findElement(getParentId() + ".rename-context.label");

        switch (renameType) {
            case 1 -> graph.findPin(hoveredPinId).ifPresent(pin -> pin.setLabel(label.get()));
            case 2 -> graph.findNode(hoveredNodeId).ifPresent(node -> node.setLabel(label.get()));
            default -> throw new IllegalStateException();
        }

        ImGui.closeCurrentPopup();
    }

    private void onDeleteContextNodes(final Object... args) {
        final var nodeIds = new int[ImNodes.numSelectedNodes()];
        ImNodes.getSelectedNodes(nodeIds);

        for (final var nodeId : nodeIds)
            graph.findNode(nodeId).ifPresent(graph::deleteNode);

        ImNodes.clearNodeSelection();
        ImGui.closeCurrentPopup();
    }

    private void onDeleteContextLinks(final Object... args) {
        final var linkIds = new int[ImNodes.numSelectedLinks()];
        ImNodes.getSelectedLinks(linkIds);

        for (final var linkId : linkIds)
            graph.findLink(linkId).ifPresent(graph::deleteLink);

        ImNodes.clearLinkSelection();
        ImGui.closeCurrentPopup();
    }
}
