package io.scriptor.node;

import io.scriptor.Context;
import io.scriptor.util.IUnique;

import java.io.PrintWriter;
import java.util.Optional;
import java.util.Queue;

public interface INode extends IUnique {

    default int id() {
        return hashCode();
    }

    Pin input(final int i);

    Pin output(final int i);

    boolean powered(final int i);

    Optional<Pin> pin(final int id);

    boolean noPredecessor(final Graph graph);

    void show();

    INode copy();

    void write(final Context context, final PrintWriter out);

    void cycle(final Graph graph, final Queue<INode> callQueue);
}
