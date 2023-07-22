package com.abstractkamen.datastructures.impl.heaps;

import org.junit.Test;

import java.util.*;
import java.util.stream.IntStream;

import static org.junit.Assert.*;

public class BinaryHeapTest {

    @Test
    public void push_pop_size_randomTest() {
        for (int j = 0; j < 100; j++) {
            // arrange
            final BinaryHeapImpl<Integer> binaryHeap = new BinaryHeapImpl<>();
            // act
            final int maxSize = 1000;
            new Random().ints(-10000, 10000).distinct().limit(maxSize).forEach(binaryHeap::push);
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
    public void push_pop_size_duplicateTest() {
            // arrange
            final BinaryHeapImpl<Integer> binaryHeap = new BinaryHeapImpl<>();
            // act
            final int maxSize = 1000;
            IntStream.generate(()-> 0).limit(maxSize).forEach(binaryHeap::push);
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
        final BinaryHeapImpl<Integer> binaryHeap = new BinaryHeapImpl<>();
        for (int i = 0; i < 10; i++) {
            final int expectedSize = i + 1;
            assertEquals(expectedSize, binaryHeap.push((int) (Math.random() * i * 1000 - 1500)));
        }
    }

    @Test
    public void peekTest() {
        final BinaryHeapImpl<Integer> binaryHeap = new BinaryHeapImpl<>();
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

    @Test(expected = NoSuchElementException.class)
    public void pop_shouldThrow_whenEmpty() {
        new BinaryHeapImpl<>().pop();
    }

    @Test(expected = ClassCastException.class)
    public void push_ShouldThrow_whenNotComparable() {
        new BinaryHeapImpl<>().push(new Object());
    }
}
