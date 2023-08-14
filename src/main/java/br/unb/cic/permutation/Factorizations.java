package br.unb.cic.permutation;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import org.javatuples.Quartet;

import lombok.val;

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
        unicycles(10);
    }

    private static void unicycles(final int n) {
        val conjugator = CANONICAL_LONG_CYCLES[n + 1].getInverse();

        val fixedZero = Cycle.of(0);
        val fixedNPlus1 = Cycle.of(n + 1);

        val unicycles = new TreeSet<Cycle>();

        factorizations(CANONICAL_LONG_CYCLES[n + 2]).forEach(System.out::println);
//                .forEach(f -> {
//                    val delta = (MulticyclePermutation) f.getRight();
//                    delta.remove(fixedZero);
//                    val u = delta.conjugateBy(CANONICAL_LONG_CYCLES[n + 2].getInverse());
//                    u.remove(fixedNPlus1);
//                    unicycles.add(u.asNCycle());
//                });
//        unicycles.forEach(System.out::println);
    }

    public static List<Quartet<Cycle, String, Permutation, String>> factorizations(final Permutation tau) {
        if (tau.isEven()) {
            throw new RuntimeException("Tau must be an odd permutation");
        }

        val n = tau.getMaxSymbol();

        List<Quartet<Cycle, String, Permutation, String>> factorizations = new ArrayList<>();

        if (tau.image(0) != 0) {
            if (n == 1) {
                factorizations.add(Quartet.with(Cycle.of(0, 1), "(0 1)",
                        new MulticyclePermutation(List.of(Cycle.of(0), Cycle.of(1))), "(0)"));
            } else {
                val tauZero = tau.getInverse().image(0);

                val conjugator = tauZero == tau.getMaxSymbol() ? Cycle.of(0) : Cycle.of(tauZero, tau.getMaxSymbol());
                val tauPrime = tau.conjugateBy(conjugator);

                for (int h = 1; h < n; h++) {
                    if (h != tauPrime.image(0)) {
                        val t = ((MulticyclePermutation) Cycle.of(n, h, 0).times(tauPrime));
                        t.remove(Cycle.of(n));

                        for (val f : factorizations(t)) {
                            val c = f.getValue0();
                            val d = f.getValue2();

                            val gamma = Cycle.of(0, n).times(c);
                            val delta = Cycle.of(n, h).times(d);

                            val gammaStr = conjugator.size() == 1 ?
                                    String.format("(0 %s)%s", n, f.getValue1()) :
                                    String.format("[(0 %s)%s]^%s", n, f.getValue1(), conjugator);

                            val deltaStr = conjugator.size() == 1 ?
                                    String.format("(%s %s)%s", n, h, f.getValue3()) :
                                    String.format("[(%s %s)%s]^%s", n, h, f.getValue3(), conjugator);

                            factorizations.add(
                                    Quartet.with(
                                            gamma.conjugateBy(conjugator).asNCycle(), gammaStr,
                                            delta.conjugateBy(conjugator), deltaStr
                                    ));
                        }
                    }
                }
            }
        }

        return factorizations;
    }
}
