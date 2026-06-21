package com.abstractkamen.datastructures.impl.trees.search;

import com.abstractkamen.datastructures.api.trees.search.ByteArraySuffixTree;
import jdk.internal.util.ArraysSupport;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * The byte[] Generic Suffix Tree implementation using Ukkonen's Algorithm
 * <p> Useful links:
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
public class GenericUkkonenByteArraySuffixTree<T> implements ByteArraySuffixTree<T> {

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
  private int longestByteArray;

  /**
   * The only available constructor expects a list of {@link UkkonenSuffixTreeInput} without any null values.
   *
   * @param input list
   */
  public GenericUkkonenByteArraySuffixTree(Collection<UkkonenByteArraySuffixTreeInput<T>> input) {
	constructTree(input);
	this.toString = String.format("GenericUkkonenByteArraySuffixTree[total-nodes-count=`%d`, text.length=`%d`, valueCache.size()=`%s`, nodesPerChar=`%.2f`, total-node-value-lists=`%d`, total-values=`%d`]", nodesCount, textSize, valueCache.size(), (float) nodesCount / textSize, valueKeysCanonicalizationCache.size(), valueKeysCanonicalizationCache.values().stream().mapToInt(Collection::size).reduce(0, Integer::sum));
	// dereference caches after tree is constructed
	this.valueKeysCanonicalizationCache = Collections.emptyMap();
	this.valueCache = Collections.emptyMap();
  }

  @Override
  public Collection<T> findAllOccurrences(byte[] pattern) {
	if (pattern == null) return Collections.emptyList();
	final UkkonenSTNode node = findSuffixNode(pattern, this.root);
	if (this.root == node) return Collections.emptyList();
	return node.actualValues;
  }

