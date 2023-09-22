package com.abstractkamen.datastructures.impl.trees.search;

import org.junit.*;

import java.util.*;
import java.util.function.*;

import static org.junit.Assert.*;

public class AvlTreeTest {
    private AvlTree<Integer> tree;

    @Before
    public void init() {
        tree = AvlTree.createComparable();
    }

    @Test
    public void testTree_withDisallowedDuplicates() {
        tree = AvlTree.createComparable(false);
        tree.add(10);
        tree.add(5);
        tree.add(15);
        tree.add(3);

        tree.add(7);
        tree.add(7);
        tree.add(7);
        tree.add(7);

        tree.add(13);
        tree.add(17);
        assertEquals(7, tree.size());
        assertEquals(1, tree.containsCount(7));
    }

    @Test
    public void givenTree_lesserShouldReturnExpected() {
        tree.add(10);
        tree.add(5);
        tree.add(15);
        tree.add(3);
        tree.add(7);
        tree.add(13);
        tree.add(17);
        assertEquals(3, (int) tree.lesser(4));
        assertEquals(3, (int) tree.lesser(5));
        assertEquals(5, (int) tree.lesser(6));
        assertEquals(5, (int) tree.lesser(7));
        assertEquals(7, (int) tree.lesser(8));
        assertEquals(7, (int) tree.lesser(9));
        assertEquals(7, (int) tree.lesser(10));
        assertEquals(10, (int) tree.lesser(11));
        assertEquals(10, (int) tree.lesser(12));
        assertEquals(10, (int) tree.lesser(13));
        assertEquals(13, (int) tree.lesser(14));
        assertEquals(13, (int) tree.lesser(15));
        assertEquals(15, (int) tree.lesser(16));
    }

    @Test
    public void givenTree_greaterShouldReturnNull_WhenItemLesserThanOrEqualToMin() {
        tree.add(10);
        tree.add(5);
        tree.add(15);
        tree.add(3);
        tree.add(7);
        tree.add(13);
        tree.add(17);
        int min = tree.min();
        assertEquals(3, min);
        for (int i = min; i > -10; --i) {
            assertNull(tree.lesser(i));
        }
    }

    @Test
    public void givenEmptyTree_lesserShouldReturnNull() {
        assertNull(tree.lesser(10));
    }

    @Test(expected = NullPointerException.class)
    public void givenNull_lesserShouldThrowExpected() {
        final AvlTree<Integer> avlTree = AvlTree.createComparable();
        avlTree.add(1);
        avlTree.lesser(null);
    }

    @Test
    public void givenNull_lesserShouldNotThrow_whenEmpty() {
        final AvlTree<Integer> avlTree = AvlTree.createComparable();
        final Integer actual = avlTree.lesser(null);
        assertNull(actual);
    }

    @Test
    public void givenTree_greaterShouldReturnExpected() {
        tree.add(10);
        tree.add(5);
        tree.add(15);
        tree.add(3);
        tree.add(7);
        tree.add(13);
        tree.add(17);
        assertEquals(10, (int) tree.greater(7));
        assertEquals(10, (int) tree.greater(8));
        assertEquals(10, (int) tree.greater(9));
        assertEquals(13, (int) tree.greater(10));
        assertEquals(13, (int) tree.greater(11));
        assertEquals(13, (int) tree.greater(12));
        assertEquals(15, (int) tree.greater(13));
        assertEquals(15, (int) tree.greater(14));
        assertEquals(17, (int) tree.greater(15));
        assertEquals(17, (int) tree.greater(16));
    }

    @Test
    public void givenTree_greaterShouldReturnNull_WhenItemGreaterThanOrEqualToMax() {
        tree.add(10);
        tree.add(5);
        tree.add(15);
        tree.add(3);
        tree.add(7);
        tree.add(13);
        tree.add(17);
        int max = tree.max();
        assertEquals(17, max);
        for (int i = max; i < 10; i++) {
            assertNull(tree.greater(i));
        }
    }

