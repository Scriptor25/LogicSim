package io.scriptor;

import imgui.ImColor;
import imgui.ImGui;
import imgui.app.Application;
import imgui.extension.imnodes.ImNodes;
import imgui.flag.ImGuiConfigFlags;
import imgui.type.ImString;
import io.scriptor.context.Context;
import io.scriptor.graph.Attribute;
import io.scriptor.graph.Blueprint;
import io.scriptor.graph.Graph;
import io.scriptor.graph.NodeEditor;
import io.scriptor.imgui.Array;
import io.scriptor.imgui.ColorEdit;
import io.scriptor.imgui.InputText;
import io.scriptor.imgui.Layout;
import io.scriptor.manager.EventManager;
import io.scriptor.manager.ResourceManager;
import io.scriptor.util.Range;
import io.scriptor.util.Task;
import org.lwjgl.glfw.GLFWKeyCallback;

import java.io.File;
import java.nio.file.Files;
import java.util.Comparator;
import java.util.Date;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.lwjgl.glfw.GLFW.*;

public class MainApp extends Application {

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

    public static void main(final String[] args) {
        Application.launch(new MainApp());
    }

    private static boolean[] getMods(final int mods) {
        final var mod_shift = (mods & GLFW_MOD_SHIFT) != 0;
        final var mod_control = (mods & GLFW_MOD_CONTROL) != 0;
        final var mod_alt = (mods & GLFW_MOD_ALT) != 0;
        final var mod_super = (mods & GLFW_MOD_SUPER) != 0;
        final var mod_caps_lock = (mods & GLFW_MOD_CAPS_LOCK) != 0;
        final var mod_num_lock = (mods & GLFW_MOD_NUM_LOCK) != 0;
        return new boolean[]{mod_shift, mod_control, mod_alt, mod_super, mod_caps_lock, mod_num_lock};
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
        final var file = new File("blueprints");
        if (file.exists()) {
            context = Task.handle(() -> new Context(file));
        } else {
            context = new Context();
        }

        graph = new Graph(context.registry());

        layout = resources.parseLayout(events, "layout/main.yml");
        final NodeEditor editor = layout.findElement("editor.editor");
        editor.graph(graph);
        graph.attributes(editor.attributes());
        editor.blueprints(context.blueprints());

        graph.add(new Attribute("In A", false));
        graph.add(new Attribute("In B", false));
        graph.add(new Attribute("Out", true));
    }

    private void onKey(long window, int key, int scancode, int action, int mods) {
        if (keyCallback != null) keyCallback.invoke(window, key, scancode, action, mods);

        final var id = "key." +
                switch (key) {
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
                } +
                switch (action) {
                    case GLFW_RELEASE -> ".release";
                    case GLFW_PRESS -> ".press";
                    case GLFW_REPEAT -> ".repeat";
                    default -> ".none";
                };
        events.invoke(id, getMods(mods));
    }

    private void save() {
        final var file = new File("blueprints");
        if (file.exists()) {
            final var bkp = new File("blueprints.bkp");
            Task.handle(() -> Files.copy(file.toPath(), bkp.toPath(), REPLACE_EXISTING));
        }

        Task.handleVoid(() -> context.write(file));
    }

    @Override
    protected void preRun() {
        keyCallback = glfwSetKeyCallback(getHandle(), this::onKey);

        final var io = ImGui.getIO();
        io.addConfigFlags(ImGuiConfigFlags.DockingEnable);
        io.addConfigFlags(ImGuiConfigFlags.NavEnableKeyboard);
        ImNodes.createContext();

        layout.start();

        final var attributeRange = new Range<>(Attribute.class);
        graph.attributes(attributeRange);
        attributeRange.sorted(Comparator.comparing(Attribute::output));
        final Array attributeArray = layout.findElement("attributes.container.array");
        attributeArray.setRange(attributeRange);

        final var blueprintRange = new Range<>(context.blueprints(), Blueprint.class);
        final Array blueprintArray = layout.findElement("blueprints.container.array");
        blueprintArray.setRange(blueprintRange);

        events.register("attributes.add-input.click", args -> graph.add(new Attribute("New In", false)));
        events.register("attributes.add-output.click", args -> graph.add(new Attribute("New Out", true)));
        events.register("attributes.container.array.select", args -> {
            events.schedule(() -> ImGui.openPopup("attributes.attribute-context"));
            selectedAttribute = (Attribute) args[1];
        });
        events.register("attributes.attribute-context.rename.click", args -> {
            events.schedule(() -> ImGui.openPopup("attributes.rename-context"));
            final InputText text = layout.findElement("attributes.rename-context.text");
            text.set(selectedAttribute.label().get());
        });
        events.register("attributes.attribute-context.delete.click", args -> graph.remove(selectedAttribute));
        events.register("attributes.rename-context.text.enter", args -> {
            selectedAttribute.label().set((String) args[1], true);
            ImGui.closeCurrentPopup();
        });

        events.register("blueprints.create.click", args -> {
            final var copy = graph.copy();
            final var blueprint = new Blueprint.Builder()
                    .label("New")
                    .baseColor(ImColor.rgb((float) Math.random(), (float) Math.random(), (float) Math.random()))
                    .inputs(copy.inputs().map(Attribute::label).map(ImString::get).toArray(String[]::new))
                    .outputs(copy.outputs().map(Attribute::label).map(ImString::get).toArray(String[]::new))
                    .function(copy.compile(true))
                    .build();
            context.add(blueprint);

            graph.clear();
        });
        events.register("blueprints.container.array.select", args -> {
            events.schedule(() -> ImGui.openPopup("blueprints.blueprint-context"));
            selectedBlueprint = (Blueprint) args[1];
        });
        events.register("blueprints.blueprint-context.rename.click", args -> {
            events.schedule(() -> ImGui.openPopup("blueprints.rename-context"));
            final InputText text = layout.findElement("blueprints.rename-context.text");
            text.set(selectedBlueprint.label().get());
        });
        events.register("blueprints.blueprint-context.color.click", args -> {
            events.schedule(() -> ImGui.openPopup("blueprints.color-context"));
            final ColorEdit color = layout.findElement("blueprints.color-context.color");
            color.color(selectedBlueprint.baseColor().get());
        });
        events.register("blueprints.blueprint-context.delete.click", args -> {
            context.remove(selectedBlueprint);
            context.registry().remove(selectedBlueprint.function());
        });
        events.register("blueprints.rename-context.text.enter", args -> {
            selectedBlueprint.label().set((String) args[1], true);
            ImGui.closeCurrentPopup();
        });
        events.register("blueprints.color-context.color.select", args -> selectedBlueprint.baseColor().set((Integer) args[1]));

        events.register("key.s.release", args -> {
            final var mods = (boolean[]) args[0];
            if (mods[1])
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
