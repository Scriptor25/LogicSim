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
package io.scriptor;

import imgui.ImColor;
import imgui.ImGui;
import imgui.app.Application;
import imgui.app.Configuration;
import imgui.extension.imnodes.ImNodes;
import imgui.flag.ImGuiConfigFlags;
import imgui.type.ImString;
import io.scriptor.context.Context;
import io.scriptor.event.EventManager;
import io.scriptor.graph.Attribute;
import io.scriptor.graph.Blueprint;
import io.scriptor.graph.Graph;
import io.scriptor.graph.NodeEditor;
import io.scriptor.imgui.Array;
import io.scriptor.imgui.ColorEdit;
import io.scriptor.imgui.InputText;
import io.scriptor.imgui.Layout;
import io.scriptor.manager.ResourceManager;
import io.scriptor.util.KeyPayload;
import io.scriptor.util.RTException;
import io.scriptor.util.Range;
import io.scriptor.util.Task;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.system.MemoryStack;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Comparator;
import java.util.Date;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static io.scriptor.util.Task.handle;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.lwjgl.glfw.GLFW.*;

public class MainApp extends Application {

    private static final String STRING_ATTRIBUTES_RENAME_CONTEXT = "attributes.rename-context";
    private static final String STRING_ATTRIBUTES_RENAME_CONTEXT_TEXT = "attributes.rename-context.text";

    private static Logger logger;

    public static Logger getLogger() {
        if (logger != null)
            return logger;

        logger = Logger.getLogger("io.scriptor");

        final var handler = new ConsoleHandler();
        handler.setFormatter(new Formatter() {
            @Override
            public String format(final LogRecord rec) {
                return "[%s][%s]%n%s%n".formatted(
                        new Date(rec.getMillis()),
                        rec.getLevel(),
                        rec.getMessage());
            }
        });

        logger.setUseParentHandlers(false);
        logger.addHandler(handler);

        return logger;
    }

    /**
     * Application entry point
     */
    public static void main(final String[] args) {
        Application.launch(new MainApp());
    }

    private static KeyPayload getMods(final int mods) {
        final var mod_shift = (mods & GLFW_MOD_SHIFT) != 0;
        final var mod_control = (mods & GLFW_MOD_CONTROL) != 0;
        final var mod_alt = (mods & GLFW_MOD_ALT) != 0;
        final var mod_super = (mods & GLFW_MOD_SUPER) != 0;
        final var mod_caps_lock = (mods & GLFW_MOD_CAPS_LOCK) != 0;
        final var mod_num_lock = (mods & GLFW_MOD_NUM_LOCK) != 0;
        return new KeyPayload(mod_shift, mod_control, mod_alt, mod_super, mod_caps_lock, mod_num_lock);
    }

    private final Context context;
    private final Graph graph;

    private final ResourceManager resources = new ResourceManager();
    private final Layout layout;

    private final EventManager events = new EventManager();
    private GLFWKeyCallback keyCallback;

    private Attribute selectedAttribute;
    private Blueprint selectedBlueprint;

    private MainApp() {
        final var file = new File("project.bff");
        if (file.exists()) {
            context = handle(() -> new Context(file));
            assert context != null;
        } else {
            context = new Context();
        }

        graph = new Graph(context.registry());

        layout = resources.parseLayout(events, "layout/main.yml");

        layout
                .findElement("editor.editor", NodeEditor.class)
                .ifPresent(editor -> {
                    editor.graph(graph);
                    graph.attributes(editor.attributes());
                    editor.blueprints(context.blueprints());
                });

        graph.add(new Attribute("In A", false));
        graph.add(new Attribute("In B", false));
        graph.add(new Attribute("Out", true));
    }