    @Test
    public void givenEmptyTree_greaterShouldReturnNull() {
        assertNull(tree.greater(10));
    }

    @Test(expected = NullPointerException.class)
    public void givenNull_greaterShouldThrowExpected_whenNotEmpty() {
        final AvlTree<Integer> avlTree = AvlTree.createComparable();
        avlTree.add(1);
        avlTree.greater(null);
    }

    @Test
    public void givenNull_greaterShouldNotThrow_whenEmpty() {
        final AvlTree<Integer> avlTree = AvlTree.createComparable();
        final Integer actual = avlTree.greater(null);
        assertNull(actual);
    }


    @Test
    public void testMax() {
        tree.add(10);
        tree.add(5);
        tree.add(15);
        tree.add(3);
        tree.add(7);
        tree.add(13);
        tree.add(17);
        assertEquals(17, tree.max().intValue());
    }

    @Test
    public void testMin() {
        tree.add(10);
        tree.add(5);
        tree.add(15);
        tree.add(3);
        tree.add(7);
        tree.add(13);
        tree.add(17);
        assertEquals(3, tree.min().intValue());
    }

    @Test
    public void testInsert() {
        tree.add(10);
        tree.add(5);
        tree.add(15);
        tree.add(3);
        tree.add(7);
        tree.add(13);
        tree.add(17);
        assertEquals("[3, 5, 7, 10, 13, 15, 17]", tree.toString());
    }

    @Test
    public void testContains() {
        tree.add(10);
        tree.add(5);
        tree.add(15);
        tree.add(3);
        tree.add(7);
        tree.add(13);
        tree.add(17);
        assertTrue(tree.contains(7));
        assertFalse(tree.contains(20));
    }

    @Test
    public void testRemove() {
        tree.add(10);
        tree.add(5);
        tree.add(15);
        tree.add(3);
        tree.add(7);
        tree.add(13);
        tree.add(17);
        tree.remove(7);
        tree.remove(15);
        assertEquals("[3, 5, 10, 13, 17]", tree.toString());
    }

    @Test
    public void testSize() {
        tree.add(10);
        tree.remove(1);
        tree.remove(2);
        tree.remove(3);
        assertEquals(1, tree.size());
        tree.remove(10);
        assertEquals(0, tree.size());
        tree.add(10);
        assertEquals(1, tree.size());
        tree.add(5);
        assertEquals(2, tree.size());
        tree.add(15);
        assertEquals(3, tree.size());
        tree.add(3);
        assertEquals(4, tree.size());
        tree.add(7);
        assertEquals(5, tree.size());
        tree.add(13);
        assertEquals(6, tree.size());
        tree.add(17);
        assertEquals(7, tree.size());
    }

    @Test
    public void test_isSize_withDuplicate() {
        AvlTree<String> tree = AvlTree.createComparable();
        assertEquals(0, tree.size());
        tree.add("one");
        assertEquals(1, tree.size());
        tree.add("one");
        assertEquals(2, tree.size());
        tree.remove("one");
        assertEquals(1, tree.size());
        tree.remove("one");
        assertEquals(0, tree.size());
    }

    @Test
    public void test_isEmpty() {
        AvlTree<String> tree = AvlTree.createComparable();
        assertTrue(tree.isEmpty());
        tree.add("one");
        assertFalse(tree.isEmpty());
        tree.add("one");
        tree.remove("one");
        assertFalse(tree.isEmpty());
        tree.remove("one");
        assertTrue(tree.isEmpty());
    }

    @Test
    public void testHeight() {
        assertEquals(-1, tree.height());
        System.out.println(tree.prettyString());
        tree.add(10);
        System.out.println(tree.prettyString());
        assertEquals(0, tree.height());
        tree.add(5);
        System.out.println(tree.prettyString());
        assertEquals(1, tree.height());
        tree.add(15);
        System.out.println(tree.prettyString());
        assertEquals(1, tree.height());
        tree.add(3);
        System.out.println(tree.prettyString());
        assertEquals(2, tree.height());
        tree.add(7);
        System.out.println(tree.prettyString());
        assertEquals(2, tree.height());
        tree.add(13);
        System.out.println(tree.prettyString());
        assertEquals(2, tree.height());
        tree.add(17);
        System.out.println(tree.prettyString());
        assertEquals(2, tree.height());
    }

