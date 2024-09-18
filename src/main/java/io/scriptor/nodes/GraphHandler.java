package io.scriptor.nodes;

import imgui.ImGui;
import imgui.extension.imnodes.ImNodes;
import imgui.flag.ImGuiInputTextFlags;
import imgui.flag.ImGuiKey;
import imgui.flag.ImGuiMouseButton;
import imgui.type.ImInt;
import imgui.type.ImString;

public class GraphHandler {

    private static final String PIN_CONTEXT = "pin-context";
    private static final String NODE_CONTEXT = "node-context";
    private static final String LINK_CONTEXT = "link-context";
    private static final String EDITOR_CONTEXT = "editor-context";
    private static final String CREATE_LINK_CONTEXT = "create-link-context";
    private static final String RENAME_CONTEXT = "rename-context";
    private static final String DELETE_CONTEXT = "delete-context";
    private static final String PIN_ID = "pin-id";
    private static final String NODE_ID = "node-id";
    private static final String LINK_ID = "link-id";
    private static final String ITEM_TYPE = "item-type";
    private static final String ITEM_ID = "item-id";

    private static final int ITEM_TYPE_PIN = 1;
    private static final int ITEM_TYPE_NODE = 2;

    private static final ImString label = new ImString();

    public static void show(final Graph graph, final boolean isEditorHovered) {

        handleLinkCreated(graph);
        handleLinkDropped();

        handleMouseClickRight(isEditorHovered);
        handleDeletePressed(graph);

        showPinContext(graph);
        showNodeContext(graph);
        showLinkContext(graph);
        showEditorContext(graph);
        showCreateLinkContext(graph);
        showRenameContext(graph);
        showDeleteContext(graph);
    }

    public static void handleMouseClickRight(final boolean isEditorHovered) {
        final var hoveredPinId = ImNodes.getHoveredPin();
        final var hoveredNodeId = ImNodes.getHoveredNode();
        final var hoveredLinkId = ImNodes.getHoveredLink();

        if (ImGui.isMouseClicked(ImGuiMouseButton.Right)) {
            if (hoveredPinId != -1) {
                ImGui.openPopup(PIN_CONTEXT);
                ImGui.getStateStorage().setInt(ImGui.getID(PIN_ID), hoveredPinId);
            } else if (hoveredNodeId != -1) {
                ImGui.openPopup(NODE_CONTEXT);
                ImGui.getStateStorage().setInt(ImGui.getID(NODE_ID), hoveredNodeId);
            } else if (hoveredLinkId != -1) {
                ImGui.openPopup(LINK_CONTEXT);
                ImGui.getStateStorage().setInt(ImGui.getID(LINK_ID), hoveredLinkId);
            } else if (isEditorHovered) {
                ImGui.openPopup(EDITOR_CONTEXT);
            }
        }
    }

    public static void handleLinkCreated(final Graph graph) {
        final var sourceNodeId = new ImInt();
        final var sourcePinId = new ImInt();
        final var targetNodeId = new ImInt();
        final var targetPinId = new ImInt();

        if (ImNodes.isLinkCreated(sourceNodeId, sourcePinId, targetNodeId, targetPinId))
            graph.createLink(sourceNodeId.get(), sourcePinId.get(), targetNodeId.get(), targetPinId.get());
    }

    public static void handleLinkDropped() {
        final var pinId = new ImInt();

        if (ImNodes.isLinkDropped(pinId)) {
            ImGui.openPopup(CREATE_LINK_CONTEXT);
            ImGui.getStateStorage().setInt(ImGui.getID(PIN_ID), pinId.get());
        }
    }

    public static void handleDeletePressed(final Graph graph) {
        if (ImGui.isKeyPressed(ImGuiKey.Delete)) {
            if (ImNodes.numSelectedNodes() == 1 && ImNodes.numSelectedLinks() == 0) {
                final var nodeId = new int[1];
                ImNodes.getSelectedNodes(nodeId);
                graph.findNode(nodeId[0]).ifPresent(graph::deleteNode);
                ImNodes.clearNodeSelection();
            } else if (ImNodes.numSelectedNodes() == 0 && ImNodes.numSelectedLinks() == 1) {
                final var linkId = new int[1];
                ImNodes.getSelectedLinks(linkId);
                graph.findLink(linkId[0]).ifPresent(graph::deleteLink);
                ImNodes.clearLinkSelection();
            } else {
                ImGui.openPopup(DELETE_CONTEXT);
            }
        }
    }

    public static void showPinContext(final Graph graph) {
        boolean rename = false;

        final var pinId = ImGui.getStateStorage().getInt(ImGui.getID(PIN_ID));
        final var pin = graph.findPin(pinId);

        if (pin.isPresent() && ImGui.beginPopup(PIN_CONTEXT)) {
            if (ImGui.button("Rename")) {
                rename = true;
                ImGui.closeCurrentPopup();
            }
            ImGui.endPopup();
        }

        if (rename) {
            label.set(pin.get().getLabel());
            ImGui.openPopup(RENAME_CONTEXT);
            ImGui.getStateStorage().setInt(ImGui.getID(ITEM_TYPE), ITEM_TYPE_PIN);
            ImGui.getStateStorage().setInt(ImGui.getID(ITEM_ID), pinId);
        }
    }

