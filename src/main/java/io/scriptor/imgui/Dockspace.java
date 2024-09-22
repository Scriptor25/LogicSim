package io.scriptor.imgui;

import imgui.ImGui;
import imgui.ImGuiViewport;

public class Dockspace extends Element {

    public final int viewport;
    public final int flags;

    public Dockspace(final Layout root, final String id, final Integer viewport, final Integer[] flags) {
        super(root, id);
        this.viewport = viewport;
        int f = 0;
        for (final var flag : flags) f |= flag;
        this.flags = f;
    }

    @Override
    protected void onShow() {
        final ImGuiViewport v;
        if (viewport == 0) v = ImGui.getMainViewport();
        else if (viewport == 1) v = ImGui.getWindowViewport();
        else throw new IllegalStateException();

        ImGui.dockSpaceOverViewport(v, flags);
    }
}
