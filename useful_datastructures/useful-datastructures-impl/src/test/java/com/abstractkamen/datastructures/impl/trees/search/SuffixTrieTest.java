package com.abstractkamen.datastructures.impl.trees.search;

import com.abstractkamen.datastructures.api.trees.search.SuffixTrie;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

public class SuffixTrieTest {

    @Test
    public void testInsert() {
        final SuffixTrie tree = new SuffixTrieReverseImpl();
        assertTrue(tree.insert("cat"));
        assertTrue(tree.insert("car"));
        // already exists
        assertFalse(tree.insert("cat"));
        assertEquals(10, tree.size());
        assertEquals(2, tree.completeWords());
    }

    @Test
    public void testToString_returnsExpectedInLexicographicalOrder() {
        final Collection<String> expected = new TreeSet<>(List.of("app", "banana", "house",
                                                                  "hose", "apple", "car",
                                                                  "extremes", "extreme",
                                                                  "extremities"));
        final SuffixTrieReverseImpl tree = new SuffixTrieReverseImpl();
        expected.forEach(tree::insert);
        assertEquals(expected.toString(), tree.toString());
        System.out.println(tree.prettyString());
    }

    @Test
    public void testStartsWith_returnsExpectedInLexicographicalOrder() {
        final Collection<String> list = List.of("app", "banana", "house",
                                                "hose", "apple", "car",
                                                "extremes", "extreme",
                                                "extremities");
        final SuffixTrieReverseImpl tree = new SuffixTrieReverseImpl();
        list.forEach(tree::insert);
        assertEquals(Arrays.asList("extreme", "extremes", "extremities"), tree.startsWith("ex", Integer.MAX_VALUE));
    }

    @Test
    public void testContains() {
        final Collection<String> list = List.of("app", "banana", "house",
                                                "hose", "apple", "car",
                                                "extremes", "extreme",
                                                "extremities");
        final SuffixTrieReverseImpl trie = new SuffixTrieReverseImpl();
        list.forEach(trie::insert);
        assertTrue(trie.contains("app"));
        assertTrue(trie.contains("hose"));
        assertTrue(trie.contains("apple"));
        assertTrue(trie.contains("extremes"));
        assertTrue(trie.contains("extremities"));
        assertTrue(trie.contains("house"));

        assertFalse(trie.contains("aapp"));
        assertFalse(trie.contains("hhose"));
        assertFalse(trie.contains("applee"));
        assertFalse(trie.contains("extremess"));
        assertFalse(trie.contains("extremmities"));
        assertFalse(trie.contains("houoioise"));
    }

    @Test
    public void testIsSubstring() {
        final Collection<String> list = List.of("app", "banana", "house",
                                                "hose", "apple", "car",
                                                "extremes", "extreme",
                                                "extremities");
        final SuffixTrieReverseImpl trie = new SuffixTrieReverseImpl();
        list.forEach(trie::insert);
        // prefixes
        assertTrue(trie.isPrefix("extremities"));
        assertTrue(trie.isPrefix("extremitie"));
        assertTrue(trie.isPrefix("extremiti"));
        assertTrue(trie.isPrefix("extremit"));
        assertTrue(trie.isPrefix("extremi"));
        assertTrue(trie.isPrefix("extrem"));
        assertTrue(trie.isPrefix("extre"));
        assertTrue(trie.isPrefix("extr"));
        assertTrue(trie.isPrefix("ext"));
        assertTrue(trie.isPrefix("ex"));
        assertTrue(trie.isPrefix("e"));

        assertFalse(trie.isSuffix("extremitie"));
        assertFalse(trie.isSuffix("extremiti"));
        assertFalse(trie.isSuffix("extremit"));
        assertFalse(trie.isSuffix("extremi"));
        assertFalse(trie.isSuffix("extrem"));
        assertFalse(trie.isSuffix("extre"));
        assertFalse(trie.isSuffix("extr"));
        assertFalse(trie.isSuffix("ext"));
        assertFalse(trie.isSuffix("ex"));

        assertFalse(trie.isPrefix("xtremities"));
        assertFalse(trie.isPrefix("tremities"));
        assertFalse(trie.isPrefix("remities"));
        assertFalse(trie.isPrefix("emities"));
        assertFalse(trie.isPrefix("mities"));
        assertFalse(trie.isPrefix("ities"));
        assertFalse(trie.isPrefix("ties"));
        assertFalse(trie.isPrefix("ies"));
        assertFalse(trie.isPrefix("es"));
        assertFalse(trie.isPrefix("s"));
        // suffixes
        assertTrue(trie.isSuffix("extremities"));
        assertTrue(trie.isSuffix("xtremities"));
        assertTrue(trie.isSuffix("tremities"));
        assertTrue(trie.isSuffix("remities"));
        assertTrue(trie.isSuffix("emities"));
        assertTrue(trie.isSuffix("mities"));
        assertTrue(trie.isSuffix("ities"));
        assertTrue(trie.isSuffix("ties"));
        assertTrue(trie.isSuffix("ies"));
        assertTrue(trie.isSuffix("es"));
        assertTrue(trie.isSuffix("s"));
        // not existing
        assertFalse(trie.isPrefix("these"));
        assertFalse(trie.isPrefix("strings"));
        assertFalse(trie.isPrefix("don't"));
        assertFalse(trie.isPrefix("exist"));
    }

