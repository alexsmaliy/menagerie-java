package adm2e.tsp.rules;

import static adm2e.tsp.rules.DecisionRule.Decision.ACCEPT;
import static adm2e.tsp.rules.DecisionRule.Decision.REJECT;

public enum GreedyRule implements DecisionRule {
    INSTANCE;

    @Override
    public Decision apply(double currentCost, double nextCost) {
        return nextCost < currentCost ? ACCEPT : REJECT;
    }
}
