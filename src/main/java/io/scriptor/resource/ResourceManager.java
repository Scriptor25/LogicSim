/*
 * This file is part of https://github.com/Scriptor25/LogicSim
 *
 * Copyright (C) 2025  Felix Schreiber
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see https://www.gnu.org/licenses/.
 */
package io.scriptor.resource;

import io.scriptor.event.EventManager;
import io.scriptor.imgui.Component;
import io.scriptor.imgui.Element;
import io.scriptor.imgui.Enumeration;
import io.scriptor.imgui.Layout;
import io.scriptor.util.IYamlNode;
import io.scriptor.util.RTException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Stream;

public class ResourceManager {

    private static final String STRING_ENUM_COLON = "enum:";
    private static final String STRING_ELEMENT = "element";

    private final Map<String, Component> components = new HashMap<>();
    private final Map<String, Enumeration> enumerations = new HashMap<>();
    private final Map<String, IYamlNode> templates = new HashMap<>();

    public @NotNull Component getComponent(final @NotNull String id) {
        if (components.containsKey(id))
            return components.get(id);

        parse("component/" + id + ".yml");
        if (components.containsKey(id))
            return components.get(id);

        throw new RTException("no component registered for id '%s'", id);
    }

    public void putComponent(final @NotNull String id, final @NotNull Component component) {
        if (components.containsKey(id))
            throw new RTException("overriding already registered component with id '%s'", id);
        components.put(id, component);
    }

    public @NotNull Enumeration getEnumeration(final @NotNull String id) {
        if (enumerations.containsKey(id))
            return enumerations.get(id);

        parse("enum/" + id.replace(STRING_ENUM_COLON, "") + ".yml");
        if (enumerations.containsKey(id))
            return enumerations.get(id);

        throw new RTException("no enumeration registered for id '%s'", id);
    }

    public void putEnumeration(final @NotNull String id, final @NotNull Enumeration enumeration) {
        if (enumerations.containsKey(id))
            throw new RTException("overriding already registered enumeration with id '%s'", id);
        enumerations.put(id, enumeration);
    }

    public @NotNull IYamlNode getTemplate(final @NotNull String id) {
        if (templates.containsKey(id))
            return templates.get(id);

        parse("template/" + id + ".yml");
        if (templates.containsKey(id))
            return templates.get(id);

        throw new RTException("no template registered for id '%s'", id);
    }

    public void putTemplate(final @NotNull String id, final @NotNull IYamlNode template) {
        if (templates.containsKey(id))
            throw new RTException("overriding already registered template with id '%s'", id);
        templates.put(id, template);
    }

    public @NotNull IYamlNode getYaml(final @NotNull String name) {
        try (final var stream = ClassLoader.getSystemResourceAsStream(name)) {
            if (stream != null)
                return IYamlNode.from(new Yaml().loadAs(stream, Map.class));
        } catch (final IOException e) {
            throw new RTException(e, "no resource with name '%s'", name);
        }
        throw new RTException("no resource with name '%s'", name);
    }

    public void parse(final @NotNull String name) {
        final var yaml = getYaml(name);
        parseUses(yaml);

        final var type = yaml.get("type").as(String.class);
        switch (type) {
            case "component" -> parseComponent(yaml);
            case "enum" -> parseEnumeration(yaml);
            case STRING_ELEMENT -> parseTemplate(yaml);
            default -> throw new RTException("no parser registered for type '%s'", type);
        }
    }

    public void parseUses(final @NotNull IYamlNode yaml) {
        final var usesYaml = yaml.get("uses");
        if (usesYaml.notEmpty())
            for (final var useYaml : usesYaml) {
                final var use = useYaml.as(String.class);
                parse(use);
            }
    }

    public void parseComponent(final @NotNull IYamlNode yaml) {
        final var id = yaml.get("id").as(String.class);

        Class<?> clazz;
        try {
            final var className = yaml.get("class").as(String.class);
            clazz = ClassLoader.getSystemClassLoader().loadClass(className);
        } catch (final ClassNotFoundException e) {
            throw new RTException(e);
        }

        final var fieldsYaml = yaml.get("fields");
        final var fields = new Component.Field[fieldsYaml.size()];
        int i = 0;
        for (final var fieldYaml : fieldsYaml) {
            final var fieldName = fieldYaml.get("name").as(String.class);
            final var fieldType = fieldYaml.get("type").as(String.class);
            final var fieldArray = fieldYaml.get("array").as(Boolean.class, false);
            final var fieldHasDefault = fieldYaml.get("default").notEmpty();
            final var fieldDefault = fieldYaml.get("default").as(Object.class, new Object());
            fields[i++] = new Component.Field(fieldName, fieldType, fieldArray, fieldHasDefault, fieldDefault);
        }

        final var elementsYaml = yaml.get("elements");

        putComponent(id, new Component(id, clazz, fields, elementsYaml));
    }

