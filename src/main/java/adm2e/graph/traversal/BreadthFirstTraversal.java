package adm2e.graph.traversal;

import adm2e.graph.representations.DirectedGraph;
import adm2e.graph.representations.DirectedGraph.EdgeConsumer;
import adm2e.graph.representations.DirectedGraph.VisitTracker;
import gnu.trove.TIntCollection;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.list.linked.TIntLinkedList;

import java.util.function.IntPredicate;

public final class BreadthFirstTraversal {
    private static final IntPredicate NEVER_STOP = i -> false;

    /**
     * Traverses the graph breadth-first, exhaustively. The edge consumer is called with
     * every &lt;x, y&gt; edge whenever neither x nor y have been visited yet.
     */
    public static void traverse(DirectedGraph graph,
                                EdgeConsumer edgeConsumer) {
        traverse(
            graph,
            edgeConsumer,
            NEVER_STOP);
    }

    /**
     * Traverses the graph breadth-first, stopping whenever the traversal first reaches
     * an &lt;x, y&gt; edge the stopping condition responds TRUE for y. The edge consumer
     * is fed with every edge, including the one that causes traversal to stop.
     */
    public static void traverse(DirectedGraph graph,
                                EdgeConsumer edgeConsumer,
                                IntPredicate stoppingCondition) {
        traverse(
            graph,
            0,
            getDefaultVisitTracker(graph.numVertices()),
            edgeConsumer,
            stoppingCondition);
    }

    /**
     * Traverses the graph breadth-first, starting with the specified valid vertex index,
     * and a visit tracker provided by the caller. The visit tracker can be used to
     * blacklist certain nodes from traversal. For example, it can be used to find
     * connected components by building up a list of visited vertices, and then calling
     * this method again with the partially completed list and a new, unvisited starting
     * vertex.
     */
    public static void traverse(DirectedGraph graph,
                                int startingVertex,
                                VisitTracker visitTracker,
                                EdgeConsumer edgeConsumer,
                                IntPredicate stoppingCondition) {
        int numVertices = graph.numVertices();
        if (startingVertex < 0 || startingVertex >= numVertices) return;
        TIntLinkedList toVisit = new TIntLinkedList();
        toVisit.add(startingVertex);
        traverse(
            graph,
            getDefaultVisitTracker(numVertices),
            toVisit,
            edgeConsumer,
            stoppingCondition);
    }

    private static void traverse(DirectedGraph graph,
                                 VisitTracker visitTracker,
                                 TIntLinkedList toVisit,
                                 EdgeConsumer edgeConsumer,
                                 IntPredicate stoppingCondition) {
        boolean stop = false;
        do {
            int currentVertex = toVisit.removeAt(0);
            visitTracker.markVisited(currentVertex);
            TIntCollection children = graph.childrenOfVertex(currentVertex, true);
            TIntIterator iterator = children.iterator();
            for (int i = 0; i < children.size(); i++) {
                int child = iterator.next();
                if (!visitTracker.checkVisited(child)) {
                    toVisit.add(child);
                    edgeConsumer.consume(currentVertex, child);
                    if (stoppingCondition.test(child)) {
                        stop = true;
                        break;
                    }
                }
            }
        } while (!toVisit.isEmpty() && !stop);
    }

    private static VisitTracker getDefaultVisitTracker(int numVertices) {
        boolean[] wasVisited = new boolean[numVertices];
        return new VisitTracker() {
            @Override
            public void markVisited(int i) {
                if (i >= 0 && i < numVertices) wasVisited[i] = true;
            }

            @Override
            public boolean checkVisited(int i) {
                if (i >= 0 && i < numVertices) return wasVisited[i];
                else return false;
            }
        };
    }
}
