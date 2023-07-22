package com.abstractkamen.datastructures.impl.trees.search;

import com.abstractkamen.datastructures.api.trees.search.SuffixTrie;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This implementation uses the same insert/delete/search algorithm as the {@link PrefixTrieImpl}. In order to implement the
 * {@link SuffixTrie} it reverses the input string. The cost of insert/delete/search is double that of the {@link PrefixTrieImpl}, but is
 * still O(m) where m is the length of the input string.
 */
public class ReversePrefixTrie extends PrefixTrieImpl implements SuffixTrie {

    @Override
    protected boolean shouldDecrementWordOnDelete(PrefixTrieNode n) {
        return !(n instanceof ReversePrefixTrieNode);
    }

    @Override
    protected void visitWordNode(PrefixTrieNode node, StringBuilder visitor, StringBuilder currentChars) {
        if (node instanceof ReversePrefixTrieNode) {
            visitor.append(": (");
            visitor.append(new StringBuilder(currentChars).reverse());
            visitor.append(")");
        } else {
            super.visitWordNode(node, visitor, currentChars);
        }
    }

    @Override
    public boolean insert(String string) {
        final boolean insert = super.insert(string);
        if (insert) {
            final String reversed = getReversedString(string);
            new ReversePrefixTrieNode(insert(reversed, getRoot(), 0, ReversePrefixTrieNode::new));
        }
        return insert;
    }

    @Override
    public boolean delete(String string) {
        final boolean delete = super.delete(string);
        if (delete) {
            delete(getRoot(), getReversedString(string), 0);
        }
        return delete;
    }

    @Override
    public Collection<String> endsWith(String suffix, int limit) {
        final List<String> inReverse = new ArrayList<>();
        search(getRoot(), getReversedString(suffix), 0, inReverse, limit, ReversePrefixTrieNode.class::isInstance, new StringBuilder());
        return inReverse.stream().map(this::getReversedString).collect(Collectors.toList());
    }

    @Override
    public boolean isSuffix(String suffix) {
        final List<String> strings = new ArrayList<>();
        search(getRoot(), getReversedString(suffix), 0, strings, 1, ReversePrefixTrieNode.class::isInstance, new StringBuilder());
        return !strings.isEmpty();
    }

    @Override
    public boolean isPrefix(String prefix) {
        return !startsWith(prefix, 1).isEmpty();
    }

    @Override
    public Collection<String> startsWith(String prefix, int limit) {
        final List<String> strings = new ArrayList<>();
        search(getRoot(), prefix, 0, strings, limit, l -> !(l instanceof ReversePrefixTrieNode), new StringBuilder());
        return strings;
    }

    @Override
    public String toString() {
        final List<String> strings = new ArrayList<>();
        search(getRoot(), "", 0, strings, completeWords(), l -> !(l instanceof ReversePrefixTrieNode), new StringBuilder());
        return strings.toString();
    }

    @Override
    public String longestCommonSubstring() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<String> substringWith(String substring, int limit) {
        throw new UnsupportedOperationException();
    }

    private String getReversedString(String string) {
        return new StringBuilder(string).reverse().toString();
    }

    private static class ReversePrefixTrieNode extends PrefixTrieNode {

        ReversePrefixTrieNode(int c, PrefixTrieNode parent) {
            super(c, parent);
        }

        /**
         * Copies the template and replaces it in its parent.
         * @param template node
         */
        ReversePrefixTrieNode(PrefixTrieNode template) {
            super(template.getC(), template.getParent());
            setC(template.getC());
            setIsWord(true);
            template.getChildren().forEach(this::addChild);
            template.getParent().addChild(getC(), this);
        }

    }
}
