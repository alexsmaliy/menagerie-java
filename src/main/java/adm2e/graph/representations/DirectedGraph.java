package adm2e.graph.representations;

import gnu.trove.TIntCollection;

import java.util.Optional;

public interface DirectedGraph {

    int NO_SUCH_NODE = -1;

    void addEdge(String from, String to, boolean directed);
    int numVertices();
    Optional<String> labelOfVertex(int vertex);
    TIntCollection childrenOfVertex(int vertex, boolean deduplicateEdges);

    @FunctionalInterface
    interface EdgeConsumer {
        void consume(int from, int to);
    }

    interface VisitTracker {
        void markVisited(int i);
        boolean checkVisited(int i);
    }
}
