package adm2e.tsp;

import adm2e.tsp.rules.DecisionRule;
import adm2e.tsp.representations.SolverAction;
import adm2e.tsp.representations.TspContext;
import adm2e.tsp.representations.TspSolution;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import adm2e.tsp.rules.DecisionRule.Decision;
import static adm2e.tsp.rules.DecisionRule.Decision.ACCEPT;

public final class HeuristicTspSolver {

    private final TspContext context;
    private int[] currentSolution;
    private boolean reachedFixedPoint;
    private final Supplier<DecisionRule> decisionRuleSupplier;

    private HeuristicTspSolver(TspContext context,
                               Supplier<DecisionRule> decisionRuleSupplier) {
        this.context = context;
        this.decisionRuleSupplier = decisionRuleSupplier;
        this.currentSolution = randomPermutation(context);
        this.reachedFixedPoint = false;
    }

    public static HeuristicTspSolver create(String[] labels,
                                            double[][] distances,
                                            Supplier<DecisionRule> decisionRuleSupplier) {
        TspContext context = new TspContext(labels, distances, decisionRuleSupplier.get());
        return new HeuristicTspSolver(context, decisionRuleSupplier);
    }

    /**
     * Create a copy of this solver with the same data and heuristic, but a
     * different initial solution that probably isn't stuck yet.
     */
    public HeuristicTspSolver reinitializedCopy() {
        TspContext copyContext = new TspContext(context.getVertexLabels(),
            context.getVertexDistances(),
            decisionRuleSupplier.get());
        return new HeuristicTspSolver(copyContext, decisionRuleSupplier);
    }

    // Initialize search with a random solution -- a more or less
    // uniformly random permutation of N vertices. We make a list
    // of longs whose last 3 decimal digits are 0, add the vertex
    // indices, sort the longs, and recover the vertex indices
    // using modulo division.
    // ASSUMPTION: there are N <= 1000 unique vertices in the input.
    private static int[] randomPermutation(TspContext context) {
        int numVertices = context.getNumVertices();
        Random random = ThreadLocalRandom.current();
        int[] path = new int[numVertices];
        long[] randomizer = new long[numVertices];
        for (int i = 0; i < numVertices; i++) {
            randomizer[i] = 1000L * random.nextInt(Integer.MAX_VALUE) + i;
        }
        Arrays.sort(randomizer);
        for (int i = 0; i < numVertices; i++) {
            path[i] = (int) (randomizer[i] % 1000);
        }
        return path;
    }

    /**
     * Runs this search instance until it reaches a fixed point.
     * and further iteration becomes fruitless.
     * @return the solution representing the local min
     *         found by this search attempt
     */
    public TspSolution getFixedPointSolution() {
        while (!reachedFixedPoint) {
            iterateOnce();
        }
        return new TspSolution(context, currentSolution);
    }

    /**
     * Runs one iteration of the heuristic. If this solver has already
     * reached its local min, it just returns that without doing any work.
     */
    public TspSolution getNextSolution() {
        iterateOnce();
        return new TspSolution(context, currentSolution);
    }

    /**
     * Returns true when this solver gets stuck in a local min and
     * no longer makes progress.
     */
    public boolean reachedFixedPoint() {
        return reachedFixedPoint;
    }

    // This is the core logic for generating candidates for the next state.
    private void doActionForAllPairsOfEdges(int[] selectedReachableSolution,
                                            AtomicReference<Double> selectedReachableCost,
                                            SolverAction action) {
        int numVertices = context.getNumVertices();
        // First, edges <0, 1> and <2, 3>, <0, 1> and <4, 5>, ..., <2, 3> and <4, 5>, etc.
        for (int i = 0, j = 1;
             j < numVertices - 2;
             i += 2, j += 2) {

            for (int k = i + 2, m = j + 2;
                 m < numVertices;
                 k += 2, m += 2) {

                action.perform(selectedReachableSolution, selectedReachableCost, i, j, k, m);
            }
        }
        // Then, edges <1, 2> and <3, 4>, <1, 2> and <5, 6>, ..., <3, 4> and <5, 6>, etc.
        for (int i = 1, j = 2;
             j < numVertices - 2;
             i += 2, j += 2) {

            for (int k = i + 2, m = j + 2;
                 m < numVertices;
                 k += 2, m += 2) {

                action.perform(selectedReachableSolution, selectedReachableCost, i, j, k, m);
            }
        }
        // Finally, edge <0, LAST> and <1, 2>, <0, LAST> and <3, 4>, etc.
        for (int i = 0, j = numVertices - 1, k = 1, m = 2;
             m < numVertices - 1;
             k += 2, m += 2) {

            action.perform(selectedReachableSolution, selectedReachableCost, i, j, k, m);
        }
    }

