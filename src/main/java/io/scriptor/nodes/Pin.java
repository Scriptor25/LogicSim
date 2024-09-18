package io.scriptor.nodes;

import imgui.ImGui;
import imgui.extension.imnodes.ImNodes;
import imgui.extension.imnodes.flag.ImNodesPinShape;

import java.util.UUID;

public class Pin {

    private final UUID id;
    private String label;
    private final boolean output;

    public Pin(final String label, final boolean output) {
        this.id = UUID.randomUUID();
        this.label = label;
        this.output = output;
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

    public boolean isOutput() {
        return output;
    }

    public boolean isInput() {
        return !output;
    }

    public void show() {
        if (output) {
            ImNodes.beginOutputAttribute(getId(), ImNodesPinShape.CircleFilled);
            ImGui.textUnformatted(getLabel());
            ImNodes.endOutputAttribute();
        } else {
            ImNodes.beginInputAttribute(getId(), ImNodesPinShape.CircleFilled);
            ImGui.textUnformatted(getLabel());
            ImNodes.endInputAttribute();
        }
    }
}
