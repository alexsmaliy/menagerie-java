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
        return pairUp(initialIndexes, 0, points.length);
    }

    // Each int[] is a full pairing and has a length equal to the number of points.
    // Successive pairs of entries are, implicitly, indexes of paired points,
    // namely, { p0_0, p0_1, p1_0, p1_1, p2_0, ... }. At a depth of N, this method
    // sets the N-th pair at indexes (N * 2) and (N * 2 + 1).
    private static Stream<int[]> pairUp(int[] indexes, int depth, int numPoints) {
        if (indexes.length == 0) {
            return Stream.empty();
        } else if (indexes.length == 2) {
            int[] pairs = new int[numPoints];
            Arrays.fill(pairs, -1);
            pairs[depth * 2] = indexes[0];
            pairs[depth * 2 + 1] = indexes[1];
            return Stream.of(pairs);
        } else {
            // This is the non-trivial case. We build every pair between the first remaining
            // index and every other remaining index. For each choice of pair, we exclude the
            // indices we selected and recurse into the remaining indices.
            // We do not build every possible pair, because then we will generate pairings
            // that differ only in the order of the pairs. Namely, given [1, 2, 3, 4], if we
            // build only [1, 2] at depth 0, the only choice is [3, 4] at depth 1. If we build
            // both [1, 2] and [3, 4] at depth 0, we then build both complements at depth 1 and
            // end up with two equivalent pairings, [[1, 2], [3, 4]] and [[3, 4], [1, 2]].
            Stream<int[]> pairings = Stream.empty();
            for (int i = 1; i < indexes.length; i++) {
                int[] newIndexes = new int[indexes.length - 2];
                for (int j = 1, k = 0; j < indexes.length; j++) {
                    if (j != i) {
                        newIndexes[k++] = indexes[j];
                    }
                }
                final int p1 = indexes[0];
                final int p2 = indexes[i];
                Stream<int[]> partialPairings = pairUp(newIndexes, depth + 1, numPoints);
                partialPairings = partialPairings.peek(pairing -> {
                    pairing[depth * 2] = p1;
                    pairing[depth * 2 + 1] = p2;
                });
                pairings = Stream.of(pairings, partialPairings).flatMap(x -> x);
            }
            return pairings;
        }
    }
}
