package adm2e.tsp.demos.ioutils;

import adm2e.tsp.rules.DecisionRule;

import java.nio.file.Path;
import java.util.function.Supplier;

public abstract class SettingsForMode {
    private final int numTrials;
    private final Path inputFile;

    SettingsForMode(int numTrials, Path inputFile) {
        this.inputFile = inputFile;
        this.numTrials = numTrials;
    }

    public abstract Supplier<DecisionRule> buildDecisionRuleSupplier(DecisionRuleBuilder builder);

    public int getNumTrials() {
        return this.numTrials;
    }

    public Path getInputFile() {
        return this.inputFile;
    }

    static class Greedy extends SettingsForMode {

        Greedy(int numTrials, Path inputFile) {
            super(numTrials, inputFile);
        }

        @Override
        public Supplier<DecisionRule> buildDecisionRuleSupplier(DecisionRuleBuilder builder) {
            return builder.buildFrom(this);
        }
    }

    static class Annealing extends SettingsForMode {

        private final int reduceTempAfter;
        private final int stopAfter;

        Annealing(int numTrials,
                  Path inputFile,
                  int reduceTempAfter,
                  int stopAfter) {
            super(numTrials, inputFile);
            this.reduceTempAfter = reduceTempAfter;
            this.stopAfter = stopAfter;
        }

        int getReduceTempAfter() {
            return this.reduceTempAfter;
        }

        int getStopAfter() {
            return this.stopAfter;
        }

        @Override
        public Supplier<DecisionRule> buildDecisionRuleSupplier(DecisionRuleBuilder builder) {
            return builder.buildFrom(this);
        }
    }
}