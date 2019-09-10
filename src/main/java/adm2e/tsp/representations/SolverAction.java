package adm2e.tsp.representations;

import java.util.concurrent.atomic.AtomicReference;

// Just a simple functional interface to type-alias this ugly function signature.
@FunctionalInterface
public interface SolverAction {
    void perform(int[] selectedReachableSolution,
                 AtomicReference<Double> selectedReachableCost,
                 int i, int j, int k, int m);
}
