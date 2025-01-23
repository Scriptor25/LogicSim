package io.scriptor.imgui;

import imgui.extension.imnodes.ImNodes;
import org.jetbrains.annotations.NotNull;

public class Minimap extends Element {

    public final boolean enable;
    public final float size;
    public final int location;

    public Minimap(final @NotNull Layout root,
                   final @NotNull String id,
                   final @NotNull Boolean enable,
                   final @NotNull Float size,
                   final @NotNull Integer location) {
        super(root, id);
        this.enable = enable;
        this.size = size;
        this.location = location;
    }

    @Override
    protected void onShow() {
        if (enable) ImNodes.miniMap(size, location);
    }
}
