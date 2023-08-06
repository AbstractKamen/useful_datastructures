package com.abstractkamen.datastructures.impl.heaps;

import com.abstractkamen.datastructures.api.heaps.AdjustableHeap;
import com.abstractkamen.datastructures.api.heaps.Heap;
import com.abstractkamen.datastructures.api.heaps.MergeableHeap;

import java.util.*;

/**
 * An implementation of an adjustable binary heap data structure.
 *
 * <p>
 * This heap allows for efficient operations to insert elements, retrieve and remove the element
 * with the highest priority (minimum value based on the provided comparator), and adjust the priority
 * of existing elements. The elements stored in the heap must be comparable, and a custom comparator
 * can be provided during construction to define the ordering of the elements.
 * </p>
 *
 * <p>
 * The AdjustableHeap maintains a partial ordering, also known as a "weak ordering," among its elements.
 * Unlike traditional binary heaps, this heap does not require a total order relation among elements.
 * As a result, the element's relative priority may not be strictly determined with respect to other elements.
 * </p>
 *
 * <p>
 * The heap implementation supports constant time complexity (O(1)) for both the increaseKey and decreaseKey
 * operations. When an element's priority is increased or decreased, the heap ensures that the ordering
 * property is maintained with a single swap operation, without the need for full heapify operations.
 * </p>
 *
 * <p>
 * This heap implementation is not thread-safe, and it does not allow duplicate elements. The provided
 * comparator should be consistent with equals() and hashcode() otherwise unpredictable behavior may occur.
 * </p>
 *
 * @param <T> The type of elements stored in the heap, must be comparable.
 * @see Heap
 * @see MergeableHeap
 * @see AdjustableHeap
 */
public class AdjustableBinaryHeap<T> implements Heap<T>, MergeableHeap<T>, AdjustableHeap<T> {

    private final List<PriorityItem> items;
    private final Map<T, PriorityItem> priorityMap;
    private final Comparator<T> comparator;

    /**
     * Create an {@code AdjustableBinaryHeap<T>} with a custom comparator
     *
     * @param comparator custom comparator
     *
     */
    public AdjustableBinaryHeap(Comparator<T> comparator) {
        this.comparator = comparator;
        this.items = new ArrayList<>();
        this.priorityMap = new HashMap<>();
    }

    /**
     * Create an {@code AdjustableBinaryHeap<T>} with natural order comparator in a type safe way.
     * @param <T> comparable type
     */
    public static <T extends Comparable<T>> AdjustableBinaryHeap<T> createComparable() {
        final Comparator<T> c = Comparable::compareTo;
        return new AdjustableBinaryHeap<>(c);
    }

    @Override
    public int push(T item) {
        if (!priorityMap.containsKey(item)) {
            final PriorityItem priorityItem = new PriorityItem(item);
            priorityItem.setIndex(items.size());
            final int size = size();
            items.add(priorityItem);
            heapifyUp(size);
            priorityMap.put(item, priorityItem);
            return size + 1;
        } else {
            return -1;
        }
    }

    @Override
    public T peek() {
        return isEmpty() ? null : items.get(0).item;
    }

    @Override
    public T pop() {
        if (isEmpty()) {
            throw new NoSuchElementException();
        } else {
            final T result = items.get(0).item;
            final PriorityItem lastItem = items.remove(items.size() - 1);
            priorityMap.remove(result);
            if (!items.isEmpty()) {
                lastItem.setIndex(0);
                items.set(0, lastItem);
                final int size = size();
                heapifyDown(0, size);
            }
            return result;
        }
    }

    @Override
    public int size() {
        return items.size();
    }

    @Override
    public boolean isEmpty() {
        return items.isEmpty();
    }

    @Override
    public Comparator<T> comparator() {
        return comparator;
    }

    /**
     * Increases the priority of an existing element in the heap.
     *
     * <p>
     * If the element is not present in the heap or it is already at the highest priority,
     * the operation has no effect, and false is returned. Otherwise, the element's priority
     * is increased, and the heap property is maintained with a single swap operation.
     * </p>
     *
     * <p>
     * The increaseKey operation has a time complexity of O(1).
     * </p>
     *
     * @param item The element whose priority needs to be increased.
     * @return True if the priority was successfully increased, false otherwise.
     */
    @Override
    public boolean increaseKey(T item) {
        final PriorityItem priorityItem = priorityMap.get(item);
        int s;
        if (priorityItem == null || (s = size()) <= 1 || priorityItem.index == s) return false;
        final int originIndex = priorityItem.index;
        final int leftChild = leftChild(originIndex);
        // current item is a leaf, so we can't increase further
        if (leftChild >= s) return false;

        final int rightChild = leftChild + 1;
        if (rightChild < s) {
            final PriorityItem leftChildItem = items.get(leftChild);
            final PriorityItem rightChildItem = items.get(rightChild);
            if (greaterThan(leftChildItem, priorityItem)) {
                swapPriority(priorityItem, leftChildItem);
            } else {
                swapPriority(priorityItem, rightChildItem);
            }
        } else {
            swapPriority(priorityItem, items.get(leftChild));
        }
        // if the first element of the heap is increased we must make sure it's the minimum
        if (originIndex == 0 && greaterThan(0, 1)) {
            swap(0, 1);
        }
        return true;
    }

