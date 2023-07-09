package com.abstractkamen.datastructures.api.trees.search;

import java.util.Collection;

/**
 * An extension of the prefix trie. This structure can perform fast suffix matches at the cost of a structure double the size of a
 * PrefixTrie.
 */
public interface SuffixTrie extends PrefixTrie {

    /**
     * Try to find strings which end with suffix. Matching strings are always in arbitrary order.
     *
     * @param suffix to look for
     * @param limit maximum number of found strings
     * @return collection of words with the same suffix
     */
    Collection<String> endsWith(String suffix, int limit);

    /**
     * Checks if suffix is present in this trie.
     *
     * @param suffix to check
     * @return true if suffix exists
     */
    boolean isSuffix(String suffix);
}
