package io.scriptor.context;

import imgui.type.ImBoolean;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class State {

    private final Registry registry;

    private final Map<UUID, Boolean> attribs = new HashMap<>();
    private final Map<UUID, Map<Integer, Boolean>> regs = new HashMap<>();

    private final Map<UUID, boolean[]> results = new HashMap<>();

    public State(final Registry registry) {
        this.registry = registry;
    }

    public void setAttrib(final UUID attrib, final boolean value) {
        attribs.put(attrib, value);
    }

    public boolean getAttrib(final UUID attrib) {
        return attribs.computeIfAbsent(attrib, key -> false);
    }

    public void setReg(final UUID reg, final int index, final boolean value) {
        regs.computeIfAbsent(reg, key -> new HashMap<>()).put(index, value);
    }

    public boolean getReg(final UUID reg, final int index) {
        return regs.computeIfAbsent(reg, key -> new HashMap<>()).computeIfAbsent(index, key -> false);
    }

    public boolean call(final UUID uuid, final UUID callee, final boolean[] args) {
        final var result = new ImBoolean();
        registry.get(callee).ifPresentOrElse(fn -> {
            results.put(uuid, new boolean[fn.numOutputs()]);
            fn.exec(new State(registry), args, results.get(uuid));
            result.set(true);
        }, () -> result.set(false));
        return result.get();
    }

    public boolean getResult(final UUID uuid, final int index) {
        return results.get(uuid)[index];
    }
}
