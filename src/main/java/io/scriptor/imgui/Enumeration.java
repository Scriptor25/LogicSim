package io.scriptor.imgui;

public record Enumeration(String id, Entry[] entries) {

    public record Entry(String name, int value) {
    }
}