    // Default implementation of SolverAction: swap a pair of edges and invoke
    // the decision rule on the result.
    private void singleMoveInSearchSpace(int[] selectedReachableSolution,
                                         AtomicReference<Double> selectedReachableCost,
                                         int i, int j, int k, int m) {
        swap(currentSolution, i, j, k, m);
        double newCost = context.getPathCost(currentSolution);
        Decision decision =
            context.getDecisionRule().apply(selectedReachableCost.get(), newCost);
        if (decision == ACCEPT) {
            selectedReachableCost.set(newCost);
            System.arraycopy(currentSolution, 0, selectedReachableSolution, 0, context.getNumVertices());
        }
        unswap(currentSolution, i, j, k, m);
    }

    // Edges <i, j> and <k, m> turn into <i, k> and <j, m>.
    // This only requires swapping k and j.
    private static void swap(int[] indexes, int i, int j, int k, int m) {
        int jj = indexes[j];
        indexes[j] = indexes[k];
        indexes[k] = jj;
    }

    // Looks like swap() is its own inverse, since it swaps two elements, and swapping
    // them again restores the original order.
    private static void unswap(int[] indexes, int i, int j, int k, int m) {
        swap(indexes, i, j, k, m);
    }

    /**
     * The actual heuristic for generating successive solutions.
     */
    private void iterateOnce() {
        // Check if we should search at all.
        if (context.getDecisionRule().searchBudgetExceeded()) {
            reachedFixedPoint = true;
        }

        // Do nothing if we're as good as can be.
        if (reachedFixedPoint) return;

        int numVertices = context.getNumVertices();
        int[] selectedReachableSolution =
            Arrays.copyOf(currentSolution, currentSolution.length);
        AtomicReference<Double> selectedReachableCost =
            new AtomicReference<>(context.getPathCost(currentSolution));

        // Modifies selectedReachableSolution and selectedReachableCost.
        SolverAction solverAction = this::singleMoveInSearchSpace;
        doActionForAllPairsOfEdges(
            selectedReachableSolution,
            selectedReachableCost,
            solverAction);

        // If we are in danger of getting stuck, do a heroic depth-2 search.
        double currentCost = context.getPathCost(currentSolution);
        double nextCost = selectedReachableCost.get();
        if (context.getDecisionRule().fixedPointDetected(currentCost, nextCost)) {
            SolverAction nestedAction = (srs, src, i, j, k, m) -> {
                swap(currentSolution, i, j, k, m);
                doActionForAllPairsOfEdges(srs, src, solverAction);
                unswap(currentSolution, i, j, k, m);
            };
            doActionForAllPairsOfEdges(
                selectedReachableSolution,
                selectedReachableCost,
                nestedAction);
            // If still stuck, give up on further iteration.
            currentCost = context.getPathCost(currentSolution);
            nextCost = selectedReachableCost.get();
            if (context.getDecisionRule().fixedPointDetected(currentCost, nextCost)) {
                reachedFixedPoint = true;
            }
            // If looking one step ahead got us unstuck,
            // take the step from current state to the improved state we found.
            else System.arraycopy(selectedReachableSolution, 0, currentSolution, 0, numVertices);
        }
        // If not stuck, take the step from current state to the improved state we found.
        else System.arraycopy(selectedReachableSolution, 0, currentSolution, 0, numVertices);
    }
}
