package adm2e.tsp.rules;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import static adm2e.tsp.rules.DecisionRule.Decision.ACCEPT;
import static adm2e.tsp.rules.DecisionRule.Decision.REJECT;

/**
 * Encapsulates the state transition logic for the solver. Always accepts
 * better solutions, but also sometimes accepts worse solutions, and as a
 * consequence gets itself unstuck sometimes. The probability of accepting
 * a worse solution starts high and gets reduced after an uninterrupted
 * sequence of acceptable state transitions. The rule decides that iteration
 * is stuck after an uninterrupted sequence of same-cost iterations.
 */
public final class AnnealingRule implements DecisionRule {
    // parameters for this instance
    private final int consecutiveAcceptsBeforeTempReduced;
    private final int maxConsecutiveSameCurrentCost;
    private final double coolingRate;

    // state
    private final Random random = ThreadLocalRandom.current();
    private double currentTemperature;
    private int consecutiveAccepts;
    private double lastSeenCurrentCost;
    private int consecutiveSameCurrentCost;

    public AnnealingRule(int consecutiveAcceptsBeforeTempReduced,
                         int maxConsecutiveSameCurrentCost,
                         double initialTemperature,
                         double coolingRate) {
        this.consecutiveAcceptsBeforeTempReduced = consecutiveAcceptsBeforeTempReduced;
        this.maxConsecutiveSameCurrentCost = maxConsecutiveSameCurrentCost;
        this.currentTemperature = initialTemperature;
        this.coolingRate = coolingRate;
        this.consecutiveAccepts = 0;
        this.lastSeenCurrentCost = Double.MAX_VALUE;
        this.consecutiveSameCurrentCost = 0;
    }

    @Override
    public Decision apply(double currentCost, double nextCost) {
        // If nextCost <= currentCost, this value is at least equal to 1,
        // and the test should always pass. The test also passes if
        // the diff is close to zero -- solution readily moves
        // between equally good states.
        double criterion = Math.exp((currentCost - nextCost) / currentTemperature);
        if (criterion > random.nextDouble()) {
            consecutiveAccepts++;
            if (consecutiveAccepts > consecutiveAcceptsBeforeTempReduced) {
                System.out.println("Reducing temperature!");
                consecutiveAccepts = 0;
                currentTemperature = currentTemperature * coolingRate;
            }
            return ACCEPT;
        } else {
            return REJECT;
        }
    }

    @Override
    public boolean searchBudgetExceeded() {
        boolean exceeded = currentTemperature < AnnealingRuleParamDefaults.TEMP_LOWER_THRESHOLD;
        if (exceeded) System.out.println("Temperature below threshold!");
        return exceeded;
    }

    @Override
    public boolean fixedPointDetected(double currentCost, double bestCost) {
        if (currentCost < lastSeenCurrentCost) {
            lastSeenCurrentCost = currentCost;
            consecutiveSameCurrentCost = 0;
            return false;
        }
        double relativeDelta = (lastSeenCurrentCost - currentCost) / currentCost;
        if (relativeDelta < AnnealingRuleParamDefaults.RELATIVE_COST_CHANGE_LOWER_THRESHOLD) {
            consecutiveSameCurrentCost++;
        }
        boolean stuck = consecutiveSameCurrentCost >= maxConsecutiveSameCurrentCost;
        if (stuck) System.out.println("Solution stabilized!");
        return stuck;
    }
}
