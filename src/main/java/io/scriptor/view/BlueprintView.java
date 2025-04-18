package io.scriptor.view;

import imgui.ImGui;
import imgui.flag.ImGuiKey;
import imgui.flag.ImGuiMouseButton;
import io.scriptor.context.Context;
import io.scriptor.event.EventManager;
import io.scriptor.graph.Blueprint;
import io.scriptor.graph.Graph;
import io.scriptor.util.Range;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;

import static io.scriptor.util.Constants.*;

public class BlueprintView extends View {


    private final ListView<Blueprint> blueprintListView;
    private final TextInputView labelView;
    private final ColorInputView colorView;

    private final Popup blueprintPopup;
    private final Popup labelPopup;
    private final Popup colorPopup;

    private final Context context;

    private Blueprint selectedBlueprint;

    public BlueprintView(final @NotNull EventManager events, final @NotNull Context context) {
        super(events);

        this.context = context;

        blueprintListView = new ListView<>(
                events,
                new Range<>(context.blueprints())
                        .sorted(Comparator.comparing(Blueprint::label)),
                this::onBlueprintSelected,
                blueprint -> {
                    ImGui.selectable(blueprint.label().get());
                    return ImGui.isItemHovered();
                });
        labelView = new TextInputView(events, this::onLabelEnter);
        colorView = new ColorInputView(events, color -> selectedBlueprint.baseColor().set(color));
        blueprintPopup = new Popup(events, this::showBlueprintContext);
        labelPopup = new Popup(events, labelView::show);
        colorPopup = new Popup(events, colorView::show);
    }

    private void onBlueprintSelected(final @NotNull Blueprint blueprint) {
        selectedBlueprint = blueprint;
        if (ImGui.isMouseClicked(ImGuiMouseButton.Right) || ImGui.isKeyPressed(ImGuiKey.Menu))
            events.scheduleTask(blueprintPopup::open);
        if (blueprint.editable() && ImGui.isMouseClicked(ImGuiMouseButton.Left))
            events.callService(ID_BLUEPRINT_EDIT, blueprint);
        if (blueprint.editable()) {
            ImGui.beginTooltip();
            ImGui.textUnformatted("Dependencies:");
            for (final var other : context.blueprints())
                if (other != blueprint && blueprint.uses(other, false)) {
                    ImGui.bullet();
                    ImGui.textUnformatted(other.label().get());
                }
            ImGui.endTooltip();
        }
    }

    private void onLabelEnter(final @NotNull String label) {
        if (!label.isEmpty())
            selectedBlueprint.label().set(label, true);
        ImGui.closeCurrentPopup();
    }

    private void showBlueprintContext() {
        if (ImGui.selectable("Set Label")) {
            labelView.value(selectedBlueprint.label().get());
            events.scheduleTask(labelPopup::open);
        }
        if (ImGui.selectable("Set Color")) {
            colorView.value(selectedBlueprint.baseColor());
            events.scheduleTask(colorPopup::open);
        }
        ImGui.beginDisabled(!selectedBlueprint.editable());
        if (ImGui.selectable("Edit"))
            events.callService(ID_BLUEPRINT_EDIT, selectedBlueprint);
        ImGui.beginDisabled(selectedBlueprint.used());
        if (ImGui.selectable("Delete"))
            events.callService(ID_BLUEPRINT_DELETE, selectedBlueprint);
        ImGui.endDisabled();
        ImGui.endDisabled();
    }

    @Override
    public void show() {
        if (!ImGui.begin("Blueprints")) {
            ImGui.end();
            return;
        }

        if (ImGui.button("Add Blueprint"))
            events.callService(
                    ID_BLUEPRINT_NEW,
                    new Blueprint.Builder()
                            .label("New Blueprint")
                            .source(new Graph(context))
                            .build(context));

        if (ImGui.beginChild("blueprints"))
            blueprintListView.show();
        ImGui.endChild();

        events.runTasks(this);
        ImGui.end();

        blueprintPopup.show();
        labelPopup.show();
        colorPopup.show();
    }
}
