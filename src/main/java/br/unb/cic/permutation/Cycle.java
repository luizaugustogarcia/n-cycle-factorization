package br.unb.cic.permutation;

import java.util.Arrays;
import lombok.val;
import org.apache.commons.lang.ArrayUtils;

public class Cycle implements Permutation, Comparable<Cycle> {
    private final int[] symbols;
    private int[] symbolIndexes;
    private int minSymbol = -1;
    private int maxSymbol = -1;
    private Cycle inverse;
    private Integer hashCode;

    public Cycle(final int... symbols) {
        this.symbols = symbols;
        updateInternalState();
    }

    public static Cycle of(final String cycle) {
        val strSymbols = cycle.replace("(", "").replace(")", "").split(",|\\s");
        val symbols = new int[strSymbols.length];
        for (var i = 0; i < strSymbols.length; i++) {
            val strSymbol = strSymbols[i];
            symbols[i] = Integer.parseInt(strSymbol);
        }
        return of(symbols);
    }

    public static Cycle of(final int... symbols) {
        return new Cycle(symbols);
    }

    public int[] getSymbols() {
        return symbols;
    }

    private void updateInternalState() {
        for (val symbol : symbols) {
            if (minSymbol == -1 || symbol < minSymbol) {
                minSymbol = symbol;
            }
            if (symbol > maxSymbol) {
                maxSymbol = symbol;
            }
        }

        symbolIndexes = new int[maxSymbol + 1];

        Arrays.fill(symbolIndexes, -1);

        for (var i = 0; i < symbols.length; i++) {
            symbolIndexes[symbols[i]] = i;
        }
    }

    public int getMaxSymbol() {
        return maxSymbol;
    }

    public int getMinSymbol() {
        return minSymbol;
    }

    @Override
    public String toString() {
        return defaultStringRepresentation();
    }

    @Override
    public Cycle getInverse() {
        if (inverse == null) {
            val symbolsCopy = new int[this.symbols.length];
            System.arraycopy(this.symbols, 0, symbolsCopy, 0, this.symbols.length);
            ArrayUtils.reverse(symbolsCopy);
            inverse = Cycle.of(symbolsCopy);
        }
        return inverse;
    }

    private String defaultStringRepresentation() {
        val defaultCycle = this.startingBy(minSymbol);

        val representation = new StringBuilder().append("(");

        for (var i = 0; ; i++) {
            representation.append(defaultCycle.symbols[i]);
            if (i == defaultCycle.symbols.length - 1) {
                break;
            }
            representation.append(" ");
        }
        representation.append(")");

        return representation.toString();
    }

    @Override
    public int hashCode() {
        if (hashCode == null) {
            hashCode = Arrays.hashCode(startingBy(getMinSymbol()).getSymbols());
        }
        return hashCode;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }

        if (getClass() != obj.getClass()) {
            return false;
        }

        val other = (Cycle) obj;

        if (size() != other.size()) {
            return false;
        }

        if (getMinSymbol() != other.getMinSymbol() || getMaxSymbol() != other.getMaxSymbol()) {
            return false;
        }

        if (this.hashCode() != obj.hashCode()) {
            return false;
        }

        return Arrays.equals(startingBy(getMinSymbol()).getSymbols(),
                ((Cycle) obj).startingBy(((Cycle) obj).getMinSymbol()).getSymbols());
    }

    public boolean isEven() {
        return this.size() % 2 == 1;
    }

    public int image(final int a) {
        return symbols[(symbolIndexes[a] + 1) % symbols.length];
    }

    public Cycle startingBy(final int symbol) {
        if (this.symbols[0] == symbol) {
            return this;
        }

        val index = indexOf(symbol);
        val symbols = new int[this.symbols.length];
        System.arraycopy(this.symbols, index, symbols, 0, symbols.length - index);
        System.arraycopy(this.symbols, 0, symbols, symbols.length - index, index);

        return Cycle.of(symbols);
    }

    @Override
    public int compareTo(final Cycle o) {
        return this.defaultStringRepresentation().compareTo(o.defaultStringRepresentation());
    }

    public int get(final int i) {
        return symbols[i];
    }

    public int indexOf(final int symbol) {
        return symbolIndexes[symbol];
    }

    public boolean contains(final int symbol) {
        return (symbol >= 0 && symbol <= symbolIndexes.length - 1) && symbolIndexes[symbol] != -1;
    }

    @Override
    public int size() {
        return symbols.length;
    }

    @Override
    public Cycle asNCycle() {
        return this;
    }
}