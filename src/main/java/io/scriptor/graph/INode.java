package io.scriptor.graph;

import imgui.extension.imnodes.ImNodes;
import io.scriptor.instruction.Instruction;
import io.scriptor.util.IUnique;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface INode extends IUnique {

    default int id() {
        return uuid().hashCode();
    }

    default boolean notSelected() {
        return !ImNodes.isNodeSelected(id());
    }

    default void select() {
        ImNodes.selectNode(id());
    }

    @NotNull Pin input(final int i);

    @NotNull Pin output(final int i);

    boolean powered(final @NotNull Graph graph, final boolean output, final int index);

    @NotNull Optional<Pin> pin(final int id);

    boolean noPredecessor(final @NotNull Graph graph);

    boolean noSuccessors(final @NotNull Graph graph);

    @NotNull List<INode> successors(final @NotNull Graph graph);

    void show(final @NotNull Graph graph);

    @NotNull INode copy();

    void compile(final @NotNull Graph graph, final @NotNull Collection<Instruction> instructions, final @NotNull Set<INode> compiled);

    boolean @NotNull [] exec(final @NotNull Graph graph, final @NotNull Set<INode> executing);
}
