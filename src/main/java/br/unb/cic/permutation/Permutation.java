package br.unb.cic.permutation;

public interface Permutation {

    Permutation getInverse();

    Cycle asNCycle();

    default Permutation conjugateBy(final Permutation conjugator) {
        return PermutationGroups.computeProduct(conjugator, this, conjugator.getInverse());
    }

    default Permutation times(final Permutation operand) {
        return PermutationGroups.computeProduct(true, this, operand);
    }

    boolean contains(int i);

    int image(int a);

    int getMaxSymbol();

    boolean isEven();
}
