package io.scriptor.imgui;

import io.scriptor.util.YamlNode;

public record Component(String id, Class<?> clazz, Field[] fields, YamlNode elementsYaml) {

    public record Field(String name, String type, boolean array, Object def) {

        public Field index(final int i) {
            return new Field(name + '[' + i + ']', type, false, null);
        }
    }
}
