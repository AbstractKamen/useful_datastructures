package com.abstractkamen.datastructures.api.heaps;

/**
 * An interface representing an adjustable binary heap data structure.
 *
 * <p>
 * An adjustable heap is a binary heap that allows for efficient adjustments of element
 * priorities.
 * </p>
 *
 * <p>
 * This interface provides methods to increase or decrease the priority of existing elements
 * in the heap while maintaining the heap property defined by the comparator.
 * </p>
 *
 * <p>
 * Depending on the implementation the adjustable heap maintains a partial ordering, also known as a "weak ordering," among
 * its elements. Unlike traditional binary heaps, this heap does not require a total order
 * relation among elements. As a result, the element's relative priority may not be strictly
 * determined with respect to other elements.
 * </p>
 *
 * <p>
 * The adjustable heap implementation is not thread-safe, and it does not allow duplicate elements.
 * The provided comparator should be consistent with equals() and hashcode() to ensure correct behavior.
 * </p>
 *
 * @param <T> The type of elements stored in the heap.
 * @see Heap
 * @see MergeableHeap
 */
public interface AdjustableHeap<T> {
    /**
     * Try to increase {@code item} if it's present in this heap moving it further away from the root. Increasing will always happen
     * relative to other values in the heap.
     *
     * @param item to be increased
     * @return true if item is present and successfully decreased
     */
    boolean increaseKey(T item);

    /**
     * Try to decrease {@code item} if it's present in this heap moving it closer to the root. Decreasing will always happen
     * relative to other values in the heap.
     *
     * @param item to be decreased
     * @return true if item is present and successfully decreased
     */
    boolean decreaseKey(T item);

    /**
     * Check if the item is present in the heap.
     *
     * @param item to check
     * @return true if item is present in this heap
     */
    boolean containsKey(T item);
}
