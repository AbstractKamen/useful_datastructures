package com.abstractkamen.datastructures.api.heaps;

import java.util.Comparator;
import java.util.NoSuchElementException;

/**
 * A heap is a data structure which satisfies the heap property. Depending on a compare operation by using either
 * {@link java.util.Comparator} or {@link java.lang.Comparable} the best(minimum) element will always be at the root of the heap
 * and available to be queried in O(1) constant time.
 *
 * @param <T> The type of elements stored in the heap.
 */
public interface Heap<T> {

    /**
     * Inserts an element into the heap and ensures that the
     * minimum heap property is maintained.
     *
     * @param item The element to be inserted.
     * @return The new size of the heap after insertion.
     */
    int push(T item);

    /**
     * Retrieves the best element of the heap without removing it.
     *
     * @return The best element of the heap, or {@code null}
     *         if the heap is empty.
     */
    T peek();

    /**
     * Removes and retrieves the best element of the heap,
     * and ensures that the heap property is maintained after removal.
     *
     * @return The best element of the heap.
     * @throws NoSuchElementException if the heap is empty.
     */
    T pop();

    /**
     * Gets the number of elements currently stored in the heap.
     *
     * @return The size of the heap.
     */
    int size();

    /**
     * Checks if the heap is empty.
     *
     * @return {@code true} if the heap is empty, {@code false} otherwise.
     */
    boolean isEmpty();

    /**
     * Get the comparator used to order items in this heap.
     *
     * @return The comparator used to order items in this heap. Never null.
     */
    Comparator<T> comparator();

}
