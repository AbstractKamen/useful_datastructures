/**
 * Copyright 2012 Alessandro Bahgat Shehata
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.abstractkamen.datastructures.impl.trees.search;

import com.abstractkamen.datastructures.api.trees.search.SuffixTree;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicReference;

/**
 * The Generic suffix tree implementation using Ukkonen's Algorithm
 * </p> Useful links:
 * <ul>
 *     <li><a href="https://www.cs.helsinki.fi/u/ukkonen/SuffixT1withFigs.pdf">Esko Ukkonen On–line construction of suffix trees</a></li>
 *     <li><a href="https://github.com/abahgat/suffixtree">Good Implementation of generic suffix tree</a></li>
 *     <li><a href="https://en.wikipedia.org/wiki/Ukkonen's_algorithm">Ukkonen Algorithm Wiki</a></li>
 *     <li><a href="https://brenden.github.io/ukkonen-animation/">Ukkonen algorithm visualisation by Brenden Kokoszka</a></li>
 *     <li><a href="https://www.baeldung.com/cs/ukkonens-suffix-tree-algorithm#bd-conclusion">Baeldung Ukkonen Algorithm Explanation</a></li>
 * </ul>
 *
 * @param <T> type of value a match can return
 * @author kamen.hristov
 */
public class GenericUkkonenSuffixTree<T> implements SuffixTree<T> {

    private final UkkonenSTNode root = new UkkonenSTNode();

    /**
     * This thing will be empty after tree is constructed. Its purpose is to deduplicate similar lists of values for each node in the
     * post-processing phase.
     * <pre>
     *     UkkonenSTNode A → [1, 2, 3]
     *     UkkonenSTNode B → [1, 2, 3]
     *     // have both A and B -> [1, 2, 3] the same list and remove the other
     * </pre>
     */
    private Map<Collection<Integer>, Collection<T>> valueKeysCanonicalizationCache = new HashMap<>();
    /**
     * Stores the T values for the tree during construction.
     */
    private Map<Integer, T> valueCache = new HashMap<>();
    // metrics fields if we ever need them
    private int textSize;
    private int nodesCount;
    private final String toString;

    /**
     * The only available constructor expects a list of {@link UkkonenSuffixTreeInput} without any null values.
     *
     * @param input list
     */
    public GenericUkkonenSuffixTree(Collection<UkkonenSuffixTreeInput<T>> input) {
        constructTree(input);
        this.toString = String.format("GenericUkkonenSuffixTree[total-nodes-count=`%d`, text.length=`%d`, valueCache.size()=`%s`, nodesPerChar=`%.2f`, total-node-value-lists=`%d`, total-values=`%d`]", nodesCount, textSize, valueCache.size(), (float) nodesCount / textSize, valueKeysCanonicalizationCache.size(), valueKeysCanonicalizationCache.values().stream().mapToInt(Collection::size).reduce(0, Integer::sum));
        // dereference caches after tree is constructed
        this.valueKeysCanonicalizationCache = Collections.emptyMap();
        this.valueCache = Collections.emptyMap();
    }

