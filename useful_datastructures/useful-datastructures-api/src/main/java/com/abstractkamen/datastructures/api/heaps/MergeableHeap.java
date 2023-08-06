package com.abstractkamen.datastructures.api.heaps;

/**
 * A mergeable heap data structure is a heap that allows for efficient merging of two heaps
 * into a single heap. Elements stored in the heap must be comparable, and the provided
 * comparator defines the ordering of the elements.
 * </p>
 *
 * <p>
 * The merge operation combines both heaps into a new heap with all elements from
 * both heaps, maintaining the heap property defined by the comparator of the heap
 * invoking {@link #mergeWith(Heap)}.
 * </p>
 *
 * @param <T> The type of elements stored in the heap.
 * @see Heap
 * @see AdjustableHeap
 */
public interface MergeableHeap<T> {
    /**
     * Merges this heap with the other using {@code this.}{@link Heap#comparator()}. The heaps will be merged regardless of {@code other}
     * 's order.
     * Depending on the implementation this may have an impact on performance.
     *
     * <pre>
     * Example:
     *     {@code MergeableHeap<Heap<Integer>> a = new SomeMergeableHeapImpl(Integer::compare);}
     *     // can be merged with
     *     {@code MergeableHeap<Heap<Integer>> b = new SomeMergeableHeapImpl(Comparator.reverseOrder(Integer::compare));}
     *     // different implementations will have different time complexity
     *     a.mergeWith(b);
     * </pre>
     *
     * @param other heap
     * @return {@code this} merged with other
     * @throws ClassCastException if {@code other} not the same instance as {@code this}
     */
    Heap<T> mergeWith(Heap<T> other);
}
