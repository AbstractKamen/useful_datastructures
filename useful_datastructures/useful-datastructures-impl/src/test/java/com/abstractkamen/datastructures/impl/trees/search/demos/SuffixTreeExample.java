package com.abstractkamen.datastructures.impl.trees.search.demos;

import com.abstractkamen.datastructures.api.trees.search.SuffixTree;
import com.abstractkamen.datastructures.impl.trees.search.GenericUkkonenSuffixTree;
import com.abstractkamen.datastructures.impl.trees.search.UkkonenSuffixTreeInput;

import java.util.List;

public class SuffixTreeExample {

    public static void main(String[] args) {
        final var input = List.of(
                new UkkonenSuffixTreeInput<>("banana", "A"),
                new UkkonenSuffixTreeInput<>("bananabanana", "B"),
                new UkkonenSuffixTreeInput<>("bananabananabanana", "C")
        );
        final SuffixTree<String> tree = new GenericUkkonenSuffixTree<>(input);

        System.out.println("Should contain `banana`: " + tree.contains("banana"));
        System.out.println("Should contain `bananabanana`: " + tree.contains("bananabanana"));
        System.out.println("Should contain `bananabananabanana`: " + tree.contains("bananabananabanana"));
        System.out.println("Should return 3 strings for `b`: " + tree.findAllOccurrences("b").size());
        System.out.println("Should return 3 strings for `ba`: " + tree.findAllOccurrences("ba").size());
        System.out.println("Should return 3 strings for `ban`: " + tree.findAllOccurrences("ban").size());
        System.out.println("Should return 3 strings for `bana`: " + tree.findAllOccurrences("bana").size());
        System.out.println("Should return 3 strings for `banan`: " + tree.findAllOccurrences("banan").size());
        System.out.println("Should return 3 strings for `banana`: " + tree.findAllOccurrences("banana").size());
        System.out.println("Should return 2 strings for `bananab`: " + tree.findAllOccurrences("bananab").size());
        System.out.println("Should return 2 strings for `bananaba`: " + tree.findAllOccurrences("bananaba").size());
        System.out.println("Should return 2 strings for `bananaban`: " + tree.findAllOccurrences("bananaban").size());
        System.out.println("Should return 2 strings for `bananabana`: " + tree.findAllOccurrences("bananabana").size());
        System.out.println("Should return 2 strings for `bananabanan`: " + tree.findAllOccurrences("bananabanan").size());
        System.out.println("Should return 2 strings for `bananabanana`: " + tree.findAllOccurrences("bananabanana").size());
        System.out.println("Should return 1 string  for `bananabananab`:  " + tree.findAllOccurrences("bananabananab").size());

        System.out.println(((GenericUkkonenSuffixTree<?>) tree).prettyTreeString());
        System.out.println("Total nodes count: "+ tree.nodesCount());
        System.out.println("Values count: "+ tree.valuesCount());
        System.out.println("Text size: "+ tree.textSize());
    }
}