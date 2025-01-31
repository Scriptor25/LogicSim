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

    private final Context context;
    private final Consumer<Blueprint> create;

    private Blueprint selectedBlueprint;

    public BlueprintView(final @NotNull EventManager events, final @NotNull Context context, final @NotNull Consumer<Blueprint> create) {
        super(events);

        this.context = context;
        this.create = create;

        blueprintView = new ListView<>(
                events,
                new Range<>(context.blueprints())
                        .sorted(Comparator.comparing(Blueprint::label)),
                blueprint -> {
                    selectedBlueprint = blueprint;
                    events.scheduleTask(() -> ImGui.openPopup("blueprint"));
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

        ImGui.end();
    }
}