    @Override
    public Collection<T> findAllOccurrences(String pattern) {
        if (pattern == null) return Collections.emptyList();
        final UkkonenSTNode node = findSuffixNode(pattern, this.root);
        if (this.root == node) return Collections.emptyList();
        return node.actualValues;
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

    @Override
    public String toString() {
        return toString;
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
                    int end = child.end;
                    final StringBuilder sb = new StringBuilder("-".repeat(indent));
                    for (int i = start; i < end; i++) sb.append(child.source.charAt(i));
                    final Collection<?> childSrc = child.keyIndexes.isEmpty() ? child.actualValues : child.keyIndexes;
                    sb.append("`").append(childSrc.size()).append("`")
                            .append(childSrc);
                    if (child.suffixLink != null) {
                        sb.append("->");
                        if (child.suffixLink == root) {
                            sb.append("[root]").append("`").append(0).append("`")
                                    .append("[]");
                        } else {
                            final int suffixEnd = child.suffixLink.end;
                            for (int i = child.suffixLink.start; i < suffixEnd; i++) sb.append(child.suffixLink.source.charAt(i));
                            final Collection<?> suffixSrc = child.keyIndexes.isEmpty() ? child.actualValues : child.keyIndexes;
                            sb.append("`").append(suffixSrc.size()).append("`")
                                    .append(suffixSrc);
                        }
                    }
                    sj.add(sb.toString());

                    dfs(child, indent + 2);
                }
            }

        }.dfs(this.root, 2);
        return sj.toString();
    }

    private void constructTree(Collection<UkkonenSuffixTreeInput<T>> input) {
        int index = 0;

        final StringBuilder prefix = new StringBuilder();
        for (UkkonenSuffixTreeInput<T> in : input) {
            final String key = in.key();
            final AtomicReference<UkkonenSTNode> activeLeaf = new AtomicReference<>(root);
            UkkonenSTNode suffix = root;
            valueCache.put(index, in.value());

            for (int i = 0; i < key.length(); i++) {
                int codePoint = key.codePointAt(i);
                prefix.appendCodePoint(codePoint);

                var activeNode = update(suffix, prefix.toString(), key, i, index, activeLeaf);
                // deepest possible suffix
                activeNode = canonize(activeNode.first(), activeNode.second());

                suffix = activeNode.first();
                prefix.setLength(0);
                prefix.append(activeNode.second());

                if (Character.isSupplementaryCodePoint(codePoint)) {
                    i++;
                }
            }

            // link leaves' suffixes
            final UkkonenSTNode ref = activeLeaf.get();
            if (ref.suffixLink == null && ref != root && ref != suffix) {
                ref.suffixLink = suffix;
            }
            index++;
        }
        postProcessing(root);
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
            int j = 0;

            while (j < child.length() && i < patternLen) {
                final int textCodePoint = child.codePointAt(j);
                final int patternCodePoint = pattern.codePointAt(i);
                if (textCodePoint != patternCodePoint) {
                    return root;
                }
                i += 1;
                j += 1;
            }
            node = child;
        }
        return node;
    }

    private Pair<Boolean, UkkonenSTNode> testAndSplit(UkkonenSTNode currentNode, String prefix, int t, String input, int index, int value) {
        // find deepest
        var canonized = canonize(currentNode, prefix);
        UkkonenSTNode deepestSuffix = canonized.first();
        String deepestSuffixString = canonized.second();

        if (!deepestSuffixString.isEmpty()) {
            UkkonenSTNode nextSuffix = deepestSuffix.children.get(deepestSuffixString.codePointAt(0));
            // mismatch on current path
            if (nextSuffix == null) {
                return new Pair<>(false, deepestSuffix);
            }
            // check if deepest suffix still matches
            if (nextSuffix.length() > deepestSuffixString.length() && nextSuffix.codePointAt(deepestSuffixString.length()) == t) {
                return new Pair<>(true, deepestSuffix);
            } else {
                // build a new node
                final UkkonenSTNode newNode = new UkkonenSTNode(deepestSuffixString);
                // drop prefix of current node source string but keep original
                nextSuffix.start += deepestSuffixString.length();

                // link s -> r
                newNode.children.put(nextSuffix.codePointAt(0), nextSuffix);
                deepestSuffix.children.put(deepestSuffixString.codePointAt(0), newNode);

                return new Pair<>(false, newNode);
            }

        } else {
            final UkkonenSTNode next = deepestSuffix.children.get(t);
            if (next == null) {
                // no path to deepestSuffix
                return new Pair<>(false, deepestSuffix);
            } else {
                int remainder = input.length() - index;

                if (remainder == next.length() && next.matches(input, index, remainder)) {
                    // found a path just in time -> add current value to found
                    next.addValue(value);
                    return new Pair<>(true, deepestSuffix);
                } else if (remainder > next.length() && next.matches(input, index, next.length())) {
                    return new Pair<>(true, deepestSuffix);
                } else if (next.length() > remainder && next.matches(input, index, remainder)) {
                    // split current
                    UkkonenSTNode newNode = new UkkonenSTNode(input, index, input.length());
                    newNode.addValue(value);
                    // drop prefix of current node source string but keep original
                    next.start += remainder;
                    newNode.children.put(next.codePointAt(0), next);
                    deepestSuffix.children.put(t, newNode);

                    return new Pair<>(false, deepestSuffix);
                } else {
                    // common string
                    return new Pair<>(true, deepestSuffix);
                }
            }
        }

    }

    /**
     * Finds the deepest possible path for given suffix and string
     *
     * @param suffix node
     * @param str    string
     * @return (deepestSuffix, deepestSuffixString)
     */
    private Pair<UkkonenSTNode, String> canonize(UkkonenSTNode suffix, String str) {

        if (str.isEmpty()) return new Pair<>(suffix, str);

        int i = 0;
        UkkonenSTNode nextSuffix = suffix.children.get(str.codePointAt(i));
        while (nextSuffix != null && (str.length() - i) >= nextSuffix.length()) {
            i += nextSuffix.length();
            suffix = nextSuffix;
            if (i < str.length()) {
                nextSuffix = suffix.children.get(str.codePointAt(i));
            } else {
                nextSuffix = null;
            }
        }

        return new Pair<>(suffix, str.substring(i));
    }

    private Pair<UkkonenSTNode, String> update(UkkonenSTNode suffix, String prefix, String input, int index, int value, AtomicReference<UkkonenSTNode> activeLeaf) {
        String tempstr = prefix;
        int newChar = prefix.codePointBefore(prefix.length());

        // oldr ← root; (end–point, r)
        UkkonenSTNode oldroot = root;

        // ← test–and–split(s,(k, i − 1), ti);
        var ret = testAndSplit(suffix, prefix.substring(0, prefix.length() - Character.charCount(newChar)), newChar, input, index, value);

        UkkonenSTNode transition = ret.second();
        boolean endpoint = ret.first();

        UkkonenSTNode leaf;
        while (!endpoint) {
            UkkonenSTNode path = transition.children.get(newChar);
            if (path != null) {
                // treat path as leave if present
                leaf = path;
                leaf.addValue(value);
            } else {
                // new leaf
                leaf = new UkkonenSTNode(input, index, input.length());
                leaf.addValue(value);
                transition.children.put(newChar, leaf);
            }

            // link leaves
            if (activeLeaf.get() != root) {
                activeLeaf.get().suffixLink = leaf;
            }
            activeLeaf.set(leaf);

            if (oldroot != root) {
                oldroot.suffixLink = transition;
            }

            oldroot = transition;

            if (suffix.suffixLink == null) {
                // handle _|_
                if (!tempstr.isEmpty()) {
                    tempstr = tempstr.substring(Character.charCount(tempstr.codePointAt(0)));
                }
            } else {
                final var canonized = canonize(suffix.suffixLink, dropLast(tempstr));
                suffix = canonized.first();
                tempstr = (canonized.second() + new String(Character.toChars(tempstr.codePointBefore(tempstr.length()))));
            }
            // (end–point, r) ← test–and–split(s,(k, i − 1), ti);
            ret = testAndSplit(suffix, dropLast(tempstr), newChar, input, index, value);
            transition = ret.second();
            endpoint = ret.first();

        }

        if (oldroot != root) {
            oldroot.suffixLink = transition;
        }

        return new Pair<>(suffix, tempstr);
    }

    private String dropLast(String seq) {
        if (seq.isEmpty()) return "";
        final int charsCount = Character.charCount(seq.codePointBefore(seq.length()));
        return seq.substring(0, seq.length() - charsCount);
    }

    private void postProcessing(UkkonenSTNode root) {
        // dfs post order with stacks
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
                node.keyIndexes.addAll(child.keyIndexes);
            }
        }

        itStack.push(root);
        while (!itStack.isEmpty()) {
            final UkkonenSTNode node = itStack.pop();
            for (UkkonenSTNode child : node.children.values()) {
                itStack.push(child);
            }
            node.postProcess();
        }
    }

    private record Pair<A, B>(A first, B second) {}

    private class UkkonenSTNode {

        String source;
        int start;
        int end;

        /**
         * This collection holds the index of a key source. After tree is constructed it is mapped to actual input value.
         */
        Collection<Integer> keyIndexes = new TreeSet<>();
        Map<Integer, UkkonenSTNode> children = new HashMap<>();
        UkkonenSTNode suffixLink;
        /**
         * Upon tree construction this collection will hold actual values in input order
         */
        Collection<T> actualValues = Collections.emptyList();

        UkkonenSTNode() {
            // tree prop
            nodesCount++;
        }

        UkkonenSTNode(String label) {
            this();
            this.source = label;
            this.start = 0;
            this.end = label.length();
        }

        UkkonenSTNode(String source, int start, int end) {
            this();
            this.source = source;
            this.start = start;
            this.end = end;
        }

        int length() {
            return end - start;
        }

        int codePointAt(int index) {
            return source.codePointAt(start + index);
        }

        boolean matches(String other, int offset, int len) {
            return source.regionMatches(start, other, offset, len);
        }

        boolean addValue(int index) {
            if (!keyIndexes.add(index)) return false;
            // propagate to suffix links
            UkkonenSTNode currentLink = this.suffixLink;
            while (currentLink != null) {
                if (!currentLink.addValue(index)) break;

                currentLink = currentLink.suffixLink;
            }
            return true;
        }

        /**
         * Dereferences unused pointers and swaps mutable collections with immutable ones
         */
        void postProcess() {
            this.children = Map.copyOf(children);
            this.actualValues = valueKeysCanonicalizationCache.computeIfAbsent(keyIndexes,
                    keys -> keys.stream().sorted()
                            .map(valueCache::get)
                            .toList());
            this.keyIndexes = Collections.emptyList();
            // not needed because tree is immutable
            this.suffixLink = null;
            if (this.source != null) {
                // tree prop
                textSize += this.source.length();
            }
        }
    }
}