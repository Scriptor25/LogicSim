package io.scriptor.imgui;

import imgui.extension.imnodes.ImNodes;

public class Minimap extends Element {

    public final boolean enable;
    public final float size;
    public final int location;

    public Minimap(final Layout root, final String id, final Boolean enable, final Float size, final Integer location) {
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
