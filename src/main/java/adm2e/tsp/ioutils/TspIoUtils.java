package adm2e.tsp.ioutils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

/**
 * <p>Just a bag of static methods for reading data from sample TSP data files of
 * the form:</p>
 * <table>
 *     <tr><td>VertexA</td><td>VertexB</td><td>3.5</td></tr>
 *     <tr><td>VertexA</td><td>VertexC</td><td>3.5</td></tr>
 *     <tr><td>VertexA</td><td>VertexD</td><td>3.5</td></tr>
 *     <tr><td>...</td><td>...</td><td>...</td></tr>
 * </table>
 * <p>Distance along every edge should be specified in the input file once, but
 * does not depend on order -- A-to-B or B-to-A are both fine.</p>
 */
public final class TspIoUtils {
    private TspIoUtils() { /* utility class */ }

    /**
     * Streams lines from specified input.
     */
    public static Stream<String> readRawInput(Path path) throws IOException {
        return Files.newBufferedReader(path)
                    .lines()
                    .filter(s -> !s.isEmpty());
    }

    public static void processLines(Stream<String> lines,
                                    Set<String> labelCollector,
                                    Map<String, Map<String, Double>> distancesCollector) {
        lines.forEach(line -> processLine(line, labelCollector, distancesCollector));
        // Input was invalid.
        if (labelCollector.isEmpty()) throw new RuntimeException("Input file was empty!");
    }

    /**
     * Tokenizes lines into vertex labels and distances. Builds up
     * a set of vertex labels and a map of vertex->vertex->distance.
     */
    private static void processLine(String line,
                                   Set<String> labelCollector,
                                   Map<String, Map<String, Double>> distances) {
        // Tokenize line on whitespace.
        String[] tokens = line.split("\\s+");
        // Select TO and FROM so that FROM < TO lexicographically.
        // This avoids having both A -> B and B -> A in the parsed input.
        String from = tokens[0].compareTo(tokens[1]) < 0
            ? tokens[0]
            : tokens[1];
        String to = tokens[0].compareTo(tokens[1]) < 0
            ? tokens[1]
            : tokens[0];
        Double distance = Double.parseDouble(tokens[2]);
        // Initialize bucket when needed.
        if (!distances.containsKey(from)) {
            distances.put(from, new HashMap<>());
        }
        // Throw if there are multiple records for A <-> B and they disagree.
        if (distances.get(from).containsKey(to)
            && !distances.get(from).get(to).equals(distance)) {
            throw new RuntimeException(
                "Encountered conflicting records for the edge between "
                    + from + " and " + to + "!");
        }

        distances.get(from).put(to, distance);
        labelCollector.add(from);
        labelCollector.add(to);
    }

    /**
     * A (symmetric) adjacency matrix with weights between nodes <i, j>.
     */
    public static double[][] buildEdgeWeightMatrix(String[] labels,
                                                   Map<String, Map<String, Double>> distances) {
        int numVertices = labels.length;
        double[][] edgeWeightMatrix = new double[numVertices][numVertices];
        for (int i = 0; i < numVertices - 1; i++) {
            for (int j = i + 1; j < numVertices; j++) {
                if (!distances.containsKey(labels[i])
                    || !distances.get(labels[i]).containsKey(labels[j])) {
                    throw new RuntimeException(
                        "Could not find distance between "
                            + labels[i] + " and " + labels[j] + "!");
                }
                edgeWeightMatrix[i][j] = distances.get(labels[i]).get(labels[j]);
                edgeWeightMatrix[j][i] = edgeWeightMatrix[i][j];
            }
        }
        return edgeWeightMatrix;
    }
}
