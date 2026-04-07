package com.abstractkamen.datastructures.api.trees.search;

import java.util.Collection;

/**
 * Simple generic key value suffix tree contract. Keys are strings values are generic and their insertion depends on the implementation.
 *
 * @param <T> type of value
 */
public interface SuffixTree<T> {

    /**
     * Returns the values for each string matching the provided pattern.
     *
     * @param pattern string
     * @return list of values
     */
    Collection<T> findAllOccurrences(String pattern);

    /**
     * True if the provided substring is present in the suffix tree.
     *
     * @param pattern string
     * @return true if present else false
     */
    boolean contains(String pattern);

    /**
     * The total size of the text held by this tree.
     *
     * @return text size
     */
    int textSize();

    /**
     * The total nodes of this tree
     *
     * @return node count
     */
    int nodesCount();

    /**
     * Total values inserted with their string keys into the tree.
     *
     * @return value count
     */
    int valuesCount();
}