package adm2e.tsp.rules;

public final class AnnealingRuleParamDefaults {
    private AnnealingRuleParamDefaults() { /* contants class */ }
    // The number of consecutive times the decision rule accepts a move before we make
    // the acceptance criterion more strict. Note tht at the start of the search, we
    // accept almost any move, so, at least initially, there are many consecutive
    // acceptances. Also, every time we accept a worse solution at random, several
    // subsequent solutions will also be improvements, providing opportunities to
    // reduce the temperature even late in the search.
    public static final int CONSECUTIVE_ACCEPTS_BEFORE_TEMP_REDUCED = 20;
    // How many search iterations must result in approximately the same outcome
    // (within a certain relative tolerance) before we declare the search done.
    public static final int MAX_CONSECUTIVE_SAME_CURRENT_COST = 5;
    // Not configurable via the command line. The initial temperature of the system.
    public static final double DEFAULT_INITIAL_TEMPERATURE = 1.0;
    // Not configurable via the command line. The temp of the system is reduced by this
    // multiplicative factor after CONSECUTIVE_ACCEPTS_BEFORE_TEMP_REDUCED consecutive
    // successful moves.
    public static final double DEFAULT_TEMP_REDUCTION_FACTOR = 0.5;
    // Not configurable via the command line. The solver stops iterating if temperature
    // falls below this threshold.
    public static final double TEMP_LOWER_THRESHOLD = 16.0 * Double.MIN_VALUE;
    // Not configurable via the command line. The solver does not consider a candidate
    // state significantly different from the current state if its cost is within this
    // fraction of the current cost.
    public static final double RELATIVE_COST_CHANGE_LOWER_THRESHOLD = 0.001;
}
