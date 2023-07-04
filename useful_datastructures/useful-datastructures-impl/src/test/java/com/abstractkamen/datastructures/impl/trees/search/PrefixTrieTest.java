package com.abstractkamen.datastructures.impl.trees.search;

import com.abstractkamen.datastructures.api.trees.search.PrefixTrie;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;

import static org.junit.Assert.*;

public class PrefixTrieTest {

    @Test
    public void testInsert() {
        final PrefixTrie tree = new PrefixTrieImpl();
        assertTrue(tree.insert("cat"));
        assertTrue(tree.insert("car"));
        // already exists
        assertFalse(tree.insert("cat"));
        assertEquals(4, tree.size());
        assertEquals(2, tree.completeWords());
    }

    @Test
    public void testToString_returnsExpectedInLexicographicalOrder() {
        final Collection<String> expected = new TreeSet<>(List.of("app", "banana", "house",
                                                                  "hose", "apple", "car",
                                                                  "extremes", "extreme",
                                                                  "extremities"));
        final PrefixTrieImpl tree = new PrefixTrieImpl();
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
        final PrefixTrieImpl tree = new PrefixTrieImpl();
        list.forEach(tree::insert);
        assertEquals(Arrays.asList("extreme", "extremes", "extremities"), tree.startsWith("ex", Integer.MAX_VALUE));
    }

    @Test
    public void testContains() {
        final Collection<String> list = List.of("app", "banana", "house",
                                                "hose", "apple", "car",
                                                "extremes", "extreme",
                                                "extremities");
        final PrefixTrieImpl trie = new PrefixTrieImpl();
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
        final PrefixTrieImpl trie = new PrefixTrieImpl();
        list.forEach(trie::insert);
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
        // suffixes
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
        // not existing
        assertFalse(trie.isPrefix("these"));
        assertFalse(trie.isPrefix("strings"));
        assertFalse(trie.isPrefix("don't"));
        assertFalse(trie.isPrefix("exist"));
    }

    @Test
    public void testDelete() {
        final PrefixTrie tree = new PrefixTrieImpl();
        tree.insert("app");
        tree.insert("apple");
        tree.insert("application");
        tree.insert("banana");
        assertEquals(18, tree.size());
        assertEquals(4, tree.completeWords());
        assertTrue(tree.delete("apple"));
        assertFalse(tree.delete("orange"));
        assertEquals(17, tree.size());
        assertEquals(3, tree.completeWords());
        final Collection<String> startsWithResult = tree.startsWith("app", Integer.MAX_VALUE);
        assertEquals(startsWithResult, List.of("app", "application"));
        tree.insert("cat");
        tree.insert("car");
        assertEquals(21, tree.size());
        assertEquals(5, tree.completeWords());
        assertTrue(tree.delete("cat"));
        assertEquals(20, tree.size());
        assertEquals(4, tree.completeWords());
        assertTrue(tree.delete("app"));
        assertEquals(20, tree.size());
        assertEquals(3, tree.completeWords());
        System.out.println(tree.prettyString());
        assertTrue(tree.delete("application"));
        System.out.println(tree.prettyString());
        assertEquals(9, tree.size());
        assertEquals(2, tree.completeWords());
        assertTrue(tree.delete("banana"));
        System.out.println(tree.prettyString());
        assertEquals(3, tree.size());
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
        PrefixTrie trie = new PrefixTrieImpl();
        trie.insert("apple");
        trie.insert("banana");
        trie.insert("application");

        Collection<String> startsWithResult = trie.startsWith("app", Integer.MAX_VALUE);
        assertEquals(2, startsWithResult.size());
        assertTrue(startsWithResult.contains("apple"));
        assertTrue(startsWithResult.contains("application"));
    }

    @Test
    public void testInsertDuplicate() {
        final PrefixTrie trie = new PrefixTrieImpl();
        trie.insert("apple");
        assertFalse(trie.insert("apple"));
    }

    @Test
    public void testSizeAndCompleteWords() {
        final PrefixTrie trie = new PrefixTrieImpl();
        trie.insert("apple");
        trie.insert("banana");
        trie.insert("application");
        assertEquals(18, trie.size());
        assertEquals(3, trie.completeWords());
        trie.delete("apple");
        assertEquals(17, trie.size());
        assertEquals(2, trie.completeWords());
    }
}
