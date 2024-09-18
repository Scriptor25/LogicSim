package io.scriptor.nodes;

import imgui.ImGui;
import imgui.extension.imnodes.ImNodes;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

public class Node {

    private final UUID id;
    private String label;
    private final List<Pin> pins = new ArrayList<>();

    public Node(final String label) {
        this.id = UUID.randomUUID();
        this.label = label;
    }

    public int getId() {
        return id.hashCode();
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(final String label) {
        this.label = label;
    }

    public Stream<Pin> getPins() {
        return pins.stream();
    }

    public Pin createIn(final String label) {
        final var pin = new Pin(label, false);
        pins.add(pin);
        return pin;
    }

    public Pin createOut(final String label) {
        final var pin = new Pin(label, true);
        pins.add(pin);
        return pin;
    }

    public void show() {
        ImNodes.beginNode(getId());

        ImNodes.beginNodeTitleBar();
        ImGui.textUnformatted(getLabel());
        ImNodes.endNodeTitleBar();

        final var in = getPins()
                .filter(Pin::isInput)
                .toArray(Pin[]::new);
        final var out = getPins()
                .filter(Pin::isOutput)
                .toArray(Pin[]::new);

        int i = 0;
        for (; i < Math.min(in.length, out.length); ++i) {
            in[i].show();
            ImGui.sameLine();
            out[i].show();
        }
        for (; i < in.length; ++i) in[i].show();
        for (; i < out.length; ++i) out[i].show();

        ImNodes.endNode();
    }

    public void deletePin(final Pin pin) {
        pins.remove(pin);
    }

    public Optional<Pin> findPin(final int id) {
        return getPins().filter(pin -> pin.getId() == id).findAny();
    }
}
