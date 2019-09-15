package cp3e.matching;

import java.math.BigInteger;
import java.text.NumberFormat;
import java.util.stream.IntStream;

public class ComplexityCalculator {
    private static final String FORMAT_STRING = "%10s%26s%24s%16s";

    public static void main(String[] args) {
        System.out.println(String.format(FORMAT_STRING,
            "NUM POINTS", "ORDERED (n!)", "IGNORE WITHIN PAIRS", "IGNORE ALL"));
        NumberFormat formatter = NumberFormat.getIntegerInstance();
        IntStream.rangeClosed(2, 20).filter(i -> i % 2 == 0).forEach(i ->
            System.out.println(String.format(FORMAT_STRING,
                i,
                formatter.format(factorial(i)),
                formatter.format(ignoringOrderWithinEachPair(i)),
                formatter.format(ignoringOrderWithinAndAmongPairs(i))))
        );
    }

    private static BigInteger factorial(int n) {
        BigInteger fac = BigInteger.ONE;
        for (int i = 2; i <= n; i++) {
            fac = fac.multiply(BigInteger.valueOf(i));
        }
        return fac;
    }

    private static BigInteger ignoringOrderWithinEachPair(int n) {
        return factorial(n).divide(BigInteger.TWO.pow(n/2));
    }

    private static BigInteger ignoringOrderWithinAndAmongPairs(int n) {
        BigInteger ret = ignoringOrderWithinEachPair(n);
        return ret.divide(factorial(n/2));
    }
}
