package adm2e.tsp.ioutils;

import adm2e.tsp.rules.AnnealingRule;
import adm2e.tsp.rules.DecisionRule;
import adm2e.tsp.rules.GreedyRule;

import java.util.function.Supplier;

// A visitor that configures decision rules based on settings objects.
// Different rules have different sets of configurable parameters.
public class DecisionRuleBuilder {
    private static final Supplier<DecisionRule> GREEDY_RULE_SUPPLIER = () -> GreedyRule.INSTANCE;

    Supplier<DecisionRule> buildFrom(SettingsForMode.Greedy settings) {
        return GREEDY_RULE_SUPPLIER;
    }

    Supplier<DecisionRule> buildFrom(SettingsForMode.Annealing settings) {
        return () -> new AnnealingRule(
            settings.getReduceTempAfter(),
            settings.getStopAfter(),
            settings.getInitTemp(),
            settings.getCoolFactor());
    }
}
