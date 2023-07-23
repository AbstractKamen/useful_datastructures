package com.abstractkamen.datastructures.api.heaps;

/**
 * A heap data structure function allowing the merging of two heaps of the same type into one.
 * @param <T> type of the heap
 */
public interface MergeableHeap<T extends Heap<?>> {
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
     */
    T mergeWith(T other);
}
