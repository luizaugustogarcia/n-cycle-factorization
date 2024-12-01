package br.unb.cic.permutation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.stream.Collectors;
import lombok.val;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

public class Factorizations {

    public static final Cycle[] CANONICAL_LONG_CYCLES;

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
        unicycles(Integer.parseInt(args[0]));
    }

    private static void unicycles(final int n) {
        val conjugator = CANONICAL_LONG_CYCLES[n + 1].getInverse();
        
        val fixedZero = Cycle.of(0);
        val fixedNPlus1 = Cycle.of(n + 1);

        val unicycles = new TreeSet<Cycle>();

        factorizations(CANONICAL_LONG_CYCLES[n + 2])
                .forEach(f -> {
                    val delta = (MulticyclePermutation) f.getRight();
                    delta.remove(fixedZero);
                    val u = delta.conjugateBy(CANONICAL_LONG_CYCLES[n + 2].getInverse());
                    u.remove(fixedNPlus1);
                    unicycles.add(u.asNCycle());
                });
        unicycles.forEach(System.out::println);
    }

    public static Stream<Pair<Cycle, Permutation>> factorizations(final Permutation tau) {
        if (tau.isEven()) {
            throw new RuntimeException("Tau must be an odd permutation");
        }

        val n = tau.getMaxSymbol();

        if (tau.image(0) != 0) {
            if (n == 1) {
                return Stream.of(Pair.of(Cycle.of(0, 1),
                        new MulticyclePermutation(List.of(Cycle.of(0), Cycle.of(1)))));
            } else {
                val tauZero = tau.getInverse().image(0);

                val conjugator = Cycle.of(tauZero, tau.getMaxSymbol());
                val tauPrime = tau.conjugateBy(conjugator);

                return IntStream.range(1, n).boxed().parallel().flatMap(h -> {
                    if (h != tauPrime.image(0)) {
                        val t = ((MulticyclePermutation) Cycle.of(n, h, 0).times(tauPrime));
                        t.remove(Cycle.of(n));

                        return factorizations(t).map(f -> {
                            val c = f.getLeft();
                            val d = f.getRight();

                            val gamma = Cycle.of(n, 0).times(c);
                            val delta = Cycle.of(n, h).times(d);

                            return Pair.of(gamma.conjugateBy(conjugator).asNCycle(),
                                    delta.conjugateBy(conjugator));
                        });
                    } else {
                        return Stream.empty();
                    }
                });
            }
        }

        return Stream.empty();
    }
}
