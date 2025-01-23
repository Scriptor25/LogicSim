package io.scriptor.context;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class State {

    private final Registry registry;

    private final Map<UUID, Boolean> attribs = new HashMap<>();
    private final Map<UUID, Map<Integer, Boolean>> regs = new HashMap<>();

    private final Map<Integer, State> children = new HashMap<>();
    private final Map<UUID, boolean[]> results = new HashMap<>();

    public State(final @NotNull Registry registry) {
        this.registry = registry;
    }

    public @NotNull State child(final int hash) {
        return children.computeIfAbsent(hash, key -> new State(registry));
    }

    public void setAttrib(final @NotNull UUID attrib, final boolean value) {
        attribs.put(attrib, value);
    }

    public boolean getAttrib(final @NotNull UUID attrib) {
        return attribs.computeIfAbsent(attrib, key -> false);
    }

    public void setReg(final @NotNull UUID reg, final int index, final boolean value) {
        regs.computeIfAbsent(reg, key -> new HashMap<>()).put(index, value);
    }

    public boolean getReg(final @NotNull UUID reg, final int index) {
        return regs.computeIfAbsent(reg, key -> new HashMap<>()).computeIfAbsent(index, key -> false);
    }

    public boolean call(final @NotNull UUID uuid, int hash, final @NotNull UUID callee, final boolean @NotNull [] args) {
        return registry.get(callee).map(fn -> {
            results.put(uuid, new boolean[fn.numOutputs()]);
            fn.exec(child(hash), hash, args, results.get(uuid));
            return true;
        }).orElse(false);
    }

    public boolean getResult(final @NotNull UUID uuid, final int index) {
        return results.get(uuid)[index];
    }
}
