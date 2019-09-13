package adm2e.tsp.demos;

import adm2e.tsp.HeuristicTspSolver;
import adm2e.tsp.demos.ioutils.DecisionRuleBuilder;
import adm2e.tsp.demos.ioutils.SettingsForMode;
import adm2e.tsp.demos.ioutils.TspCommandLineParser;
import adm2e.tsp.representations.TspSolution;
import adm2e.tsp.rules.DecisionRule;

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

public class DemoTspSolver {

    public static void main(String[] args) throws IOException {
        // Parse command line.
        SettingsForMode settings = TspCommandLineParser.parse(args);
        if (settings == null) return; // unable to parse command line
        Supplier<DecisionRule> decisionRuleSupplier =
            settings.buildDecisionRuleSupplier(new DecisionRuleBuilder());
        int numTrials = settings.getNumTrials();

        Stream<String> lines = readRawInput(settings.getInputFile());

        // Parse file input.
        Map<String, Map<String, Double>> labelToLabelToDistance = new HashMap<>();
        Set<String> labelCollector = new HashSet<>();
        processLines(lines, labelCollector, labelToLabelToDistance);
        String[] labels = labelCollector.toArray(new String[0]);
        Arrays.sort(labels);

        // Build data structure that the solver uses.
        double[][] distances = buildEdgeWeightMatrix(labels, labelToLabelToDistance);

        // Initialize and run the solver some number of times.
        TspSolution bestSolution = null;
        double bestCost = Double.MAX_VALUE;
        HeuristicTspSolver solver =
            HeuristicTspSolver.create(labels, distances, decisionRuleSupplier);
        for (int i = 0; i < numTrials; i++) {
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
