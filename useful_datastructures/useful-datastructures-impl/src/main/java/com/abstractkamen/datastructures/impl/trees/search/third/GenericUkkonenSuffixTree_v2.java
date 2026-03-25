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
package com.abstractkamen.datastructures.impl.trees.search.third;

import com.abstractkamen.datastructures.api.trees.search.SuffixTree;
import com.abstractkamen.datastructures.impl.trees.search.UkkonenSuffixTreeInput;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.concurrent.atomic.AtomicReference;

/**
 * The Generic suffix tree implementation using Ukkonen's Algorithm
 * </p>Useful links:
 * <ul>
 *     <li><a href="https://www.cs.helsinki.fi/u/ukkonen/SuffixT1withFigs.pdf">Esko Ukkonen On–line construction of suffix trees</a></li>
 *     <li><a href="https://brenden.github.io/ukkonen-animation/">Ukkonen algorithm visualisation by Brenden Kokoszka</a></li>
 *     <li><a href="https://en.wikipedia.org/wiki/Ukkonen's_algorithm">Ukkonen Algorithm Wiki</a></li>
 *     <li><a href="https://www.baeldung.com/cs/ukkonens-suffix-tree-algorithm#bd-conclusion">Baeldung Ukkonen Algorithm Explanation</a></li>
 * </ul>
 *
 * @param <T> type of value a match can return
 * @author kamen.hristov
 */
public class GenericUkkonenSuffixTree_v2<T> implements SuffixTree<T> {

    private final UkkonenSTNode root = new UkkonenSTNode();
    private final Map<Integer, T> valueCache = new HashMap<>();
    private int textSize;
    private int nodesCount;
    private final String toString;
    /**
     * This thing will be empty after tree is constructed. Its purpose is to deduplicate similar lists of values for each node in the
     * post-processing phase.
     * <pre>
     *     UkkonenSTNode A → [1, 2, 3]
     *     UkkonenSTNode B → [1, 2, 3]
     *     // have both A and B -> [1, 2, 3] the same list and remove the other
     * </pre>
     */
    private Map<Collection<Integer>, List<Integer>> valueKeysCanonicalizationCache = new HashMap<>();

    /**
     * The only available constructor expects a list of {@link UkkonenSuffixTreeInput} without any null values.
     *
     * @param input list
     */
    public GenericUkkonenSuffixTree_v2(Collection<UkkonenSuffixTreeInput<T>> input) {
        constructTree(input);
        this.toString = String.format("GenericUkkonenSuffixTree[total-nodes-count=`%d`, text.length=`%d`, valueCache.size()=`%s`, nodesPerChar=`%.2f`, total-node-value-lists=`%d`]", nodesCount, textSize, valueCache.size(), (float) nodesCount / textSize, valueKeysCanonicalizationCache.size());
        // dereference cache after it has served its purpose
        valueKeysCanonicalizationCache = Collections.emptyMap();
    }