    @Test
    public void testDelete() {
        final SuffixTrie tree = new SuffixTrieReverseImpl();
        tree.insert("app");
        tree.insert("apple");
        tree.insert("application");
        tree.insert("banana");
        assertEquals(42, tree.size());
        assertEquals(4, tree.completeWords());
        assertTrue(tree.delete("apple"));
        assertFalse(tree.delete("orange"));
        assertEquals(36, tree.size());
        assertEquals(3, tree.completeWords());
        final Collection<String> startsWithResult = tree.startsWith("app", Integer.MAX_VALUE);
        assertEquals(startsWithResult, List.of("app", "application"));
        tree.insert("cat");
        tree.insert("car");
        assertEquals(46, tree.size());
        assertEquals(5, tree.completeWords());
        assertTrue(tree.delete("cat"));
        assertEquals(42, tree.size());
        assertEquals(4, tree.completeWords());
        assertTrue(tree.delete("app"));
        assertEquals(39, tree.size());
        assertEquals(3, tree.completeWords());
        System.out.println(tree.prettyString());
        assertTrue(tree.delete("application"));
        System.out.println(tree.prettyString());
        assertEquals(18, tree.size());
        assertEquals(2, tree.completeWords());
        assertTrue(tree.delete("banana"));
        System.out.println(tree.prettyString());
        assertEquals(6, tree.size());
        assertEquals(1, tree.completeWords());
        assertTrue(tree.delete("car"));
        final String actualEmpty = tree.prettyString();
        assertEquals("empty tree", actualEmpty);
        System.out.println(actualEmpty);
        assertEquals(0, tree.size());
        assertEquals(0, tree.completeWords());
    }

    @Test
    public void testInsertAndStartsWith() {
        SuffixTrie trie = new SuffixTrieReverseImpl();
        trie.insert("apple");
        trie.insert("banana");
        trie.insert("application");

        Collection<String> startsWithResult = trie.startsWith("app", Integer.MAX_VALUE);
        assertEquals(2, startsWithResult.size());
        assertTrue(startsWithResult.contains("apple"));
        assertTrue(startsWithResult.contains("application"));
    }

    @Test
    public void testInsertAndEndsWith() {
        SuffixTrie trie = new SuffixTrieReverseImpl();
        final Collection<String> expected = new LinkedList<>();
        expected.add("complication");
        expected.add("application");
        expected.add("communication");
        expected.add("generation");
        expected.add("situation");
        expected.forEach(trie::insert);
        trie.insert("apple");
        trie.insert("banana");

        Collection<String> startsWithResult = trie.endsWith("ation", Integer.MAX_VALUE);
        assertEquals(expected.size(), startsWithResult.size());
        assertEquals(expected.toString(), startsWithResult.toString());
    }

    @Test
    public void testInsertDuplicate() {
        final SuffixTrie trie = new SuffixTrieReverseImpl();
        trie.insert("apple");
        assertFalse(trie.insert("apple"));
    }

    @Test
    public void testSizeAndCompleteWords() {
        final SuffixTrie trie = new SuffixTrieReverseImpl();
        trie.insert("apple");
        trie.insert("banana");
        trie.insert("application");
        assertEquals(39, trie.size());
        assertEquals(3, trie.completeWords());
        trie.delete("apple");
        assertEquals(33, trie.size());
        assertEquals(2, trie.completeWords());
    }
}
