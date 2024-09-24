package io.scriptor.imgui;

import imgui.ImColor;
import imgui.ImGui;

public class ColorEdit extends Element {

    private final String label;
    private final String event;
    private final float[] col = new float[3];

    public ColorEdit(final Layout root, final String id, final String label, final String event) {
        super(root, id);
        this.label = label;
        this.event = id + '.' + event;
    }

    public void color(final float r, final float g, final float b) {
        col[0] = r;
        col[1] = g;
        col[2] = b;
    }

    public void color(final int rgb) {
        final var s = 1.f / 255.f;
        col[0] = s * (rgb >> 16 & 0xff);
        col[1] = s * (rgb >> 8 & 0xff);
        col[2] = s * (rgb & 0xff);
    }

    public int color() {
        return ImColor.rgb(col[2], col[1], col[0]);
    }

    @Override
    protected void onShow() {
        if (ImGui.colorEdit3(label, col))
            getEvents().invoke(event, this, color());
    }
}
