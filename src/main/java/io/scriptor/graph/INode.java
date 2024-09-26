package io.scriptor.graph;

import io.scriptor.instruction.Instruction;
import io.scriptor.util.IUnique;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface INode extends IUnique {

    default int id() {
        return uuid().hashCode();
    }

    Pin input(final int i);

    Pin output(final int i);

    boolean powered(Graph graph, final boolean output, final int index);

    Optional<Pin> pin(final int id);

    boolean noPredecessor(final Graph graph);

    boolean noSuccessors(final Graph graph);

    List<INode> successors(final Graph graph);

    void show(final Graph graph);

    INode copy();

    void compile(final Graph graph, final Collection<Instruction> instructions, final Set<INode> compiled);

    boolean[] exec(final Graph graph, final Set<INode> executing);
}
