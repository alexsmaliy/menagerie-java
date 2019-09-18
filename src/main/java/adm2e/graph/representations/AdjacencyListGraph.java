package adm2e.graph.representations;

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AdjacencyListGraph {
    private final List<TIntArrayList> edges;
    private final TIntObjectHashMap<String> keyToLabel;
    private final TObjectIntHashMap<String> labelToKey;

    public AdjacencyListGraph() {
        this.edges = new ArrayList<>();
        this.keyToLabel = new TIntObjectHashMap<>();
        this.labelToKey = new TObjectIntHashMap<>();
    }

    public void addEdge(String from, String to, boolean directed) {
        maybeInitializeLabel(from);
        maybeInitializeLabel(to);
        int keyOfFrom = labelToKey.get(from);
        int keyOfTo = labelToKey.get(to);
        edges.get(keyOfFrom).add(keyOfTo);
        if (directed) edges.get(keyOfTo).add(keyOfFrom);
    }

    private void maybeInitializeLabel(String label) {
        if (!labelToKey.containsKey(label)) {
            labelToKey.put(label, labelToKey.size());
            keyToLabel.put(keyToLabel.size(), label);
            edges.add(new TIntArrayList());
        }
    }

    public int numVertices() {
        return edges.size();
    }

    public Optional<String> labelOfVertex(int vertex) {
        if (vertex < 0 || vertex >= numVertices()) return Optional.empty();
        return Optional.of(keyToLabel.get(vertex));
    }
}
