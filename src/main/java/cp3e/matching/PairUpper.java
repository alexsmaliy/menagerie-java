package cp3e.matching;

import cp3e.matching.Pairing.LabeledPoint;

import java.util.Arrays;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Given an even-numbered list of labeled points, exhaustively generates every unique
 * pairing of those points and finds those that globally minimize the sum of the
 * pairwise distances.
 */
public final class PairUpper {
    private final LabeledPoint[] points;
    private final double[][] distances;

    public PairUpper(LabeledPoint[] points) {
        if (points.length % 2 != 0)
            throw new RuntimeException("Must have an even number of points for pairing!");
        this.points = points;
        int numPoints = points.length;
        distances = new double[numPoints][numPoints];
        for (int i = 0; i < numPoints - 1; i++) {
            for (int j = i + 1; j < numPoints; j++) {
                double iToJ = distance(points[i], points[j]);
                distances[i][j] = iToJ;
                distances[j][i] = iToJ;
            }
        }
    }

    private static double distance(LabeledPoint i, LabeledPoint j) {
        return Math.abs(Math.hypot(
            i.getPoint().getX() - j.getPoint().getX(),
            i.getPoint().getY() - j.getPoint().getY()));
    }

    public Queue<Pairing> bestPairings() {
        ConcurrentLinkedQueue<Pairing> bests = new ConcurrentLinkedQueue<>();
        bests.add(Pairing.THE_WORST_PAIRING);
        pairUp().forEach(pairing -> {
            double pathSum = Pairing.calculatePathSum(pairing, distances);
            double currentBest = bests.peek().getTotalDistance();
            if (pathSum < currentBest) {
                bests.clear();
                bests.add(new Pairing(pairing, distances, points));
                return;
            }
            if (pathSum == currentBest) {
                bests.add(new Pairing(pairing, distances, points));
            }
        });
        return bests;
    }

    private Stream<int[]> pairUp() {
        int[] initialIndexes = IntStream.range(0, points.length).toArray();
        int[] emptyPrefix = new int[points.length];
        return pairUp(emptyPrefix, initialIndexes, 0, points.length);
    }

    // Each int[] is a full pairing and has a length equal to the number of points.
    // Successive pairs of entries are, implicitly, indexes of paired points,
    // namely, { p0_0, p0_1, p1_0, p1_1, p2_0, ... }. At a depth of N, this method
    // sets the N-th pair at indexes (N * 2) and (N * 2 + 1).
    private static Stream<int[]> pairUp(int[] prefix, int[] remainingIndexes, int depth, int numPoints) {
        if (remainingIndexes.length == 0) {
            return Stream.of(Arrays.copyOf(prefix, numPoints));
        } else if (remainingIndexes.length == 2) {
            int[] completedPairing = Arrays.copyOf(prefix, numPoints);
            completedPairing[depth * 2] = remainingIndexes[0];
            completedPairing[depth * 2 + 1] = remainingIndexes[1];
            return Stream.of(completedPairing);
        } else {
            // This is the non-trivial case. We build every pair between the first remaining
            // index and every other remaining index. For each choice of pair, we exclude the
            // indices we selected and recurse into the remaining indices.
            // We do not build every possible pair, because then we will generate pairings
            // that differ only in the order of the pairs. Namely, given [1, 2, 3, 4], if we
            // build only [1, 2] at depth 0, the only choice is [3, 4] at depth 1. If we build
            // both [1, 2] and [3, 4] at depth 0, we then build both complements at depth 1 and
            // end up with two equivalent pairings, [[1, 2], [3, 4]] and [[3, 4], [1, 2]].
            return IntStream.range(1, remainingIndexes.length).mapToObj(i -> {
                int[] newIndexes = new int[remainingIndexes.length - 2];
                for (int j = 1, k = 0; j < remainingIndexes.length; j++) {
                    if (j != i) {
                        newIndexes[k++] = remainingIndexes[j];
                    }
                }
                int[] prefixCopy = Arrays.copyOf(prefix, numPoints);
                final int p1 = remainingIndexes[0];
                final int p2 = remainingIndexes[i];
                prefixCopy[depth * 2] = p1;
                prefixCopy[depth * 2 + 1] = p2;
                return pairUp(prefixCopy, newIndexes, depth + 1, numPoints);
            }).flatMap(s -> s);
        }
    }
}
