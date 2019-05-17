package ru.ifmo.rain.ustinov.mapper;

import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

public class ParallelMapperImpl implements ParallelMapper {

    private List<Thread> threads;
    private final Queue<Task> tasks;
    private static final int MAX_TASK_NUMBER = 100_000;

    class Task {
        Runnable runnable;

        final AtomicInteger counter;

        Task(Runnable runnable, final AtomicInteger counter) {
            this.runnable = runnable;
            this.counter = counter;
        }
    }

    public ParallelMapperImpl(int threadCount) {
        if (threadCount <= 0) {
            throw new IllegalArgumentException("Number of threads must be greater than 0");
        }

        threads = new ArrayList<>();
        tasks = new LinkedList<>();

        final Runnable TASK = () -> {
            try {
                while (!Thread.interrupted()) {
                    doTask();
                }
            } catch (InterruptedException ignored) {
                // do nothing
            } finally {
                Thread.currentThread().interrupt();
            }
        };

        for (int i = 0; i < threadCount; i++) {
            threads.add(new Thread(TASK));
        }
        threads.forEach(Thread::start);
    }

    private void doTask() throws InterruptedException {
        Task task;
        synchronized (tasks) {
            while (tasks.isEmpty()) {
                tasks.wait();
            }
            task = tasks.poll();
        }
        task.runnable.run();

        while (true) {
            int prev = task.counter.get();
            int next = task.counter.get() - 1;
            if (task.counter.compareAndSet(prev, next)) {
                break;
            }
        }

    }

    private void addTask(Task task) throws InterruptedException {
        synchronized (tasks) {
            while (tasks.size() >= MAX_TASK_NUMBER) {
                tasks.wait();
            }
            tasks.add(task);
            tasks.notifyAll();
        }
    }

    class State {
        boolean b = false;

        void makeTrue() {
            b = true;
        }

        boolean isTrue() {
            return b;
        }
    }

    /**
     * Maps function {@code f} over specified {@code args}.
     * Mapping for each element performs in parallel.
     *
     * @throws InterruptedException if calling thread was interrupted
     */
    @Override
    public <T, R> List<R> map(Function<? super T, ? extends R> f, List<? extends T> args) throws InterruptedException {
        final AtomicInteger counter = new AtomicInteger(args.size());
        List<R> results = Collections.synchronizedList(new ArrayList<>(Collections.nCopies(args.size(), null)));
        final RuntimeException suppressedException = new RuntimeException();
        final State flag = new State();
        for (int i = 0; i < args.size(); i++) {
            final int ind = i;
            addTask(new Task(() -> {
                try {
                    results.set(ind, f.apply(args.get(ind)));
                } catch (Exception e) {
                    flag.makeTrue();
                    suppressedException.addSuppressed(e);
                }
            }, counter));
        }

        while (counter.get() > 0) {
            if (Thread.interrupted()) {
                throw new InterruptedException("Process is interrupted.");
            }
        }
        if (flag.isTrue()) {
            close();
            throw suppressedException;
        }

        return results;
    }

    /**
     * Stops all threads. All unfinished mappings leave in undefined state.
     */
    @Override
    public void close() {
        threads.forEach(Thread::interrupt);

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException ignored) {
                // do nothing
            }
        }
    }
}
