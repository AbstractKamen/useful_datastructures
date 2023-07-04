package com.abstractkamen.datastructures.api.trees.search;

import java.util.Collection;

/**
 * The PrefixTrie can perform fast prefix matching. By traversing the trie based on the characters in
 * the prefix being searched, it is possible to find all strings in the Trie that have the given prefix.
 */
public interface PrefixTrie {

    /**
     * Try to find strings which start with prefix. Matching strings are always in lexicographical order.
     *
     * @param prefix to look for
     * @param limit maximum number of found strings
     * @return collection of words with the same prefix
     */
    Collection<String> startsWith(String prefix, int limit);

    /**
     * Checks if a word exists in this trie.
     *
     * @param string to check
     * @return true if word is in this trie
     */
    boolean contains(String string);

    /**
     * Checks if prefix is present in this trie.
     *
     * @param prefix to check
     * @return true if prefix exists
     */
    boolean isPrefix(String prefix);

    /**
     * Inserts a word into the trie. Duplicate words will be ignored.
     *
     * @param string word to be inserted
     * @return true if the word was inserted false if it already existed
     */
    boolean insert(String string);

    /**
     * Deletes a word from the trie. The number of complete words will be decremented by one and the size of the trie will shrink
     * by the number of unique characters and their placements in the deleted word if they aren't used by a different word.
     *
     * @param string to be deleted
     * @return true if word is successfully deleted false if word was not in the trie
     */
    boolean delete(String string);

    /**
     * The number of nodes in this trie.
     * @return number of nodes
     */
    int size();

    /**
     * The number of complete words in this trie.
     *
     * @return number of complete words
     */
    int completeWords();

    /**
     * A detailed string representation of the structure of this trie.
     *
     * @return detailed string
     */
    String prettyString();

}
