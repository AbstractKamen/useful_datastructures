package com.abstractkamen.datastructures.impl.trees.search.demos;

import com.abstractkamen.datastructures.api.trees.search.BinarySearchTree;
import com.abstractkamen.datastructures.impl.trees.search.AvlTree;

import java.util.*;

/**
 * Demo class showing AvlTree features and behaviour.
 */
public class AvlTreeDemo {

    public static void main(String[] args) {
        final BinarySearchTree<Integer> avlTree = AvlTree.createComparable();
        final TreeSet<Integer> treeSet = new TreeSet<>();
        demoCurrentState(avlTree, treeSet);
        demoAdd(avlTree, treeSet, 13, 5, 11, 3, 7, 17);
        demoCurrentState(avlTree, treeSet);
        demoAdd(avlTree, treeSet, -13, -5, -11, -3, -7, -17);
        demoCurrentState(avlTree, treeSet);
        demoAdd(avlTree, treeSet, 5);
        System.out.println("TreeSet only permits unique values so the trees inOrder look different");
        demoCurrentState(avlTree, treeSet);
        avlTree.remove(5);
        System.out.println("removed 5 from AvlTree now they look the same again");
        demoCurrentState(avlTree, treeSet);
        demoDescending(avlTree, treeSet);
        final String valueFormat = "%s value TreeSet: %d AvlTree: %d%n";
        System.out.printf(valueFormat, "Min", treeSet.first(), avlTree.min());
        System.out.printf(valueFormat, "Max", treeSet.last(), avlTree.max());

        demoDescending(avlTree, treeSet);

        demoRemove(avlTree, treeSet, 1, 2, 4, 6, 7, 8, 9, 9, 12, -17, -3, -5);
        demoCurrentState(avlTree, treeSet);

        containsDemo(avlTree, treeSet, 11);
        containsDemo(avlTree, treeSet, -13);
        containsDemo(avlTree, treeSet, 1113);

        System.out.println("clearing...");
        treeSet.clear();
        avlTree.clear();
        demoCurrentState(avlTree, treeSet);
        System.out.println();
        System.out.println("large tree");
        new Random().ints(50, Short.MIN_VALUE, Short.MAX_VALUE).mapToObj(i -> {
                avlTree.add(i);
                return avlTree;
            }).reduce((t1, t2) -> t1).map(BinarySearchTree::prettyString)
            .ifPresent(System.out::println);
    }

    private static void demoDescending(BinarySearchTree<Integer> avlTree, TreeSet<Integer> treeSet) {
        final Iterator<Integer> treeSetDescendingIterator = treeSet.descendingIterator();
        final Iterator<Integer> avlTreeDescendingIterator = avlTree.descendingIterator();
        final StringJoiner treeSetJoiner = new StringJoiner(", ", "TreeSet[", "]");
        final StringJoiner avlTreeJoiner = new StringJoiner(", ", "AvlTree[", "]");
        while (treeSetDescendingIterator.hasNext()) {
            treeSetJoiner.add(String.valueOf(treeSetDescendingIterator.next()));
            avlTreeJoiner.add(String.valueOf(avlTreeDescendingIterator.next()));
        }
        System.out.println("Descending order iteration:");
        System.out.println(treeSetJoiner);
        System.out.println(avlTreeJoiner);
    }

    private static void demoCurrentState(BinarySearchTree<Integer> avlTree, TreeSet<Integer> treeSet) {
        final String format = "AvlTree size:%d\t\t\t\t\t%s%njava.util.TreeSet size:%d\t\t%s%n%nAvlTree as tree:%n%s%n";
        System.out.printf(format, avlTree.size(), avlTree, treeSet.size(), treeSet, avlTree.prettyString());
    }

    private static void containsDemo(BinarySearchTree<Integer> avlTree, TreeSet<Integer> treeSet, int i) {
        final String format = "Contains:%d TreeSet: %s AvlTree: %s%n";
        System.out.printf(format, i, treeSet.contains(i), avlTree.contains(i));
    }

    private static void demoAdd(BinarySearchTree<Integer> avlTree, TreeSet<Integer> treeSet, int... ints) {
        System.out.println("adding " + Arrays.toString(ints));
        for (int i : ints) {
            avlTree.add(i);
            treeSet.add(i);
        }
    }

    private static void demoRemove(BinarySearchTree<Integer> avlTree, TreeSet<Integer> treeSet, int... ints) {
        System.out.println("removing " + Arrays.toString(ints));
        for (int i : ints) {
            avlTree.remove(i);
            treeSet.remove(i);
        }
    }
}
