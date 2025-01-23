package io.scriptor.imgui;

import imgui.ImGui;
import imgui.ImGuiViewport;
import io.scriptor.util.RTException;
import org.jetbrains.annotations.NotNull;

public class Dockspace extends Element {

    public final int viewport;
    public final int flags;

    public Dockspace(final @NotNull Layout root, final @NotNull String id, final @NotNull Integer viewport, final @NotNull Integer @NotNull [] flags) {
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
        else throw new RTException("invalid viewport id '%d'", viewport);

        ImGui.dockSpaceOverViewport(v, flags);
    }
}