    public void parseEnumeration(final @NotNull IYamlNode yaml) {
        final var id = yaml.get("id").as(String.class);

        final var entriesYaml = yaml.get("entries");
        final var entries = new Enumeration.Entry[entriesYaml.size()];
        int i = 0;
        for (final var entryYaml : entriesYaml) {
            final var entryName = entryYaml.get("name").as(String.class);
            final var entryValue = entryYaml.get("value").as(Integer.class);
            entries[i++] = new Enumeration.Entry(entryName, entryValue);
        }

        putEnumeration(STRING_ENUM_COLON + id, new Enumeration(id, entries));
    }

    public void parseTemplate(final @NotNull IYamlNode yaml) {
        final var id = yaml.get("id").as(String.class);
        final var contentYaml = yaml.get("content");

        putTemplate(id, contentYaml);
    }

    public @NotNull Layout parseLayout(final @NotNull EventManager events, final @NotNull String name) {
        final var yaml = getYaml(name);
        parseUses(yaml);

        final List<Element> elements = new ArrayList<>();
        final var layout = new Layout(events, elements);

        for (final var elementYaml : yaml.get("elements"))
            elements.addAll(Arrays.asList(parseElement(elementYaml, layout, null)));

        return layout;
    }

    public @NotNull Element @NotNull [] parseElement(final @NotNull IYamlNode yaml,
                                                     final @NotNull Layout root,
                                                     final @Nullable String parentId) {
        if (yaml.get("use").notEmpty()) {
            final var id = yaml.get("use").as(String.class);
            return parseElement(getTemplate(id), root, parentId);
        }

        final var id = yaml.get("id").as(String.class, "");
        final var type = yaml.get("type").as(String.class);

        return parseElement(yaml, root, parentId, id, getComponent(type));
    }

    public Class<?> getClassForType(final @NotNull String type) {
        switch (type) {
            case STRING_ELEMENT -> {
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
                if (type.startsWith(STRING_ENUM_COLON))
                    return Integer.class;

                return getComponent(type).clazz();
            }
        }
    }

    public @NotNull Object parseField(final @NotNull IYamlNode yaml,
                                      final @NotNull Layout root,
                                      final @NotNull String parentId,
                                      final @NotNull Component.Field field) {
        if (field.array()) {
            final List<Object> values = new ArrayList<>();
            int i = 0;
            for (final var subYaml : yaml) {
                final var value = parseField(subYaml, root, parentId, field.index(i++));
                if (value instanceof Object[] array)
                    values.addAll(Arrays.asList(array));
                else values.add(value);
            }
            return values.toArray(size -> (Object[]) Array.newInstance(getClassForType(field.type()), size));
        }

        return switch (field.type()) {
            case STRING_ELEMENT -> parseElement(yaml, root, parentId);
            case "int" -> yaml.as(Number.class, field.hasDefault() ? (Number) field.defaultValue() : 0).intValue();
            case "float" -> yaml.as(Number.class, field.hasDefault() ? (Number) field.defaultValue() : 0).floatValue();
            case "boolean" -> yaml.as(Boolean.class, field.hasDefault() && (Boolean) field.defaultValue());
            case "string" -> yaml.as(String.class, field.hasDefault() ? (String) field.defaultValue() : "");
            default -> {
                if (field.type().startsWith(STRING_ENUM_COLON)) {
                    final var name = yaml.as(String.class, field.hasDefault() ? (String) field.defaultValue() : "");
                    yield Arrays.stream(getEnumeration(field.type()).entries())
                            .filter(entry -> entry.name().equals(name))
                            .map(Enumeration.Entry::value)
                            .findFirst()
                            .orElse(Integer.MAX_VALUE);
                }

                yield parseElement(yaml, root, parentId, field.name(), getComponent(field.type()));
            }
        };
    }

    public @NotNull Element @NotNull [] parseElement(final @NotNull IYamlNode yaml,
                                                     final @NotNull Layout root,
                                                     final @Nullable String parentId,
                                                     final @NotNull String id,
                                                     final @NotNull Component component) {

        final var elementId = parentId == null ? id : parentId + '.' + id;
        final var args = Stream.concat(
                Stream.of(root, elementId),
                Arrays.stream(component.fields()).map(field -> parseField(yaml.get(field.name()), root, elementId, field))
        ).toArray();

        final Element element;
        try {
            final var ctor = component.clazz().getConstructor(
                    Arrays.stream(args)
                            .map(Object::getClass)
                            .toArray(Class[]::new)
            );
            final var instance = ctor.newInstance(args);
            element = (Element) instance;
        } catch (final NoSuchMethodException |
                       InstantiationException |
                       IllegalAccessException |
                       InvocationTargetException e) {
            throw new RTException(e, "failed to create instance of component with id '%s'", component.id());
        }

        if (component.elementsYaml().notEmpty()) {
            final List<Element> elements = new ArrayList<>();
            elements.add(element);
            for (final var elementYaml : component.elementsYaml())
                elements.addAll(Arrays.asList(parseElement(elementYaml, root, parentId)));
            return elements.toArray(Element[]::new);
        }

        return new Element[]{element};
    }
}
