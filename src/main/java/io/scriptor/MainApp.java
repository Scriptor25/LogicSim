package io.scriptor;

import imgui.ImGui;
import imgui.app.Application;
import imgui.extension.imnodes.ImNodes;
import imgui.flag.ImGuiConfigFlags;
import io.scriptor.imgui.Layout;
import io.scriptor.manager.EventManager;
import io.scriptor.manager.ResourceManager;

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

    public MainApp() {
        layout = resources.parseLayout(events, "layout/main.yml");
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
