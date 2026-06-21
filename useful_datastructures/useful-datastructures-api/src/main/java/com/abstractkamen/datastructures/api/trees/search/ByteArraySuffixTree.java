package com.abstractkamen.datastructures.api.trees.search;

import java.util.Collection;

/**
 * Simple generic key value suffix tree contract. Keys are byte[] values are generic and their insertion depends on the implementation.
 *
 * @param <T> type of value
 */
public interface ByteArraySuffixTree<T> {

  /**
   * Returns the values for each byte[] matching the provided pattern.
   *
   * @param pattern bits
   * @return list of values
   */
  Collection<T> findAllOccurrences(byte[] pattern);

  /**
   * True if the provided byte[] is present in the suffix tree.
   *
   * @param pattern string
   * @return true if present else false
   */
  boolean contains(byte[] pattern);

  /**
   * The total size of the byte[] held by this tree.
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