    @Test
    public void testClear() {
        tree.add(10);
        tree.add(5);
        tree.add(15);
        tree.clear();
        assertEquals("[]", tree.toString());
        assertEquals("empty tree", tree.prettyString());
        assertEquals(0, tree.size());
    }

    @Test
    public void testIterator_iteratesInOrder() {
        tree.add(10);
        tree.add(5);
        tree.add(15);
        tree.add(3);
        tree.add(7);
        tree.add(13);
        tree.add(17);

        List<Integer> list = new ArrayList<>();
        for (int integer : tree) {
            list.add(integer);
        }
        assertEquals("[3, 5, 7, 10, 13, 15, 17]", list.toString());
    }

    @Test
    public void testIterator_iteratesRemove() {
        tree.add(1);
        tree.add(2);
        tree.add(3);
        tree.add(4);
        tree.add(5);
        final Iterator<Integer> it = tree.iterator();
        tree.prettyString();

        final Supplier<String> printer = () -> {
            final String before = tree.toString();
            final int i = it.next();
            final int size = tree.size();
            it.remove();
            assertEquals(size - 1, tree.size());
            assertFalse(tree.contains(i));
            return String.format("%s - [%d] -> %s%n%s", before, i, tree, tree.prettyString());
        };
        printer.get();
        assertEquals(2, tree.height());
        printer.get();
        assertEquals(1, tree.height());
        printer.get();
        assertEquals(1, tree.height());
        printer.get();
        assertEquals(0, tree.height());
        printer.get();
        assertEquals(-1, tree.height());
    }

    @Test
    public void testContainsCount() {
        assertEquals(0, tree.containsCount(123));
        tree.add(1);
        tree.add(1);
        tree.add(2);
        tree.add(-1);
        assertEquals(0, tree.containsCount(123));
        assertEquals(2, tree.containsCount(1));
        tree.remove(1);
        assertEquals(1, tree.containsCount(1));
        tree.add(1);
        final Iterator<Integer> it = tree.iterator();
        assertEquals(2, tree.containsCount(1));
        it.next();
        it.next();
        it.remove();
        assertEquals(1, tree.containsCount(1));
        it.next();
        it.remove();
        assertEquals(0, tree.containsCount(1));
        assertEquals(2, tree.size());
        assertFalse(tree.isEmpty());
        assertEquals(1, tree.containsCount(-1));
        assertEquals(1, tree.containsCount(2));
        tree.remove(-1);
        tree.remove(2);
        assertEquals(0, tree.size());
        assertTrue(tree.isEmpty());
        assertEquals(0, tree.containsCount(-1));
        assertEquals(0, tree.containsCount(2));
    }

    @Test(expected = NoSuchElementException.class)
    public void iteratorThrows_WhenTheyDontHaveNext() {
        // arrange
        // act
        tree.iterator().next();
        // assert
    }

    @Test(expected = NoSuchElementException.class)
    public void descendingIteratorThrows_WhenTheyDontHaveNext() {
        // arrange
        // act
        tree.descendingIterator().next();
        // assert
    }

    @Test
    public void testDescendingIterator_iteratesRemove() {
        tree.add(1);
        tree.add(2);
        tree.add(3);
        tree.add(4);
        tree.add(5);
        final Iterator<Integer> it = tree.descendingIterator();
        final Consumer<Object> printer = o -> {
            final String before = tree.toString();
            final int i = it.next();
            final int size = tree.size();
            it.remove();
            assertEquals(size - 1, tree.size());
            assertFalse(tree.contains(i));
            System.out.println(String.format("%s - [%d] -> %s%n%s", before, i, tree, tree.prettyString()));
        };
        printer.accept(null);
        assertEquals(2, tree.height());
        printer.accept(null);
        assertEquals(1, tree.height());
        printer.accept(null);
        assertEquals(1, tree.height());
        printer.accept(null);
        assertEquals(0, tree.height());
        printer.accept(null);
        assertEquals(-1, tree.height());
    }

