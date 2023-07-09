package br.unb.cic.permutation;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
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
        unicycles(4);
    }

    private static void unicycles(final int n) {
        factorizations(CANONICAL_LONG_CYCLES[n + 1]).forEach(System.out::println);
    }

    public static List<Pair<Permutation, Permutation>> factorizations(final Permutation sigma) {
        if (!sigma.isEven()) {
            throw new RuntimeException("Tau must be even");
        }

        val factorizations = new ArrayList<Pair<Permutation, Permutation>>();

        if (sigma.isIdentity()) {
            factorizations.add(ImmutablePair.of(Cycle.of(0), Cycle.of(0)));
        } else {
            if (sigma instanceof MulticyclePermutation) {
                val _sigma = (MulticyclePermutation) sigma;
                if (_sigma.size() == 2 && _sigma.getSymbols().size() == 4 && _sigma.stream().allMatch(c -> c.size() == 2)) {
                    val _1 = _sigma.getNonTrivialCycles().get(0).get(0);
                    val _2 = _sigma.getNonTrivialCycles().get(0).get(1);
                    val _3 = _sigma.getNonTrivialCycles().get(1).get(0);
                    val _4 = _sigma.getNonTrivialCycles().get(1).get(1);

                    //(234)(134)(234)(123)

                    val pair = ImmutablePair.of(Cycle.of(_2, _3, _4).times(Cycle.of(_1, _2, _3).conjugateBy(Cycle.of(_2, _3, _4))),
                            Cycle.of(_2, _3, _4).times(Cycle.of(_1, _2, _3)));

                    factorizations.add(pair);

                    return factorizations;
                }
            }

            val n = sigma.getMaxSymbol();

            val a = sigma.getMinMovedSymbol();

            val b = a + 1;

            for (int k = a + 1; k <= n; k++) {
                if (k == b) {
                    continue;
                }

                // o inverso de _3CycleFactor deve ser crescente e aplicável à \sigma
                val _3CycleFactor = Cycle.of(a, k, b);

                var sigmaPrime = ((MulticyclePermutation) _3CycleFactor.times(sigma));

                if (breaksInto3Cycles(sigma, sigmaPrime)) {
                    if (isProduct2Disjoint2Cycles(sigmaPrime) || is2Move(sigmaPrime)) {
                        final var removeSymbols = IntStream.range(0, b).<String>mapToObj(i -> "(" + i + ")").collect(Collectors.joining());

                        sigmaPrime = new MulticyclePermutation(sigmaPrime.toString().replace(removeSymbols, ""));

                        for (val f : factorizations(sigmaPrime)) {
                            val d = f.getLeft();
                            val c = f.getRight();

                            val gamma = _3CycleFactor.times(d.conjugateBy(_3CycleFactor));
                            val delta = _3CycleFactor.times(c);

                            factorizations.add(ImmutablePair.of(gamma, delta));
                        }
                    }
                }
            }
        }

        return factorizations;
    }

    private static boolean is2Move(MulticyclePermutation sigmaPrime) {
        return sigmaPrime.stream()
                .allMatch(Cycle::isEven);
    }

    private static boolean isProduct2Disjoint2Cycles(MulticyclePermutation sigmaPrime) {
        if (!sigmaPrime.isIdentity()) {
            final var disjointCycles = new MulticyclePermutation(sigmaPrime.stream()
                    .map(c -> c.size() == 1 ? "" : c.toString())
                    .collect(Collectors.joining()));
            if (disjointCycles.size() == 2 && disjointCycles.getSymbols().size() == 4) {
                return true;
            }
        }
        return false;
    }

    private static boolean breaksInto3Cycles(Permutation sigma, MulticyclePermutation sigmaPrime) {
        return numberOfCycles(sigmaPrime) == numberOfCycles(sigma.times(Cycle.of(0))) + 2;
    }

    private static int numberOfCycles(final Permutation permutation) {
        if (permutation instanceof MulticyclePermutation) {
            return ((MulticyclePermutation) permutation).size();
        }
        return 1;
    }
}