    private void onKey(long window, int key, int scancode, int action, int mods) {
        if (keyCallback != null) keyCallback.invoke(window, key, scancode, action, mods);

        final var id = "key." + switch (key) {
            case GLFW_KEY_SPACE -> "space";
            case GLFW_KEY_ESCAPE -> "escape";
            case GLFW_KEY_ENTER -> "enter";
            case GLFW_KEY_TAB -> "tab";
            case GLFW_KEY_BACKSPACE -> "backspace";
            case GLFW_KEY_INSERT -> "insert";
            case GLFW_KEY_DELETE -> "delete";
            case GLFW_KEY_RIGHT -> "right";
            case GLFW_KEY_LEFT -> "left";
            case GLFW_KEY_DOWN -> "down";
            case GLFW_KEY_UP -> "up";
            case GLFW_KEY_PAGE_UP -> "page-up";
            case GLFW_KEY_PAGE_DOWN -> "page-down";
            case GLFW_KEY_HOME -> "home";
            case GLFW_KEY_END -> "end";
            case GLFW_KEY_CAPS_LOCK -> "caps-lock";
            case GLFW_KEY_SCROLL_LOCK -> "scroll-lock";
            case GLFW_KEY_NUM_LOCK -> "num-lock";
            case GLFW_KEY_PRINT_SCREEN -> "print-screen";
            case GLFW_KEY_PAUSE -> "pause";
            case GLFW_KEY_F1 -> "f1";
            case GLFW_KEY_F2 -> "f2";
            case GLFW_KEY_F3 -> "f3";
            case GLFW_KEY_F4 -> "f4";
            case GLFW_KEY_F5 -> "f5";
            case GLFW_KEY_F6 -> "f6";
            case GLFW_KEY_F7 -> "f7";
            case GLFW_KEY_F8 -> "f8";
            case GLFW_KEY_F9 -> "f9";
            case GLFW_KEY_F10 -> "f10";
            case GLFW_KEY_F11 -> "f11";
            case GLFW_KEY_F12 -> "f12";
            case GLFW_KEY_F13 -> "f13";
            case GLFW_KEY_F14 -> "f14";
            case GLFW_KEY_F15 -> "f15";
            case GLFW_KEY_F16 -> "f16";
            case GLFW_KEY_F17 -> "f17";
            case GLFW_KEY_F18 -> "f18";
            case GLFW_KEY_F19 -> "f19";
            case GLFW_KEY_F20 -> "f20";
            case GLFW_KEY_F21 -> "f21";
            case GLFW_KEY_F22 -> "f22";
            case GLFW_KEY_F23 -> "f23";
            case GLFW_KEY_F24 -> "f24";
            case GLFW_KEY_F25 -> "f25";
            case GLFW_KEY_KP_ENTER -> "kp-enter";
            case GLFW_KEY_LEFT_SHIFT -> "left-shift";
            case GLFW_KEY_LEFT_CONTROL -> "left-control";
            case GLFW_KEY_LEFT_ALT -> "left-alt";
            case GLFW_KEY_LEFT_SUPER -> "left-super";
            case GLFW_KEY_RIGHT_SHIFT -> "right-shift";
            case GLFW_KEY_RIGHT_CONTROL -> "right-control";
            case GLFW_KEY_RIGHT_ALT -> "right-alt";
            case GLFW_KEY_RIGHT_SUPER -> "right-super";
            case GLFW_KEY_MENU -> "menu";
            default -> glfwGetKeyName(key, scancode);
        } + switch (action) {
            case GLFW_RELEASE -> ".release";
            case GLFW_PRESS -> ".press";
            case GLFW_REPEAT -> ".repeat";
            default -> ".none";
        };
        events.invokeEvent(id, getMods(mods));
    }

    private void save() {
        final var file = new File("project.bff");
        if (file.exists()) {
            final var bkp = new File("backup.bff");
            handle(() -> Files.copy(file.toPath(), bkp.toPath(), REPLACE_EXISTING));
        }

        Task.handleVoid(() -> context.write(file));
    }

    @Override
    protected void configure(final Configuration config) {
        config.setTitle("Logic Sim");
    }

