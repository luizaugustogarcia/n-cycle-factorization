package br.unb.cic.permutation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.stream.Collectors;
import lombok.val;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.paukov.combinatorics3.Generator;

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
        val unicycles = new TreeSet<Cycle>();
        Generator.permutation(0,1,2,3,4).simple()
                        .forEach(p -> {
                            val pi = Cycle.of("(" + p.stream()
                                    .map(Object::toString)
                                    .collect(Collectors.joining(",")) + ")");
                            val sigma = CANONICAL_LONG_CYCLES[5];
                            val spi = sigma.times(pi.getInverse());
                            if (spi.size() == 1 && spi.asNCycle().size() == 5) {
                                unicycles.add(pi);
                            }
                        });
        unicycles.forEach(System.out::println);
        System.out.println("----------");
        unicycles(Integer.parseInt(args[0]));
    }

    private static void unicycles(final int n) {
        val conjugator = new MulticyclePermutation();
        for (int i = 1; i <= n + 1; i++) {
            conjugator.add(Cycle.of(i - 1, i));
        }

        val fixedZero = Cycle.of(0);
        val fixedNPlus1 = Cycle.of(n + 1);

        val unicycles = new TreeSet<Cycle>();
        val memo = new HashMap<Permutation, List<Pair<Cycle, Permutation>>>();
        factorizations(CANONICAL_LONG_CYCLES[n + 2], memo)
                .forEach(f -> {
                    val delta = (MulticyclePermutation) f.getRight();
                    delta.remove(fixedZero);
                    val u = delta.conjugateBy(CANONICAL_LONG_CYCLES[n + 2].getInverse());
                    u.remove(fixedNPlus1);
                    unicycles.add(u.asNCycle());
                });
        unicycles.forEach(System.out::println);
    }

    public static List<Pair<Cycle, Permutation>> factorizations(Permutation tau,
            final Map<Permutation, List<Pair<Cycle, Permutation>>> memo) {
        if (memo.containsKey(tau)) {
            return memo.get(tau);
        }

        if (tau.image(0) == 0) {
            return Collections.emptyList();
        }

        val n = tau.getMaxSymbol();

        if (n == 1) {
            return List.of(ImmutablePair.of(Cycle.of(0, 1),
                    new MulticyclePermutation(List.of(Cycle.of(0), Cycle.of(1)))));
        }

        val factorizations = new ArrayList<Pair<Cycle, Permutation>>();

        val tauZero = tau.getInverse().image(0);
        val conjugator = Cycle.of(tauZero, n);
        tau = tau.conjugateBy(conjugator);

        for (int h = 1; h < n; h++) {
            if (h != tau.image(0)) {
                val t = ((MulticyclePermutation) Cycle.of(n, h, 0).times(tau));
                t.remove(Cycle.of(n));

                for (val f : factorizations(t, memo)) {
                    val c = f.getLeft();
                    val d = f.getRight();

                    val gamma = Cycle.of(n, 0).times(c).asNCycle();
                    val delta = Cycle.of(n, h).times(d);

                    factorizations.add(ImmutablePair.of(
                            gamma.conjugateBy(conjugator).asNCycle(),
                            delta.conjugateBy(conjugator)));
                }
            }
        }

        memo.put(tau, factorizations);

        return factorizations;
    }
}
