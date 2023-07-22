package com.abstractkamen.datastructures.impl.trees.search;

import com.abstractkamen.datastructures.api.trees.search.PrefixTrie;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Predicate;

/**
 * Basic implementation of a prefix 'trie'. Each character of an inserted string is stored in a node in lexicographical order and each
 * node has children and a parent. All nodes have one common ancestor which is the {@link #root}. A node is marked if the path from the
 * {@link #root} to it forms a word (a string inserted by {@link #insert(String)}).
 */
public class PrefixTrieImpl implements PrefixTrie {

    private static final String LINE_SEPARATOR = System.lineSeparator();
    /**
     * root node which can never be null
     */
    private final PrefixTrieNode root;
    /**
     * number of complete words
     */
    private int completeWords;
    /**
     * number of nodes
     */
    private int size;

    public PrefixTrieImpl() {
        this.root = new PrefixTrieNode();
    }

    protected PrefixTrieNode getRoot() {
        return root;
    }

    @Override
    public Collection<String> startsWith(String prefix, int limit) {
        final List<String> strings = new ArrayList<>();
        search(this.root, prefix, 0, strings, limit, l -> true, new StringBuilder());
        return strings;
    }

    @Override
    public boolean isPrefix(String substring) {
        return search(this.root, substring, 0, new ArrayList<>(), 1, l -> true, new StringBuilder());
    }

    @Override
    public boolean insert(String string) {
        if (string == null || string.isEmpty()) return false;
        final PrefixTrieNode inserted = insert(string, this.root, 0, PrefixTrieNode::new);
        if (inserted.isWord()) {
            // was inserted before
            return false;
        }
        this.completeWords++;
        inserted.setIsWord(true);
        return true;
    }

