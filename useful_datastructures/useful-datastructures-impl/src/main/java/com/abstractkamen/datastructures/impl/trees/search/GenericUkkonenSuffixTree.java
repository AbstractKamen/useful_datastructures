package com.abstractkamen.datastructures.impl.trees.search;

import com.abstractkamen.datastructures.api.trees.search.SuffixTree;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;

/**
 * The Generic suffix tree implementation using Ukkonen's Algorithm
 * </p>Useful links:
 * <ul>
 *     <li><a href="https://brenden.github.io/ukkonen-animation/">Ukkonen algorithm visualisation by Brenden Kokoszka</a></li>
 *     <li><a href="https://en.wikipedia.org/wiki/Ukkonen's_algorithm">Ukkonen Algorithm Wiki</a></li>
 *     <li><a href="https://www.baeldung.com/cs/ukkonens-suffix-tree-algorithm#bd-conclusion">Baeldung Ukkonen Algorithm Explanation</a></li>
 * </ul>
 *
 * @param <T> type of value a match can return
 * @author kamen.hristov
 */
public class GenericUkkonenSuffixTree<T> implements SuffixTree<T> {

    // THE TERMINATOR
    private static final char THE_TERMINATOR = '$';
    private char[] text = {};
    private final UkkonenSTNode root;
    private int textSize;
    private int nodesCount;
    private final Map<Integer, List<T>> valueCache = new HashMap<>();
    private final String toString;
    /**
     * This thing will be empty after tree is constructed. Its purpose is to deduplicate similar lists of values for each node in the
     * post-processing phase.
     * <pre>
     *     Node A → [1, 2, 3]
     *     Node B → [1, 2, 3]
     *     // have both A and B -> [1, 2, 3] the same list and remove the other
     * </pre>
     */
    private Map<Collection<Integer>, List<Integer>> valueKeysCanonicalizationCache = new HashMap<>();

    /**
     * The only available constructor expects a list of {@link UkkonenSuffixTreeInput} without any null values.
     *
     * @param input list
     */
    public GenericUkkonenSuffixTree(List<UkkonenSuffixTreeInput<T>> input) {
        this.root = constructTree(input);
        this.toString = String.format("GenericUkkonenSuffixTree[total-nodes-count=`%d`, text.length=`%d`, valueCache.size()=`%s`, nodesPerChar=`%.2f`, total-node-value-lists=`%d`]", nodesCount, textSize, valueCache.size(), (float) nodesCount / textSize, valueKeysCanonicalizationCache.size());
        // dereference cache after it has served its purpose
        valueKeysCanonicalizationCache = Collections.emptyMap();
    }

