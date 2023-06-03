package br.unb.cic.permutation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;
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
        val conjugator = CANONICAL_LONG_CYCLES[n + 2].getInverse();

        val fixedZero = Cycle.of(0);
        val fixedNPlus1 = Cycle.of(n + 1);

        factorizations(CANONICAL_LONG_CYCLES[n + 2])
                .forEach(f -> {
//                    val delta = (MulticyclePermutation) f.getRight();
//                    delta.remove(fixedZero);
//                    val u = delta.conjugateBy(CANONICAL_LONG_CYCLES[n + 2].getInverse());
//                    u.remove(fixedNPlus1);
//                    unicycles.add(u.asNCycle());
                    System.out.println(f);
//                    System.out.println(f.getLeft().stream().map(c -> c.conjugateBy(conjugator).toString()).collect(Collectors.joining()) + ", " +
//                                       f.getRight().stream().map(c -> c.conjugateBy(conjugator).toString()).collect(Collectors.joining()));
                });
    }

    public static List<Pair<Stack<Cycle>, Stack<Cycle>>> factorizations(Permutation tau) {
        if (tau.isEven()) {
            throw new RuntimeException("Tau must be odd");
        }

        val n = tau.getMaxSymbol();

        List<Pair<Stack<Cycle>, Stack<Cycle>>> factorizations = new ArrayList<>();

        if (tau.image(0) != 0) {
            if (n == 1) {
                factorizations.add(ImmutablePair.of(
                        newStackAndPush(Cycle.of(0, 1)),
                        newStackAndPush(Cycle.of(0), Cycle.of(1))));
            } else {
                val tauZero = tau.getInverse().image(0);
                val conjugator = Cycle.of(tauZero, n);
                val tauPrime = tau.conjugateBy(conjugator);

                for (int h = 1; h < n; h++) {
                    if (h != tauPrime.image(0)) {
                        val t = ((MulticyclePermutation) Cycle.of(n, h, 0).times(tauPrime));
                        t.remove(Cycle.of(n));

                        for (val f : factorizations(t)) {
                            val c = f.getLeft();
                            val d = f.getRight();

                            val gamma = newStackAndPush();
                            for (val cPrime: c) {
                                gamma.push(cPrime.conjugateBy(conjugator).asNCycle());
                            }
                            gamma.push(Cycle.of(n, 0).conjugateBy(conjugator).asNCycle());

                            val delta = newStackAndPush();
                            for (val dPrime: d) {
                                if (dPrime.size() == 1) {
                                    delta.push(Cycle.of(conjugator.image(dPrime.getMaxSymbol())));
                                } else {
                                    delta.push(dPrime.conjugateBy(conjugator).asNCycle());
                                }
                            }
                            delta.push(Cycle.of(n, h).conjugateBy(conjugator).asNCycle());

                            factorizations.add(ImmutablePair.of(gamma, delta));
                        }
                    }
                }
            }
        }

        return factorizations;
    }

    private static Stack<Cycle> newStackAndPush(final Cycle... cycle) {
        val stack = new Stack<Cycle>();
        for (val c : cycle) {
            stack.push(c);
        }
        return stack;
    }
}