  @Override
  public boolean contains(byte[] pattern) {
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
		final ByteBuffer buffer = ByteBuffer.allocate(longestByteArray);
		for (var e : node.children.entrySet()) {
		  final UkkonenSTNode child = e.getValue();
		  int start = child.start;
		  int end = child.end;
		  final StringBuilder sb = new StringBuilder("-".repeat(indent));
		  for (int i = start; i < end; i++) buffer.put(child.source[i]);
		  sb.append(new String(Arrays.copyOf(buffer.array(), buffer.position())));
		  buffer.clear();

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
			  for (int i = child.suffixLink.start; i < suffixEnd; i++) buffer.put(child.suffixLink.source[i]);
			  sb.append(new String(Arrays.copyOf(buffer.array(), buffer.position())));
			  buffer.clear();

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

  private void constructTree(Collection<UkkonenByteArraySuffixTreeInput<T>> input) {
	int index = 0;
	if (input.isEmpty()) return;

	final int capacity = 64;
	ByteBuffer prefix = ByteBuffer.allocate(capacity);
	for (var in : input) {
	  final byte[] key = in.key();
	  longestByteArray = Math.max(longestByteArray, key.length);
	  final AtomicReference<UkkonenSTNode> activeLeaf = new AtomicReference<>(root);
	  UkkonenSTNode suffix = root;
	  valueCache.put(index, in.value());

	  for (int i = 0; i < key.length; i++) {
		byte codePoint = key[i];
		prefix = doublePrefixIfNeeded(prefix, 1);
		prefix.put(codePoint);
		final byte[] bytes = Arrays.copyOf(prefix.array(), prefix.position());

		var activeNode = update(suffix, bytes, key, i, index, activeLeaf);
		// deepest possible suffix
		activeNode = canonize(activeNode.first(), activeNode.second());

		suffix = activeNode.first();
		prefix.clear();
		prefix = doublePrefixIfNeeded(prefix, activeNode.second().length);
		prefix.put(activeNode.second());
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

  private UkkonenSTNode findSuffixNode(byte[] pattern, UkkonenSTNode root) {
	final int patternLen = pattern.length;
	UkkonenSTNode node = root;
	int i = 0;
	while (i < patternLen) {
	  final byte key = pattern[i];
	  final UkkonenSTNode child = node.children.get(key);
	  if (child == null) {
		return root;
	  }
	  int len = Math.min(child.length(), patternLen - i);
	  int mismatchI = ArraysSupport.mismatch(pattern, i, child.source, child.start, len);
	  if (mismatchI >= 0) return root;
	  i += len;
	  node = child;
	}
	return node;
  }

  private Pair<Boolean, UkkonenSTNode> testAndSplit(UkkonenSTNode currentNode, byte[] prefix, byte t, byte[] input, int index, int value) {
	// find deepest
	var canonized = canonize(currentNode, prefix);
	UkkonenSTNode deepestSuffix = canonized.first();
	byte[] deepestSuffixString = canonized.second();

	if (deepestSuffixString.length > 0) {
	  UkkonenSTNode nextSuffix = deepestSuffix.children.get(deepestSuffixString[0]);
	  // mismatch on current path
	  if (nextSuffix == null) {
		return new Pair<>(false, deepestSuffix);
	  }
	  // check if deepest suffix still matches
	  if (nextSuffix.length() > deepestSuffixString.length && nextSuffix.byteAt(deepestSuffixString.length) == t) {
		return new Pair<>(true, deepestSuffix);
	  } else {
		// build a new node
		final UkkonenSTNode newNode = new UkkonenSTNode(deepestSuffixString);
		// drop prefix of current node source string but keep original
		nextSuffix.start += deepestSuffixString.length;

		// link s -> r
		newNode.children.put(nextSuffix.byteAt(0), nextSuffix);
		deepestSuffix.children.put(deepestSuffixString[0], newNode);

		return new Pair<>(false, newNode);
	  }

	} else {
	  final UkkonenSTNode next = deepestSuffix.children.get(t);
	  if (next == null) {
		// no path to deepestSuffix
		return new Pair<>(false, deepestSuffix);
	  } else {
		int remainder = input.length - index;

		if (remainder == next.length() && next.matches(input, index, remainder)) {
		  // found a path just in time -> add current value to found
		  next.addValue(value);
		  return new Pair<>(true, deepestSuffix);
		} else if (remainder > next.length() && next.matches(input, index, next.length())) {
		  return new Pair<>(true, deepestSuffix);
		} else if (next.length() > remainder && next.matches(input, index, remainder)) {
		  // split current
		  UkkonenSTNode newNode = new UkkonenSTNode(input, index, input.length);
		  newNode.addValue(value);
		  // drop prefix of current node source string but keep original
		  next.start += remainder;
		  newNode.children.put(next.byteAt(0), next);
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
  private Pair<UkkonenSTNode, byte[]> canonize(UkkonenSTNode suffix, byte[] str) {

	if (str.length == 0) return new Pair<>(suffix, str);

	int i = 0;
	UkkonenSTNode nextSuffix = suffix.children.get(str[i]);
	while (nextSuffix != null && (str.length - i) >= nextSuffix.length()) {
	  i += nextSuffix.length();
	  suffix = nextSuffix;
	  if (i < str.length) {
		nextSuffix = suffix.children.get(str[i]);
	  } else {
		nextSuffix = null;
	  }
	}

	return new Pair<>(suffix, dropFirstN(str, i));
  }

  private Pair<UkkonenSTNode, byte[]> update(UkkonenSTNode suffix, byte[] prefix, byte[] input, int index, int value, AtomicReference<UkkonenSTNode> activeLeaf) {
	byte[] tempstr = prefix;
	byte newChar = prefix[prefix.length - 1];

	// oldr ← root; (end–point, r)
	UkkonenSTNode oldroot = root;

	// ← test–and–split(s,(k, i − 1), ti);
	var ret = testAndSplit(suffix, dropLast(prefix), newChar, input, index, value);

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
		leaf = new UkkonenSTNode(input, index, input.length);
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
		if (tempstr.length > 0) {
		  tempstr = dropFirst(tempstr);
		}
	  } else {
		final var canonized = canonize(suffix.suffixLink, dropLast(tempstr));
		suffix = canonized.first();
		tempstr = combine(canonized.second(), tempstr[tempstr.length - 1]);
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

  private static ByteBuffer doublePrefixIfNeeded(ByteBuffer prefix, int toFit) {
	final int p = prefix.position();
	if (p + toFit >= prefix.capacity()) {
	  return ByteBuffer.allocate(prefix.capacity() * 2)
			  .put(prefix.array())
			  .position(p);
	}
	return prefix;
  }

  private static byte[] combine(byte[] a, byte b) {
	return ByteBuffer.wrap(new byte[a.length + 1])
			.put(a)
			.put(b)
			.array();
  }

  private static byte[] dropLast(byte[] seq) {
	if (seq.length == 0) return seq;
	return ByteBuffer.wrap(new byte[seq.length - 1])
			.put(seq, 0, seq.length - 1)
			.array();
  }

  private static byte[] dropFirst(byte[] seq) {
	return dropFirstN(seq, 1);
  }

  private static byte[] dropFirstN(byte[] seq, int n) {
	if (seq.length == 0) return seq;
	return ByteBuffer.wrap(new byte[seq.length - n])
			.put(seq, n, seq.length - n)
			.array();
  }

  private record Pair<A, B>(A first, B second) {
  }

  private class UkkonenSTNode {

	byte[] source;
	int start;
	int end;

	/**
	 * This collection holds the index of a key source. After tree is constructed it is mapped to actual input value.
	 */
	Collection<Integer> keyIndexes = new TreeSet<>();
	Map<Byte, UkkonenSTNode> children = new HashMap<>();
	UkkonenSTNode suffixLink;
	/**
	 * Upon tree construction this collection will hold actual values in input order
	 */
	Collection<T> actualValues = Collections.emptyList();

	UkkonenSTNode() {
	  // tree prop
	  nodesCount++;
	}

	UkkonenSTNode(byte[] label) {
	  this();
	  this.source = label;
	  this.start = 0;
	  this.end = label.length;
	}

	UkkonenSTNode(byte[] source, int start, int end) {
	  this();
	  this.source = source;
	  this.start = start;
	  this.end = end;
	}

	int length() {
	  return end - start;
	}

	byte byteAt(int index) {
	  return source[start + index];
	}

	boolean matches(byte[] other, int offset, int len) {
	  // same as what String.matches uses
	  return ArraysSupport.mismatch(source, start,
			  other, offset, len) < 0;
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
		textSize += this.source.length;
	  }
	}
  }
}