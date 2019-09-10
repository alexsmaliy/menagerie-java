package adm2e.tsp.demos;

import adm2e.tsp.HeuristicTspSolver;
import adm2e.tsp.representations.TspSolution;
import adm2e.tsp.rules.DecisionRule;
import adm2e.tsp.rules.GreedyRule;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static adm2e.tsp.demos.ioutils.TspIoUtils.buildEdgeWeightMatrix;
import static adm2e.tsp.demos.ioutils.TspIoUtils.processLines;
import static adm2e.tsp.demos.ioutils.TspIoUtils.readRawInput;

/**
 * A naive greedy TSP that starts from a random solution
 * and makes the most advantageous pairwise vertex swap
 * until it gets stuck in a local minimum.
 */
public final class GreedySearch {

    // Pick criterion for accepting candidate solutions.
    // The greedy rule only picks solutions that are strictly an improvement.
    // Always return the same stateless greedy rule.
    private static Supplier<DecisionRule> DECISION_RULE_SUPPLIER = () -> GreedyRule.INSTANCE;

    private static void usage() {
        System.err.println("Usage: "
            + GreedySearch.class.getName()
            + " <path to input file> <number of retries>\n"
            + "Basic test datasets are available in data/tsp.");
    }

    public static void main(String[] args) throws IOException {
        // Get input.
        if (args.length != 2) {
            usage();
            return;
        }
        Stream<String> lines = readRawInput(args[0]);
        int numRetries = Integer.parseInt(args[1]);

        // Initialize collections for parsing input.
        Map<String, Map<String, Double>> labelToLabelToDistance = new HashMap<>();
        Set<String> labelCollector = new HashSet<>();

        // Populate collections from input.
        processLines(lines, labelCollector, labelToLabelToDistance);

        // Reshape collected input.
        String[] labels = labelCollector.toArray(new String[0]);
        Arrays.sort(labels);
        double[][] distances = buildEdgeWeightMatrix(labels, labelToLabelToDistance);

        // Run the solver several times and pick the best candidate solution.
        TspSolution bestSolution = null;
        double bestCost = Double.MAX_VALUE;
        HeuristicTspSolver solver =
            HeuristicTspSolver.create(labels, distances, DECISION_RULE_SUPPLIER);
        for (int i = 0; i < numRetries; i++) {
            solver = solver.reinitializedCopy();
            TspSolution solution = solver.getFixedPointSolution();
            if (solution.getCost() < bestCost) {
                bestCost = solution.getCost();
                bestSolution = solution;
            }
        }
        System.out.println(bestSolution);
    }
}
