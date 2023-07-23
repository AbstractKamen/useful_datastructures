package com.abstractkamen.datastructures.impl.heaps;

import org.junit.Test;

import java.util.*;
import java.util.stream.IntStream;

import static org.junit.Assert.*;

public class BinaryHeapTest {

    @Test
    public void push_pop_size_randomTest() {
        for (int j = 0; j < 10; j++) {
            // arrange
            final BinaryHeapImpl<Integer> binaryHeap = BinaryHeapImpl.createComparable();
            // act
            final int maxSize = 1000;
            new Random().ints(maxSize, -10000, 10000).forEach(binaryHeap::push);
            assertEquals(maxSize, binaryHeap.size());
            // assert
            final List<Integer> expected = new ArrayList<>(binaryHeap.size());
            final List<Integer> actual = new ArrayList<>(binaryHeap.size());
            while (!binaryHeap.isEmpty()) {
                final int popped = binaryHeap.pop();
                actual.add(popped);
                expected.add(popped);
            }
            expected.sort(Integer::compare);
            assertEquals(expected, actual);
        }
    }

    @Test
    public void restoreHeapOrder_randomTest() {
        for (int j = 0; j < 10; j++) {
            final int maxSize = 1000;
            final Comparator<Mutable> comparator = Comparator.comparing(m -> m.m);
            final BinaryHeapImpl<Mutable> heap = new BinaryHeapImpl<>(comparator);
            final List<Mutable> mutablesSortedList = new ArrayList<>();
            final Random r = new Random();
            r.ints(maxSize, -10000, 10000)
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
        final BinaryHeapImpl<Mutable> heap = new BinaryHeapImpl<>(Comparator.comparing(m -> m.m));
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
        final BinaryHeapImpl<Integer> binaryHeap = BinaryHeapImpl.createComparable();
        // act
        final int maxSize = 1000;
        IntStream.generate(() -> 0).limit(maxSize).forEach(binaryHeap::push);
        assertEquals(maxSize, binaryHeap.size());
        // assert
        final List<Integer> expected = new ArrayList<>(binaryHeap.size());
        final List<Integer> actual = new ArrayList<>(binaryHeap.size());
        while (!binaryHeap.isEmpty()) {
            final int popped = binaryHeap.pop();
            actual.add(popped);
            expected.add(popped);
        }
        expected.sort(Integer::compare);
        assertEquals(expected, actual);
    }

    @Test
    public void push_sizeTest() {
        final BinaryHeapImpl<Integer> binaryHeap = BinaryHeapImpl.createComparable();
        for (int i = 0; i < 10; i++) {
            final int expectedSize = i + 1;
            assertEquals(expectedSize, binaryHeap.push((int) (Math.random() * i * 1000 - 1500)));
        }
    }

    @Test
    public void peekTest() {
        final BinaryHeapImpl<Integer> binaryHeap = BinaryHeapImpl.createComparable();
        // when empty
        final Integer actual = binaryHeap.peek();
        assertNull(actual);
        // when size == 1
        binaryHeap.push(0);
        assertEquals(0, binaryHeap.peek().intValue());
        // when size 2
        binaryHeap.push(1);
        assertEquals(0, binaryHeap.peek().intValue());
        // when size 10
        IntStream.range(2, 10).forEach(binaryHeap::push);
        assertEquals(0, binaryHeap.peek().intValue());
        // when we add a new min value
        binaryHeap.push(-1);
        assertEquals(-1, binaryHeap.peek().intValue());
    }

    @Test
    public void mergeWith_shouldReturnExpected_whenValuesOfHeapsAreTheSame() {
        // arrange
        final List<Integer> expectedMerged = new ArrayList<>();
        final BinaryHeapImpl<Integer> left = BinaryHeapImpl.createComparable();
        final BinaryHeapImpl<Integer> right = BinaryHeapImpl.createComparable();
        for (int i = 0; i < 10; i++) {
            expectedMerged.add(i);
            expectedMerged.add(i);
            left.push(i);
            right.push(i);
        }
        // act
        final BinaryHeapImpl<Integer> actual = left.mergeWith(right);
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
            final BinaryHeapImpl<Integer> left = BinaryHeapImpl.createComparable();
            final BinaryHeapImpl<Integer> right = BinaryHeapImpl.createComparable();
            final Random r = new Random();
            final int maxSize = 1000;
            r.ints(maxSize, -10000, 10000)
                .forEach(i -> {
                    left.push(i);
                    expectedMerged.add(i);
                });
            r.ints(maxSize, -10000, 10000)
                .forEach(i -> {
                    right.push(i);
                    expectedMerged.add(i);
                });
            // act
            final BinaryHeapImpl<Integer> actual = left.mergeWith(right);
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
            final BinaryHeapImpl<Integer> left = BinaryHeapImpl.createComparable();
            final BinaryHeapImpl<Integer> right = new BinaryHeapImpl<>(((Comparator<Integer>) (Integer::compare)).reversed());
            final Random r = new Random();
            final int maxSize = 1000;
            r.ints(maxSize, -10000, 10000)
                .forEach(i -> {
                    left.push(i);
                    expectedMerged.add(i);
                });
            r.ints(maxSize, -10000, 10000)
                .forEach(i -> {
                    right.push(i);
                    expectedMerged.add(i);
                });
            // act
            final BinaryHeapImpl<Integer> actual = left.mergeWith(right);
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
            final BinaryHeapImpl<Integer> left = BinaryHeapImpl.createComparable();
            final BinaryHeapImpl<Integer> right =
                new BinaryHeapImpl<>(((Comparator<Integer>) (a, b) -> Math.random() * 1 == 1 ? a : b).reversed());
            final Random r = new Random();
            final int maxSize = 1000;
            r.ints(maxSize, -10000, 10000)
                .forEach(i -> {
                    left.push(i);
                    expectedMerged.add(i);
                });
            r.ints(maxSize, -10000, 10000)
                .forEach(i -> {
                    right.push(i);
                    expectedMerged.add(i);
                });
            // act
            final BinaryHeapImpl<Integer> actual = left.mergeWith(right);
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
        BinaryHeapImpl.createComparable().pop();
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
