package com.abstractkamen.datastructures.impl.heaps;

import com.abstractkamen.datastructures.api.heaps.Heap;
import com.abstractkamen.datastructures.api.heaps.MergeableHeap;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * The elements of the binary heap are ordered according to their natural ordering,
 * or by a Comparator provided at construction time, depending on which
 * constructor is used. This implementation does not permit null elements.
 *
 * @param <T> The type of elements stored in the binary heap.
 */
public class BinaryHeapImpl<T> implements Heap<T>, MergeableHeap<BinaryHeapImpl<T>> {
    private final Comparator<T> comparator;
    private final List<T> items = new ArrayList<>();
    private int size;

    /**
     * Create an {@code BinaryHeapImpl<T>} with a custom comparator
     * @param comparator custom comparator
     */
    public BinaryHeapImpl(Comparator<T> comparator) {
        this.comparator = comparator;
    }

    /**
     * Create an {@code BinaryHeapImpl<T>} with natural order comparator in a type safe way.
     */
    public static <T extends Comparable<T>> BinaryHeapImpl<T> createComparable() {
        final Comparator<T> c = Comparable::compareTo;
        return new BinaryHeapImpl<>(c);
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public int push(T item) {
        items.add(item);
        heapifyUp(size++);
        return size;
    }

    @Override
    public T peek() {
        return isEmpty() ? null : items.get(0);
    }

    @Override
    public T pop() {
        if (isEmpty()) {
            throw new NoSuchElementException();
        } else {
            final T result = items.get(0);
            final T lastItem = items.remove(items.size() - 1);
            --size;
            if (!items.isEmpty()) {
                items.set(0, lastItem);
                heapifyDown(0);
            }
            return result;
        }
    }

    @Override
    public String toString() {
        return items.toString();
    }

    @Override
    public Comparator<T> comparator() {
        return comparator;
    }

    @Override
    public BinaryHeapImpl<T> mergeWith(BinaryHeapImpl<T> other) {
        this.items.addAll(other.items);
        this.size += other.size;
        restoreHeapOrder();
        return this;
    }

    /**
     * Restores the minimum heap property of the binary heap after modifications to its elements.
     * This method should be called when some elements' keys are mutated to ensure that the
     * minimum heap property is maintained throughout the binary heap.
     * <p>
     * The method traverses the binary heap from the last parent node to the root, applying the
     * heapifyDown operation to each node. This process rearranges the elements to satisfy the
     * minimum heap property, considering the updated keys.
     * </p>
     * <p>
     * Note: This method assumes that the comparator used to create the binary heap is consistent
     * with the keys of the elements, and the elements are unique based on their keys.
     * </p>
     */
    public void restoreHeapOrder() {
        int i = (size >>> 1) - 1;
        for (; i >= 0; i--) {
            heapifyDown(i);
        }
    }

    private void heapifyDown(int i) {
        final int smallest = smallestChild(i);
        if (smallest != i) {
            swap(i, smallest);
            heapifyDown(smallest);
        }
    }

    private int smallestChild(int i) {
        final int left = leftChild(i);
        final int right = rightChild(i);
        int smallest = i;
        if (left < size && greaterThan(smallest, left)) {
            smallest = left;
        }
        if (right < size && greaterThan(smallest, right)) {
            smallest = right;
        }
        return smallest;
    }

    private void heapifyUp(int i) {
        if (i <= 0) return;
        final int parent = parent(i);
        if (parent >= 0 && greaterThan(parent, i)) {
            swap(i, parent);
            heapifyUp(parent);
        }
    }

    private int parent(int i) {
        return (i - 1) >> 1;
    }

    private int leftChild(int i) {
        return (i << 1) + 1;
    }

    private int rightChild(int i) {
        return (i << 1) + 2;
    }

    private void swap(int a, int b) {
        final T tempA = items.get(a);
        items.set(a, items.get(b));
        items.set(b, tempA);
    }

    private boolean greaterThan(int a, int b) {
        return comparator.compare(items.get(a), items.get(b)) > 0;
    }
}
