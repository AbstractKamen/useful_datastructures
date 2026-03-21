package com.abstractkamen.datastructures.impl.trees.search;

/**
 * A pair of String and T value which will be included in {@link GenericUkkonenSuffixTree}
 *
 * @param key   a string
 * @param value the value for this pair
 * @param <T>   type of value
 */
public record UkkonenSuffixTreeInput<T>(String key, T value) {
}