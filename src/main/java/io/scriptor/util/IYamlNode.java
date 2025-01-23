package io.scriptor.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public interface IYamlNode extends Iterable<IYamlNode> {

    static @NotNull IYamlNode from(final @Nullable Object value) {
        return from(null, value);
    }

    static IYamlNode from(final @Nullable String key, final @NotNull Map<?, ?> map) {
        final Map<String, IYamlNode> value = new HashMap<>();
        map.forEach((k, v) -> value.put((String) k, from((String) k, v)));
        return new MapNode(key, value);
    }

    static IYamlNode from(final @Nullable String key, final @NotNull List<?> list) {
        return new ListNode(key, list.stream().map(IYamlNode::from).toList());
    }

    static IYamlNode from(final @Nullable String key, final @Nullable Object value) {
        if (value == null)
            return new EmptyNode(key);
        if (value instanceof Map<?, ?> map)
            return from(key, map);
        if (value instanceof List<?> list)
            return from(key, list);
        return new DataNode(key, value);
    }

    record MapNode(@Nullable String key, Map<String, IYamlNode> value) implements IYamlNode {

        @Override
        public boolean notEmpty() {
            return !value.isEmpty();
        }

        @Override
        public void put(final String key, final @NotNull IYamlNode node) {
            value.put(key, node);
        }

        @Override
        public void append(final @NotNull IYamlNode node) {
            value.put(node.key(), node);
        }

        @Override
        public @NotNull IYamlNode get(final String key) {
            if (!value.containsKey(key))
                return new EmptyNode(key);
            return value.get(key);
        }

        @Override
        public int size() {
            return value.size();
        }

        @Override
        public @NotNull Iterator<IYamlNode> iterator() {
            return value.values().iterator();
        }
    }

    record ListNode(@Nullable String key, @NotNull List<IYamlNode> value) implements IYamlNode {

        @Override
        public boolean notEmpty() {
            return !value.isEmpty();
        }

        @Override
        public void append(final @NotNull IYamlNode node) {
            value.add(node);
        }

        @Override
        public @NotNull IYamlNode get(final int index) {
            if (index < 0 || index >= value.size())
                return new EmptyNode(null);
            return value.get(index);
        }

        @Override
        public int size() {
            return value.size();
        }

        @Override
        public @NotNull Iterator<IYamlNode> iterator() {
            return value.iterator();
        }
    }

    record DataNode(@Nullable String key, @NotNull Object value) implements IYamlNode {

        @Override
        public boolean notEmpty() {
            return true;
        }

        @Override
        public <T> @NotNull T as(final @NotNull Class<T> clazz) {
            if (clazz.isInstance(value))
                return clazz.cast(value);
            throw new RTException("cannot cast value of type '%s' to type '%s'", value.getClass(), clazz);
        }

        @Override
        public @NotNull <T> T as(final @NotNull Class<T> clazz, final @NotNull T defaultValue) {
            if (clazz.isInstance(value))
                return clazz.cast(value);
            return defaultValue;
        }
    }

    record EmptyNode(@Nullable String key) implements IYamlNode {
    }

    @Nullable String key();

    default boolean notEmpty() {
        return false;
    }

    default void put(final @Nullable String key, final @NotNull IYamlNode node) {
    }

    default void append(final @NotNull IYamlNode node) {
    }

    default @NotNull IYamlNode get(final @Nullable String key) {
        return new EmptyNode(key);
    }

    default @NotNull IYamlNode get(final int index) {
        return new EmptyNode(null);
    }

    default @NotNull <T> T as(final @NotNull Class<T> clazz) {
        throw new RTException("cannot cast emptiness to type '%s'", clazz);
    }

    default @NotNull <T> T as(final @NotNull Class<T> clazz, final @NotNull T defaultValue) {
        return defaultValue;
    }

    default int size() {
        return 0;
    }

    @Override
    default @NotNull Iterator<IYamlNode> iterator() {
        return Collections.emptyIterator();
    }
}
