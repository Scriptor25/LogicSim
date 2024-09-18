package io.scriptor;

import io.scriptor.imgui.Component;
import io.scriptor.imgui.Element;
import io.scriptor.imgui.Enumeration;
import io.scriptor.imgui.Layout;
import io.scriptor.yaml.YamlNode;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static io.scriptor.MainApp.getLogger;

public class ResourceManager {

    private final Map<String, Component> components = new HashMap<>();
    private final Map<String, Enumeration> enumerations = new HashMap<>();
    private final Map<String, YamlNode> templates = new HashMap<>();

    public Component getComponent(final String id) {
        if (components.containsKey(id))
            return components.get(id);
        throw new IllegalStateException("no component with id '" + id + "'");
    }

    public Enumeration getEnumeration(final String id) {
        if (enumerations.containsKey(id))
            return enumerations.get(id);
        throw new IllegalStateException("no enumeration with id '" + id + "'");
    }

    public YamlNode getTemplate(final String id) {
        if (templates.containsKey(id))
            return templates.get(id);
        throw new IllegalStateException("no template with id '" + id + "'");
    }

    public YamlNode getYaml(final String name) {
        Map<?, ?> map = Collections.emptyMap();
        try (final var stream = ClassLoader.getSystemResourceAsStream(name)) {
            if (stream != null) map = new Yaml().loadAs(stream, Map.class);
        } catch (final IOException e) {
            getLogger().warning(e::getMessage);
        }
        return YamlNode.fromMap(map);
    }

    public void parse(final String name) {
        final var yaml = getYaml(name);
        parseUses(yaml);

        switch (yaml.get("type").as(String.class)) {
            case "component" -> parseComponent(yaml);
            case "enum" -> parseEnumeration(yaml);
            case "element" -> parseTemplate(yaml);
            default -> throw new IllegalStateException();
        }
    }

    public void parseUses(final YamlNode yaml) {
        final var usesYaml = yaml.get("uses");
        if (!usesYaml.isEmpty())
            for (final var useYaml : usesYaml) {
                final var use = useYaml.as(String.class);
                parse(use);
            }
    }

    public void parseComponent(final YamlNode yaml) {
        final var id = yaml.get("id").as(String.class);

        Class<?> clazz = null;
        try {
            final var className = yaml.get("class").as(String.class);
            clazz = ClassLoader.getSystemClassLoader().loadClass(className);
        } catch (final ClassNotFoundException e) {
            getLogger().warning(e::getMessage);
        }

        final var fieldsYaml = yaml.get("fields");
        final var fields = new Component.Field[fieldsYaml.count()];
        int i = 0;
        for (final var fieldYaml : fieldsYaml) {
            final var fieldName = fieldYaml.get("name").as(String.class);
            final var fieldType = fieldYaml.get("type").as(String.class);
            final var fieldArray = fieldYaml.get("array").as(Boolean.class, false);
            fields[i++] = new Component.Field(fieldName, fieldType, fieldArray);
        }

        components.put(id, new Component(id, clazz, fields));
    }

    public void parseEnumeration(final YamlNode yaml) {
        final var id = yaml.get("id").as(String.class);

        final var entriesYaml = yaml.get("entries");
        final var entries = new Enumeration.Entry[entriesYaml.count()];
        int i = 0;
        for (final var entryYaml : entriesYaml) {
            final var entryName = entryYaml.get("name").as(String.class);
            final var entryValue = entryYaml.get("value").as(Integer.class);
            entries[i++] = new Enumeration.Entry(entryName, entryValue);
        }

        enumerations.put(id, new Enumeration(id, entries));
    }

    public void parseTemplate(final YamlNode yaml) {
        final var id = yaml.get("id").as(String.class);
        final var contentYaml = yaml.get("content");
        templates.put(id, contentYaml);
    }

    public Layout parseLayout(final EventManager events, final String name) {
        final var yaml = getYaml(name);
        parseUses(yaml);

        final var elementsYaml = yaml.get("elements");
        final var elements = new Element[elementsYaml.count()];

        final var layout = new Layout(events, elements);

        int i = 0;
        for (final var elementYaml : elementsYaml)
            elements[i++] = parseElement(elementYaml, layout, null);

        return layout;
    }

    public Element parseElement(final YamlNode yaml, final Layout root, final String parentId) {
        if (!yaml.get("use").isEmpty()) {
            final var use = yaml.get("use").as(String.class);
            return parseElement(getTemplate(use), root, parentId);
        }

        final var type = yaml.get("type").as(String.class);
        final var id = yaml.get("id").as(String.class, "");

        return parseComponent(yaml, root, parentId, id, getComponent(type));
    }

    public Class<?> getClassForType(final String type) {
        switch (type) {
            case "element" -> {
                return Element.class;
            }
            case "int" -> {
                return Integer.class;
            }
            case "float" -> {
                return Float.class;
            }
            case "boolean" -> {
                return Boolean.class;
            }
            case "string" -> {
                return String.class;
            }
            default -> {
                if (enumerations.containsKey(type))
                    return Integer.class;

                return getComponent(type).clazz();
            }
        }
    }

    public Object parseField(final YamlNode yaml, final Layout root, final String parentId, final String id, final String type, final boolean array) {
        if (array) {
            final var clazz = getClassForType(type);
            final var values = (Object[]) Array.newInstance(clazz, yaml.count());
            int i = -1;
            for (final var subYaml : yaml)
                values[++i] = parseField(subYaml, root, parentId, id + "[" + i + "]", type, false);
            return values;
        }

        switch (type) {
            case "element" -> {
                return parseElement(yaml, root, parentId);
            }
            case "int" -> {
                return yaml.as(Number.class, 0).intValue();
            }
            case "float" -> {
                return yaml.as(Number.class, 0.f).floatValue();
            }
            case "boolean" -> {
                return yaml.as(Boolean.class, false);
            }
            case "string" -> {
                return yaml.as(String.class, "");
            }
            default -> {
                if (enumerations.containsKey(type)) {
                    final var enumeration = enumerations.get(type);
                    final var name = yaml.as(String.class);
                    return Arrays.stream(enumeration.entries())
                            .filter(entry -> entry.name().equals(name))
                            .map(Enumeration.Entry::value)
                            .findFirst()
                            .orElse(Integer.MAX_VALUE);
                }

                return parseComponent(yaml, root, parentId, id, getComponent(type));
            }
        }
    }

    public Element parseComponent(final YamlNode yaml, final Layout root, final String parentId, final String id, final Component component) {
        if (component.clazz() == null)
            throw new IllegalStateException("component uses non existent class");

        final var elementId = parentId == null ? id : (parentId + '.' + id);
        final var args = Stream.concat(
                Stream.of(root, elementId),
                Arrays.stream(component.fields()).map(field -> parseField(
                        yaml.get(field.name()),
                        root,
                        elementId,
                        field.name(),
                        field.type(),
                        field.array()
                ))
        ).toArray();

        try {
            final var ctor = component.clazz().getConstructor(
                    Arrays.stream(args)
                            .map(Object::getClass)
                            .toArray(Class[]::new)
            );
            final var instance = ctor.newInstance(args);
            return (Element) instance;
        } catch (final NoSuchMethodException |
                       InstantiationException |
                       IllegalAccessException |
                       InvocationTargetException e) {
            getLogger().warning(e::getMessage);
            throw new IllegalStateException();
        }
    }
}
