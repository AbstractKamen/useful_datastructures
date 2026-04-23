package com.abstractkamen.datastructures.impl.trees.search.demos;

import com.abstractkamen.datastructures.api.trees.search.SuffixTree;
import com.abstractkamen.datastructures.impl.trees.search.CaseInsensitiveGenericUkkonenSuffixTree;
import com.abstractkamen.datastructures.impl.trees.search.LocalisedUkkonenSuffixTreeInput;
import com.ibm.icu.lang.UCharacter;
import com.ibm.icu.text.CaseMap;

import java.util.List;
import java.util.Locale;

public class CaseInsensitiveSuffixTreeExample {

    public static void main(String[] args) {

        final var input = List.of(
                new LocalisedUkkonenSuffixTreeInput<>("Instance©Ĥǆ", Locale.ENGLISH, "A"),
                new LocalisedUkkonenSuffixTreeInput<>("İstanbul©Ĥǅ", new Locale("tr"), "B"),
                new LocalisedUkkonenSuffixTreeInput<>("bananabananabanana", Locale.ENGLISH, "C")
        );
        System.out.println(UCharacter.foldCase("Instanceǅ", true));
        System.out.println(UCharacter.foldCase("İstanbulǅ", true));
        System.out.println(UCharacter.foldCase('ǅ', true));
        final SuffixTree<String> tree = new CaseInsensitiveGenericUkkonenSuffixTree<>(input);

        System.out.println("Should contain `ǆ`: " + tree.contains("ǆ"));
        System.out.println("Should contain `ǅ`: " + tree.contains("ǅ"));
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

        System.out.println(((CaseInsensitiveGenericUkkonenSuffixTree<?>) tree).prettyTreeString());
        System.out.println("Total nodes count: " + tree.nodesCount());
        System.out.println("Values count: " + tree.valuesCount());
        System.out.println("Text size: " + tree.textSize());
    }
}