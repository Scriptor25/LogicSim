package io.scriptor.util;

import java.util.*;

public interface IYamlNode extends Iterable<IYamlNode> {

    static IYamlNode from(final Object value) {
        return from(null, value);
    }

    static IYamlNode from(final String key, final Map<?, ?> map) {
        final Map<String, IYamlNode> value = new HashMap<>();
        map.forEach((k, v) -> value.put((String) k, from((String) k, v)));
        return new MapNode(key, value);
    }

    static IYamlNode from(final String key, final List<?> list) {
        return new ListNode(key, list.stream().map(IYamlNode::from).toList());
    }

    static IYamlNode from(final String key, final Object value) {
        if (value == null)
            return new EmptyNode(key);
        if (value instanceof Map<?, ?> map)
            return from(key, map);
        if (value instanceof List<?> list)
            return from(key, list);
        return new DataNode(key, value);
    }

    record MapNode(String key, Map<String, IYamlNode> value) implements IYamlNode {

        @Override
        public boolean notEmpty() {
            return !value.isEmpty();
        }

        @Override
        public void put(final String key, final IYamlNode node) {
            value.put(key, node);
        }

        @Override
        public void append(final IYamlNode node) {
            value.put(node.key(), node);
        }

        @Override
        public IYamlNode get(final String key) {
            if (!value.containsKey(key))
                return new EmptyNode(key);
            return value.get(key);
        }

        @Override
        public int size() {
            return value.size();
        }

        @Override
        public Iterator<IYamlNode> iterator() {
            return value.values().iterator();
        }
    }

    record ListNode(String key, List<IYamlNode> value) implements IYamlNode {

        @Override
        public boolean notEmpty() {
            return !value.isEmpty();
        }

        @Override
        public void append(final IYamlNode node) {
            value.add(node);
        }

        @Override
        public IYamlNode get(final int index) {
            if (index < 0 || index >= value.size())
                return new EmptyNode(null);
            return value.get(index);
        }

        @Override
        public int size() {
            return value.size();
        }

        @Override
        public Iterator<IYamlNode> iterator() {
            return value.iterator();
        }
    }

    record DataNode(String key, Object value) implements IYamlNode {

        @Override
        public boolean notEmpty() {
            return true;
        }

        @Override
        public <T> T as(Class<T> clazz) {
            if (clazz.isInstance(value))
                return clazz.cast(value);
            return null;
        }

        @Override
        public <T> T as(Class<T> clazz, T defaultValue) {
            if (clazz.isInstance(value))
                return clazz.cast(value);
            return defaultValue;
        }
    }

    record EmptyNode(String key) implements IYamlNode {
    }

    String key();

    default boolean notEmpty() {
        return false;
    }

    default void put(final String key, final IYamlNode node) {
    }

    default void append(final IYamlNode node) {
    }

    default IYamlNode get(final String key) {
        return new EmptyNode(key);
    }

    default IYamlNode get(final int index) {
        return new EmptyNode(null);
    }

    default <T> T as(final Class<T> clazz) {
        return null;
    }

    default <T> T as(final Class<T> clazz, final T defaultValue) {
        return defaultValue;
    }

    default int size() {
        return 0;
    }

    @Override
    default Iterator<IYamlNode> iterator() {
        return Collections.emptyIterator();
    }
}
