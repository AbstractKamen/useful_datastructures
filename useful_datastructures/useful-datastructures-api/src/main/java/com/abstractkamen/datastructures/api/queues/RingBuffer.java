package com.abstractkamen.datastructures.api.queues;

import java.util.stream.Stream;

/**
 * A ring buffer abstraction
 *
 * @param <T> the type of elements stored in the buffer.
 */
public interface RingBuffer<T> extends Iterable<T> {
    /**
     * True if buffer is empty.
     *
     * @return true if buffer is empty
     */
    boolean isEmpty();

    /**
     * Returns the number of elements currently stored in the buffer.
     *
     * @return the number of elements in the buffer.
     */
    int size();

    /**
     * Returns the total capacity of the buffer.
     *
     * @return the capacity of the buffer.
     */
    int capacity();

    /**
     * Enqueues the specified item into the buffer.
     *
     * @param item the item to be enqueued.
     * @return {@code true} if item is successfully enqueued.
     */
    boolean enqueue(T item);

    /**
     * Dequeues and returns the oldest item from the buffer.
     *
     * @return the oldest item in the buffer.
     */
    T dequeue();

    /**
     * Returns a sequential {@link java.util.stream.Stream} of items in the buffer.
     *
     * @return a sequential stream of items in the buffer.
     */
    Stream<T> stream();
}
