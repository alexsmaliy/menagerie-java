package cp3e.matching;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

final class Pairing {
    private final int[] pairing;
    private final double totalDistance;
    private final LabeledPoint[] points;

    static final Pairing THE_WORST_PAIRING = new Pairing();

    Pairing(int[] pairing,
            double[][] distances,
            LabeledPoint[] points) {
        this.pairing = pairing;
        this.totalDistance = calculatePathSum(pairing, distances);
        this.points = points;
    }

    private Pairing() {
        this.pairing = new int[0];
        this.totalDistance = Double.MAX_VALUE;
        this.points = new LabeledPoint[0];
    }

    static double calculatePathSum(int[] pairing, double[][] distances) {
        double sum = 0;
        for (int i = 0; i < pairing.length; i = i + 2) {
            sum += distances[pairing[i]][pairing[i + 1]];
        }
        return sum;
    }

    double getTotalDistance() {
        return this.totalDistance;
    }

    @Override
    public String toString() {
        List<String> pairs = new ArrayList<>();
        for (int i = 0; i < pairing.length; i = i + 2) {
            pairs.add(String.format(
                "[%s: (%.1f, %.1f), %s: (%.1f, %.1f)]",
                points[pairing[i]].getLabel(),
                points[pairing[i]].getPoint().getX(),
                points[pairing[i]].getPoint().getY(),
                points[pairing[i + 1]].getLabel(),
                points[pairing[i + 1]].getPoint().getX(),
                points[pairing[i + 1]].getPoint().getY()));
        }
        return String.join("\n", pairs) + String.format("\ntotal distance = %.3f", totalDistance);
    }

    static final class LabeledPoint {
        private final String label;
        private final Point2D point;

        LabeledPoint(String label, Point2D point) {
            this.label = label;
            this.point = point;
        }

        String getLabel() {
            return this.label;
        }

        Point2D getPoint() {
            return point;
        }
    }
}
