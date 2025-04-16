package io.scriptor.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum TriState {

    NONE(null),
    FALSE(false),
    TRUE(true);

    public static @NotNull TriState valueOf(final @Nullable Boolean bool) {
        if (bool == null)
            return NONE;
        return bool ? TRUE : FALSE;
    }

    private final Boolean bool;

    TriState(final @Nullable Boolean bool) {
        this.bool = bool;
    }

    public @Nullable Boolean getBoolean() {
        return this.bool;
    }
}
