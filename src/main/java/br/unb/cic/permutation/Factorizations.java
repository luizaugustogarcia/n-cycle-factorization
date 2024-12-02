package br.unb.cic.permutation;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;
import lombok.val;
import org.apache.commons.lang3.time.StopWatch;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Factorizations {

    private static final Map<Integer, Cycle> FIXED_SYMBOLS_CACHE = Maps.newHashMap();

    private static final Table<Integer, Integer, Map<Integer, Cycle>> THREE_CYCLES_CACHE = HashBasedTable.create();

    private static final Table<Integer, Integer, Cycle> TRANSPOSITIONS_CACHE = HashBasedTable.create();

    public static final MulticyclePermutation FIXED_0_1 = new MulticyclePermutation(List.of(fixed(0), fixed(1)));

    private static final Cycle[] CANONICAL_LONG_CYCLES;

    static {
        CANONICAL_LONG_CYCLES = new Cycle[2000];
        for (var i = 1; i < 2000; i++) {
            val p = new int[i];
            for (var j = 0; j < i; j++) {
                p[j] = j;
            }
            CANONICAL_LONG_CYCLES[i] = Cycle.of(p);
        }
    }

    public static void main(String[] args) {
        unicycles(16);
    }

    private static void unicycles(final int n) {
        val stopWatch = StopWatch.create();
        stopWatch.start();

        final var total = new AtomicLong();

        factorizations(CANONICAL_LONG_CYCLES[n + 2])
                .forEach(f -> {
                    final var t = total.incrementAndGet();
                    if (t % 1_000_000 == 0) {
                        System.out.println(t + " " + Instant.now());
                    }
//                    val delta = (MulticyclePermutation) f.getRight();
//                    delta.remove(fixedZero);
//                    val u = delta.conjugateBy(CANONICAL_LONG_CYCLES[n + 2].getInverse());
//                    u.remove(fixedNPlus1);
//                    System.out.println(f);
                });

        stopWatch.stop();
        System.out.println(total);
        System.out.println(stopWatch.getTime(TimeUnit.SECONDS));
    }

    public static Stream<Permutation> factorizations(final Permutation tau) {
        if (tau.isEven()) {
            throw new RuntimeException("Tau must be an odd permutation");
        }

        val n = tau.getMaxSymbol();

        var result = Stream.<Permutation>empty();

        if (tau.image(0) != 0) {
            if (n == 1) {
                result = Stream.of(FIXED_0_1);
            } else {
                val tauZero = tau.getInverse().image(0);

                val conjugator = transposition(tauZero, tau.getMaxSymbol());
                val tauPrime = tau.conjugateBy(conjugator);

                result = IntStream.range(1, n).boxed().parallel().flatMap(h -> {
                    if (h == tauPrime.image(0)) {
                        return Stream.empty();
                    } else {
                        val t = ((MulticyclePermutation) getTimes(h, n, tauPrime));
                        t.remove(fixed(n));

                        return factorizations(t).map(f -> getPermutation(h, f, n, conjugator));
                    }
                });
            }
        }

        return result;
    }

    private static Permutation getPermutation(Integer h, Permutation f, int n, Cycle conjugator) {
        // TODO optimize - do not multiply and create new cycles, maintain the transpositions
        return transposition(n, h).times(f).conjugateBy(conjugator);
    }

    private static Permutation getTimes(Integer h, int n, Permutation tauPrime) {
        // TODO optimize - do not multiply and create new cycles, maintain the transpositions
        return threeCycle(n, h, 0).times(tauPrime);
    }

    private static Cycle threeCycle(final int a, final int b, final int c) {
        var map = THREE_CYCLES_CACHE.get(a, b);
        if (map == null) {
            THREE_CYCLES_CACHE.put(a, b, map = Maps.newHashMap());
        }

        var threeCycle = map.get(c);
        if (threeCycle == null) {
            map.put(c, threeCycle = Cycle.of(a, b, c));
        }

        return threeCycle;
    }

    private static Cycle fixed(final int a) {
        var fixed = FIXED_SYMBOLS_CACHE.get(a);
        if (fixed != null) {
            return fixed;
        }

        FIXED_SYMBOLS_CACHE.put(a, fixed = Cycle.of(a));

        return fixed;
    }

    private static Cycle transposition(final int a, final int b) {
        var transposition = TRANSPOSITIONS_CACHE.get(a, b);
        if (transposition != null) {
            return transposition;
        }

        TRANSPOSITIONS_CACHE.put(a, b, transposition = Cycle.of(a, b));

        return transposition;
    }
}