    public static void showNodeContext(final Graph graph) {
        boolean rename = false;

        final var hoveredNodeId = ImGui.getStateStorage().getInt(ImGui.getID(NODE_ID));
        final var hoveredNode = graph.findNode(hoveredNodeId);

        if (ImGui.beginPopup(NODE_CONTEXT)) {
            final var nodeCount = ImNodes.numSelectedNodes();
            final var nodeIds = new int[nodeCount];
            ImNodes.getSelectedNodes(nodeIds);

            if (ImGui.button("Rename")) {
                rename = true;
                ImNodes.clearNodeSelection();
                ImGui.closeCurrentPopup();
            }

            if (ImGui.button("Delete")) {
                for (final var nodeId : nodeIds)
                    graph.findNode(nodeId).ifPresent(graph::deleteNode);
                if (!ImNodes.isNodeSelected(hoveredNodeId) && hoveredNode.isPresent())
                    graph.deleteNode(hoveredNode.get());
                ImNodes.clearNodeSelection();
                ImGui.closeCurrentPopup();
            }

            ImGui.endPopup();
        }

        if (hoveredNode.isPresent() && rename) {
            label.set(hoveredNode.get().getLabel());
            ImGui.openPopup(RENAME_CONTEXT);
            ImGui.getStateStorage().setInt(ImGui.getID(ITEM_TYPE), ITEM_TYPE_NODE);
            ImGui.getStateStorage().setInt(ImGui.getID(ITEM_ID), hoveredNodeId);
        }
    }

    public static void showLinkContext(final Graph graph) {

        final var hoveredLinkId = ImGui.getStateStorage().getInt(ImGui.getID(LINK_ID));
        final var hoveredLink = graph.findLink(hoveredLinkId);

        if (ImGui.beginPopup(LINK_CONTEXT)) {
            final var linkCount = ImNodes.numSelectedLinks();
            final var linkIds = new int[linkCount];
            ImNodes.getSelectedLinks(linkIds);

            if (ImGui.button("Delete")) {
                for (final var linkId : linkIds)
                    graph.findLink(linkId).ifPresent(graph::deleteLink);
                if (!ImNodes.isLinkSelected(hoveredLinkId) && hoveredLink.isPresent())
                    graph.deleteLink(hoveredLink.get());
                ImNodes.clearLinkSelection();
                ImGui.closeCurrentPopup();
            }
            ImGui.endPopup();
        }
    }

    public static void showEditorContext(final Graph graph) {
        if (ImGui.beginPopup(EDITOR_CONTEXT)) {
            if (ImGui.button("Create Node")) {
                final var node = graph.createNode("New");
                ImNodes.setNodeScreenSpacePos(node.getId(), ImGui.getMousePosX(), ImGui.getMousePosY());
                ImGui.closeCurrentPopup();
            }
            ImGui.endPopup();
        }
    }

    public static void showCreateLinkContext(final Graph graph) {
        final var pinId = ImGui.getStateStorage().getInt(ImGui.getID(PIN_ID));
        final var pin = graph.findPin(pinId);

        if (pin.isPresent() && ImGui.beginPopup(CREATE_LINK_CONTEXT)) {
            if (ImGui.button("Create Node")) {
                final var node = graph.createNode("New");

                final Pin target;
                if (pin.get().isOutput()) target = node.createIn("Input");
                else target = node.createOut("Output");

                graph.createLink(pin.get(), target);
                ImNodes.setNodeScreenSpacePos(node.getId(), ImGui.getMousePosX(), ImGui.getMousePosY());
                ImGui.closeCurrentPopup();
            }
            ImGui.endPopup();
        }
    }

    public static void showRenameContext(final Graph graph) {
        final var type = ImGui.getStateStorage().getInt(ImGui.getID(ITEM_TYPE));
        final var id = ImGui.getStateStorage().getInt(ImGui.getID(ITEM_ID));

        if (ImGui.beginPopup(RENAME_CONTEXT)) {
            ImGui.setKeyboardFocusHere();
            if (ImGui.inputText("Label", label, ImGuiInputTextFlags.EnterReturnsTrue)) {
                ImGui.setItemDefaultFocus();
                switch (type) {
                    case ITEM_TYPE_PIN -> graph.findPin(id).ifPresent(pin -> pin.setLabel(label.get()));
                    case ITEM_TYPE_NODE -> graph.findNode(id).ifPresent(node -> node.setLabel(label.get()));
                    default -> throw new IllegalStateException("Unexpected value: " + type);
                }
                ImGui.closeCurrentPopup();
            }
            ImGui.endPopup();
        }
    }

    public static void showDeleteContext(final Graph graph) {
        if (ImGui.beginPopup(DELETE_CONTEXT)) {
            ImGui.setKeyboardFocusHere();
            if (ImNodes.numSelectedNodes() != 0 && ImGui.button("Nodes")) {
                ImGui.setItemDefaultFocus();
                final var nodeIds = new int[ImNodes.numSelectedNodes()];
                ImNodes.getSelectedNodes(nodeIds);
                for (final var nodeId : nodeIds)
                    graph.findNode(nodeId).ifPresent(graph::deleteNode);
                ImNodes.clearNodeSelection();
                ImGui.closeCurrentPopup();
            }
            if (ImNodes.numSelectedLinks() != 0 && ImGui.button("Links")) {
                ImGui.setItemDefaultFocus();
                final var linkIds = new int[ImNodes.numSelectedLinks()];
                ImNodes.getSelectedLinks(linkIds);
                for (final var linkId : linkIds)
                    graph.findLink(linkId).ifPresent(graph::deleteLink);
                ImNodes.clearLinkSelection();
                ImGui.closeCurrentPopup();
            }
            ImGui.endPopup();
        }
    }

    private GraphHandler() {
    }
}
