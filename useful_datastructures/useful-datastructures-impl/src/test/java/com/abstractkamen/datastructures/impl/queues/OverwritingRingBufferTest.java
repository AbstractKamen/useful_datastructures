package com.abstractkamen.datastructures.impl.queues;

import org.junit.Before;
import org.junit.Test;

import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class OverwritingRingBufferTest {

    private OverwritingRingBuffer<Integer> toTest;

    @Before
    public void init() {
        toTest = new OverwritingRingBuffer<>(TEST_CAPACITY);
    }

    @Test
    public void constructor_shouldThrow_whenCapacityLesserThan_1() {
        try {
            new OverwritingRingBuffer<>(0);
            throw new AssertionError("expected exception");
        } catch (IllegalArgumentException e) {
            // expected exception is thrown
        }
        try {
            new OverwritingRingBuffer<>(-1);
            throw new AssertionError("expected exception");
        } catch (IllegalArgumentException e) {
            // expected exception is thrown
        }
    }

    @Test
    public void constructor_shouldNotThrow_whenCapacity_isPowerOf2() {
        int powerOfTwo = 1;
        for (int i = 0; i < 20; i++) {
            System.out.println(powerOfTwo);
            new OverwritingRingBuffer<>(powerOfTwo);
            powerOfTwo *= 2;
        }
    }

    @Test
    public void constructor_shouldThrow_whenCapacity_notPowerOf2() {
        class ThrowOnConstructAndAssert {
            void throwsWithCapacity(int cap) {
                try {
                    new OverwritingRingBuffer<>(cap);
                    throw new AssertionError("expected exception");
                } catch (IllegalArgumentException e) {
                    // expected exception is thrown
                }
            }
        }
        final ThrowOnConstructAndAssert anAssert = new ThrowOnConstructAndAssert();
        for (int i = 1; i < 100; i++) {
            if (Integer.bitCount(i) != 1) {
                anAssert.throwsWithCapacity(i);
            }
        }
    }

    @Test(expected = NoSuchElementException.class)
    public void dequeue_shouldThrowExpected_whenEmpty() {
        // arrange
        // act
        toTest.dequeue();
        // assert
    }

    @Test
    public void dequeue_shouldThrowExpected_whenFilledThenEmptied() {
        toTest.enqueue(1);
        toTest.enqueue(2);
        toTest.enqueue(3);

        assertEquals(1, (int) toTest.dequeue());
        assertEquals(2, (int) toTest.dequeue());
        assertEquals(3, (int) toTest.dequeue());

        // Buffer is empty, dequeue should throw NoSuchElementException
        try {
            toTest.dequeue();
            throw new AssertionError("Expected NoSuchElementException was not thrown");
        } catch (NoSuchElementException e) {
            // NoSuchElementException caught, test passed
        }
    }

    @Test
    public void spliterator_shouldBeOrderedAndSizedAndSubsized() {
        // arrange
        // act
        final Spliterator<Integer> actual = toTest.spliterator();
        // assert
        assertEquals(Spliterator.ORDERED | Spliterator.SIZED | Spliterator.SUBSIZED, actual.characteristics());
        assertEquals(toTest.size(), actual.estimateSize());
    }

    @Test
    public void spliterator_estimateSize_shouldReturnExpected_whenUnderCapacity() {
        // arrange
        toTest.enqueue(1);
        toTest.enqueue(1);
        toTest.enqueue(1);
        // act
        final Spliterator<Integer> actual = toTest.spliterator();
        // assert
        assertEquals(toTest.size(), actual.estimateSize());
        assertEquals(toTest.size(), 3);
    }

    @Test
    public void isEmpty_shouldReturnTrue_whenEmpty() {
        assertTrue(toTest.isEmpty());

        toTest.enqueue(1);
        toTest.enqueue(1);

        toTest.dequeue();
        toTest.dequeue();

        assertTrue(toTest.isEmpty());
    }

    @Test
    public void isEmpty_shouldReturnTrue_whenNotEmpty() {
        toTest.enqueue(1);
        assertFalse(toTest.isEmpty());
        toTest.dequeue();

        toTest.enqueue(1);
        assertFalse(toTest.isEmpty());
    }

    @Test
    public void spliterator_estimateSize_shouldReturnExpected_whenOverwritten() {
        // arrange
        toTest = new OverwritingRingBuffer<>(1);
        toTest.enqueue(1);
        toTest.enqueue(1);
        toTest.enqueue(1);
        // act
        final Spliterator<Integer> actual = toTest.spliterator();
        // assert
        assertEquals(toTest.size(), actual.estimateSize());
        assertEquals(toTest.size(), toTest.capacity());
        assertEquals(toTest.size(), 1);
    }

    @Test
    public void buffer_stream_count_shouldEqualBufferSize_whenEmpty() {
        // arrange
        // act
        final Stream<Integer> actual = toTest.stream();
        // assert
        assertEquals(toTest.size(), actual.count());
    }

    @Test
    public void buffer_stream_count_shouldEqualBufferSize_whenUnderCapacity() {
        // arrange
        toTest.enqueue(1);
        toTest.enqueue(1);
        toTest.enqueue(1);
        toTest.enqueue(1);
        // act
        final Stream<Integer> actual = toTest.stream();
        // assert
        assertEquals(toTest.size(), actual.count());
    }

    @Test
    public void buffer_stream_count_shouldEqualBufferSize_whenOverwritten() {
        // arrange
        for (int i = 0; i < toTest.capacity() * 2; i++) {
            toTest.enqueue(i);
        }
        // act
        final Stream<Integer> actual = toTest.stream();
        // assert
        assertEquals(toTest.size(), actual.count());
        assertEquals(toTest.size(), toTest.capacity());
    }

    @Test
    public void enqueueThenDeque_size_toString_capacity_shouldReturnExpected() {
        assertTrue(toTest.enqueue(1));
        assertEquals(Integer.valueOf(1), toTest.dequeue());
        assertEquals(0, toTest.size());
        assertEquals(TEST_CAPACITY, toTest.capacity());
        assertEquals("[]", toTest.toString());
    }

    @Test
    public void enqueue_size_toString_capacity_shouldReturnExpected_whenCapacityIsOne() {
        toTest = new OverwritingRingBuffer<>(1);
        for (int i = 0; i < 100; i++) {
            toTest.enqueue(i);
        }
        assertTrue(toTest.enqueue(1));
        assertEquals(1, toTest.size());
        assertEquals(1, toTest.capacity());
        assertEquals("[1]", toTest.toString());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void buffer_iterator_remove_shouldThrowUnsupportedException() {
        toTest.iterator().remove();
    }

    @Test(expected = NoSuchElementException.class)
    public void buffer_iterator_next_shouldThrow_WhenEmpty() {
        toTest.iterator().next();
    }

    @Test
    public void emptyBuffer_size_toString_shouldReturn() {
        // arrange
        // act
        // assert
        assertEquals(0, toTest.size());
        assertEquals("[]", toTest.toString());
    }

    @Test
    public void enqueue_iterate_toString_shouldReturnExpected_whenNotOverwritten() {
        // arrange
        for (int i = 0; i < toTest.capacity(); i++) {
            toTest.enqueue(i);
        }
        // act
        final String actual = toTest.toString();
        // assert
        final String expected = IntStream.range(0, toTest.capacity())
            .mapToObj(String::valueOf)
            .collect(Collectors.joining(", ", "[", "]"));
        assertEquals(expected, actual);
    }

    @Test
    public void enqueue_iterate_toString_shouldReturnExpected_whenOverwritten() {
        // arrange
        for (int i = 0; i < toTest.capacity(); i++) {
            toTest.enqueue(i);
        }
        for (int i = 0; i < 4; i++) {
            toTest.enqueue(i + toTest.capacity());
        }
        // act
        final String actual = toTest.toString();
        // assert
        final String expected = IntStream.range(4, toTest.capacity() + 4)
            .mapToObj(String::valueOf)
            .collect(Collectors.joining(", ", "[", "]"));
        assertEquals(expected, actual);
    }

    private static final int TEST_CAPACITY = 16;
}
