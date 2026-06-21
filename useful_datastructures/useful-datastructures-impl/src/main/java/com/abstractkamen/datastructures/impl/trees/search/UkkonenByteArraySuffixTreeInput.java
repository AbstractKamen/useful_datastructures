package com.abstractkamen.datastructures.impl.trees.search;

/**
 * A pair of byte[] and T value which will be included in {@link GenericUkkonenByteArraySuffixTree}
 *
 * @param key   a byte[]
 * @param value the value for this pair
 * @param <T>   type of value
 */
public record UkkonenByteArraySuffixTreeInput<T>(byte[] key, T value) {
}