    /**
     * Decreases the priority of an existing element in the heap.
     *
     * <p>
     * If the element is not present in the heap or it is already at the lowest priority,
     * the operation has no effect, and false is returned. Otherwise, the element's priority
     * is decreased, and the heap property is maintained with a single swap operation.
     * </p>
     *
     * <p>
     * The decreaseKey operation has a time complexity of O(1).
     * </p>
     *
     * @param item The element whose priority needs to be decreased.
     * @return True if the priority was successfully decreased, false otherwise.
     */
    @Override
    public boolean decreaseKey(T item) {
        final PriorityItem priorityItem = priorityMap.get(item);
        if (priorityItem == null || priorityItem.index == 0) return false;
        final int parentIndex = parent(priorityItem.index);
        if (parentIndex < 0 || parentIndex == priorityItem.index) return false;

        final PriorityItem parent = items.get(parentIndex);
        final int leftChild = leftChild(parentIndex);
        final int rightChild = leftChild + 1;
        if (rightChild == priorityItem.index) {
            final PriorityItem leftChildItem = items.get(leftChild);
            if (greaterThan(priorityItem, leftChildItem)) {
                swapPriority(priorityItem, leftChildItem);
            } else {
                swapPriority(priorityItem, parent);
            }
        } else if (leftChild == priorityItem.index) {
            swapPriority(priorityItem, parent);
        }
        return true;
    }

    @Override
    public boolean containsKey(T item) {
        return priorityMap.containsKey(item);
    }

    @Override
    public AdjustableBinaryHeap<T> mergeWith(Heap<T> other) {
        if (!(other instanceof AdjustableBinaryHeap)) throw new ClassCastException("other must be an instance of AdjustableBinaryHeap");
        final AdjustableBinaryHeap<T> cast = (AdjustableBinaryHeap<T>) other;
        int i = this.items.size();
        for (PriorityItem otherItem : cast.items) {
            if (!this.priorityMap.containsKey(otherItem.item)) {
                // forget other's priorities
                final PriorityItem mergingItem = new PriorityItem(otherItem.item);
                mergingItem.setIndex(i++);
                this.priorityMap.put(otherItem.item, mergingItem);
                this.items.add(mergingItem);
            }
        }
        restoreHeapOrder();
        return this;
    }

    /**
     * Restores the minimum heap property of the binary heap after modifications to its elements.
     * This method should be called when some elements' keys are mutated to ensure that the
     * minimum heap property is maintained throughout the binary heap.
     *
     * <p>
     * The method traverses the binary heap from the last parent node to the root, applying the
     * heapifyDown operation to each node. This process rearranges the elements to satisfy the
     * minimum heap property, considering the updated keys.
     * </p>
     *
     * <p>
     * Note: The user must ensure that the comparator used to create the binary heap is consistent
     * with the keys of the elements, and the elements are unique based on their keys. Failure to
     * maintain these conditions may result in unpredictable behavior.
     * </p>
     */
    public void restoreHeapOrder() {
        final int size = priorityMap.size();
        int i = (size >>> 1) - 1;
        for (; i >= 0; i--) {
            heapifyDown(i, size);
        }
    }

    private void heapifyDown(int i, int size) {
        while (true) {
            final int smallest = smallestChild(i, size);
            if (smallest == i) {
                return;
            }
            swap(i, smallest);
            i = smallest;
        }
    }

    private int smallestChild(int i, int size) {
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
        while (i > 0) {
            final int parent = parent(i);
            if (greaterThan(parent, i)) {
                swap(i, parent);
                i = parent;
            } else {
                return;
            }
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
        final PriorityItem itemA = items.get(a);
        itemA.setIndex(b);
        final PriorityItem itemB = items.get(b);
        itemB.setIndex(a);
        items.set(a, itemB);
        items.set(b, itemA);
    }

    private void swapPriority(PriorityItem a, PriorityItem b) {
        final int i = a.index;
        a.setIndex(b.index);
        a.setPriority(b.item);
        b.setIndex(i);
        items.set(a.index, a);
        items.set(b.index, b);
    }

    private boolean greaterThan(int a, int b) {
        return items.get(a).compareTo(items.get(b)) > 0;
    }

    private boolean greaterThan(PriorityItem a, PriorityItem b) {
        return a.compareTo(b) > 0;
    }

    private class PriorityItem implements Comparable<PriorityItem> {

        private final T item;
        private T priority;
        private int index;

        private PriorityItem(T item) {
            this.item = item;
            this.priority = item;
        }

        @Override
        public int compareTo(PriorityItem o) {
            return comparator().compare(priority, o.priority);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            @SuppressWarnings("unchecked") final PriorityItem that = (PriorityItem) o;
            return Objects.equals(item, that.item);
        }

        @Override
        public int hashCode() {
            return 31 + (item != null ? item.hashCode() : 0);
        }

        void setIndex(int index) {
            this.index = index;
        }

        void setPriority(T priority) {
            this.priority = priority;
        }
    }
}
