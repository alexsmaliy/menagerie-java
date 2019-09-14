package cp3e.matching;

import cp3e.matching.Pairing.LabeledPoint;

import java.awt.geom.Point2D;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Queue;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.lang.Double.parseDouble;

/**
 * <p>The input is the path to a file that specifies an even-numbered list of (x, y)
 * points, as follows:</p>
 * <br />
 * <table>
 *     <tr><td>LABEL:</td><td>(X, Y)</td></tr>
 *     <tr><td>LABEL2:</td><td>(X, Y)</td></tr>
 *     <tr><td>...</td></tr>
 * </table>
 * <p>The program prints all globally optimal pairings that minimize total distance
 * between the members of a pair.</p>
 */
public final class Demo {
    private static final Pattern INPUT_LINE_PATTERN =
        Pattern.compile(
            "^\\s*"
            + "(\\p{Alnum}+)"
            + "\\s*:?\\s*"
            + "\\(([0-9]*\\.?[0-9]+)"
            + "\\s*,\\s*"
            + "([0-9]*\\.?[0-9]+)\\)"
            + "\\s*$");

    private static final Function<String, LabeledPoint> PARSE_LINE = line -> {
        Matcher m = INPUT_LINE_PATTERN.matcher(line);
        if (!m.matches())
            throw new RuntimeException(line + " does not match pattern \"LABEL: (X, Y)\"!");
        double x = parseDouble(m.group(2));
        double y = parseDouble(m.group(3));
        Point2D point = new Point2D.Double(x, y);
        return new LabeledPoint(m.group(1), point);
    };

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            usage();
            return;
        }
        Stream<String> linesFromFile = readRawInput(args[0]);
        LabeledPoint[] points = linesFromFile.map(PARSE_LINE)
                                             .toArray(LabeledPoint[]::new);
        PairUpper pairUpper = new PairUpper(points);
        Queue<Pairing> bests = pairUpper.bestPairings();
        bests.forEach(System.out::println);
    }

    private static void usage() {
        System.out.println("Usage: " + Demo.class.getName() + " <INPUT_FILE>");
    }

    private static Stream<String> readRawInput(String path) throws IOException {
        Path inputFile = Path.of(path);
        return Files.newBufferedReader(inputFile)
                    .lines()
                    .filter(s -> !s.isEmpty());
    }
}
