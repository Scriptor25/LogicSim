package io.scriptor.imgui;

import io.scriptor.util.IYamlNode;
import io.scriptor.util.RTException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Objects;

public record Component(
        @NotNull String id,
        @NotNull Class<?> clazz,
        @NotNull Field @NotNull [] fields,
        @NotNull IYamlNode elementsYaml
) {

    public record Field(
            @NotNull String name,
            @NotNull String type,
            boolean array,
            boolean hasDefault,
            @NotNull Object defaultValue
    ) {

        public Field index(final int i) {
            if (!array) throw new RTException("cannot index into non-array field value");
            return new Field(name + '[' + i + ']', type, false, false, new Object());
        }
    }

    @Override
    public boolean equals(final @Nullable Object object) {
        if (this == object) return true;
        if (null == object) return false;
        if (!(object instanceof Component component)) return false;
        return Objects.equals(id, component.id)
                && Objects.equals(clazz, component.clazz)
                && Objects.equals(elementsYaml, component.elementsYaml)
                && Objects.deepEquals(fields, component.fields);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, clazz, Arrays.hashCode(fields), elementsYaml);
    }

    @Override
    public String toString() {
        return "Component{" +
                "id='" + id + '\'' +
                ", clazz=" + clazz +
                ", fields=" + Arrays.toString(fields) +
                ", elementsYaml=" + elementsYaml +
                '}';
    }
}
