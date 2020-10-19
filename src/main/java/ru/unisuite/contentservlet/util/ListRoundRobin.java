package ru.unisuite.contentservlet.util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Thread-safe round-robin to choose one form list
 */
public class ListRoundRobin<T> {
    private final List<T> list;
    private final IntegerRoundRobin roundRobin;

    public ListRoundRobin(List<T> list) {
        this.list = new ArrayList<>(list);
        this.roundRobin = new IntegerRoundRobin(list.size());
    }

    public T getNext() {
        return list.get(roundRobin.index());
    }

    // https://dzone.com/articles/atomicinteger-on-java-and-round-robin
    private static class IntegerRoundRobin {
        private final int totalIndexes;
        private final AtomicInteger atomicInteger = new AtomicInteger(-1);

        IntegerRoundRobin(int totalIndexes) {
            this.totalIndexes = totalIndexes;
        }

        int index() {
            int currentIndex;
            int nextIndex;
            do {
                currentIndex = atomicInteger.get();
                nextIndex = currentIndex < Integer.MAX_VALUE ? currentIndex + 1 : 0;
            } while (!atomicInteger.compareAndSet(currentIndex, nextIndex));
            return nextIndex % totalIndexes;
        }
    }
}
