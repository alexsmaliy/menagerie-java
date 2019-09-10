package adm2e.tsp.representations;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * A POJO representing a possible TSP solution, not necessarily a best solution.
 */
public final class TspSolution {
    private final int[] vertexVisitOrder;
    private final double cost;
    private final TspContext context;

    public TspSolution(TspContext context, int[] vertexVisitOrder) {
        this.vertexVisitOrder =
            Arrays.copyOf(vertexVisitOrder, vertexVisitOrder.length);
        this.cost = context.getPathCost(vertexVisitOrder);
        this.context = context;
    }

    public double getCost() {
        return cost;
    }

    public int[] getVertexVisitOrder() {
        return Arrays.copyOf(canonicalizeVertexOrder(), vertexVisitOrder.length);
    }

    @Override
    public String toString() {
        return "Visit order: ["
            + Arrays.stream(canonicalizeVertexOrder())
            .mapToObj(context::getVertexLabel)
            .collect(Collectors.joining(" -> "))
            + "]. Total cost: "
            + getCost();
    }

    // A copy of the vertex order with vertex 0 in position 0.
    private int[] canonicalizeVertexOrder() {
        int minIndex = 0;
        int numVertices = vertexVisitOrder.length;
        for (int i = 1; i < numVertices; i++) {
            if (vertexVisitOrder[i] < vertexVisitOrder[minIndex]) {
                minIndex = i;
            }
        }
        int[] canonical = new int[numVertices];
        System.arraycopy(vertexVisitOrder, minIndex, canonical, 0, numVertices - minIndex);
        System.arraycopy(vertexVisitOrder, 0, canonical, numVertices - minIndex, minIndex);
        return canonical;
    }
}
