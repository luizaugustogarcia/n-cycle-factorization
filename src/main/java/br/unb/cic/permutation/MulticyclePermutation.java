package br.unb.cic.permutation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.val;
import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringUtils;

public class MulticyclePermutation implements Collection<Cycle>, Permutation {

    private static final Pattern CYCLE_PATTERN = Pattern.compile("\\(([^\\(\\)]*?)\\)");

    private final List<Cycle> cycles = new ArrayList<>();

    private final Set<Integer> symbols = new HashSet<>();

    public MulticyclePermutation() {
    }

    public MulticyclePermutation(final String permutation) {
        of(permutation);
    }

    private void of(final String permutation) {
        if (!permutation.contains("(")) {
            this.add(Cycle.of(permutation));
        } else {
            val matcher = CYCLE_PATTERN.matcher(permutation);
            while (matcher.find()) {
                this.add(Cycle.of(matcher.group(1)));
            }
        }
    }

    public MulticyclePermutation(final Collection<Cycle> cycles) {
        this.addAll(cycles);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        return Objects.equals(cycles, ((MulticyclePermutation) o).cycles);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cycles);
    }

    @Override
    public String toString() {
        if (this.isEmpty()) {
            return "()";
        }
        return StringUtils.join(this, "");
    }

    @Override
    public MulticyclePermutation getInverse() {
        val permutation = new MulticyclePermutation();

        this.forEach((cycle) -> permutation.add(cycle.getInverse()));

        return permutation;
    }

    public Cycle asNCycle() {
        val nonTrivialCycles = getNonTrivialCycles();
        if (nonTrivialCycles.size() > 1) {
            throw new RuntimeException("NONCYCLICPERMUTATION");
        }
        return nonTrivialCycles.stream().findFirst().get();
    }

    @Override
    public int image(final int a) {
        for (var cycle : cycles) {
            if (cycle.contains(a)) {
                return cycle.image(a);
            }
        }
        return a;
    }

    public Set<Integer> getSymbols() {
        return symbols;
    }

    public int getMaxSymbol() {
        return getSymbols().stream().max(Comparator.comparing(Function.identity())).orElse(-1);
    }

    @Override
    public boolean isEven() {
        return this.cycles.stream().mapToInt(c -> c.isEven() ? 1 : -1).reduce(1, (a,b) -> a * b) == 1;
    }

    public List<Cycle> getNonTrivialCycles() {
        return this.stream().filter(c -> c.size() > 1).collect(Collectors.toList());
    }

    @Override
    public int size() {
        return cycles.size();
    }

    @Override
    public boolean isEmpty() {
        return cycles.isEmpty();
    }

    @Override
    public boolean contains(final Object o) {
        return symbols.contains(o);
    }

    @Override
    public boolean contains(final int o) {
        return symbols.contains(o);
    }

    @Override
    public Iterator<Cycle> iterator() {
        return cycles.iterator();
    }

    @Override
    public Object[] toArray() {
        return cycles.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return cycles.toArray(a);
    }

    @Override
    public boolean add(final Cycle cycle) {
        val symbols = cycle.getSymbols();
        for (var s : symbols) {
            this.symbols.add(s);
        }
        return cycles.add(cycle);
    }

    @Override
    public boolean addAll(final Collection<? extends Cycle> c) {
        c.forEach(this::add);
        return true;
    }

    @Override
    public boolean remove(final Object o) {
        val cycle = (Cycle) o;
        Arrays.stream(cycle.getSymbols()).forEach(symbols::remove);
        return cycles.remove(o);
    }

    @Override
    public boolean removeAll(final Collection<?> c) {
        c.forEach(o -> {
            val cycle = (Cycle) o;
            Arrays.stream(cycle.getSymbols()).forEach(symbols::remove);
        });
        return cycles.removeAll(c);
    }

    @Override
    public boolean containsAll(final Collection<?> c) {
        return new HashSet<>(cycles).containsAll(c);
    }

    @Override
    public boolean retainAll(final Collection<?> c) {
        throw new NotImplementedException();
    }

    @Override
    public void clear() {
        cycles.clear();
    }
}
