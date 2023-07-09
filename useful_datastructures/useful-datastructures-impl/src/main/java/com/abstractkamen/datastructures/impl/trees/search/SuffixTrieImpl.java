package com.abstractkamen.datastructures.impl.trees.search;

import com.abstractkamen.datastructures.api.trees.search.SuffixTrie;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class SuffixTrieImpl extends PrefixTrieImpl implements SuffixTrie {
    @Override
    protected boolean shouldDecrementWord(PrefixTrieNode n) {
        return !(n instanceof SuffixTrieNode);
    }

    @Override
    protected void visitWordNode(PrefixTrieNode node, StringBuilder visitor) {
        if (node instanceof SuffixTrieNode) {
            PrefixTrieNode c = node;
            visitor.append(": (");
            while (c != getRoot()) {
                visitor.append((char) c.getC());
                c = c.getParent();
            }
            visitor.append(")");
        } else {
            super.visitWordNode(node, visitor);
        }
    }

    @Override
    public boolean insert(String string) {
        final boolean insert = super.insert(string);
        if (insert) {
            final String reversed = getReversedString(string);
            new SuffixTrieNode(insert(reversed, getRoot(), 0, SuffixTrieNode::new));
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
        final List<PrefixTrieNode> leaves = new ArrayList<>();
        search(getRoot(), getReversedString(suffix), 0, leaves, limit, SuffixTrieNode.class::isInstance);
        return getWords(leaves).stream().map(this::getReversedString).collect(Collectors.toList());
    }

    @Override
    public boolean isSuffix(String suffix) {
        final List<PrefixTrieNode> result = new ArrayList<>();
        search(getRoot(), getReversedString(suffix), 0, result, 1, SuffixTrieNode.class::isInstance);
        return !result.isEmpty();
    }

    @Override
    public boolean isPrefix(String prefix) {
        final ArrayList<PrefixTrieNode> result = new ArrayList<>();
        search(getRoot(), prefix, 0, result, 1, l -> !(l instanceof SuffixTrieNode));
        return !result.isEmpty();
    }

    @Override
    public Collection<String> startsWith(String prefix, int limit) {
        final List<PrefixTrieNode> leaves = new ArrayList<>();
        search(getRoot(), prefix, 0, leaves, limit, l -> !(l instanceof SuffixTrieNode));
        return getWords(leaves);
    }

    @Override
    public String toString() {
        final List<PrefixTrieNode> leaves = new ArrayList<>();
        search(getRoot(), "", 0, leaves, completeWords(), l -> !(l instanceof SuffixTrieNode));
        return leaves.stream()
            .map(l -> l instanceof SuffixTrieNode ? getReversedString(getWord(l)) : getWord(l))
            .collect(Collectors.joining(", ", "[", "]"));
    }

    private String getReversedString(String string) {
        return new StringBuilder(string).reverse().toString();
    }

    private static class SuffixTrieNode extends PrefixTrieNode {

        SuffixTrieNode(int c, PrefixTrieNode parent) {
            super(c, parent);
        }

        /**
         * Copies the template and replaces it in its parent.
         * @param template node
         */
        SuffixTrieNode(PrefixTrieNode template) {
            super(template.getC(), template.getParent());
            setC(template.getC());
            setIsWord(true);
            template.getChildren().forEach(this::addChild);
            template.getParent().addChild(getC(), this);
        }
    }
}
