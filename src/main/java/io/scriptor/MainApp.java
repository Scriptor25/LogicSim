package io.scriptor;

import imgui.ImGui;
import imgui.app.Application;
import imgui.extension.imnodes.ImNodes;
import imgui.flag.ImGuiConfigFlags;
import io.scriptor.imgui.Layout;
import io.scriptor.imgui.component.NodeEditor;
import io.scriptor.nodes.Graph;

import java.util.Date;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

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

    private final EventManager events = new EventManager();
    private final ResourceManager resources = new ResourceManager();
    private final Layout layout;
    private final Graph graph;

    public MainApp() {
        layout = resources.parseLayout(events, "main.yml");

        final NodeEditor editor = layout.findElement("editor.editor");
        graph = editor.getGraph();

        final var andNode = graph.createNode("And");
        andNode.createIn("In A");
        andNode.createIn("In B");
        final var andOut = andNode.createOut("Out");

        final var notNode = graph.createNode("Not");
        final var notIn = notNode.createIn("In");
        notNode.createOut("Out");

        graph.createLink(andOut, notIn);
    }

    @Override
    protected void preRun() {
        final var io = ImGui.getIO();
        io.addConfigFlags(ImGuiConfigFlags.DockingEnable);
        io.addConfigFlags(ImGuiConfigFlags.NavEnableKeyboard);

        ImNodes.createContext();
    }

    @Override
    protected void postRun() {
        ImNodes.destroyContext();
    }

    @Override
    public void process() {
        layout.show();
    }
}
