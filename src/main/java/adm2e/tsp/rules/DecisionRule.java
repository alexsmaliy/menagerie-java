package adm2e.tsp.rules;

@FunctionalInterface
public interface DecisionRule {
    enum Decision { ACCEPT, REJECT }

    /**
     * Decides whether a proposed cost is better than the current cost.
     */
    Decision apply(double currentCost, double nextCost);

    /**
     * Hook for rules that terminate searching based on some internal state,
     * such as the number of times they've been called. The default is to run
     * forever.
     */
    default boolean searchBudgetExceeded() {
        return false;
    }

    /**
     * Hook for rules with arbitrary stopping conditions. The default is
     * to signal that a fixed point has been reached as soon as we reach
     * a local minimum.
     */
    default boolean fixedPointDetected(double currentCost, double bestCost) {
        return currentCost <= bestCost;
    }
}
