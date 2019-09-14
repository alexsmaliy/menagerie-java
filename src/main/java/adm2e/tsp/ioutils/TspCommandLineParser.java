package adm2e.tsp.ioutils;

import adm2e.tsp.Demo;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.stream.Collectors;

import static adm2e.tsp.rules.AnnealingRuleParamDefaults.DEFAULT_REDUCE_TEMP_AFTER;
import static adm2e.tsp.rules.AnnealingRuleParamDefaults.DEFAULT_INITIAL_TEMP;
import static adm2e.tsp.rules.AnnealingRuleParamDefaults.DEFAULT_STOP_AFTER;
import static adm2e.tsp.rules.AnnealingRuleParamDefaults.DEFAULT_TEMP_REDUCTION_FACTOR;

// The long and the short of it, commons-cli command line parsing is both verbose
// and not very flexible. It's awkward to express that some options are required
// when some other option or value is present. A common suggestion is multiple
// parsing passes. But that introduces its own frustrations, as the default
// parser does not make it possible to check only for a subset of the provided
// options without either dumping the rest of the command line into a "the rest"
// object or throwing an exception. This also means that being able to parse
// options correctly depends on the order in which they are provided. Gross!
public final class TspCommandLineParser {

    private static final CommandLineParser DEFAULT_PARSER = new DefaultParser();
    private static final HelpFormatter HELP_FORMATTER = new HelpFormatter();

    private static final int DEFAULT_NUM_TRIALS = 1;

    private static final Option MODE_OPTION = Option.builder()
        .argName(Arrays.stream(Heuristic.values()).map(Enum::name).collect(Collectors.joining("|")))
        .desc("the heuristic to use for finding a solution")
        .hasArg(true)
        .longOpt("heuristic")
        .numberOfArgs(1)
        .required(true)
        .type(Heuristic.class)
        .build();

    private static final Option ANNEALING_OPTION_REDUCE_TEMP_AFTER = Option.builder("r")
        .argName("num")
        .desc(String.format(
            "reduce temperature after this many consecutively accepted state transitions (default: %d)",
            DEFAULT_REDUCE_TEMP_AFTER))
        .hasArg(true)
        .longOpt("reduce-after")
        .numberOfArgs(1)
        .required(false)
        .type(Number.class)
        .build();

    private static final Option ANNEALING_OPTION_STOP_AFTER = Option.builder("s")
        .argName("num")
        .desc(String.format(
            "stop after this many consecutive iterations with no improvement in cost (default: %d)",
            DEFAULT_STOP_AFTER))
        .hasArg(true)
        .longOpt("stop-after")
        .numberOfArgs(1)
        .required(false)
        .type(Number.class)
        .build();

    private static final Option ANNEALING_MODE_INITIAL_TEMP = Option.builder("i")
        .argName("num")
        .desc(String.format(
            "starting system temperature (default: %.1f)",
            DEFAULT_INITIAL_TEMP))
        .hasArg(true)
        .longOpt("init-temp")
        .numberOfArgs(1)
        .required(false)
        .type(Number.class)
        .build();

    private static final Option ANNEALING_MODE_TEMP_REDUCTION_FACTOR = Option.builder("c")
        .argName("num")
        .desc(String.format(
            "0 < num < 1, the geometric \"cooling rate\" of the system (default: %.1f)",
            DEFAULT_TEMP_REDUCTION_FACTOR))
        .hasArg(true)
        .longOpt("cool-rate")
        .numberOfArgs(1)
        .required(false)
        .type(Number.class)
        .build();

    private static final Option NUM_TRIALS_OPTION = Option.builder("n")
        .argName("num")
        .desc("the number of trials to run")
        .hasArg(true)
        .longOpt("num-trials")
        .numberOfArgs(1)
        .required(true)
        .type(Number.class)
        .build();

    private static Options getModeSelectionOptions() {
        Options options = new Options();
        options.addOption(MODE_OPTION);
        return options;
    }

    private static Options getAnnealingModeOptions() {
        Options options = new Options();
        options.addOption(MODE_OPTION);
        options.addOption(ANNEALING_OPTION_REDUCE_TEMP_AFTER);
        options.addOption(ANNEALING_OPTION_STOP_AFTER);
        options.addOption(ANNEALING_MODE_INITIAL_TEMP);
        options.addOption(ANNEALING_MODE_TEMP_REDUCTION_FACTOR);
        options.addOption(NUM_TRIALS_OPTION);
        return options;
    }

    private static Options getGreedyModeOptions() {
        Options options = new Options();
        options.addOption(MODE_OPTION);
        options.addOption(NUM_TRIALS_OPTION);
        return options;
    }

