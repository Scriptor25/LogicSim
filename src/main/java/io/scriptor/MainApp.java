package io.scriptor;

import imgui.ImColor;
import imgui.ImGui;
import imgui.app.Application;
import imgui.extension.imnodes.ImNodes;
import imgui.flag.ImGuiConfigFlags;
import imgui.type.ImString;
import io.scriptor.imgui.Array;
import io.scriptor.imgui.InputText;
import io.scriptor.imgui.Layout;
import io.scriptor.logic.Logic;
import io.scriptor.manager.EventManager;
import io.scriptor.manager.ResourceManager;
import io.scriptor.node.Attribute;
import io.scriptor.node.Blueprint;
import io.scriptor.util.Range;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Comparator;
import java.util.Date;
import java.util.UUID;
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

    private final Context context;

    private final EventManager events = new EventManager();
    private final ResourceManager resources = new ResourceManager();
    private final Layout layout;

    private Attribute selectedAttribute;
    private Blueprint selectedBlueprint;

    public MainApp() {
        if (!new File("blueprints").exists()) {
            context = new Context();
        } else {
            Context tmp;
            try {
                tmp = new Context("blueprints");
            } catch (final IOException e) {
                getLogger().warning(e::toString);
                tmp = new Context();
            }
            context = tmp;
        }

        context.add(new Attribute("In A", false));
        context.add(new Attribute("In B", false));
        context.add(new Attribute("Out", true));

        layout = resources.parseLayout(events, "layout/main.yml");
        final NodeEditor editor = layout.findElement("editor.editor");
        editor.blueprints(context.blueprints());
        editor.attributes(context.attributes());
    }

    @Override
    protected void preRun() {
        final var io = ImGui.getIO();
        io.addConfigFlags(ImGuiConfigFlags.DockingEnable);
        io.addConfigFlags(ImGuiConfigFlags.NavEnableKeyboard);
        ImNodes.createContext();

        layout.start();

        final var attributeRange = new Range<>(context.attributes());
        attributeRange.sorted(Comparator.comparing(Attribute::label));
        attributeRange.sorted(Comparator.comparing(Attribute::output));
        final Array attributeArray = layout.findElement("attributes.container.array");
        attributeArray.setRange(attributeRange);

        final var blueprintRange = new Range<>(context.blueprints());
        blueprintRange.sorted(Comparator.comparing(Blueprint::label));
        final Array blueprintArray = layout.findElement("blueprints.container.array");
        blueprintArray.setRange(blueprintRange);

        final NodeEditor editor = layout.findElement("editor.editor");

        events.register("attributes.add-input.click", args -> context.add(new Attribute("New IN", false)));
        events.register("attributes.add-output.click", args -> context.add(new Attribute("New OUT", true)));
        events.register("attributes.container.array.select", args -> {
            events.schedule(() -> ImGui.openPopup("attributes.attribute-context"));
            selectedAttribute = (Attribute) args[1];
        });
        events.register("attributes.attribute-context.rename.click", args -> {
            events.schedule(() -> ImGui.openPopup("attributes.rename-context"));
            final InputText text = layout.findElement("attributes.rename-context.text");
            text.set(selectedAttribute.label().get());
        });
        events.register("attributes.attribute-context.delete.click", args -> context.remove(selectedAttribute)); // TODO: remove all uses
        events.register("attributes.rename-context.text.enter", args -> {
            selectedAttribute.label().set((String) args[1], true);
            ImGui.closeCurrentPopup();
        });

        events.register("blueprints.create.click", args -> {
            final var blueprint = new Blueprint.Builder()
                    .label("NEW")
                    .baseColor(ImColor.rgb((float) Math.random(), (float) Math.random(), (float) Math.random()))
                    .inputLabels(context.attributes().stream().filter(x -> !x.output()).sorted(Comparator.comparing(Attribute::label)).map(Attribute::label).map(ImString::get).toArray(String[]::new))
                    .outputLabels(context.attributes().stream().filter(Attribute::output).sorted(Comparator.comparing(Attribute::label)).map(Attribute::label).map(ImString::get).toArray(String[]::new))
                    .logic(new Logic(UUID.randomUUID(), context.attributes().toArray(Attribute[]::new), editor.graph()))
                    .build();
            context.add(blueprint);
            editor.newGraph();
            context.attributes().clear();
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
        events.register("blueprints.blueprint-context.delete.click", args -> context.remove(selectedBlueprint)); // TODO: remove all uses
        events.register("blueprints.rename-context.text.enter", args -> {
            selectedBlueprint.label().set((String) args[1], true);
            ImGui.closeCurrentPopup();
        });
    }

    @Override
    protected void postRun() {
        ImNodes.destroyContext();

        try {
            context.write("blueprints");
        } catch (final FileNotFoundException e) {
            getLogger().warning(e::toString);
        }
    }

    @Override
    public void process() {
        layout.show();
    }
}