    @Override
    public Collection<T> findAllOccurrences(String pattern) {
        if (pattern == null) return Collections.emptyList();
        final UkkonenSTNode node = findSuffixNode(pattern, this.root);
        if (this.root == node) return Collections.emptyList();
        return node.valueKeysInOrder.stream()
                .flatMap(k -> valueCache.getOrDefault(k, Collections.emptyList()).stream())
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public boolean contains(String pattern) {
        if (pattern == null) return false;
        return findSuffixNode(pattern, this.root) != this.root;
    }

    @Override
    public int textSize() {
        return textSize;
    }

    @Override
    public int nodesCount() {
        return nodesCount;
    }

    @Override
    public int valuesCount() {
        return valueCache.size();
    }

    /**
     * This is purely for debugging
     *
     * @return a string with the tree structure
     */
    public String prettyTreeString() {
        final StringJoiner sj = new StringJoiner(System.lineSeparator());
        sj.add("[root]");
        new Object() {
            private void dfs(UkkonenSTNode node, int indent) {
                if (node == null || node.children.isEmpty()) {
                    return;
                }
                for (var e : node.children.entrySet()) {
                    final UkkonenSTNode child = e.getValue();
                    int start = child.start;
                    int end = Math.min(child.end, textSize);
                    final StringBuilder sb = new StringBuilder("-".repeat(indent));
                    for (int i = start; i < end; i++) sb.append(text[i]);
                    sb.append("`").append(child.valueKeysInOrder.size()).append("`")
                            .append(child.valueKeysInOrder);
                    if (child.suffixLink != null && child.suffixLink != root) {
                        sb.append("->");
                        final int suffixEnd = Math.min(child.suffixLink.end, textSize);
                        for (int i = child.suffixLink.start; i < suffixEnd; i++) sb.append(text[i]);
                        sb.append("`").append(child.suffixLink.valueKeysInOrder.size()).append("`")
                                .append(child.suffixLink.valueKeysInOrder);
                    }
                    sj.add(sb.toString());

                    dfs(child, indent + 2);
                }
            }

        }.dfs(this.root, 2);
        return sj.toString();
    }

    @Override
    public String toString() {
        return toString;
    }

    /**
     * The node definition
     */
    private class UkkonenSTNode {
        /**
         * This set holds keys to values for the current node
         */
        Collection<Integer> valueKeysInOrder = new LinkedHashSet<>();
        // ukkonen magic
        int start;
        final int end;
        UkkonenSTNode suffixLink;
        Map<Integer, UkkonenSTNode> children = new LinkedHashMap<>();

        UkkonenSTNode(int start, int end) {
            nodesCount++;
            this.start = start;
            this.end = end;
        }

        UkkonenSTNode(int start) {
            this(start, Integer.MAX_VALUE);
        }


        /**
         * Dereferences unused pointers and swaps mutable collections with immutable ones
         */
        void postProcess() {
            // swap from set to list
            valueKeysInOrder = valueKeysCanonicalizationCache.computeIfAbsent(valueKeysInOrder, List::copyOf);
            children = Map.copyOf(children);
            // not needed because tree is immutable
            // this.suffixLink = null;
        }
    }

    /*
     * UKKONEN'S ALGORITHM
     */
    private UkkonenSTNode constructTree(List<UkkonenSuffixTreeInput<T>> input) {
        UkkonenSTNode root = new UkkonenSTNode(-1, -1);
        root.suffixLink = root;
        final Map<String, List<T>> deduplicatedInput
                = input.stream()
                .collect(Collectors.groupingBy(UkkonenSuffixTreeInput::key,
                        Collectors.mapping(UkkonenSuffixTreeInput::value, Collectors.toList())));
        int i = 0;
        int j = 0;
        this.text = getText(deduplicatedInput.keySet()).toCharArray();
        this.textSize = text.length;
        int inputEndPos = 0;
        final var it = deduplicatedInput.entrySet().iterator();
        for (; i < deduplicatedInput.size(); ++i) {
            var e = it.next();
            // holds references to nodes which must have the current input value
            final List<T> value = e.getValue();
            inputEndPos += e.getKey().length() + 1 /*the terminator len*/;
            // value storage
            valueCache.put(i, value);

            int activeEdge = -1;
            int activeLength = 0;
            int remainder = 0;
            UkkonenSTNode activeNode = root;
            UkkonenSTNode activeLeaf = root;

            for (; j < inputEndPos; ++j) {
                remainder++;
                UkkonenSTNode lastCreated = null;
                while (remainder > 0) {
                    if (activeLength == 0) {
                        activeEdge = j;
                    }
                    int edgeChar = text[activeEdge];
                    UkkonenSTNode nextNode = activeNode.children.get(edgeChar);
                    if (nextNode == null) {
                        UkkonenSTNode leaf = new UkkonenSTNode(j);
                        leaf.valueKeysInOrder.add(i);
                        activeNode.children.put(edgeChar, leaf);
                        if (lastCreated != null) {
                            lastCreated.valueKeysInOrder.add(i);
//                            if (lastCreated.suffixLink != null) {
//                                lastCreated.suffixLink.valueKeysInOrder.add(i);
//                            }
                            lastCreated.suffixLink = activeNode;
                            lastCreated = null;
                        }
                        if (activeLeaf != root) {
                            activeLeaf.suffixLink = leaf;
                        }
//                        activeLeaf.valueKeysInOrder.addAll(leaf.valueKeysInOrder);
                        activeLeaf = leaf;
                    } else {
                        if (activeLength >= edgeLength(nextNode, j)) {
                            activeEdge += edgeLength(nextNode, j);
                            activeLength -= edgeLength(nextNode, j);
//                            nextNode.valueKeysInOrder.add(i);
//                            activeNode.valueKeysInOrder.add(i);
                            activeNode = nextNode;
//                            activeNode.valueKeysInOrder.add(i);
                            continue;
                        }

                        if (text[nextNode.start + activeLength] == text[j]) {
                            if (text[j] == THE_TERMINATOR) {
                                if (nextNode.suffixLink != null) {
                                    nextNode.suffixLink.valueKeysInOrder.add(i);
                                }
                                nextNode.valueKeysInOrder.add(i);
                            }

                            activeLength++;
                            if (lastCreated != null) {
//                                if (activeNode.suffixLink != null) {
//                                    activeNode.suffixLink.valueKeysInOrder.add(i);
//                                }
//                                if (lastCreated.suffixLink != null) {
//                                    lastCreated.suffixLink.valueKeysInOrder.add(i);
//                                }
                                lastCreated.suffixLink = activeNode;
//                                activeNode.valueKeysInOrder.add(i);
                            }
                            break;
                        }
                        // split edge
                        UkkonenSTNode split = new UkkonenSTNode(nextNode.start, nextNode.start + activeLength);
                        split.valueKeysInOrder.add(i);
                        activeNode.children.put(edgeChar, split);
                        UkkonenSTNode leaf = new UkkonenSTNode(j);
                        leaf.valueKeysInOrder.add(i);
                        if (activeLeaf != root) {
//                            if (activeLeaf.suffixLink != null) {
//                                activeLeaf.suffixLink.valueKeysInOrder.add(i);
//                            }
                            activeLeaf.valueKeysInOrder.addAll(leaf.valueKeysInOrder);
                            activeLeaf.suffixLink = leaf;
                        }
                        activeLeaf = leaf;
                        split.children.put((int) text[j], leaf);
                        nextNode.start += activeLength;
                        split.children.put((int) text[nextNode.start], nextNode);

                        if (lastCreated != null) {
//                            lastCreated.valueKeysInOrder.add(i);
//                            if (activeNode.suffixLink != null) {
//                                activeNode.suffixLink.valueKeysInOrder.add(i);
//                            }
                            lastCreated.suffixLink = split;
                        }
                        lastCreated = split;
                    }

                    remainder--;
                    if (activeNode == root && activeLength > 0) {
                        activeLength -= 1;
                        activeEdge = j - remainder + 1;
                    } else {
//                        activeNode.valueKeysInOrder.add(i);
//                        if (activeNode.suffixLink != null) {
//                            activeNode.suffixLink.valueKeysInOrder.add(i);
//                        }
                        activeNode = Optional.ofNullable(activeNode.suffixLink).orElse(root);
                    }
                }
            }
        }
        postProcessing(root);
        return root;
    }

    private String getText(Set<String> input) {
        final StringBuilder sb = new StringBuilder();
        for (String in : input) {
            sb.append(in).append(THE_TERMINATOR);
        }
        return sb.toString();
    }

    private void postProcessing(UkkonenSTNode root) {
        final Deque<UkkonenSTNode> itStack = new ArrayDeque<>(nodesCount);
        final Deque<UkkonenSTNode> postOrderStack = new ArrayDeque<>(nodesCount);
        postOrderStack.push(root);
        itStack.push(root);
        while (!itStack.isEmpty()) {
            final UkkonenSTNode node = itStack.pop();
            for (UkkonenSTNode child : node.children.values()) {
                itStack.push(child);
                postOrderStack.push(child);
            }
        }

        while (!postOrderStack.isEmpty()) {
            final UkkonenSTNode node = postOrderStack.pop();
            for (var child : node.children.values()) {
                // parent holds all ids of children fo easy lookup
                node.valueKeysInOrder.addAll(child.valueKeysInOrder);
            }

            /*
             * "path compression" removing grand total of 5 nodes for 90k input... I keep it anyway
             * GenericUkkonenSuffixTree[total-nodes-count=`2263933`, text.length=`2155112`, valueCache.size()=`92672`, nodesPerChar=`1.05`] Tree construction in  00:00:05.980
             * GenericUkkonenSuffixTree[total-nodes-count=`2263928`, text.length=`2155112`, valueCache.size()=`92672`, nodesPerChar=`1.05`] Tree construction in  00:00:05.670
             */
            final var it = node.children.entrySet().iterator();
            while (it.hasNext()) {
                var entry = it.next();
                if (entry.getKey() == THE_TERMINATOR) {
                    nodesCount--;
                    it.remove();
                }
            }
            node.postProcess();
        }

        // root doesn't need to hold values at all
        root.valueKeysInOrder = Collections.emptyList();
        root.suffixLink = null;
    }

    private int edgeLength(UkkonenSTNode node, int i) {
        return Math.min(node.end, i + 1) - node.start;
    }

    private UkkonenSTNode findSuffixNode(String pattern, UkkonenSTNode root) {
        final int patternLen = pattern.length();
        UkkonenSTNode node = root;
        int i = 0;
        while (i < patternLen) {
            final int key = pattern.codePointAt(i);
            final UkkonenSTNode child = node.children.get(key);
            if (child == null) {
                return root;
            }
            final int end = Math.min(child.end, this.textSize);
            int j = child.start;

            while (j < end && i < patternLen) {
                final int textCodePoint = Character.codePointAt(this.text, j);
                final int patternCodePoint = pattern.codePointAt(i);
                if (textCodePoint != patternCodePoint) {
                    return root;
                }
                i += 1;
                j += 1;
            }
            if (Character.isSupplementaryCodePoint(key)) {
                i++;
            }
            node = child;
        }
        return node;
    }

}