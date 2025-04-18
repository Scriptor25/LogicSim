package io.scriptor.util;

import org.jetbrains.annotations.NotNull;

public record Color(float r, float g, float b) {

    public static @NotNull Color mix(final @NotNull Color a, final @NotNull Color b, final float t) {
        final var at = new Color((1.0f - t) * a.r, (1.0f - t) * a.g, (1.0f - t) * a.b);
        final var bt = new Color(t * b.r, t * b.g, t * b.b);
        return new Color(at.r + bt.r, at.g + bt.g, at.b + bt.b);
    }

    public int asInt(final float alpha) {
        final var ir = (int) (Math.clamp(r, 0.0f, 0.999f) * 256.0f);
        final var ig = (int) (Math.clamp(g, 0.0f, 0.999f) * 256.0f);
        final var ib = (int) (Math.clamp(b, 0.0f, 0.999f) * 256.0f);
        final var ia = (int) (Math.clamp(alpha, 0.0f, 0.999f) * 256.0f);
        return ((ia & 0xff) << 24) | ((ib & 0xff) << 16) | ((ig & 0xff) << 8) | (ir & 0xff);
    }
}
