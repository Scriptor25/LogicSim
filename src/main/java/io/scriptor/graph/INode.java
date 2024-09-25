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

    Optional<Pin> pin(final int id);

    boolean noPredecessor(final Graph graph);

    boolean noSuccessors(final Graph graph);

    List<INode> successors(final Graph graph);

    void show(final Graph graph);

    INode copy();

    void compile(Graph graph, final Collection<Instruction> instructions, Set<INode> compiled);
}
