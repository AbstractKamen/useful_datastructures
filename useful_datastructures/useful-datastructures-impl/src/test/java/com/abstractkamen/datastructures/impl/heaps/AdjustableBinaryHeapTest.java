package com.abstractkamen.datastructures.impl.heaps;

import org.junit.Test;

import java.util.*;
import java.util.stream.IntStream;

import static org.junit.Assert.*;

public class AdjustableBinaryHeapTest {
    @Test
    public void decreaseKey_test() {
        final List<List<Integer>> expected = Arrays.asList(
            Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9),
            Arrays.asList(0, 1, 2, 3, 4, 9, 5, 6, 7, 8),
            Arrays.asList(0, 1, 2, 9, 3, 4, 5, 6, 7, 8),
            Arrays.asList(0, 9, 1, 2, 3, 4, 5, 6, 7, 8),
            Arrays.asList(9, 0, 1, 2, 3, 4, 5, 6, 7, 8)
        );
        for (int i = 0; i < expected.size(); i++) {
            final AdjustableBinaryHeap<Integer> adjustableBinaryHeap = AdjustableBinaryHeap.createComparable();
            for (int j = 0; j < 10; j++) {
                adjustableBinaryHeap.push(j);
            }
            final List<Integer> actualOrder = new ArrayList<>();
            for (int j = 0; j < i; j++) {
                assertTrue(adjustableBinaryHeap.decreaseKey(9));
            }
            while (!adjustableBinaryHeap.isEmpty()) {
                actualOrder.add(adjustableBinaryHeap.pop());
            }
            assertEquals((i + 1) + "", expected.get(i), actualOrder);
        }
    }

    @Test
    public void increaseKey_test() {
        final List<List<Integer>> expected = Arrays.asList(
            Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9),
            Arrays.asList(1, 0, 2, 3, 4, 5, 6, 7, 8, 9),
            Arrays.asList(1, 2, 3, 0, 4, 5, 6, 7, 8, 9),
            Arrays.asList(1, 2, 3, 4, 5, 6, 0, 7, 8, 9)
        );
        for (int i = 0; i < expected.size(); i++) {
            final AdjustableBinaryHeap<Integer> adjustableBinaryHeap = AdjustableBinaryHeap.createComparable();
            for (int j = 0; j < 10; j++) {
                adjustableBinaryHeap.push(j);
            }
            final List<Integer> actualOrder = new ArrayList<>();
            for (int j = 0; j < i; j++) {
                assertTrue(adjustableBinaryHeap.increaseKey(0));
            }
            while (!adjustableBinaryHeap.isEmpty()) {
                actualOrder.add(adjustableBinaryHeap.pop());
            }
            assertEquals((i + 1) + "", expected.get(i), actualOrder);
        }
    }

    @Test
    public void push_pop_size_randomTest() {
        for (int j = 0; j < 10; j++) {
            // arrange
            final AdjustableBinaryHeap<Integer> adjustableBinaryHeap = AdjustableBinaryHeap.createComparable();
            // act
            final int maxSize = 1000;
            final long expecteSize = new Random().ints(maxSize, -10000, 10000).distinct().peek(adjustableBinaryHeap::push).count();
            assertEquals(expecteSize, adjustableBinaryHeap.size());
            // assert
            final List<Integer> expected = new ArrayList<>(adjustableBinaryHeap.size());
            final List<Integer> actual = new ArrayList<>(adjustableBinaryHeap.size());
            while (!adjustableBinaryHeap.isEmpty()) {
                final int popped = adjustableBinaryHeap.pop();
                actual.add(popped);
                expected.add(popped);
            }
            expected.sort(Integer::compare);
            assertEquals(expected, actual);
        }
    }

    @Test
    public void restoreHeapOrder_randomTest() {
        for (int j = 0; j < 1; j++) {
            final int maxSize = 10;
            final Comparator<Mutable> comparator = Comparator.comparing(m -> m.m);
            final AdjustableBinaryHeap<Mutable> heap = new AdjustableBinaryHeap<>(comparator);
            final List<Mutable> mutablesSortedList = new ArrayList<>();
            final Random r = new Random();
            r.ints(maxSize, -10000, 10000)
                .distinct()
                .forEach(i -> {
                    final Mutable mutable = new Mutable(i);
                    heap.push(mutable);
                    mutablesSortedList.add(mutable);
                });
            mutablesSortedList.sort(comparator);
            assertEquals(mutablesSortedList.get(0), heap.peek());
            for (Mutable mutable : mutablesSortedList) {
                mutable.m = r.ints(1, -10000, 10000).sum();
            }
            final Mutable leastBeforeOrder = heap.peek();
            assertEquals(mutablesSortedList.get(0), leastBeforeOrder);
            heap.restoreHeapOrder();
            mutablesSortedList.sort(comparator);
            assertNotEquals(mutablesSortedList.get(0), leastBeforeOrder);
            assertEquals(mutablesSortedList.get(0), heap.peek());
            final List<Mutable> heapOrder = new ArrayList<>();
            while (!heap.isEmpty()) {
                heapOrder.add(heap.pop());
            }
            assertEquals(mutablesSortedList, heapOrder);
        }
    }

    @Test
    public void restoreHeapOrderTest() {
        final AdjustableBinaryHeap<Mutable> heap = new AdjustableBinaryHeap<>(Comparator.comparing(m -> m.m));
        // add numbers in order [i :: 10]
        for (int i = 0; i < 10; i++) {
            heap.push(new Mutable(i));
        }
        final Mutable least = heap.peek();
        assertEquals(0, least.m);
        least.m = 150;
        assertEquals(least, heap.peek());
        heap.restoreHeapOrder();
        assertEquals(1, heap.peek().m);
    }

    @Test
    public void push_pop_size_duplicateTest() {
        // arrange
        final AdjustableBinaryHeap<Integer> adjustableBinaryHeap = AdjustableBinaryHeap.createComparable();
        // act
        final int maxSize = 100;
        IntStream.generate(() -> 0).limit(maxSize).forEach(adjustableBinaryHeap::push);
        assertEquals(1, adjustableBinaryHeap.size());
        // assert
        final List<Integer> actual = new ArrayList<>(adjustableBinaryHeap.size());
        while (!adjustableBinaryHeap.isEmpty()) {
            final int popped = adjustableBinaryHeap.pop();
            actual.add(popped);
        }
        assertEquals(Collections.singletonList(0), actual);
    }

    @Test
    public void push_sizeTest() {
        final AdjustableBinaryHeap<Integer> adjustableBinaryHeap = AdjustableBinaryHeap.createComparable();
        for (int i = 0; i < 10; i++) {
            final int expectedSize = i + 1;
            assertEquals(expectedSize, adjustableBinaryHeap.push((int) (Math.random() * i * 1000 - 1500)));
        }
    }

    @Test
    public void peekTest() {
        final AdjustableBinaryHeap<Integer> adjustableBinaryHeap = AdjustableBinaryHeap.createComparable();
        // when empty
        final Integer actual = adjustableBinaryHeap.peek();
        assertNull(actual);
        // when size == 1
        adjustableBinaryHeap.push(0);
        assertEquals(0, adjustableBinaryHeap.peek().intValue());
        // when size 2
        adjustableBinaryHeap.push(1);
        assertEquals(0, adjustableBinaryHeap.peek().intValue());
        // when size 10
        IntStream.range(2, 10).forEach(adjustableBinaryHeap::push);
        assertEquals(0, adjustableBinaryHeap.peek().intValue());
        // when we add a new min value
        adjustableBinaryHeap.push(-1);
        assertEquals(-1, adjustableBinaryHeap.peek().intValue());
    }

    @Test
    public void mergeWith_shouldReturnExpected_whenValuesOfHeapsAreTheSame() {
        // arrange
        final List<Integer> expectedMerged = new ArrayList<>();
        final AdjustableBinaryHeap<Integer> left = AdjustableBinaryHeap.createComparable();
        final AdjustableBinaryHeap<Integer> right = AdjustableBinaryHeap.createComparable();
        for (int i = 0; i < 10; i++) {
            expectedMerged.add(i);
            if (i % 2 == 0) {
                left.push(i);
            } else {
                right.push(i);
            }
        }
        // act
        final AdjustableBinaryHeap<Integer> actual = left.mergeWith(right);
        // assert
        assertSame(left, actual);
        assertEquals(expectedMerged.size(), left.size());
        final List<Integer> actualMerged = new ArrayList<>();
        while (!actual.isEmpty()) {
            actualMerged.add(actual.pop());
        }
        assertEquals(expectedMerged, actualMerged);
    }

    @Test
    public void mergeWith_shouldReturnExpected_whenValuesOfHeapsAreRandom() {
        for (int j = 0; j < 10; j++) {
            // arrange
            final List<Integer> expectedMerged = new ArrayList<>();
            final AdjustableBinaryHeap<Integer> left = AdjustableBinaryHeap.createComparable();
            final AdjustableBinaryHeap<Integer> right = AdjustableBinaryHeap.createComparable();
            final Random r = new Random();
            final int maxSize = 1000;
            r.ints(maxSize, -10000, 10000)
                .distinct()
                .forEach(i -> {
                    if (i % 2 == 0) {
                        left.push(i);
                    } else {
                        right.push(i);
                    }
                    expectedMerged.add(i);
                });
            // act
            final AdjustableBinaryHeap<Integer> actual = left.mergeWith(right);
            // assert
            assertSame(left, actual);
            assertEquals(expectedMerged.size(), left.size());
            expectedMerged.sort(left.comparator());
            final List<Integer> actualMerged = new ArrayList<>();
            while (!actual.isEmpty()) {
                actualMerged.add(actual.pop());
            }
            assertEquals(expectedMerged, actualMerged);
        }
    }

    @Test
    public void mergeWith_shouldReturnExpected_whenValuesOfHeapsAreRandom_secondHeapHasReversedOrder() {
        for (int j = 0; j < 10; j++) {
            // arrange
            final List<Integer> expectedMerged = new ArrayList<>();
            final AdjustableBinaryHeap<Integer> left = AdjustableBinaryHeap.createComparable();
            final AdjustableBinaryHeap<Integer> right = new AdjustableBinaryHeap<>(((Comparator<Integer>) (Integer::compare)).reversed());
            final Random r = new Random();
            final int maxSize = 1000;
            r.ints(maxSize, -10000, 10000)
                .distinct()
                .forEach(i -> {
                    if (i % 2 == 0) {
                        left.push(i);
                    } else {
                        right.push(i);
                    }
                    expectedMerged.add(i);
                });
            // act
            final AdjustableBinaryHeap<Integer> actual = left.mergeWith(right);
            // assert
            assertSame(left, actual);
            assertEquals(expectedMerged.size(), left.size());
            expectedMerged.sort(left.comparator());
            final List<Integer> actualMerged = new ArrayList<>();
            while (!actual.isEmpty()) {
                actualMerged.add(actual.pop());
            }
            assertEquals(expectedMerged, actualMerged);
        }
    }

    @Test
    public void mergeWith_shouldReturnExpected_whenValuesOfHeapsAreRandom_secondHeapHasRandomOrder() {
        for (int j = 0; j < 10; j++) {
            // arrange
            final List<Integer> expectedMerged = new ArrayList<>();
            final AdjustableBinaryHeap<Integer> left = AdjustableBinaryHeap.createComparable();
            final AdjustableBinaryHeap<Integer> right =
                new AdjustableBinaryHeap<>(((Comparator<Integer>) (a, b) -> Math.random() * 1 == 1 ? a : b).reversed());
            final Random r = new Random();
            final int maxSize = 10;
            r.ints(maxSize, -10000, 10000)
                .distinct()
                .forEach(i -> {
                    if (i % 2 == 0) {
                        left.push(i);
                    } else {
                        right.push(i);
                    }
                    expectedMerged.add(i);
                });
            // act
            final AdjustableBinaryHeap<Integer> actual = left.mergeWith(right);
            // assert
            assertSame(left, actual);
            assertEquals(expectedMerged.size(), left.size());
            expectedMerged.sort(left.comparator());
            final List<Integer> actualMerged = new ArrayList<>();
            while (!actual.isEmpty()) {
                actualMerged.add(actual.pop());
            }
            assertEquals(expectedMerged, actualMerged);
        }
    }

    @Test(expected = NoSuchElementException.class)
    public void pop_shouldThrow_whenEmpty() {
        AdjustableBinaryHeap.createComparable().pop();
    }

    private static class Mutable {
        int m;

        Mutable(int m) {
            this.m = m;
        }

        @Override
        public String toString() {
            return "" + m;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Mutable mutable = (Mutable) o;
            return m == mutable.m;
        }

        @Override
        public int hashCode() {
            return m;
        }
    }
}
