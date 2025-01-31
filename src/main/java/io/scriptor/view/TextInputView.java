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

    private String hint;

    public TextInputView(final @NotNull EventManager events, final @NotNull Consumer<String> enter) {
        super(events);
        this.enter = enter;
    }

    public void hint(final @NotNull String hint) {
        this.hint = hint;
    }

    @Override
    public void show() {
        ImGui.setKeyboardFocusHere();
        if (ImGui.inputTextWithHint(
                "##label",
                hint,
                temporary,
                ImGuiInputTextFlags.EnterReturnsTrue))
            enter.accept(temporary.get());
        ImGui.setItemDefaultFocus();
    }
}
