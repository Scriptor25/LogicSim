package io.scriptor.view;

import imgui.ImGui;
import io.scriptor.event.EventManager;
import org.jetbrains.annotations.NotNull;

import static io.scriptor.util.Constants.*;

public class SimulationView extends View {

    public SimulationView(final @NotNull EventManager events) {
        super(events);
    }

    @Override
    public void show() {
        if (!ImGui.begin("Simulation")) {
            ImGui.end();
            return;
        }

        {
            final var value = new int[1];
            value[0] = TICKS_PER_FRAME;
            ImGui.sliderInt("Ticks Per Frame", value, 1, 1000);
            if (ImGui.isItemHovered())
                ImGui.setTooltip("controls by how much time is changing every frame");
            TICKS_PER_FRAME = value[0];
        }

        {
            final var value = new int[1];
            value[0] = TICKS_PER_CLOCK;
            ImGui.sliderInt("Ticks Per Clock", value, 1, 1000);
            if (ImGui.isItemHovered())
                ImGui.setTooltip("controls on which tick count the clock will trigger");
            TICKS_PER_CLOCK = value[0];
        }

        {
            final var value = new int[1];
            value[0] = TICK_THRESHOLD;
            ImGui.sliderInt("Tick Threshold", value, 1, 1000);
            if (ImGui.isItemHovered())
                ImGui.setTooltip("controls how many ticks the clock will be high");
            TICK_THRESHOLD = value[0];
        }

        events.runTasks(this);
        ImGui.end();
    }
}