    @Override
    public boolean delete(String string) {
        return delete(this.root, string, 0);
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public int completeWords() {
        return completeWords;
    }

    @Override
    public String toString() {
        return startsWith("", completeWords).toString();
    }

    @Override
    public boolean contains(String string) {
        final List<String> result = new ArrayList<>();
        search(this.root, string, 0, result, 1, l -> true, new StringBuilder());
        return !result.isEmpty();
    }

    @Override
    public String prettyString() {
        if (this.root.children.isEmpty()) {
            return "empty tree";
        } else {
            final StringBuilder sb = new StringBuilder("NaiveTrie total nodes: ")
                .append(size).append(LINE_SEPARATOR)
                .append("total complete words:")
                .append(completeWords).append(LINE_SEPARATOR);
            visitAllNodes(0, this.root, ">>>", sb, new StringBuilder());
            return sb.toString();
        }
    }

    /**
     * Visit a node which we know is a word.
     *
     * @param node word node
     * @param visitor sb visitor
     */
    protected void visitWordNode(PrefixTrieNode node, StringBuilder visitor, StringBuilder currentChars) {
        // if current is word append the whole word to the current node
        visitor.append(": (");
        visitor.append(currentChars);
        visitor.append(")");
    }

    /**
     * Inserts a word character by character where first a character is lookup in the root and if it's missing a new node is created and
     * appended to the root after which the algorithm is repeated recursively until we reach the end of the word. The final result of the
     * recursion will be the leaf node.
     * @param word input sting
     * @param root root node
     * @param i current character index
     * @param factory node factory - useful in extensions
     * @return the 'inserted' node
     */
    protected PrefixTrieNode insert(String word, PrefixTrieNode root, int i, BiFunction<Integer, PrefixTrieNode, PrefixTrieNode> factory) {
        if (i == word.length()) {
            return root;
        } else {
            final int c = word.charAt(i);
            PrefixTrieNode child = root.children.get(c);
            if (child == null) {
                child = factory.apply(c, root);
                size++;
                root.children.put(c, child);
            }
            return insert(word, child, i + 1, factory);
        }
    }

    /**
     * Searching method for this trie. Will walk through all characters in needle and find a possible path of nodes for them. If the path
     * exists return true.
     * @param n current node
     * @param needle string
     * @param i offset
     * @param result string storage
     * @param resultLimit storage limit
     * @param resultTest add to result collection if true
     * @return true if a needle path exists
     */
    protected boolean search(PrefixTrieNode n, String needle, int i, Collection<String> result, int resultLimit,
                             Predicate<PrefixTrieNode> resultTest, StringBuilder currentChars) {
        if (n == null) {
            // we fell off the tree
            return false;
        }
        if (n != this.root) {
            currentChars.append(n.getChar());
        }
        if (result.size() >= resultLimit) {
            //we hit the limit and must return, but we haven't fallen off
            return true;
        }
        if (n.isWord() && i >= needle.length() && resultTest.test(n)) {
            result.add(currentChars.toString());
        }
        if (i >= needle.length()) {
            // assume always hit
            for (PrefixTrieNode c : n.children.values()) {
                // new branch -> new StringBuilder
                search(c, needle, i + 1, result, resultLimit, resultTest, new StringBuilder(currentChars));
            }
            return true;
        } else {
            final int c = needle.charAt(i);
            // go deeper
            return search(n.children.get(c), needle, i + 1, result, resultLimit, resultTest, currentChars);
        }
    }

    protected boolean delete(PrefixTrieNode n, String word, int i) {
        if (n == null || i > word.length()) {
            return false;
        }
        if (n.isWord() && i == word.length()) {
            if (shouldDecrementWordOnDelete(n)) {
                completeWords--;
            }
            if (!n.children.isEmpty()) {
                n.setIsWord(false);
            } else {
                removeNode(n.parent, n.c);
            }
            return true;
        } else if (i < word.length()) {
            final int c = word.charAt(i);
            return delete(n.children.get(c), word, i + 1);
        } else {
            return false;
        }
    }

    /**
     * {@link #completeWords} decrementing condition. Can be overridden in extensions.
     *
     * @param n word node
     * @return true if {@link #completeWords} should be decremented
     */
    protected boolean shouldDecrementWordOnDelete(PrefixTrieNode n) {
        return true;
    }

    private void visitAllNodes(int size, PrefixTrieNode node, String prefix, StringBuilder visitor, StringBuilder currentChars) {
        final String pointer = "├─► ";
        final String hookPointer = "└─► ";
        final String pointerConnection = "│";
        // depth-first search
        // visit current
        if (node != this.root) {

            if (size > 1) {
                visitor.append(prefix).append(pointer);
            } else {
                visitor.append(prefix).append(hookPointer);
            }
            currentChars.append(node.getChar());
            visitor.append(node.getChar());
        }
        if (node.isWord()) {
            visitWordNode(node, visitor, currentChars);
        }
        visitor.append(LINE_SEPARATOR);
        final String nextPrefix;
        if (node.parent != null && size > 1) {
            nextPrefix = prefix + pointerConnection + " ".repeat(pointer.length());
        } else if (node != this.root) {
            nextPrefix = prefix + " ".repeat(pointer.length());
        } else {
            nextPrefix = prefix;
        }
        // visit children
        int s = node.children.size();
        for (PrefixTrieNode child : node.children.values()) {
            visitAllNodes(s--, child, nextPrefix, visitor, currentChars);
        }
        if (this.root != node) {
            currentChars.setLength(currentChars.length() - 1);
        }
    }

    private void removeNode(PrefixTrieNode parent, int c) {
        if (parent != null) {
            parent.children.remove(c);
            if (parent.children.isEmpty()) {
                removeNode(parent.parent, parent.c);
            }
            size--;
        }
    }

    protected static class PrefixTrieNode {

        private final Map<Integer, PrefixTrieNode> children;
        private boolean isWord;
        private int c;
        private PrefixTrieNode parent;

        PrefixTrieNode() {
            children = new TreeMap<>();
        }

        PrefixTrieNode(int c, PrefixTrieNode parent) {
            this();
            this.c = c;
            this.parent = parent;
        }

        void setIsWord(boolean isWord) {
            this.isWord = isWord;
        }

        void addChild(int c, PrefixTrieNode child) {
            this.children.put(c, child);
        }

        void setC(int c) {
            this.c = c;
        }

        int getC() {
            return c;
        }

        char getChar() {
            return (char) c;
        }

        Map<Integer, PrefixTrieNode> getChildren() {
            return children;
        }

        PrefixTrieNode getParent() {
            return parent;
        }

        boolean isWord() {
            return isWord;
        }
    }
}