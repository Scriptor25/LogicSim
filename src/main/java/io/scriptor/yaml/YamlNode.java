package io.scriptor.yaml;

import java.util.*;

public class YamlNode implements Iterable<YamlNode> {

    private enum Type {
        MAP,
        LIST,
        VALUE
    }

    public static YamlNode fromMap(final Map<?, ?> map) {
        final var node = new YamlNode(Type.MAP);
        map.forEach((key, value) -> {
            if (value instanceof Map<?, ?> v)
                node.put(key, fromMap(v));
            else if (value instanceof List<?> v)
                node.put(key, fromList(v));
            else
                node.put(key, new YamlNode(Type.VALUE, value));
        });
        return node;
    }

    public static YamlNode fromList(final List<?> list) {
        final var node = new YamlNode(Type.LIST);
        list.forEach(value -> {
            if (value instanceof Map<?, ?> v)
                node.append(fromMap(v));
            else if (value instanceof List<?> v)
                node.append(fromList(v));
            else
                node.append(new YamlNode(Type.VALUE, value));
        });
        return node;
    }

    private final Type type;
    private final Object value;
    private final List<YamlNode> childrenList = new ArrayList<>();
    private final Map<Object, YamlNode> childrenMap = new HashMap<>();

    private YamlNode(final Type type) {
        this(type, null);
    }

    private YamlNode(final Type type, final Object value) {
        this.type = type;
        this.value = value;
    }

    public boolean isEmpty() {
        return switch (type) {
            case MAP -> childrenMap.isEmpty();
            case LIST -> childrenList.isEmpty();
            case VALUE -> value == null;
        };
    }

    public void put(final Object key, final YamlNode node) {
        if (type != Type.MAP) throw new IllegalStateException();
        childrenMap.put(key, node);
    }

    public void append(final YamlNode node) {
        if (type != Type.LIST) throw new IllegalStateException();
        childrenList.add(node);
    }

    public YamlNode get(final Object key) {
        if (type != Type.MAP) throw new IllegalStateException();
        return childrenMap.computeIfAbsent(key, k -> new YamlNode(Type.VALUE));
    }

    public YamlNode get(final int index) {
        if (type != Type.LIST) throw new IllegalStateException();
        return childrenList.get(index);
    }

    public <T> T as(final Class<T> c) {
        if (type != Type.VALUE) throw new IllegalStateException();
        return c.cast(value);
    }

    public <T> T as(final Class<T> c, final T def) {
        if (type != Type.VALUE) throw new IllegalStateException();
        if (value == null) return def;
        return c.cast(value);
    }

    public int count() {
        if (type == Type.MAP) return childrenMap.size();
        if (type == Type.LIST) return childrenList.size();
        throw new IllegalStateException();
    }

    @Override
    public Iterator<YamlNode> iterator() {
        if (type == Type.MAP) return childrenMap.values().iterator();
        if (type == Type.LIST) return childrenList.iterator();
        throw new IllegalStateException();
    }
}