    @Override
    protected void preRun() {
        try (final var iconStream = ClassLoader.getSystemResourceAsStream("image/icon.png")) {
            if (iconStream != null) {
                final var icon = handle(() -> ImageIO.read(iconStream));
                assert icon != null;

                final var width = icon.getWidth();
                final var height = icon.getHeight();
                final var rgb = new int[width * height];
                icon.getRGB(0, 0, width, height, rgb, 0, width);

                for (int i = 0; i < rgb.length; ++i) {
                    final var alpha = (rgb[i] >> 24) & 0xff;
                    final var blue = (rgb[i] >> 16) & 0xff;
                    final var green = (rgb[i] >> 8) & 0xff;
                    final var red = rgb[i] & 0xff;
                    rgb[i] = (alpha << 24) | (red << 16) | (green << 8) | blue;
                }

                try (final var stack = MemoryStack.stackPush()) {
                    final var pixels = stack.malloc(width * height * Integer.BYTES);
                    pixels.asIntBuffer().put(rgb);

                    final var images = GLFWImage.malloc(1, stack);
                    final var image = images.get(0);
                    image.width(width);
                    image.height(height);
                    image.pixels(pixels);
                    glfwSetWindowIcon(getHandle(), images);
                }
            }
        } catch (final IOException e) {
            throw new RTException(e);
        }

        keyCallback = glfwSetKeyCallback(getHandle(), this::onKey);

        final var io = ImGui.getIO();
        io.setMouseDrawCursor(false);
        io.addConfigFlags(ImGuiConfigFlags.DockingEnable);
        io.addConfigFlags(ImGuiConfigFlags.NavEnableKeyboard);
        ImNodes.createContext();

        layout.start();

        final var attributeRange = new Range<>(Attribute.class);
        graph.attributes(attributeRange);
        attributeRange.sorted(Comparator.comparing(Attribute::output));
        layout
                .findElement("attributes.container.array", Array.class)
                .ifPresent(array -> {
                    array.setRange(attributeRange);
                    array.<Attribute>setElement(value -> {
                        if (value.output()) ImGui.beginDisabled();
                        ImGui.checkbox("##powered", value.powered());
                        if (value.output()) ImGui.endDisabled();
                        ImGui.sameLine();
                        return ImGui.selectable(value.label().get());
                    });
                });

        final var blueprintRange = new Range<>(context.blueprints(), Blueprint.class);
        blueprintRange.sorted(Comparator.comparing(Blueprint::label));
        layout
                .findElement("blueprints.container.array", Array.class)
                .ifPresent(array -> array.setRange(blueprintRange));

        events.registerEvent("attributes.add-input.click", args -> {
            selectedAttribute = new Attribute("New In", false);
            graph.add(selectedAttribute);
            events.scheduleTask(() -> ImGui.openPopup(STRING_ATTRIBUTES_RENAME_CONTEXT));
            layout
                    .findElement(STRING_ATTRIBUTES_RENAME_CONTEXT_TEXT, InputText.class)
                    .ifPresent(text -> text.set(selectedAttribute.label().get()));
        });
        events.registerEvent("attributes.add-output.click", args -> {
            selectedAttribute = new Attribute("New Out", true);
            graph.add(selectedAttribute);
            events.scheduleTask(() -> ImGui.openPopup(STRING_ATTRIBUTES_RENAME_CONTEXT));
            layout
                    .findElement(STRING_ATTRIBUTES_RENAME_CONTEXT_TEXT, InputText.class)
                    .ifPresent(text -> text.set(selectedAttribute.label().get()));
        });
        events.<Array.Payload<Attribute>>registerEvent("attributes.container.array.select", payload -> {
            events.scheduleTask(() -> ImGui.openPopup("attributes.attribute-context"));
            selectedAttribute = payload.value();
        });
        events.registerEvent("attributes.attribute-context.rename.click", args -> {
            events.scheduleTask(() -> ImGui.openPopup(STRING_ATTRIBUTES_RENAME_CONTEXT));
            layout
                    .findElement(STRING_ATTRIBUTES_RENAME_CONTEXT_TEXT, InputText.class)
                    .ifPresent(text -> text.set(selectedAttribute.label().get()));
        });
        events.registerEvent("attributes.attribute-context.delete.click", args -> graph.remove(selectedAttribute));
        events.<InputText.Payload>registerEvent("attributes.rename-context.text.enter", payload -> {
            selectedAttribute.label().set(payload.value(), true);
            ImGui.closeCurrentPopup();
        });

        events.registerEvent("blueprints.create.click", args -> {
            final var copy = graph.copy();
            selectedBlueprint = new Blueprint.Builder()
                    .label("New")
                    .baseColor(ImColor.rgb((float) Math.random(), (float) Math.random(), (float) Math.random()))
                    .inputs(copy.inputs().map(Attribute::label).map(ImString::get).toArray(String[]::new))
                    .outputs(copy.outputs().map(Attribute::label).map(ImString::get).toArray(String[]::new))
                    .function(copy.compile(true))
                    .build();
            context.add(selectedBlueprint);
            graph.clear();

            events.scheduleTask(() -> ImGui.openPopup("blueprints.rename-context"));
            layout
                    .findElement("blueprints.rename-context.text", InputText.class)
                    .ifPresent(text -> text.set(selectedBlueprint.label().get()));
        });
        events.<Array.Payload<Blueprint>>registerEvent("blueprints.container.array.select", payload -> {
            events.scheduleTask(() -> ImGui.openPopup("blueprints.blueprint-context"));
            selectedBlueprint = payload.value();
        });
        events.registerEvent("blueprints.blueprint-context.rename.click", args -> {
            events.scheduleTask(() -> ImGui.openPopup("blueprints.rename-context"));
            layout
                    .findElement("blueprints.rename-context.text", InputText.class)
                    .ifPresent(text -> text.set(selectedBlueprint.label().get()));
        });
        events.registerEvent("blueprints.blueprint-context.color.click", args -> {
            events.scheduleTask(() -> ImGui.openPopup("blueprints.color-context"));
            layout
                    .findElement("blueprints.color-context.color", ColorEdit.class)
                    .ifPresent(color -> color.color(selectedBlueprint.baseColor().get()));
        });
        events.registerEvent("blueprints.blueprint-context.delete.click", args -> {
            context.remove(selectedBlueprint);
            context.registry().remove(selectedBlueprint.function());
        });
        events.<InputText.Payload>registerEvent("blueprints.rename-context.text.enter", payload -> {
            selectedBlueprint.label().set(payload.value(), true);
            ImGui.closeCurrentPopup();
        });
        events.<ColorEdit.Payload>registerEvent("blueprints.color-context.color.select", payload -> selectedBlueprint.baseColor().set(payload.value()));

        events.<KeyPayload>registerEvent("key.s.press", payload -> {
            if (payload.control())
                save();
        });
    }

    @Override
    public void process() {
        layout.show();
    }

    @Override
    protected void postRun() {
        ImNodes.destroyContext();
        save();
    }
}
