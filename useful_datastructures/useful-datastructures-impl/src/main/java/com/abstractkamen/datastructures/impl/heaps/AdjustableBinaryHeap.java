package com.abstractkamen.datastructures.impl.heaps;

import com.abstractkamen.datastructures.api.heaps.*;

import java.util.Comparator;

/**
 * An implementation of an adjustable binary heap data structure.
 *
 * @param <T> The type of elements stored in the heap, must be comparable.
 * @see Heap
 * @see MergeableHeap
 * @see AdjustableHeap
 */
public class AdjustableBinaryHeap<T> extends BinaryHeap<T> implements Heap<T>, MergeableHeap<T>, AdjustableHeap<T> {

    /**
     * Create an {@code AdjustableBinaryHeap<T>} with a custom comparator
     *
     * @param comparator custom comparator
     */
    public AdjustableBinaryHeap(Comparator<T> comparator) {
        super(comparator, DEFAULT_CAPACITY);
    }

    /**
     * Create an {@code AdjustableBinaryHeap<T>} with a custom comparator
     *
     * @param capacity   initial capacity
     * @param comparator custom comparator
     */
    public AdjustableBinaryHeap(Comparator<T> comparator, int capacity) {
        super(comparator, capacity);
    }

    /**
     * Create an {@code AdjustableBinaryHeap<T>} with natural order comparator in a type safe way.
     *
     * @param <T> comparable type
     */
    public static <T extends Comparable<T>> AdjustableBinaryHeap<T> createComparable() {
        final Comparator<T> c = Comparable::compareTo;
        return new AdjustableBinaryHeap<>(c);
    }

    /**
     * Create an {@code AdjustableBinaryHeap<T>} with natural order comparator in a type safe way.
     *
     * @param capacity initial capacity
     * @param <T>      comparable type
     */
    public static <T extends Comparable<T>> AdjustableBinaryHeap<T> createComparable(int capacity) {
        final Comparator<T> c = Comparable::compareTo;
        return new AdjustableBinaryHeap<>(c);
    }

    @Override
    public boolean increaseKey(T item, T increasedItem) {
        final int compare = comparator().compare(item, increasedItem);
        if (compare > 0) return false;
        final Object[] items = getItems();
        int i = indexOf(items, size(), item);
        if (i > -1) {
            if (compare == 0) return false;
            items[i] = increasedItem;
            heapifyDown(items, comparator(), i, size());
        } else {
            push(increasedItem);
        }
        return true;
    }

    @Override
    public boolean decreaseKey(T item, T decreasedItem) {
        final int compare = comparator().compare(item, decreasedItem);
        if (compare < 0) return false;
        final Object[] items = getItems();
        int i = indexOf(items, size(), item);
        if (i > -1) {
            if (compare == 0) return false;
            items[i] = decreasedItem;
            heapifyUp(items, comparator(), i);
        } else {
            push(decreasedItem);
        }
        return true;
    }

    @Override
    public AdjustableBinaryHeap<T> mergeWith(Heap<T> other) {
        return (AdjustableBinaryHeap<T>) super.mergeWith(other);
    }

    private static int indexOf(Object[] items, int size, Object item) {
        int i = -1;
        for (int j = 0; j < size; j++) {
            if (item == items[j]) {
                i = j;
                break;
            }
        }
        return i;
    }
}
