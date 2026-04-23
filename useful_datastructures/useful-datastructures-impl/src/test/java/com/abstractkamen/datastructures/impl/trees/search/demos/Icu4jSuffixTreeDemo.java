package com.abstractkamen.datastructures.impl.trees.search.demos;

import com.abstractkamen.datastructures.api.trees.search.SuffixTree;
import com.abstractkamen.datastructures.impl.trees.search.GenericUkkonenSuffixTree;
import com.abstractkamen.datastructures.impl.trees.search.UkkonenSuffixTreeInput;
import com.ibm.icu.lang.UCharacter;

import java.util.List;

/**
 * This example folds all strings before input in the tree using {@link UCharacter#foldCase(String, boolean)}.
 * Then the same folding is applied to query strings which solves the case-insensitive tree use case.
 */
public class Icu4jSuffixTreeDemo {

    public static void main(String[] args) {
        final var input = List.of(
                new UkkonenSuffixTreeInput<>(UCharacter.foldCase("kiss", true), "A"),
                new UkkonenSuffixTreeInput<>(UCharacter.foldCase("ßanana©Ĥǅ", true), "A"),
                new UkkonenSuffixTreeInput<>(UCharacter.foldCase("ßananabanana©Ĥǅ", true), "B"),
                new UkkonenSuffixTreeInput<>(UCharacter.foldCase("ßananabananabanana©Ĥǅ", true), "C"),
                new UkkonenSuffixTreeInput<>(UCharacter.foldCase("Instance©Ĥǆ", true), "D"),
                new UkkonenSuffixTreeInput<>(UCharacter.foldCase("İstanbul©Ĥǅ", true), "E")
        );
        final SuffixTree<String> tree = new GenericUkkonenSuffixTree<>(input);
        System.out.println("Should contain `issa`: " + tree.contains(UCharacter.foldCase("issa", false)));
        System.out.println("Should contain `iss`: " + tree.contains(UCharacter.foldCase("iss", true)));
        System.out.println("Should contain `iß`: " + tree.contains(UCharacter.foldCase("iß", true)));
        System.out.println("Should contain `ssa`: " + tree.contains(UCharacter.foldCase("ssa", true)));
        System.out.println("Should contain `ssanana`: " + tree.contains(UCharacter.foldCase("ssanana", true)));
        System.out.println("Should contain `ß`: " + tree.contains(UCharacter.foldCase("ß", true)));
        System.out.println("Should contain `ǆ`: " + tree.contains(UCharacter.foldCase("ǆ", true)));
        System.out.println("Should contain `ǅ`: " + tree.contains(UCharacter.foldCase("ǅ", true)));
        System.out.println("Should contain `banana`: " + tree.contains(UCharacter.foldCase("banana", true)));
        System.out.println("Should contain `Ĥǅ`: " + tree.contains(UCharacter.foldCase("Ĥǅ", true)));
        System.out.println("Should contain `ßananabanana`: " + tree.contains(UCharacter.foldCase("ßananabanana", true)));
        System.out.println("Should contain `ßananabananabanana`: " + tree.contains(UCharacter.foldCase("ßananabananabanana", true)));
        System.out.println("Should return 3 strings for `ß`: - this part of ki[ss] is folded into ki[ß] -> 4 strings with ß" + tree.findAllOccurrences(UCharacter.foldCase("ß", true)).size());
        System.out.println("Should return 3 strings for `ßa`: " + tree.findAllOccurrences(UCharacter.foldCase("ßa", true)).size());
        System.out.println("Should return 3 strings for `ßan`: " + tree.findAllOccurrences(UCharacter.foldCase("ßan", true)).size());
        System.out.println("Should return 3 strings for `ßana`: " + tree.findAllOccurrences(UCharacter.foldCase("ßana", true)).size());
        System.out.println("Should return 3 strings for `ßanan`: " + tree.findAllOccurrences(UCharacter.foldCase("ßanan", true)).size());
        System.out.println("Should return 3 strings for `ßanana`: " + tree.findAllOccurrences(UCharacter.foldCase("ßanana", true)).size());
        System.out.println("Should return 2 strings for `ßananab`: " + tree.findAllOccurrences(UCharacter.foldCase("ßananab", true)).size());
        System.out.println("Should return 2 strings for `ßananaba`: " + tree.findAllOccurrences(UCharacter.foldCase("ßananaba", true)).size());
        System.out.println("Should return 2 strings for `ßananaban`: " + tree.findAllOccurrences(UCharacter.foldCase("ßananaban", true)).size());
        System.out.println("Should return 2 strings for `ßananabana`: " + tree.findAllOccurrences(UCharacter.foldCase("ßananabana", true)).size());
        System.out.println("Should return 2 strings for `ßananabanan`: " + tree.findAllOccurrences(UCharacter.foldCase("ßananabanan", true)).size());
        System.out.println("Should return 2 strings for `ßananabanana`: " + tree.findAllOccurrences(UCharacter.foldCase("ßananabanana", true)).size());
        System.out.println("Should return 1 string  for `ßananabananab`:  " + tree.findAllOccurrences(UCharacter.foldCase("ßananabananab", true)).size());

        System.out.println(((GenericUkkonenSuffixTree<?>) tree).prettyTreeString());
        System.out.println("Total nodes count: " + tree.nodesCount());
        System.out.println("Values count: " + tree.valuesCount());
        System.out.println("Text size: " + tree.textSize());
    }
}