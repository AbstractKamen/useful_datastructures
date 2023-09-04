package com.abstractkamen.datastructures.impl.heaps;

import com.abstractkamen.datastructures.api.heaps.*;

import java.util.*;

/**
 * The elements of the binary heap are ordered according to their natural ordering,
 * or by a Comparator provided at construction time, depending on which
 * constructor is used. This implementation does not permit null elements.
 *
 * @param <T> The type of elements stored in the binary heap.
 */
public class BinaryHeap<T> implements Heap<T>, MergeableHeap<T> {
    protected static final int DEFAULT_CAPACITY = 16;
    private final Comparator<T> comparator;
    private Object[] items;
    private int size;

    /**
     * Create an {@code BinaryHeap<T>} with a custom comparator and capacity.
     *
     * @param comparator custom comparator
     * @param capacity   initial capacity
     */
    public BinaryHeap(Comparator<T> comparator, int capacity) {
        this.comparator = comparator;
        this.items = new Object[capacity];
    }

    /**
     * Create an {@code BinaryHeap<T>} with a custom comparator.
     *
     * @param comparator custom comparator
     */
    public BinaryHeap(Comparator<T> comparator) {
        this(comparator, DEFAULT_CAPACITY);
    }

    /**
     * Create an {@code BinaryHeap<T>} with natural order comparator in a type safe way.
     *
     * @param <T> comparable type
     */
    public static <T extends Comparable<T>> BinaryHeap<T> createComparable() {
        final Comparator<T> c = Comparable::compareTo;
        return new BinaryHeap<>(c);
    }

    /**
     * Create an {@code BinaryHeap<T>} with natural order comparator in a type safe way.
     *
     * @param capacity initial capacity
     * @param <T>      comparable type
     */
    public static <T extends Comparable<T>> BinaryHeap<T> createComparable(int capacity) {
        final Comparator<T> c = Comparable::compareTo;
        return new BinaryHeap<>(c, capacity);
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
        insert(item);
        return size;
    }

    protected int insert(T item) {
        final int i = size;
        if (items.length <= i + 1) {
            items = Arrays.copyOf(items, i << 1);
        }
        items[i] = item;
        final int index = heapifyUp(items, comparator, i);
        ++size;
        return index;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T peek() {
        return isEmpty() ? null : (T) items[0];
    }

    @Override
    @SuppressWarnings("unchecked")
    public T pop() {
        if (size == 0) {
            throw new NoSuchElementException();
        } else {
            final T result = (T) items[0];
            --size;
            if (size > 0) {
                items[0] = items[size];
                heapifyDown(0, size, comparator, items);
            }
            return result;
        }
    }

    @Override
    public String toString() {
        return "nope";
    }

    @Override
    public Comparator<T> comparator() {
        return comparator;
    }

    @Override
    public BinaryHeap<T> mergeWith(Heap<T> other) {
        if (!(other instanceof BinaryHeap)) throw new ClassCastException("other must be an instance of BinaryHeap");
        final BinaryHeap<T> cast = (BinaryHeap<T>) other;
        int prevSize = this.size;
        this.size += cast.size;
        this.items = Arrays.copyOf(items, size);
        System.arraycopy(cast.items, 0, items, prevSize, cast.size);
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
            heapifyDown(i, size, comparator, items);
        }
    }

    protected Object[] getItems() {
        return items;
    }

    protected static <T> void heapifyDown(int i, int size, Comparator<T> comparator, Object[] items) {
        int half = size >>> 1;
        while (i < half) {
            final int smallest = smallestChild(i, size, comparator, items);
            if (smallest == i) {
                return;
            }
            swap(items, i, smallest);
            i = smallest;
        }
    }

    private static <T> int smallestChild(int i, int size, Comparator<T> comparator, Object[] items) {
        final int left = (i << 1) + 1;
        final int right = left + 1;
        int smallest = i;
        if (left < size && greaterThanOrEqual(smallest, left, comparator, items)) {
            smallest = left;
        }
        if (right < size && greaterThanOrEqual(smallest, right, comparator, items)) {
            smallest = right;
        }
        return smallest;
    }

    protected static <T> int heapifyUp(Object[] items, Comparator<T> comparator, int i) {
        while (i > 0) {
            final int parent = (i - 1) >>> 1;
            if (greaterThanOrEqual(parent, i, comparator, items)) {
                swap(items, i, parent);
                i = parent;
            } else {
                return i;
            }
        }
        return i;
    }

    private static void swap(Object[] items, int a, int b) {
        final Object tempA = items[a];
        items[a] = items[b];
        items[b] = tempA;
    }

    @SuppressWarnings("unchecked")
    private static <T> boolean greaterThanOrEqual(int a, int b, Comparator<T> c, Object[] items) {
        return c.compare((T) items[a], (T) items[b]) >= 0;
    }
}
