package ru.ifmo.rain.ustinov.concurrent;


import info.kgeorgiy.java.advanced.concurrent.ListIP;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import static java.lang.Math.min;

public class IterativeParallelism implements ListIP {

    private final ParallelMapper mapper;

    /**
     * Default constructor.
     * Creates an IterativeParallelism instance operating without {@link ParallelMapper}.
     */
    public IterativeParallelism() {
        mapper = null;
    }

    /**
     * Mapper constructor.
     * Creates an IterativeParallelism instance with {@link ParallelMapper} as a core mapper.
     *
     * @param mapper {@link ParallelMapper} instance
     */
    public IterativeParallelism(ParallelMapper mapper) {
        this.mapper = mapper;
    }

    private <T, U, V> U calculate(int threadCount, List<? extends T> list, Function<Stream<? extends T>, ? extends V> map,
                                  Function<Stream<? extends V>, ? extends U> reduce) throws InterruptedException {
        final List<Thread> threads = new ArrayList<>(threadCount);
        if (threadCount == 0) {
            throw new IllegalArgumentException("Number of threads is zero");
        }
        threadCount = min(threadCount, list.size());
        List<V> results;

        final int partSize = list.size() / threadCount;
        final int remainder = list.size() % threadCount;

        List<Stream<? extends T>> streams = new ArrayList<>(threadCount);
        int shift = 0;
        for (int i = 0; i < threadCount; i++) {
            int k = (i >= remainder ? 0 : 1);
            streams.add(list.subList(i * partSize + shift, (i + 1) * partSize + shift + k).stream());
            shift += k;
        }
        if (mapper != null) {
            results = mapper.map(map, streams);
        } else {
            results = new ArrayList<>(Collections.nCopies(threadCount, null));
            for (int i = 0; i < threadCount; i++) {
                final int ind = i;
                threads.add(new Thread(() -> results.set(ind, map.apply(streams.get(ind)))));
                threads.get(ind).start();
            }

            InterruptedException exception = null;
            for (int i = 0; i < threadCount; i++) {
                try {
                    threads.get(i).join();
                } catch (InterruptedException e) {
                    if (exception == null) {
                        exception = e;
                    } else exception.addSuppressed(e);
                }
            }
            if (exception != null) {
                throw exception;
            }
        }
        return reduce.apply(results.stream());
    }

    /**
     * Join values to string.
     *
     * @param threadCount number of concurrent threads.
     * @param values values to join.
     *
     * @return list of joined result of {@link #toString()} call on each value.
     *
     * @throws InterruptedException if executing thread was interrupted.
     */
    @Override
    public String join(int threadCount, List<?> values) throws InterruptedException {
        return calculate(threadCount, values, x -> x.map(Object::toString).collect(Collectors.joining()), x -> x.collect(Collectors.joining()));
    }

    /**
     * Filters values by predicate.
     *
     * @param threadCount number of concurrent threads.
     * @param values values to filter.
     * @param predicate filter predicate.
     *
     * @return list of values satisfying given predicated. Order of values is preserved.
     *
     * @throws InterruptedException if executing thread was interrupted.
     */
    @Override
    public <T> List<T> filter(int threadCount, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return calculate(threadCount, values, x -> x.filter(predicate), x -> x.flatMap(y -> y).collect(Collectors.toList()));
    }

    /**
     * Maps values.
     *
     * @param threadCount number of concurrent threads.
     * @param values values to filter.
     * @param f mapper function.
     *
     * @return list of values mapped by given function.
     *
     * @throws InterruptedException if executing thread was interrupted.
     */
    @Override
    public <T, U> List<U> map(int threadCount, List<? extends T> values, Function<? super T, ? extends U> f) throws InterruptedException {
        return calculate(threadCount, values, x -> x.map(f), x -> x.flatMap(y -> y).collect(Collectors.toList()));
    }

    /**
     * Returns maximum value.
     *
     * @param threadCount number or concurrent threads.
     * @param values values to get maximum of.
     * @param comparator value comparator.
     * @param <T> value type.
     *
     * @return maximum of given values
     *
     * @throws InterruptedException if executing thread was interrupted.
     * @throws java.util.NoSuchElementException if not values are given.
     */
    @Override
    public <T> T maximum(int threadCount, List<? extends T> values, Comparator<? super T> comparator) throws InterruptedException {
        Function<Stream<? extends T>, ? extends T> max = x -> x.max(comparator).orElse(null);
        return calculate(threadCount, values, max, max);
    }

    /**
     * Returns minimum value.
     *
     * @param threadCount number or concurrent threads.
     * @param values values to get minimum of.
     * @param comparator value comparator.
     * @param <T> value type.
     *
     * @return minimum of given values
     *
     * @throws InterruptedException if executing thread was interrupted.
     * @throws java.util.NoSuchElementException if not values are given.
     */
    @Override
    public <T> T minimum(int threadCount, List<? extends T> values, Comparator<? super T> comparator) throws InterruptedException {
        Function<Stream<? extends T>, ? extends T> min = x -> x.min(comparator).orElse(null);
        return calculate(threadCount, values, min, min);
    }

    /**
     * Returns whether all values satisfies predicate.
     *
     * @param threadCount number or concurrent threads.
     * @param values values to test.
     * @param predicate test predicate.
     * @param <T> value type.
     *
     * @return whether all values satisfies predicate or {@code true}, if no values are given.
     *
     * @throws InterruptedException if executing thread was interrupted.
     */
    @Override
    public <T> boolean all(int threadCount, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return calculate(threadCount, values, x -> x.allMatch(predicate), x -> x.allMatch(y -> y));
    }

    /**
     * Returns whether any of values satisfies predicate.
     *
     * @param threadCount number or concurrent threads.
     * @param values values to test.
     * @param predicate test predicate.
     * @param <T> value type.
     *
     * @return whether any value satisfies predicate or {@code false}, if no values are given.
     *
     * @throws InterruptedException if executing thread was interrupted.
     */
    @Override
    public <T> boolean any(int threadCount, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return calculate(threadCount, values, x -> x.anyMatch(predicate), x -> x.anyMatch(y -> y));
    }
}
