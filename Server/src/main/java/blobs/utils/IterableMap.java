package blobs.utils;

import java.util.Iterator;
import java.util.function.Function;

public class IterableMap<Val, MVal> implements Iterable<MVal> {
    private final Iterable<Val> base;
    private final Function<Val, MVal> mapper;

    private IterableMap(Iterable<Val> base, Function<Val, MVal> mapper) {
        this.base = base;
        this.mapper = mapper;
    }

    public static <Val, MVal> IterableMap<Val, MVal> of(Iterable<Val> base, Function<Val, MVal> mapper) {
        return new IterableMap<>(base, mapper);
    }

    @Override
    public Iterator<MVal> iterator() {
        Iterator<Val> baseIterator = base.iterator();
        return new Iterator<>() {
            @Override
            public boolean hasNext() {
                return baseIterator.hasNext();
            }

            @Override
            public MVal next() {
                return mapper.apply(baseIterator.next());
            }
        };
    }
}
