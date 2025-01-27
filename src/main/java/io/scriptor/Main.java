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
import imgui.extension.imnodes.ImNodes;
import imgui.flag.ImGuiConfigFlags;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;
import imgui.type.ImString;
import io.scriptor.context.Context;
import io.scriptor.event.EventManager;
import io.scriptor.event.KeyPayload;
import io.scriptor.event.StringPayload;
import io.scriptor.graph.Attribute;
import io.scriptor.graph.Blueprint;
import io.scriptor.graph.Graph;
import io.scriptor.graph.NodeEditor;
import io.scriptor.imgui.Array;
import io.scriptor.imgui.ColorEdit;
import io.scriptor.imgui.InputText;
import io.scriptor.imgui.Layout;
import io.scriptor.resource.ResourceManager;
import io.scriptor.util.RTException;
import io.scriptor.util.Range;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import static io.scriptor.util.ID.*;
import static io.scriptor.util.Task.handle;
import static io.scriptor.util.Task.handleVoid;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.util.Objects.requireNonNullElse;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.stb.STBImage.stbi_image_free;
import static org.lwjgl.stb.STBImage.stbi_load_from_memory;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Main {

    private static KeyPayload makePayload(final int key, final int scancode, final int action, final int mods) {
        final var mod_shift = (mods & GLFW_MOD_SHIFT) != 0;
        final var mod_control = (mods & GLFW_MOD_CONTROL) != 0;
        final var mod_alt = (mods & GLFW_MOD_ALT) != 0;
        final var mod_super = (mods & GLFW_MOD_SUPER) != 0;
        final var mod_caps_lock = (mods & GLFW_MOD_CAPS_LOCK) != 0;
        final var mod_num_lock = (mods & GLFW_MOD_NUM_LOCK) != 0;
        return new KeyPayload(
                key,
                scancode,
                action,
                mod_shift,
                mod_control,
                mod_alt,
                mod_super,
                mod_caps_lock,
                mod_num_lock);
    }

    /**
     * Application entry point
     */
    public static void main(final String[] args) {
        new Main();
    }

    private long window;

    private final ImGuiImplGlfw imGuiGlfw = new ImGuiImplGlfw();
    private final ImGuiImplGl3 imGuiGl3 = new ImGuiImplGl3();

    private Context context;
    private EventManager events;
    private Layout rootLayout;

    private Graph graph;

    private Attribute selectedAttribute;
    private Blueprint selectedBlueprint;

    private Main() {
        onInit();
        onStart();
        while (!glfwWindowShouldClose(window))
            onFrame();
        onStop();
        onExit();
    }

    private void load() {
        final var file = new File("project.bff");
        if (file.exists()) {
            context = handle(() -> new Context(file));
        } else {
            context = new Context();
        }
    }

    private void save() {
        final var file = new File("project.bff");
        if (file.exists()) {
            final var bkp = new File("backup.bff");
            handleVoid(() -> Files.copy(file.toPath(), bkp.toPath(), REPLACE_EXISTING));
        }
        handleVoid(() -> context.write(file));
    }

    private void onInit() {
        GLFWErrorCallback
                .createPrint(System.err)
                .set();

        glfwInit();
        glfwDefaultWindowHints();
        window = glfwCreateWindow(1024, 768, "Java Logic Sim", NULL, NULL);

        try (final var iconStream = ClassLoader.getSystemResourceAsStream("image/icon.png")) {
            assert iconStream != null;
            try (final var stack = MemoryStack.stackPush()) {
                final var buffer = stack.bytes(iconStream.readAllBytes());
                final var width = new int[1];
                final var height = new int[1];
                final var pixels = stbi_load_from_memory(buffer, width, height, new int[1], 4);
                assert pixels != null;
                final var images = GLFWImage.malloc(1, stack);
                final var image = images.get(0);
                image.set(width[0], height[0], pixels);
                glfwSetWindowIcon(window, images);
                stbi_image_free(pixels);
            }
        } catch (final IOException e) {
            throw new RTException(e);
        }

        glfwSetKeyCallback(window, this::onKey);
        glfwSetFramebufferSizeCallback(window, this::onSize);

        glfwMakeContextCurrent(window);
        GL.createCapabilities();
        glfwSwapInterval(1);

        ImGui.createContext();
        ImNodes.createContext();

        final var io = ImGui.getIO();
        io.addConfigFlags(ImGuiConfigFlags.DockingEnable);
        io.addConfigFlags(ImGuiConfigFlags.NavEnableKeyboard);

        imGuiGlfw.init(window, true);
        imGuiGl3.init();
    }

    private static boolean attributeCustomView(final @NotNull Attribute value) {
        if (value.output()) ImGui.beginDisabled();
        ImGui.checkbox("##powered", value.powered());
        if (value.output()) ImGui.endDisabled();
        ImGui.sameLine();
        return ImGui.selectable(value.label().get());
    }

    private void onAddInput() {
        selectedAttribute = new Attribute("New In", false);
        graph.add(selectedAttribute);
        events.scheduleTask(() -> ImGui.openPopup(ID_ATTRIBUTES_RENAME_CONTEXT));
        rootLayout
                .findElement(ID_ATTRIBUTES_RENAME_CONTEXT_TEXT, InputText.class)
                .ifPresent(text -> text.set(selectedAttribute.label().get()));
    }

    private void onAddOutput() {
        selectedAttribute = new Attribute("New Out", true);
        graph.add(selectedAttribute);
        events.scheduleTask(() -> ImGui.openPopup(ID_ATTRIBUTES_RENAME_CONTEXT));
        rootLayout
                .findElement(ID_ATTRIBUTES_RENAME_CONTEXT_TEXT, InputText.class)
                .ifPresent(text -> text.set(selectedAttribute.label().get()));
    }

    private void onAttributeSelect(final @NotNull Array.Payload<Attribute> payload) {
        events.scheduleTask(() -> ImGui.openPopup(ID_ATTRIBUTES_ATTRIBUTE_CONTEXT));
        selectedAttribute = payload.value();
    }

    private void onAttributeRename() {
        events.scheduleTask(() -> ImGui.openPopup(ID_ATTRIBUTES_RENAME_CONTEXT));
        rootLayout
                .findElement(ID_ATTRIBUTES_RENAME_CONTEXT_TEXT, InputText.class)
                .ifPresent(text -> text.set(selectedAttribute.label().get()));
    }

    private void onAttributeDelete() {
        graph.remove(selectedAttribute);
    }

    private void onAttributeRenameEnter(final @NotNull InputText.Payload payload) {
        selectedAttribute.label().set(payload.value(), true);
        ImGui.closeCurrentPopup();
    }

    private void onBlueprintCreate() {
        final var copy = graph.copy();
        selectedBlueprint = new Blueprint.Builder()
                .label("New Blueprint")
                .baseColor(ImColor.rgb((float) Math.random(), (float) Math.random(), (float) Math.random()))
                .inputs(copy
                        .inputs()
                        .map(Attribute::label)
                        .map(ImString::get)
                        .toArray(String[]::new))
                .outputs(copy
                        .outputs()
                        .map(Attribute::label)
                        .map(ImString::get)
                        .toArray(String[]::new))
                .function(copy.compile(true))
                .build();
        context.add(selectedBlueprint);
        graph.clear();

        events.scheduleTask(() -> ImGui.openPopup(ID_BLUEPRINTS_RENAME_CONTEXT));
        rootLayout
                .findElement(ID_BLUEPRINTS_RENAME_CONTEXT_TEXT, InputText.class)
                .ifPresent(text -> text.set(selectedBlueprint.label().get()));
    }

    private void onBlueprintSelect(final @NotNull Array.Payload<Blueprint> payload) {
        events.scheduleTask(() -> ImGui.openPopup(ID_BLUEPRINTS_BLUEPRINT_CONTEXT));
        selectedBlueprint = payload.value();
    }

    private void onBlueprintRename() {
        events.scheduleTask(() -> ImGui.openPopup(ID_BLUEPRINTS_RENAME_CONTEXT));
        rootLayout
                .findElement(ID_BLUEPRINTS_RENAME_CONTEXT_TEXT, InputText.class)
                .ifPresent(text -> text.set(selectedBlueprint.label().get()));
    }

    private void onBlueprintColor() {
        events.scheduleTask(() -> ImGui.openPopup(ID_BLUEPRINTS_COLOR_CONTEXT));
        rootLayout
                .findElement(ID_BLUEPRINTS_COLOR_CONTEXT_COLOR, ColorEdit.class)
                .ifPresent(color -> color.color(selectedBlueprint.baseColor().get()));
    }

    private void onBlueprintDelete() {
        context.remove(selectedBlueprint);
        context.registry().remove(selectedBlueprint.function());
    }

    private void onBlueprintRenameEnter(final @NotNull InputText.Payload payload) {
        selectedBlueprint.label().set(payload.value(), true);
        ImGui.closeCurrentPopup();
    }

    private void onBlueprintColorSelect(final @NotNull ColorEdit.Payload payload) {
        selectedBlueprint
                .baseColor()
                .set(payload.value());
    }

    private void onStart() {
        load();

        graph = new Graph(context.registry());
        events = new EventManager();
        final var resources = new ResourceManager();

        rootLayout = resources.parseLayout(events, "layout/main.yml");
        rootLayout
                .findElement(ID_EDITOR_EDITOR, NodeEditor.class)
                .ifPresent(editor -> {
                    editor.graph(graph);
                    graph.attributes(editor.attributes());
                    editor.blueprints(context.blueprints());
                });

        graph.add(new Attribute("In A", false));
        graph.add(new Attribute("In B", false));
        graph.add(new Attribute("Out", true));

        rootLayout.start();

        final var attributeRange = new Range<>(Attribute.class);
        graph.attributes(attributeRange);
        attributeRange.sorted(Comparator.comparing(Attribute::output));
        rootLayout
                .findElement(ID_ATTRIBUTES_CONTAINER_ARRAY, Array.class)
                .ifPresent(array -> array
                        .range(attributeRange)
                        .element(Main::attributeCustomView));

        final var blueprintRange = new Range<>(context.blueprints(), Blueprint.class);
        blueprintRange.sorted(Comparator.comparing(Blueprint::label));
        rootLayout
                .findElement(ID_BLUEPRINTS_CONTAINER_ARRAY, Array.class)
                .ifPresent(array -> array.range(blueprintRange));

        events.registerEvent(ID_ATTRIBUTES_ADD_INPUT_CLICK, this::onAddInput);
        events.registerEvent(ID_ATTRIBUTES_ADD_OUTPUT_CLICK, this::onAddOutput);
        events.registerEvent(ID_ATTRIBUTES_CONTAINER_ARRAY_SELECT, this::onAttributeSelect);
        events.registerEvent(ID_ATTRIBUTES_ATTRIBUTE_CONTEXT_RENAME_CLICK, this::onAttributeRename);
        events.registerEvent(ID_ATTRIBUTES_ATTRIBUTE_CONTEXT_DELETE_CLICK, this::onAttributeDelete);
        events.registerEvent(ID_ATTRIBUTES_RENAME_CONTEXT_TEXT_ENTER, this::onAttributeRenameEnter);
        events.registerEvent(ID_BLUEPRINTS_CREATE_CLICK, this::onBlueprintCreate);
        events.registerEvent(ID_BLUEPRINTS_CONTAINER_ARRAY_SELECT, this::onBlueprintSelect);
        events.registerEvent(ID_BLUEPRINTS_BLUEPRINT_CONTEXT_RENAME_CLICK, this::onBlueprintRename);
        events.registerEvent(ID_BLUEPRINTS_BLUEPRINT_CONTEXT_COLOR_CLICK, this::onBlueprintColor);
        events.registerEvent(ID_BLUEPRINTS_BLUEPRINT_CONTEXT_DELETE_CLICK, this::onBlueprintDelete);
        events.registerEvent(ID_BLUEPRINTS_RENAME_CONTEXT_TEXT_ENTER, this::onBlueprintRenameEnter);
        events.registerEvent(ID_BLUEPRINTS_COLOR_CONTEXT_COLOR_SELECT, this::onBlueprintColorSelect);

        events.<KeyPayload>registerEvent("key.s.press+control", payload -> save());

        events.offerService(ID_CLIPBOARD_GET, () -> requireNonNullElse(glfwGetClipboardString(window), ""));
        events.<StringPayload>offerService(ID_CLIPBOARD_SET, payload -> glfwSetClipboardString(window, payload.value()));
    }

    private void onFrame() {
        onFrameBegin();
        onUpdate();
        onFrameEnd();
    }

    private void onFrameBegin() {
        glfwPollEvents();

        imGuiGl3.newFrame();
        imGuiGlfw.newFrame();

        ImGui.newFrame();
    }

    private void onUpdate() {
        rootLayout.show();
    }

    private void onFrameEnd() {
        ImGui.render();

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        imGuiGl3.renderDrawData(ImGui.getDrawData());

        glfwSwapBuffers(window);
    }

    private void onStop() {
        save();
    }

    private void onExit() {
        imGuiGl3.shutdown();
        imGuiGlfw.shutdown();

        ImNodes.destroyContext();
        ImGui.destroyContext();

        glfwDestroyWindow(window);
        glfwTerminate();
    }

    private static final Map<Integer, String> keymap = new HashMap<>();

    static {
        keymap.put(GLFW_KEY_SPACE, "space");
        keymap.put(GLFW_KEY_ESCAPE, "escape");
        keymap.put(GLFW_KEY_ENTER, "enter");
        keymap.put(GLFW_KEY_TAB, "tab");
        keymap.put(GLFW_KEY_BACKSPACE, "backspace");
        keymap.put(GLFW_KEY_INSERT, "insert");
        keymap.put(GLFW_KEY_DELETE, "delete");
        keymap.put(GLFW_KEY_RIGHT, "right");
        keymap.put(GLFW_KEY_LEFT, "left");
        keymap.put(GLFW_KEY_DOWN, "down");
        keymap.put(GLFW_KEY_UP, "up");
        keymap.put(GLFW_KEY_PAGE_UP, "page-up");
        keymap.put(GLFW_KEY_PAGE_DOWN, "page-down");
        keymap.put(GLFW_KEY_HOME, "home");
        keymap.put(GLFW_KEY_END, "end");
        keymap.put(GLFW_KEY_CAPS_LOCK, "caps-lock");
        keymap.put(GLFW_KEY_SCROLL_LOCK, "scroll-lock");
        keymap.put(GLFW_KEY_NUM_LOCK, "num-lock");
        keymap.put(GLFW_KEY_PRINT_SCREEN, "print-screen");
        keymap.put(GLFW_KEY_PAUSE, "pause");
        keymap.put(GLFW_KEY_F1, "f1");
        keymap.put(GLFW_KEY_F2, "f2");
        keymap.put(GLFW_KEY_F3, "f3");
        keymap.put(GLFW_KEY_F4, "f4");
        keymap.put(GLFW_KEY_F5, "f5");
        keymap.put(GLFW_KEY_F6, "f6");
        keymap.put(GLFW_KEY_F7, "f7");
        keymap.put(GLFW_KEY_F8, "f8");
        keymap.put(GLFW_KEY_F9, "f9");
        keymap.put(GLFW_KEY_F10, "f10");
        keymap.put(GLFW_KEY_F11, "f11");
        keymap.put(GLFW_KEY_F12, "f12");
        keymap.put(GLFW_KEY_F13, "f13");
        keymap.put(GLFW_KEY_F14, "f14");
        keymap.put(GLFW_KEY_F15, "f15");
        keymap.put(GLFW_KEY_F16, "f16");
        keymap.put(GLFW_KEY_F17, "f17");
        keymap.put(GLFW_KEY_F18, "f18");
        keymap.put(GLFW_KEY_F19, "f19");
        keymap.put(GLFW_KEY_F20, "f20");
        keymap.put(GLFW_KEY_F21, "f21");
        keymap.put(GLFW_KEY_F22, "f22");
        keymap.put(GLFW_KEY_F23, "f23");
        keymap.put(GLFW_KEY_F24, "f24");
        keymap.put(GLFW_KEY_F25, "f25");
        keymap.put(GLFW_KEY_KP_ENTER, "kp-enter");
        keymap.put(GLFW_KEY_LEFT_SHIFT, "left-shift");
        keymap.put(GLFW_KEY_LEFT_CONTROL, "left-control");
        keymap.put(GLFW_KEY_LEFT_ALT, "left-alt");
        keymap.put(GLFW_KEY_LEFT_SUPER, "left-super");
        keymap.put(GLFW_KEY_RIGHT_SHIFT, "right-shift");
        keymap.put(GLFW_KEY_RIGHT_CONTROL, "right-control");
        keymap.put(GLFW_KEY_RIGHT_ALT, "right-alt");
        keymap.put(GLFW_KEY_RIGHT_SUPER, "right-super");
        keymap.put(GLFW_KEY_MENU, "menu");
    }

    private void onKey(final long window, final int key, final int scancode, final int action, final int mods) {
        final var keyString = keymap.computeIfAbsent(key, k -> glfwGetKeyName(key, scancode));

        final var actionString = switch (action) {
            case GLFW_RELEASE -> "release";
            case GLFW_PRESS -> "press";
            case GLFW_REPEAT -> "repeat";
            default -> "none";
        };

        final var payload = makePayload(key, scancode, action, mods);
        events.invokeEvent("key", payload);

        final var id = new StringBuilder();
        id
                .append("key.")
                .append(keyString)
                .append('.')
                .append(actionString);
        if (payload.shift())
            id.append("+shift");
        if (payload.control())
            id.append("+control");
        if (payload.alt())
            id.append("+alt");
        if (payload.super_())
            id.append("+super");
        if (payload.caps())
            id.append("+caps");
        if (payload.num())
            id.append("+num");
        events.invokeEvent(id.toString(), payload);
    }

    private void onSize(final long window, final int width, final int height) {
        onFrame();
    }
}
