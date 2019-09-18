package adm2e.graph;

import adm2e.graph.representations.AdjacencyListGraph;
import adm2e.graph.representations.DirectedGraph;
import adm2e.graph.traversal.BreadthFirstTraversal;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.function.IntPredicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Demo {
    private static final Pattern INPUT_LINE_PATTERN = Pattern.compile(
        "^\\s*"
        + "(\\p{Alnum}+)"
        + "\\s*(<?\\s*[=-]+\\s*>?)\\s*"
        + "(\\p{Alnum}+)"
        + "\\s*$"
    );

    private static final Option PATH_BETWEEN_NODES_OPTION = Option.builder()
        .desc("print shortest path between two nodes")
        .hasArg(true)
        .longOpt("shortest-path")
        .numberOfArgs(2)
        .required(false)
        .build();

    private static final CommandLineParser DEFAULT_PARSER = new DefaultParser();
    private static final HelpFormatter HELP_FORMATTER = new HelpFormatter();

    public static void main(String[] args) throws IOException {
        // Parse command line.
        CommandLine cli;
        try {
            cli = DEFAULT_PARSER.parse(getOptions(), args, true);
        } catch (ParseException e) {
            usage();
            return;
        }
        // Build data representation.
        Stream<String> lines = readRawInput(cli.getArgs()[0]);
        DirectedGraph graph = buildGraph(lines);
        // Run requested solver.
        if (cli.hasOption(PATH_BETWEEN_NODES_OPTION.getLongOpt())) {
            String[] labels = cli.getOptionValues(PATH_BETWEEN_NODES_OPTION.getLongOpt());
            shortestPath(graph, labels[0], labels[1]);
        }
    }

    /**
     * BFS to find and print the shortest path between two nodes,
     * if they are valid and a path actually exists.
     */
    public static void shortestPath(DirectedGraph graph, String label1, String label2) {
        int indexForLabel1 = graph.indexForLabel(label1).orElseThrow(() ->
            new RuntimeException(label1 + " not present in input data!"));
        int indexForLabel2 = graph.indexForLabel(label2).orElseThrow(() ->
            new RuntimeException(label2 + " not present in input data!"));

        int numVertices = graph.numVertices();
        DirectedGraph.VisitTracker visitTracker =
            BreadthFirstTraversal.getDefaultVisitTracker(numVertices);
        // The edge consumer is an array that records the parent of each node as we encounter them.
        int[] parentOf = new int[numVertices];
        Arrays.fill(parentOf, -1);
        DirectedGraph.EdgeConsumer edgeConsumer = (x, y) -> {
            parentOf[y] = x;
        };
        // Stop when we find the destination.
        IntPredicate stopCondition = i -> i == indexForLabel2;
        // Solve.
        BreadthFirstTraversal.traverse(graph, indexForLabel1, visitTracker, edgeConsumer, stopCondition);
        // Print the solution, if we found one.
        if (parentOf[indexForLabel2] != -1) {
            LinkedList<Integer> path = new LinkedList<>();
            int current = indexForLabel2;
            while (current != -1) {
                path.addFirst(current);
                current = parentOf[current];
            }
            String solution = path.stream()
                .map(i -> graph.labelOfVertex(i).get())
                .collect(Collectors.joining(" -> "));
            System.out.println(solution);
        } else {
            System.out.println("FAILED TO FIND A PATH!");
        }
    }

    private static Stream<String> readRawInput(String path) throws IOException {
        Path inputFile = Path.of(path);
        return Files.newBufferedReader(inputFile)
            .lines()
            .filter(s -> !s.isEmpty());
    }

    private static DirectedGraph buildGraph(Stream<String> lines) {
        DirectedGraph graph = new AdjacencyListGraph();
        lines.forEach(line -> {
            Matcher m  = INPUT_LINE_PATTERN.matcher(line);
            if (!m.matches())
                throw new RuntimeException(
                    line + " does not match pattern \"LABEL <-> | -- | <- | -> LABEL\"!");
            String label1 = m.group(1);
            String operator = m.group(2);
            String label2 = m.group(3);
            int lt = operator.indexOf('<');
            int gt = operator.indexOf('>');
            if (lt == -1) {
                graph.addEdge(label1, label2, true);
            }
            if (gt == -1) {
                graph.addEdge(label2, label1, true);
            }
            if (lt > -1 && gt > -1) {
                graph.addEdge(label1, label2, false);
            }
        });
        return graph;
    }

    private static Options getOptions() {
        Options options = new Options();
        OptionGroup optionGroup = new OptionGroup();
        optionGroup.setRequired(true);
        optionGroup.addOption(PATH_BETWEEN_NODES_OPTION);
        options.addOptionGroup(optionGroup);
        return options;
    }

    private static void usage() {
        String command = Demo.class.getName();
        HELP_FORMATTER.printHelp(
            120, // width
            command,
            null, // header
            getOptions(),
            null, // footer
            true); // do print usage example
    }
}
