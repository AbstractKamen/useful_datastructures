package com.abstractkamen.datastructures.impl.trees.search;

import com.abstractkamen.datastructures.api.trees.search.PrefixTrie;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class PrefixTrieImpl implements PrefixTrie {

    private static final String LINE_SEPARATOR = System.lineSeparator();
    private final PrefixTrieNode root;
    private int completeWords;
    private int size;

    public PrefixTrieImpl() {
        this.root = new PrefixTrieNode();
    }

    protected PrefixTrieNode getRoot() {
        return root;
    }

    @Override
    public Collection<String> startsWith(String prefix, int limit) {
        final List<PrefixTrieNode> leaves = new ArrayList<>();
        search(this.root, prefix, 0, leaves, limit, l -> true);
        return getWords(leaves);
    }

    @Override
    public boolean isPrefix(String substring) {
        return search(this.root, substring, 0, new ArrayList<>(), 1, l -> true);
    }

    @Override
    public boolean insert(String string) {
        if (string == null || string.isEmpty()) return false;
        final PrefixTrieNode inserted = insert(string, this.root, 0, PrefixTrieNode::new);
        if (inserted.isWord) {
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
        final List<PrefixTrieNode> result = new ArrayList<>();
        search(this.root, string, 0, result, 1, l -> true);
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
            visitAllNodes(0, this.root, ">>>", sb);
            return sb.toString();
        }
    }

    /**
     * Visit a node which we know is a word.
     *
     * @param node word node
     * @param visitor sb visitor
     */
    protected void visitWordNode(PrefixTrieNode node, StringBuilder visitor) {
        // if current is word append the whole word to the current node
        PrefixTrieNode c = node;
        visitor.append(": (");
        final int l = visitor.length();
        while (c != this.root) {
            visitor.insert(l, (char) c.c);
            c = c.parent;
        }
        visitor.append(")");
    }

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

    protected Collection<String> getWords(List<PrefixTrieNode> leaves) {
        return leaves.stream().map(this::getWord).collect(Collectors.toList());
    }

    protected String getWord(PrefixTrieNode l) {
        final StringBuilder sb = new StringBuilder();
        while (l != this.root) {
            sb.insert(0, (char) l.c);
            l = l.parent;
        }
        return sb.toString();
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
    protected boolean search(PrefixTrieNode n, String needle, int i, Collection<PrefixTrieNode> result, int resultLimit,
                             Predicate<PrefixTrieNode> resultTest) {
        if (n == null) {
            // we fell off the tree
            return false;
        } else if (result.size() >= resultLimit) {
            //we hit the limit and must return, but we haven't fallen off
            return true;
        }
        if (n.isWord && i >= needle.length() && resultTest.test(n)) {
            result.add(n);
        }
        if (i >= needle.length()) {
            // assume always hit
            for (PrefixTrieNode c : n.children.values()) {
                search(c, needle, i + 1, result, resultLimit, resultTest);
            }
            return true;
        } else {
            final int c = needle.charAt(i);
            // go deeper
            return search(n.children.get(c), needle, i + 1, result, resultLimit, resultTest);
        }
    }

    protected boolean delete(PrefixTrieNode n, String word, int i) {
        if (n == null || i > word.length()) {
            return false;
        }
        if (n.isWord && i == word.length()) {
            if (shouldDecrementWord(n)) {
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
     * wordCount decrementing condition. Can be overridden in extensions.
     *
     * @param n word node
     * @return true if wordCount should be decremented
     */
    protected boolean shouldDecrementWord(PrefixTrieNode n) {
        return true;
    }

    private void visitAllNodes(int size, PrefixTrieNode node, String prefix, StringBuilder visitor) {
        final String pointer = "├─► ";
        final String hookPointer = "└─► ";
        final String pointerConnection = "│";
        // depth-first search
        // visit current
        if (node != this.root) {
            if (size > 1)
                visitor.append(prefix).append(pointer);
            else {
                visitor.append(prefix).append(hookPointer);
            }
            visitor.append((char) node.c);
        }
        if (node.isWord) {
            visitWordNode(node, visitor);
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
            visitAllNodes(s--, child, nextPrefix, visitor);
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
        private int distanceFromRoot;
        private PrefixTrieNode parent;

        PrefixTrieNode() {
            children = new TreeMap<>();
            this.distanceFromRoot = 1;
        }

        PrefixTrieNode(int c, PrefixTrieNode parent) {
            this();
            this.c = c;
            this.parent = parent;
            this.distanceFromRoot = parent.distanceFromRoot + 1;
        }

        protected void setIsWord(boolean isWord) {
            this.isWord = isWord;
        }

        protected void addChild(int c, PrefixTrieNode child) {
            this.children.put(c, child);
        }

        protected void setC(int c) {
            this.c = c;
        }

        protected int getC() {
            return c;
        }

        protected Map<Integer, PrefixTrieNode> getChildren() {
            return children;
        }

        public PrefixTrieNode getParent() {
            return parent;
        }
    }
}