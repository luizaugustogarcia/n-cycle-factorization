package br.unb.cic.permutation;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;
import lombok.val;

import org.apache.commons.lang.ArrayUtils;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
        val total = new AtomicLong();

        val fixedNPlus1 = Cycle.of(n + 1);

        factorizations(CANONICAL_LONG_CYCLES[n + 2])
                .forEach(f -> {
                    val t = total.incrementAndGet();
                    if (t % 1_000_000 == 0) {
                        System.out.println(t + " " + Instant.now());
                    }

                    val u = (MulticyclePermutation) f.conjugateBy(CANONICAL_LONG_CYCLES[n + 2].getInverse());
                    u.remove(fixedNPlus1);

                    val pi = u.asNCycle().getSymbols();

                    if (isCanonical(pi)) {
                       System.out.println("canonical " + Arrays.toString(pi));
                       if (getKappaMoves(pi, 2).findAny().isEmpty()) {
                           throw new RuntimeException("Unicycle " + Arrays.toString(pi) + " has no 2-moves");
                       }
                    }
                });

        System.out.println(total);
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
                        val t = ((MulticyclePermutation) threeCycle(n, h, 0).times(tauPrime));
                        t.remove(fixed(n));

                        return factorizations(t).map(f -> transposition(n, h).times(f).conjugateBy(conjugator));
                    }
                });
            }
        }

        return result;
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

    public static boolean isCanonical(final int[] p) {
        for (int i = 0; i < p.length - 1; i++) {
            final var rotation = rotate(i, p);
            if (Arrays.compare(rotation, p) == -1 || Arrays.compare(mirror(rotation), p) == -1) {
                return false;
            }
        }
        return true;
    }

    private static int[] mirror(final int[] rotation) {
        final var mirror = new int[rotation.length];
        for (int i = rotation.length - 1; i >= 1; i--) {
            mirror[mirror.length - i] = (rotation.length - rotation[i]) % rotation.length;
        }
        return mirror;
    }

    private static int[] rotate(final int i, final int[] c) {
        if (i == 0) {
            return c;
        }

        final var rotation = new int[c.length];
        rotation[0] = i;
        for (int j = 1; j < c.length; j++) {
            rotation[j] = (c[j] + i) % c.length;
        }

        return startingByZero(rotation);
    }

    public static int[] startingByZero(final int[] rotation) {
        if (rotation[0] == 0) {
            return rotation;
        }

        final var index = ArrayUtils.indexOf(rotation, 0);
        final var symbols = new int[rotation.length];
        System.arraycopy(rotation, index, symbols, 0, symbols.length - index);
        System.arraycopy(rotation, 0, symbols, symbols.length - index, index);

        return symbols;
    }

    public static Stream<int[]> getKappaMoves(final int[] pi, final int kappa) {
        return IntStream.range(0, pi.length - 2)
                .boxed().flatMap(i -> IntStream.range(i + 1, pi.length - 1)
                        .boxed().flatMap(j -> IntStream.range(j + 1, pi.length).boxed()
                                .map(k -> {
                                    var bonds = 0;

                                    if (isBond(pi, i - 1, j)) {
                                        bonds++;
                                    }

                                    if (isBond(pi, k - 1, i)) {
                                        bonds++;
                                    }

                                    if (isBond(pi, j - 1, k)) {
                                        bonds++;
                                    }

                                    if (bonds == kappa) {
                                        return new int[]{i, j, k};
                                    }

                                    return null;
                                }))).filter(Objects::nonNull);
    }

    public static boolean isBond(final int[] p, final int i, final int j) {
        final int n = p.length, iMod = mod(n, i), jMod = mod(n, j);
        return mod(n, p[iMod] + 1) == mod(n, p[jMod]);
    }

    private static int mod(int n, int p) {
        return Math.floorMod(p, n);
    }
}