    // Another peculiarity of commons-cli is that validating a numerical option
    // requires setting `type(Number.class)`, rather than Integer.class or whatever.
    // Then, `getParsedOptionValue()` will return either a Long or Double as Object,
    // requiring tedious casting.
    public static SettingsForMode parse(String[] args) {
        CommandLine firstPass;
        try {
            firstPass = DEFAULT_PARSER.parse(getModeSelectionOptions(), args, true);
            switch (Heuristic.valueOf(firstPass.getOptionValue("heuristic"))) {
                case GREEDY:
                    try {
                        CommandLine secondPass = DEFAULT_PARSER.parse(getGreedyModeOptions(), args, false);
                        int numTrials = getAsInt(secondPass, NUM_TRIALS_OPTION, DEFAULT_NUM_TRIALS);
                        Path inputFile = new File(secondPass.getArgs()[0]).toPath();
                        return new SettingsForMode.Greedy(numTrials, inputFile);
                    } catch (ParseException | ArrayIndexOutOfBoundsException e) {
                        greedyModeUsage();
                        return null;
                    }
                case ANNEALING:
                    try {
                        CommandLine secondPass = DEFAULT_PARSER.parse(getAnnealingModeOptions(), args, false);
                        int numTrials = getAsInt(
                            secondPass,
                            NUM_TRIALS_OPTION,
                            DEFAULT_NUM_TRIALS);
                        int reduceTempAfter = getAsInt(
                            secondPass,
                            ANNEALING_OPTION_REDUCE_TEMP_AFTER,
                            DEFAULT_REDUCE_TEMP_AFTER);
                        int stopAfter = getAsInt(
                            secondPass,
                            ANNEALING_OPTION_STOP_AFTER,
                            DEFAULT_STOP_AFTER);
                        double initTemp = getAsDouble(
                            secondPass,
                            ANNEALING_MODE_INITIAL_TEMP,
                            DEFAULT_INITIAL_TEMP);
                        double coolFactor = getAsDouble(
                            secondPass,
                            ANNEALING_MODE_TEMP_REDUCTION_FACTOR,
                            DEFAULT_TEMP_REDUCTION_FACTOR);
                        Path inputFile = new File(secondPass.getArgs()[0]).toPath();
                        return new SettingsForMode.Annealing(
                            numTrials, inputFile, reduceTempAfter,
                            stopAfter, initTemp, coolFactor);
                    } catch (ParseException | ArrayIndexOutOfBoundsException e) {
                        annealingModeUsage();
                        return null;
                    }
            }
        } catch (IllegalArgumentException | ParseException e) {
            modeSelectUsage();
        }
        return null;
    }

    private static int getAsInt(CommandLine cli, Option option, int defaultValue)
        throws ParseException {
        String flag = option.getOpt() == null ? option.getLongOpt() : option.getOpt();
        return cli.hasOption(flag)
            ? ((Number) cli.getParsedOptionValue(flag)).intValue()
            : defaultValue;
    }

    private static double getAsDouble(CommandLine cli, Option option, double defaultValue)
        throws ParseException {
        String flag = option.getOpt() == null ? option.getLongOpt() : option.getOpt();
        return cli.hasOption(flag)
            ? ((Number) cli.getParsedOptionValue(flag)).doubleValue()
            : defaultValue;
    }

    private static void modeSelectUsage() {
        String command = Demo.class.getName() + " INPUT_FILE";
        HELP_FORMATTER.printHelp(
            120, // width
            command,
            null, // header
            getModeSelectionOptions(),
            null, // footer
            true); // do print usage example
    }

    private static void greedyModeUsage() {
        String command = Demo.class.getName()
            + " INPUT_FILE --"
            + MODE_OPTION.getLongOpt()
            + " "
            + Heuristic.GREEDY.name();
        Options tempOptions = new Options();
        tempOptions.addOption(NUM_TRIALS_OPTION);
        HELP_FORMATTER.printHelp(
            120, // width
            command,
            null, // header
            tempOptions,
            null, // footer
            true); // do print usage example
    }

    private static void annealingModeUsage() {
        String command = Demo.class.getName()
            + " INPUT_FILE --"
            + MODE_OPTION.getLongOpt()
            + " "
            + Heuristic.ANNEALING.name();
        Options tempOptions = new Options();
        tempOptions.addOption(NUM_TRIALS_OPTION);
        tempOptions.addOption(ANNEALING_OPTION_REDUCE_TEMP_AFTER);
        tempOptions.addOption(ANNEALING_OPTION_STOP_AFTER);
        tempOptions.addOption(ANNEALING_MODE_INITIAL_TEMP);
        tempOptions.addOption(ANNEALING_MODE_TEMP_REDUCTION_FACTOR);
        HELP_FORMATTER.printHelp(
            120, // width
            command,
            null, // header
            tempOptions,
            null, // footer
            true); // do print usage example
    }

    public enum Heuristic {
        GREEDY,
        ANNEALING,
    }
}
