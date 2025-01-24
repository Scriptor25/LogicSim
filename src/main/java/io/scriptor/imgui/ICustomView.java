package io.scriptor.imgui;

@FunctionalInterface
public interface ICustomView<T> {

    boolean show(final T content);
}
