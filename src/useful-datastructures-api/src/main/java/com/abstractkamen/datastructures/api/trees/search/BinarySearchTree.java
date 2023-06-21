package com.abstractkamen.datastructures.api.trees.search;

import java.util.Iterator;
import java.util.stream.Stream;

public interface BinarySearchTree<T> extends Iterable<T> {
    boolean isEmpty();

    int size();

    void add(T item);

    void remove(T item);

    boolean contains(T item);

    int containsCount(T item);

    int height();

    T min();

    T max();

    String prettyString();

    Stream<T> stream();

    void clear();

    Iterator<T> descendingIterator();
}
