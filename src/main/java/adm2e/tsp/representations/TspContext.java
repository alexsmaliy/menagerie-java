package adm2e.tsp.representations;

import adm2e.tsp.rules.DecisionRule;

import java.util.Arrays;

/**
 * Stores the search space for a TSP problem.
 */
public final class TspContext {
    private final String[] vertexLabels;
    private final double[][] vertexDistances;
    private final DecisionRule decisionRule;

    public TspContext(String[] vertexLabels,
                       double[][] vertexDistances,
                       DecisionRule decisionRule) {
        if (vertexLabels.length != vertexDistances.length
            || vertexDistances.length != vertexDistances[0].length) {
            throw new IllegalArgumentException(
                "Given N vertices, the matrix of distances between them should be NxN.");
        }

        int numVertices = vertexLabels.length;
        this.vertexLabels = Arrays.copyOf(vertexLabels, numVertices);
        this.vertexDistances = copySquareSymmetricArray(vertexDistances);
        this.decisionRule = decisionRule;
    }

    public int getNumVertices() {
        return vertexLabels.length;
    }

    public String[] getVertexLabels() {
        return Arrays.copyOf(vertexLabels, vertexLabels.length);
    }

    public String getVertexLabel(int i) {
        return vertexLabels[i];
    }

    private static double[][] copySquareSymmetricArray(double[][] orig) {
        int size = orig.length;
        double[][] copy = new double[size][size];
        for (int i = 0; i < size; i++) {
            for (int j = i + 1; j < size; j++) {
                copy[i][j] = orig[i][j];
                copy[j][i] = copy[i][j];
            }
        }
        return copy;
    }

    /**
     * Edge weight between two vertices.
     */
    public double getDistance(int i, int j) {
        return vertexDistances[i][j];
    }

    public double[][] getVertexDistances() {
        return copySquareSymmetricArray(vertexDistances);
    }

    public DecisionRule getDecisionRule() {
        return decisionRule;
    }

    public double getPathCost(int[] vertexVisitOrder) {
        double cost = 0;
        // Add up distances between successive visited vertices.
        for (int i = 0; i < vertexVisitOrder.length - 1; i++) {
            cost += getDistance(vertexVisitOrder[i], vertexVisitOrder[i + 1]);
        }
        // Add final leg between last visited vertex and initial vertex.
        cost += getDistance(vertexVisitOrder[getNumVertices() - 1],
            vertexVisitOrder[0]);
        return cost;
    }
}
