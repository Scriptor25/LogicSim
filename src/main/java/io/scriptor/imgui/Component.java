package io.scriptor.imgui;

public record Component(String id, Class<?> clazz, Field[] fields) {

    public record Field(String name, String type, boolean array, Object def) {

        public Field index(final int i) {
            return new Field(name + '[' + i + ']', type, false, null);
        }
    }
}
