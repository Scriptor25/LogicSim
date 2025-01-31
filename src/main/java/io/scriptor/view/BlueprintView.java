package io.scriptor.view;

import imgui.ImGui;
import io.scriptor.context.Context;
import io.scriptor.event.EventManager;
import io.scriptor.graph.Blueprint;
import io.scriptor.graph.Graph;
import io.scriptor.util.Range;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.function.Consumer;

public class BlueprintView extends View {

    private final ListView<Blueprint> blueprintView;
    private final TextInputView setLabelView;
    private final ColorInputView setColorView;

    private final PopupView blueprintContext;
    private final PopupView setLabelContext;
    private final PopupView setColorContext;

    private final Context context;
    private final Consumer<Blueprint> create;
    private final Consumer<Blueprint> edit;

    private Blueprint selectedBlueprint;

    public BlueprintView(final @NotNull EventManager events,
                         final @NotNull Context context,
                         final @NotNull Consumer<Blueprint> create,
                         final @NotNull Consumer<Blueprint> edit) {
        super(events);

        this.context = context;
        this.create = create;
        this.edit = edit;

        setLabelView = new TextInputView(events, label -> {
            if (!label.isEmpty())
                selectedBlueprint.label().set(label, true);
            ImGui.closeCurrentPopup();
        });
        setLabelContext = new PopupView(events, setLabelView::show);
        setColorView = new ColorInputView(events, color -> selectedBlueprint.baseColor().set(color));
        setColorContext = new PopupView(events, setColorView::show);
        blueprintContext = new PopupView(events, () -> {
            if (ImGui.selectable("Set Label")) {
                setLabelView.value(selectedBlueprint.label().get());
                events.scheduleTask(setLabelContext::open);
            }
            if (ImGui.selectable("Set Color")) {
                setColorView.value(selectedBlueprint.baseColor());
                events.scheduleTask(setColorContext::open);
            }
            if (selectedBlueprint.editable()) {
                if (ImGui.selectable("Edit"))
                    edit.accept(selectedBlueprint);
                if (ImGui.selectable("Delete"))
                    events.scheduleTask(() -> context.remove(selectedBlueprint));
            }
        });
        blueprintView = new ListView<>(
                events,
                new Range<>(context.blueprints())
                        .sorted(Comparator.comparing(Blueprint::label)),
                blueprint -> {
                    selectedBlueprint = blueprint;
                    events.scheduleTask(blueprintContext::open);
                },
                blueprint -> ImGui.selectable(blueprint.label().get()));
    }

    @Override
    public void show() {
        if (!ImGui.begin("Blueprints")) {
            ImGui.end();
            return;
        }

        if (ImGui.button("Add Blueprint"))
            create.accept(new Blueprint.Builder()
                    .label("New Blueprint")
                    .source(new Graph(context))
                    .build(context));

        blueprintView.show();
        events.runTasks(this);
        ImGui.end();

        blueprintContext.show();
        setLabelContext.show();
        setColorContext.show();
    }
}
