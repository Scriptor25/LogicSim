package io.scriptor.imgui;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Objects;

public record Enumeration(@NotNull String id, @NotNull Entry @NotNull [] entries) {

    public record Entry(@NotNull String name, int value) {
    }

    @Override
    public boolean equals(final @Nullable Object object) {
        if (this == object) return true;
        if (null == object) return false;
        if (!(object instanceof Enumeration that)) return false;
        return Objects.equals(id, that.id) && Objects.deepEquals(entries, that.entries);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, Arrays.hashCode(entries));
    }

    @Override
    public String toString() {
        return "Enumeration{" +
                "id='" + id + '\'' +
                ", entries=" + Arrays.toString(entries) +
                '}';
    }
}
