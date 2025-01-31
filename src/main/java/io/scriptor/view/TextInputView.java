package io.scriptor.view;

import imgui.ImGui;
import imgui.flag.ImGuiInputTextFlags;
import imgui.type.ImString;
import io.scriptor.event.EventManager;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class TextInputView extends View {

    private final ImString temporary = new ImString();
    private final Consumer<String> enter;

    private String value;

    public TextInputView(final @NotNull EventManager events, final @NotNull Consumer<String> enter) {
        super(events);
        this.enter = enter;
    }

    public void value(final @NotNull String value) {
        this.value = value;
        this.temporary.clear();
    }

    @Override
    public void show() {
        ImGui.setKeyboardFocusHere();
        if (ImGui.inputTextWithHint(
                "##label",
                value,
                temporary,
                ImGuiInputTextFlags.EnterReturnsTrue))
            enter.accept(temporary.get());
        ImGui.setItemDefaultFocus();
    }
}
