package adm2e.graph.representations;

import gnu.trove.TIntCollection;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.set.hash.TIntHashSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AdjacencyListGraph implements DirectedGraph {
    private final List<TIntArrayList> edges;
    private final TIntObjectHashMap<String> keyToLabel;
    private final TObjectIntHashMap<String> labelToKey;

    public AdjacencyListGraph() {
        this.edges = new ArrayList<>();
        this.keyToLabel = new TIntObjectHashMap<>();
        this.labelToKey = new TObjectIntHashMap<>();
    }

    @Override
    public void addEdge(String from, String to, boolean directed) {
        maybeInitializeLabel(from);
        maybeInitializeLabel(to);
        int keyOfFrom = labelToKey.get(from);
        int keyOfTo = labelToKey.get(to);
        edges.get(keyOfFrom).add(keyOfTo);
        if (!directed) edges.get(keyOfTo).add(keyOfFrom);
    }

    private void maybeInitializeLabel(String label) {
        if (!labelToKey.containsKey(label)) {
            labelToKey.put(label, labelToKey.size());
            keyToLabel.put(keyToLabel.size(), label);
            edges.add(new TIntArrayList());
        }
    }

    @Override
    public int numVertices() {
        return edges.size();
    }

    @Override
    public Optional<String> labelOfVertex(int vertex) {
        return Optional.ofNullable(keyToLabel.get(vertex));
    }

    @Override
    public TIntCollection childrenOfVertex(int vertex, boolean deduplicateEdges) {
        if (vertex < 0 || vertex >= numVertices()) return new TIntArrayList();
        if (deduplicateEdges) return new TIntHashSet(edges.get(vertex));
        else return new TIntArrayList(edges.get(vertex));
    }
}
