package com.abstractkamen.datastructures.impl.trees.search.demos;

import com.abstractkamen.datastructures.api.trees.search.PrefixTrie;
import com.abstractkamen.datastructures.impl.trees.search.PrefixTrieImpl;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Demo class showing PrefixTrie features and behaviour.
 */
public class PrefixTrieDemo {
    public static void main(String[] args) {
        final Collection<String> list = List.of("apple", "apricot", "avocado",
                                                "ant", "app", "ants", "apps", "apis",
                                                "analyse", "alms", "alarm", "alarms");
        final PrefixTrie trie = new PrefixTrieImpl();
        list.forEach(trie::insert);
        System.out.println(trie);
        System.out.println(trie.prettyString());

        startsWithDemo(trie, "a");
        startsWithDemo(trie, "ap");
        startsWithDemo(trie, "app");
        startsWithDemo(trie, "apple");
        deleteDemo(trie, "appi");
        deleteDemo(trie, "app");
        startsWithDemo(trie, "app");
        deleteDemo(trie, "api");
        startsWithDemo(trie, "a");
        System.out.println(trie.prettyString());
        list.forEach(s -> deleteDemo(trie, s));
        System.out.println(trie);
        System.out.println(trie.prettyString());
        startsWithDemo(trie, "a");
        System.out.println("size: " + trie.size());
        System.out.println("words: " + trie.completeWords());

        demoShakespeare();
    }

    private static void demoShakespeare() {
        try (final InputStream inputStream = PrefixTrieDemo.class.getClassLoader().getResourceAsStream("shakespeare.txt")) {
            final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            final long insertTime = System.currentTimeMillis();
            final PrefixTrie trie = new PrefixTrieImpl();
            reader.lines().flatMap(s -> Arrays.stream(s.split("\s"))).forEach(trie::insert);
            System.out.printf("Shakespeare's Sonnets inserted in %.3fms%n", (System.currentTimeMillis() - insertTime) / 1000.);
            System.out.println(trie.size());
            System.out.println(trie.completeWords());
            final long findAvocado = System.nanoTime();
            System.out.printf("Time it took to check if 'avocado' is present[%s] in Shakespeare's Sonnets %.6fns",
                              trie.isPrefix("avocado"), (System.nanoTime() - findAvocado) / 1000_000.);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void deleteDemo(PrefixTrie trie, String toDelete) {
        final String msg = trie.delete(toDelete) ? "true" : "false, reason: not present in tree";
        System.out.printf("deleting '%s' -> deleted:%s%n", toDelete, msg);
    }

    private static void startsWithDemo(PrefixTrie trie, String prefix) {
        System.out.printf("looking up '%s' %s%n", prefix, trie.startsWith(prefix, 4));
    }
}
