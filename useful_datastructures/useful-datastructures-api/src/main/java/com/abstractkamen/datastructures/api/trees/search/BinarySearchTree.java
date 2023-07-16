package com.abstractkamen.datastructures.api.trees.search;

import java.util.Iterator;
import java.util.stream.Stream;

/**
 * Basic binary search tree collection which permits duplicates.
 *
 * @param <T> type of elements
 */
public interface BinarySearchTree<T> extends Iterable<T> {

    /**
     * Returns true if tree has no elements.
     *
     * @return true if tree has no elements.
     */
    boolean isEmpty();

    /**
     * Get the current number of elements in the tree.
     *
     * @return current number of elements
     */
    int size();

    /**
     * Adds an item to the tree.
     *
     * @param item to be added
     * @throws ClassCastException if item cannot be compared
     */
    void add(T item);

    /**
     * Removes an item to from the tree.
     *
     * @param item to be removed
     * @throws ClassCastException if item cannot be compared
     */
    void remove(T item);

    /**
     * Check if an item exists in the tree.
     *
     * @param item to check
     * @return true if item exists in the tree
     * @throws ClassCastException if item cannot be compared
     */
    boolean contains(T item);

    /**
     * Check how many items are equal to {@code item} there are in the tree.
     *
     * @param item to check
     * @return number of items equal to {@code item}
     * @throws ClassCastException if item cannot be compared
     */
    int containsCount(T item);

    /**
     * Get the height of this tree.
     *
     * @return the current height of this tree
     */
    int height();

    /**
     * Get the first item greater than {@code item}.
     *
     * @param item to check
     * @return found greater item or null if no item is greater
     * @throws ClassCastException if item cannot be compared
     */
    T greater(T item);

    /**
     * Get the first item lesser than {@code item}.
     *
     * @param item to check
     * @return found lesser item or null if no item is lesser
     * @throws ClassCastException if item cannot be compared
     */
    T lesser(T item);

    /**
     * Get the minimum item.
     *
     * @return minimum item
     */
    T min();

    /**
     * Get the maximum item.
     *
     * @return maximum item
     */
    T max();

    /**
     * Get a detailed representation of the structure of this tree.
     *
     * @return detailed pretty string
     */
    String prettyString();

    /**
     * Get a sequential stream over elements in this tree.
     *
     * @return a sequential stream over elements in this tree
     */
    Stream<T> stream();

    /**
     * Remove all elements from this tree.
     */
    void clear();

    /**
     * Get an iterator over the elements in this tree, in descending order.
     *
     * @return descending order iterator over the elements in this tree
     */
    Iterator<T> descendingIterator();
}
