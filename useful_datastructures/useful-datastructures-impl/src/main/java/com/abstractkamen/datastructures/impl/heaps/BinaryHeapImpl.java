package com.abstractkamen.datastructures.impl.heaps;

import com.abstractkamen.datastructures.api.heaps.BinaryHeap;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;

public class BinaryHeapImpl<T> implements BinaryHeap<T> {
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
     * Create an {@code BinaryHeapImpl<T>} with natural order comparator. {@code T} is expected to be {@code instanceof Comparable<T>}
     */
    @SuppressWarnings("unchecked")
    public BinaryHeapImpl() {
        this(Comparator.comparing(t -> ((Comparable<Object>) t)));
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
        failFastCheckComparable(item);
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

    boolean greaterThan(int a, int b) {
        return comparator.compare(items.get(a), items.get(b)) > 0;
    }

    private void failFastCheckComparable(T item) {
        comparator.compare(item, item);
    }
}