    @Override
    public Collection<T> findAllOccurrences(String word) {
        UkkonenSTNode tmpNode = findSuffixNode(word, this.root);
        if (tmpNode == root) {
            return Collections.emptyList();
        }
        return tmpNode.values.stream().map(valueCache::get).toList();
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
                    sb.append("`").append(child.values.size()).append("`")
                            .append(child.values);
                    if (child.suffixLink != null) {
                        sb.append("->");
                        if (child.suffixLink == root) {
                            sb.append("[root]").append("`").append(0).append("`")
                                    .append("[]");
                        } else {
                            final int suffixEnd = child.suffixLink.end;
                            for (int i = child.suffixLink.start; i < suffixEnd; i++) sb.append(child.suffixLink.source.charAt(i));
                            sb.append("`").append(child.suffixLink.values.size()).append("`")
                                    .append(child.suffixLink.values);
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

        for (UkkonenSuffixTreeInput<T> in : input) {
            final String key = in.key();
            final AtomicReference<UkkonenSTNode> activeLeaf = new AtomicReference<>(root);
            UkkonenSTNode suffix = root;
            valueCache.put(index, in.value());
            String text = "";
            // iterate over the string, one char at a time
            for (int i = 0; i < key.length(); i++) {
                // line 6
                int codePoint = key.codePointAt(i);
                text += new String(Character.toChars(codePoint));
                // line 7: update the tree with the new transitions due to this new char
                var active = update(suffix, text, key, i, index, activeLeaf);
                // line 8: make sure the active pair is canonical
                active = canonize(active.first(), active.second());

                suffix = active.first();
                text = active.second();

                if (Character.isSupplementaryCodePoint(codePoint)) {
                    i++;
                }
            }

            // link leaves' suffixes
            final UkkonenSTNode ref = activeLeaf.get();
            if (ref.suffixLink == null && ref != root && ref != suffix) {
                ref.suffixLink = suffix;
            }
            this.textSize += key.length();
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
            if (Character.isSupplementaryCodePoint(key)) {
                i++;
            }
            node = child;
        }
        return node;
    }

    private Pair<Boolean, UkkonenSTNode> testAndSplit(final UkkonenSTNode inputs, final String stringPart, final int t, final String input, final int index, final int value) {
        // find deepest
        var canonized = canonize(inputs, stringPart);
        UkkonenSTNode suffix = canonized.first();
        String str = canonized.second();

        if (!"".equals(str)) {
            UkkonenSTNode g = suffix.children.get(str.codePointAt(0));

            // After canonize, 'str' is the remainder of the string that could not be
            // traversed by following a full edge. We get the edge for the first
            // character of 'str'. If this edge doesn't exist, it means we have a
            // mismatch and a new edge needs to be created. Return false to signal this.
            if (g == null) {
                return new Pair<>(false, suffix);
            }
            // must see whether "str" is substring of the label of an edge
            if (g.length() > str.length() && g.codePointAt(str.length()) == t) {
                return new Pair<>(true, suffix);
            } else {
                // build a new node
                final UkkonenSTNode newNode = new UkkonenSTNode(str);

                g.setLabel(g.source, g.start + str.length(), g.end);

                // link s -> r
                newNode.children.put(g.codePointAt(0), g);
                suffix.children.put(str.codePointAt(0), newNode);

                return new Pair<>(false, newNode);
            }

        } else {
            final UkkonenSTNode suffixOfSuffix = suffix.children.get(t);
            if (null == suffixOfSuffix) {
                // no path to suffix
                return new Pair<>(false, suffix);
            } else {
                int remainderLength = input.length() - index;

                if (remainderLength == suffixOfSuffix.length() && suffixOfSuffix.regionMatches(input, index, remainderLength)) {
                    // update payload of destination node
                    suffixOfSuffix.addValue(value);
                    return new Pair<>(true, suffix);
                } else if (remainderLength > suffixOfSuffix.length() && suffixOfSuffix.regionMatches(input, index, suffixOfSuffix.length())) {
                    return new Pair<>(true, suffix);
                } else if (suffixOfSuffix.length() > remainderLength && suffixOfSuffix.regionMatches(input, index, remainderLength)) {
                    // need to split as above

                    UkkonenSTNode newEdge = new UkkonenSTNode(input, index, input.length());
                    newEdge.addValue(value);

                    suffixOfSuffix.setLabel(suffixOfSuffix.source, suffixOfSuffix.start + remainderLength, suffixOfSuffix.end);

                    newEdge.children.put(suffixOfSuffix.codePointAt(0), suffixOfSuffix);

                    suffix.children.put(t, newEdge);

                    return new Pair<>(false, suffix);
                } else {
                    // common string
                    return new Pair<>(true, suffix);
                }
            }
        }

    }

    /**
     * Accoring to Ukkonen thie end
     * @param suffix
     * @param str
     * @return
     */
    private Pair<UkkonenSTNode, String> canonize(UkkonenSTNode suffix, String str) {

        if ("".equals(str)) return new Pair<>(suffix, str);

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

    private Pair<UkkonenSTNode, String> update(UkkonenSTNode suffix, String stringPart, String input, int index, int value, AtomicReference<UkkonenSTNode> activeLeaf) {
        String tempstr = stringPart;
        int newChar = stringPart.codePointBefore(stringPart.length());

        // oldr ← root; (end–point, r)
        UkkonenSTNode oldroot = root;

        // ← test–and–split(s,(k, i − 1), ti);
        var ret = testAndSplit(suffix, stringPart.substring(0, stringPart.length() - Character.charCount(newChar)), newChar, input, index, value);

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

            // update suffix link for newly created leaf
            if (activeLeaf.get() != root) {
                activeLeaf.get().suffixLink = leaf;
            }
            activeLeaf.set(leaf);

            if (oldroot != root) {
                oldroot.suffixLink = transition;
            }

            oldroot = transition;

            if (null == suffix.suffixLink) {
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
        oldroot = root;

        return new Pair<>(suffix, tempstr);
    }

    private String dropLast(String seq) {
        if (seq.isEmpty()) {
            return "";
        }
        return seq.substring(0, seq.length() - Character.charCount(seq.codePointBefore(seq.length())));
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
                node.values.addAll(child.values);
            }

            node.postProcess();
        }

        // root doesn't need to hold values at all
        root.values = Collections.emptyList();
        root.suffixLink = null;
    }

    private record Pair<A, B>(A first, B second) {

    }

    private class UkkonenSTNode {

        private String source;
        private int start;
        private int end;
        private Collection<Integer> values = new LinkedHashSet<>();
        private Map<Integer, UkkonenSTNode> children;
        private UkkonenSTNode suffixLink;

        UkkonenSTNode() {
            children = new HashMap<>();
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

        String getLabel() {
            return source.substring(start, end);
        }

        void setLabel(String source, int start, int end) {
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

        boolean regionMatches(String other, int ooffset, int len) {
            return source.regionMatches(start, other, ooffset, len);
        }

        void addValue(int index) {
            if (values.contains(index)) return;
            values.add(index);

            // propagate to suffix links
            UkkonenSTNode iter = this.suffixLink;
            while (iter != null) {
                if (iter.values.contains(index)) {
                    break;
                }
                iter.addValue(index);
                iter = iter.suffixLink;
            }
        }

        /**
         * Dereferences unused pointers and swaps mutable collections with immutable ones
         */
        void postProcess() {
            // swap from set to list
            this.values = valueKeysCanonicalizationCache.computeIfAbsent(this.values, List::copyOf);
            children = Map.copyOf(children);
            // not needed because tree is immutable
            // this.suffixLink = null;
        }
    }
}