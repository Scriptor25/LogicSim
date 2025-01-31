package io.scriptor.view;

import imgui.ImGui;
import io.scriptor.context.Context;
import io.scriptor.event.EventManager;
import io.scriptor.event.IPayload;
import io.scriptor.graph.Blueprint;
import io.scriptor.graph.Graph;
import io.scriptor.util.Range;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;

import static io.scriptor.util.Constants.*;

public class BlueprintView extends View {

    public record Payload(@NotNull Blueprint value) implements IPayload {
    }

    private final ListView<Blueprint> blueprintListView;
    private final TextInputView labelTextInputView;
    private final ColorInputView colorInputView;

    private final PopupView blueprintPopupView;
    private final PopupView labelPopupView;
    private final PopupView colorPopupView;

    private final Context context;

    private Blueprint selectedBlueprint;

    public BlueprintView(final @NotNull EventManager events, final @NotNull Context context) {
        super(events);

        this.context = context;

        labelTextInputView = new TextInputView(events, label -> {
            if (!label.isEmpty())
                selectedBlueprint.label().set(label, true);
            ImGui.closeCurrentPopup();
        });
        labelPopupView = new PopupView(events, labelTextInputView::show);
        colorInputView = new ColorInputView(events, color -> selectedBlueprint.baseColor().set(color));
        colorPopupView = new PopupView(events, colorInputView::show);
        blueprintPopupView = new PopupView(events, () -> {
            if (ImGui.selectable("Set Label")) {
                labelTextInputView.value(selectedBlueprint.label().get());
                events.scheduleTask(labelPopupView::open);
            }
            if (ImGui.selectable("Set Color")) {
                colorInputView.value(selectedBlueprint.baseColor());
                events.scheduleTask(colorPopupView::open);
            }
            if (selectedBlueprint.editable()) {
                if (ImGui.selectable("Edit"))
                    events.callVoidService(ID_BLUEPRINT_EDIT, new Payload(selectedBlueprint));
                if (ImGui.selectable("Delete"))
                    events.callVoidService(ID_BLUEPRINT_DELETE, new Payload(selectedBlueprint));
            }
        });
        blueprintListView = new ListView<>(
                events,
                new Range<>(context.blueprints())
                        .sorted(Comparator.comparing(Blueprint::label)),
                blueprint -> {
                    selectedBlueprint = blueprint;
                    events.scheduleTask(blueprintPopupView::open);
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
            events.callVoidService(
                    ID_BLUEPRINT_NEW,
                    new Payload(new Blueprint.Builder()
                            .label("New Blueprint")
                            .source(new Graph(context))
                            .build()));

        blueprintListView.show();
        events.runTasks(this);
        ImGui.end();

        blueprintPopupView.show();
        labelPopupView.show();
        colorPopupView.show();
    }
}