    @Test
    public void testIterator_iteratesRemove_negatives() {
        tree.add(-1);
        tree.add(-2);
        tree.add(-3);
        tree.add(-4);
        tree.add(-5);
        final Iterator<Integer> it = tree.iterator();
        tree.prettyString();

        final Supplier<String> printer = () -> {
            final String before = tree.toString();
            final int i = it.next();
            final int size = tree.size();
            it.remove();
            assertEquals(size - 1, tree.size());
            assertFalse(tree.contains(i));
            return String.format("%s - [%d] -> %s%n%s", before, i, tree, tree.prettyString());
        };
        printer.get();
        assertEquals(2, tree.height());
        printer.get();
        assertEquals(1, tree.height());
        printer.get();
        assertEquals(1, tree.height());
        printer.get();
        assertEquals(0, tree.height());
        printer.get();
        assertEquals(-1, tree.height());
    }

    @Test
    public void testDescendingIterator_iteratesRemove_negatives() {
        tree.add(-1);
        tree.add(-2);
        tree.add(-3);
        tree.add(-4);
        tree.add(-5);
        final Iterator<Integer> it = tree.descendingIterator();
        tree.prettyString();

        final Supplier<String> printer = () -> {
            final String before = tree.toString();
            final int i = it.next();
            final int size = tree.size();
            it.remove();
            assertEquals(size - 1, tree.size());
            assertFalse(tree.contains(i));
            return String.format("%s - [%d] -> %s%n%s", before, i, tree, tree.prettyString());
        };
        printer.get();
        assertEquals(2, tree.height());
        printer.get();
        assertEquals(1, tree.height());
        printer.get();
        assertEquals(1, tree.height());
        printer.get();
        assertEquals(0, tree.height());
        printer.get();
        assertEquals(-1, tree.height());

    }

    @Test(expected = NoSuchElementException.class)
    public void testIterator_iteratesRemoveThrows_WhenCalledTwice() {
        tree.add(10);
        tree.add(5);
        tree.add(15);
        tree.add(3);
        tree.add(7);
        tree.add(13);
        tree.add(17);

        final Iterator<Integer> it = tree.iterator();
        it.next();
        it.next();
        it.next();
        it.next();
        it.remove();
        it.remove();
    }

    @Test(expected = NoSuchElementException.class)
    public void testIterator_iteratesRemoveThrows_WhenEmpty() {
        AvlTree.createComparable().iterator().remove();
    }

    @Test
    public void testBalance_expectSearchToBeFast_InsertNotSoMuch() {
        final int expectedMax = 1000;
        final int expectedMin = 0;
        final long addBegin = System.currentTimeMillis();
        for (int i = expectedMin; i <= expectedMax; i++) {
            tree.add(i);
        }
        final double insertTime = (System.currentTimeMillis() - addBegin) / 1_000f;
        System.out.printf("Added[%d] time:%.3fms%n", expectedMax, insertTime);
        final long maxBegin = System.currentTimeMillis();
        final Integer max = tree.max();
        final double maxTime = (System.currentTimeMillis() - maxBegin) / 1_000f;
        System.out.printf("Max[%d] time: %.3fms%n", max, maxTime);
        assertEquals(expectedMax, max.intValue());

        final long minBegin = System.currentTimeMillis();
        final Integer min = tree.min();
        final double minTime = (System.currentTimeMillis() - minBegin) / 1_000f;
        System.out.printf("Min[%d] time: %.3fms%n", min, minTime);
        assertEquals(expectedMax, max.intValue());
    }

}