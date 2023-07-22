package com.abstractkamen.datastructures.api.heaps;

import java.util.NoSuchElementException;

/**
 * A binary heap implementation that represents a binary tree
 * with the minimum heap property. The smallest element will
 * always be at the root of the heap.
 *
 * @param <T> The type of elements stored in the binary heap.
 */
public interface BinaryHeap<T> {

    /**
     * Inserts an element into the binary heap and ensures that the
     * minimum heap property is maintained.
     *
     * @param item The element to be inserted.
     * @return The new size of the binary heap after insertion.
     */
    int push(T item);

    /**
     * Retrieves the smallest element of the binary heap without removing it.
     *
     * @return The smallest element of the binary heap, or {@code null}
     *         if the heap is empty.
     */
    T peek();

    /**
     * Removes and retrieves the smallest element of the binary heap,
     * and ensures that the minimum heap property is maintained after removal.
     *
     * @return The smallest element of the binary heap.
     * @throws NoSuchElementException if the heap is empty.
     */
    T pop();

    /**
     * Gets the number of elements currently stored in the binary heap.
     *
     * @return The size of the binary heap.
     */
    int size();

    /**
     * Checks if the binary heap is empty.
     *
     * @return {@code true} if the binary heap is empty, {@code false} otherwise.
     */
    boolean isEmpty();

}
