package adm2e.tsp.demos;

import adm2e.tsp.HeuristicTspSolver;
import adm2e.tsp.representations.TspSolution;
import adm2e.tsp.rules.AnnealingRule;
import adm2e.tsp.rules.DecisionRule;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

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
 * <p>A simple "annealing" TSP solver that always accepts improved solutions
 * and sometimes accepts bad solutions. Slightly worse bad solutions are
 * more acceptable than greatly worse solutions. The threshold for acceptance
 * also becomes more strict over time.</p>
 * <br />
 * <p>This solver works best for non-trivial problem spaces, such as the tsp_48.txt
 * sample dataset.</p>
 */
public final class AnnealingSearch {

    // Metaparameter: the number of consecutive times the decision rule accepts a move
    // before we make the acceptance criterion more strict. Note tht at the start of
    // the search, we accept almost any move, so, at least initially, there are many
    // consecutive acceptances. Also, every time we accept a worse solution at random,
    // several subsequent solutions will also be improvements, providing opportunities
    // to reduce the temperature even late in the search.
    private static final int CONSECUTIVE_ACCEPTS_BEFORE_TEMP_REDUCED = 20;
    // Metaparameter: how many search iterations must result in approximately the same
    // outcome (within a certain relative tolerance) before we declare the search done.
    private static final int MAX_CONSECUTIVE_SAME_CURRENT_COST = 5;
    // The number of times we start in a random position in the search space. This demo
    // returns the best result among all attempts undertaken.
    private static final int DEFAULT_NUM_TRIALS = 1;

    public static void main(String[] args) throws IOException {

        CommandLineParser cli = CommandLineParser.parse(args);
        if (cli == CommandLineParser.BAD_RESULT) return;

        Stream<String> lines = readRawInput(cli.inputFile);

        // Initialize collections for parsing input.
        Map<String, Map<String, Double>> labelToLabelToDistance = new HashMap<>();
        Set<String> labelCollector = new HashSet<>();

        // Populate collections from input.
        processLines(lines, labelCollector, labelToLabelToDistance);

        // Reshape collected input.
        String[] labels = labelCollector.toArray(new String[0]);
        Arrays.sort(labels);
        double[][] distances = buildEdgeWeightMatrix(labels, labelToLabelToDistance);

        // Pick criterion for accepting candidate solutions.
        // The annealing rule explores the solution space more diligently by
        // occasionally picking suboptimal solutions to avoid getting stuck.
        // This solution rule is stateful, so the supplier creates distinct
        // instances on demand.
        Supplier<DecisionRule> decisionRuleSupplier = () ->
            new AnnealingRule(cli.tempReductionAfter, cli.stuckAfter);

        // Run the solver several times and pick the best candidate solution.
        TspSolution bestSolution = null;
        double bestCost = Double.MAX_VALUE;
        HeuristicTspSolver solver =
            HeuristicTspSolver.create(labels, distances, decisionRuleSupplier);
        for (int i = 0; i < cli.numTrials; i++) {
            solver = solver.reinitializedCopy();
            TspSolution solution = solver.getFixedPointSolution();
            if (solution.getCost() < bestCost) {
                bestCost = solution.getCost();
                bestSolution = solution;
            }
        }
        System.out.println(bestSolution);
    }

    private static void usage() {
        String command = AnnealingSearch.class.getName() + " INPUT_FILE";
        new HelpFormatter().printHelp(
            120, // width
            command,
            null, // header
            CommandLineParser.getOptions(),
            null, // footer
            true); // do print usage example
    }

    // Encapsulates the somewhat elaborate input parsing for this demo.
    private static final class CommandLineParser {
        private static final CommandLineParser BAD_RESULT = null;

        private static final String TEMP_REDUCTION_FLAG = "r";
        private static final String STUCK_FLAG = "s";
        private static final String NUM_TRIALS_FLAG = "n";

        private final int tempReductionAfter;
        private final int stuckAfter;
        private final int numTrials;
        private final String inputFile;

        private CommandLineParser(int tempReductionAfter,
                                  int stuckAfter,
                                  int numTrials,
                                  String inputFile) {
            this.tempReductionAfter = tempReductionAfter;
            this.stuckAfter = stuckAfter;
            this.numTrials = numTrials;
            this.inputFile = inputFile;
        }

        private static CommandLineParser parse(String[] args) {
            CommandLine cli;
            try {
                cli = new DefaultParser().parse(getOptions(), args, false);
            } catch (ParseException e) {
                usage();
                return BAD_RESULT;
            }

            int tempReductionAfter;
            if (cli.hasOption(TEMP_REDUCTION_FLAG)) {
                tempReductionAfter = Integer.parseInt(cli.getOptionValue(TEMP_REDUCTION_FLAG));
            } else {
                tempReductionAfter = CONSECUTIVE_ACCEPTS_BEFORE_TEMP_REDUCED;
            }
            int stuckAfter;
            if (cli.hasOption(STUCK_FLAG)) {
                stuckAfter = Integer.parseInt(cli.getOptionValue("s"));
            } else {
                stuckAfter = MAX_CONSECUTIVE_SAME_CURRENT_COST;
            }
            int numTrials;
            if (cli.hasOption(NUM_TRIALS_FLAG)) {
                numTrials = Integer.parseInt(cli.getOptionValue(NUM_TRIALS_FLAG));
            } else {
                numTrials = DEFAULT_NUM_TRIALS;
            }
            String inputFile;
            if (cli.getArgs().length == 0) {
                usage();
                return BAD_RESULT;
            } else {
                inputFile = cli.getArgs()[0];
            }
            return new CommandLineParser(tempReductionAfter, stuckAfter, numTrials, inputFile);
        }

        private static Options getOptions() {
            Options options = new Options();
            Option tempReductionThreshold = new Option(
                TEMP_REDUCTION_FLAG,
                "reduce-after",
                true, // option does have a value
                "reduce temperature after this many consecutively accepted state transitions");
            Option stuckCondition = new Option(
                STUCK_FLAG,
                "stop-after",
                true, // option does have a value
                "stop after this many consecutive iterations with no improvement in cost"
            );
            Option numRetries = new Option(
                NUM_TRIALS_FLAG,
                "num-trials",
                true,
                "number of trials to run"
            );
            options.addOption(tempReductionThreshold);
            options.addOption(stuckCondition);
            options.addOption(numRetries);
            return options;
        }
    }
}
