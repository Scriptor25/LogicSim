package io.scriptor.graph;

import imgui.ImGui;
import imgui.extension.imnodes.ImNodes;
import imgui.extension.imnodes.flag.ImNodesMiniMapLocation;
import imgui.flag.ImGuiKey;
import imgui.flag.ImGuiMouseButton;
import imgui.type.ImInt;
import io.scriptor.imgui.Array;
import io.scriptor.imgui.Element;
import io.scriptor.imgui.Layout;
import io.scriptor.util.Range;

import java.util.*;

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

    private Graph clipboard;

    public NodeEditor(final Layout root, final String id) {
        super(root, id);

        attributes.sorted(Comparator.comparing(Attribute::label));
        attributes.sorted(Comparator.comparing(Attribute::output));

        blueprints.sorted(Comparator.comparing(Blueprint::label));

        getEvents().register(getParentId() + ".node-context.copy.click", this::onNodeContextCopyClick);
        getEvents().register(getParentId() + ".node-context.cut.click", this::onNodeContextCutClick);
        getEvents().register(getParentId() + ".node-context.duplicate.click", this::onNodeContextDuplicateClick);
        getEvents().register(getParentId() + ".node-context.delete.click", this::onNodeContextDeleteClick);
        getEvents().register(getParentId() + ".link-context.delete.click", this::onLinkContextDeleteClick);
        getEvents().register(getParentId() + ".editor-context.paste.click", this::onEditorContextPasteClick);
        getEvents().register(getParentId() + ".editor-context.add.click", this::onEditorContextAddClick);
        getEvents().register(getParentId() + ".editor-context.clear.click", args -> graph.clear());
        getEvents().register(getParentId() + ".add-context.attributes.select", this::onAddContextAttributesSelect);
        getEvents().register(getParentId() + ".add-context.blueprints.select", this::onAddContextBlueprintsSelect);
        getEvents().register(getParentId() + ".delete-context.nodes.click", args -> deleteSelectedNodes());
        getEvents().register(getParentId() + ".delete-context.links.click", args -> deleteSelectedLinks());
    }

    public void graph(final Graph graph) {
        this.graph = graph;
    }

    public Range<Attribute> attributes() {
        return attributes;
    }

    public void blueprints(final Collection<?> blueprints) {
        this.blueprints.collection(blueprints);
    }

    private void onNodeContextCopyClick(final Object... args) {
        final var nodeIds = new int[ImNodes.numSelectedNodes()];
        ImNodes.getSelectedNodes(nodeIds);

        final List<INode> nodes = new ArrayList<>();
        if (!ImNodes.isNodeSelected(hoveredNode.id()))
            nodes.add(hoveredNode);
        for (final var nodeId : nodeIds)
            graph.findNode(nodeId).ifPresent(nodes::add);

        if (clipboard != null) clipboard.clear();
        clipboard = graph.copy(nodes.toArray(INode[]::new));
    }

    private void onNodeContextCutClick(final Object... args) {
        onNodeContextCopyClick(args);

        if (!ImNodes.isNodeSelected(hoveredNode.id()))
            graph.remove(hoveredNode);
        deleteSelectedNodes();
    }

    private void onNodeContextDuplicateClick(final Object... args) {
        onNodeContextCopyClick(args);
        graph.paste(clipboard);
    }

    private void onNodeContextDeleteClick(final Object... args) {
        deleteSelectedNodes();

        if (!ImNodes.isNodeSelected(hoveredNode.id()))
            graph.remove(hoveredNode);
    }

    private void onLinkContextDeleteClick(final Object... args) {
        deleteSelectedLinks();

        if (!ImNodes.isLinkSelected(hoveredLink.id()))
            graph.remove(hoveredLink);
    }

    private void onEditorContextPasteClick(final Object... args) {
        if (clipboard != null)
            graph.paste(clipboard);
    }

    private void onEditorContextAddClick(final Object... args) {
        getEvents().schedule(() -> ImGui.openPopup(getParentId() + ".add-context"));

        attributes.clearTempFilters();
        blueprints.clearTempFilters();
    }

    private void onAddContextAttributesSelect(final Object... args) {
        final var attribute = (Attribute) args[1];

        final INode node;
        if (attribute.output()) node = new Output(UUID.randomUUID(), attribute);
        else node = new Input(UUID.randomUUID(), attribute);

        onAddNode(node);
    }

    private void onAddContextBlueprintsSelect(final Object... args) {
        final var blueprint = (Blueprint) args[1];
        final var node = new Node(UUID.randomUUID(), blueprint);

        onAddNode(node);
    }

    private void onAddNode(final INode node) {
        graph.add(node);
        ImNodes.setNodeScreenSpacePos(node.id(), mouseX, mouseY);

        if (source != null) {
            target = source.output() ? node.input(0) : node.output(0);

            if (target.output()) graph.findLinks(source).forEach(graph::remove);
            else graph.findLinks(target).forEach(graph::remove);

            final Link link;
            if (source.output()) link = new Link(UUID.randomUUID(), source, target);
            else link = new Link(UUID.randomUUID(), target, source);
            graph.add(link);

            source = null;
            target = null;
        }

        attributes.clearTempFilters();
        blueprints.clearTempFilters();
    }

    @Override
    protected void onStart() {
        final Array attributeArray = getRoot().findElement(getParentId() + ".add-context.attributes");
        attributeArray.setRange(attributes);

        final Array blueprintArray = getRoot().findElement(getParentId() + ".add-context.blueprints");
        blueprintArray.setRange(blueprints);
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
        handleKeyboard();

        handleLinkDropped();
        handleLinkCreated();
    }

    private void deleteSelectedNodes() {
        final var nodeIds = new int[ImNodes.numSelectedNodes()];
        ImNodes.getSelectedNodes(nodeIds);

        for (final var nodeId : nodeIds)
            graph.findNode(nodeId).ifPresent(graph::remove);

        ImNodes.clearNodeSelection();
    }

    private void deleteSelectedLinks() {
        final var linkIds = new int[ImNodes.numSelectedLinks()];
        ImNodes.getSelectedLinks(linkIds);

        for (final var linkId : linkIds)
            graph.findLink(linkId).ifPresent(graph::remove);

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
                getEvents().schedule(() -> ImGui.openPopup(getParentId() + ".node-context"));
                graph.findNode(hoveredNodeId).ifPresent(node -> hoveredNode = node);
            } else if (hoveredLinkId != -1) {
                getEvents().schedule(() -> ImGui.openPopup(getParentId() + ".link-context"));
                graph.findLink(hoveredLinkId).ifPresent(link -> hoveredLink = link);
            } else if (isEditorHovered) {
                getEvents().schedule(() -> ImGui.openPopup(getParentId() + ".editor-context"));
            }
        }
    }

    private void handleKeyboard() {
        if (ImGui.isKeyReleased(ImGuiKey.Delete)) {
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

            getEvents().schedule(() -> ImGui.openPopup(getParentId() + ".delete-context"));
        }
    }

    private void handleLinkDropped() {
        final var pinId = new ImInt();

        if (ImNodes.isLinkDropped(pinId)) {
            getEvents().schedule(() -> ImGui.openPopup(getParentId() + ".add-context"));

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
            graph.findPin(sourceId.get()).ifPresent(pin -> source = pin);
            graph.findPin(targetId.get()).ifPresent(pin -> target = pin);

            if (target.output()) graph.findLinks(source).forEach(graph::remove);
            else graph.findLinks(target).forEach(graph::remove);

            final Link link;
            if (source.output()) link = new Link(UUID.randomUUID(), source, target);
            else link = new Link(UUID.randomUUID(), target, source);
            graph.add(link);

            source = null;
            target = null;
        }
    }
}
