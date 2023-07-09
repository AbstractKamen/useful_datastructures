package com.abstractkamen.datastructures.impl.trees.search.demos;

import com.abstractkamen.datastructures.api.trees.search.SuffixTrie;
import com.abstractkamen.datastructures.impl.trees.search.SuffixTrieImpl;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Demo class showing PrefixTrie features and behaviour.
 */
public class SuffixTrieDemo {
    public static void main(String[] args) {
        final Collection<String> list = List.of("apple", "apricot", "avocado",
                                                "ant", "app", "ants", "apps", "apis",
                                                "analyse", "alms", "alarm", "alarms");
        final SuffixTrie trie = new SuffixTrieImpl();
        list.forEach(trie::insert);
        System.out.println(trie);
        System.out.println(trie.prettyString());

        startsWithDemo(trie, "a");
        startsWithDemo(trie, "ap");
        startsWithDemo(trie, "app");
        startsWithDemo(trie, "apple");
        ensWithDemo(trie, "s");
        ensWithDemo(trie, "ms");
        ensWithDemo(trie, "rms");
        ensWithDemo(trie, "arm");
        ensWithDemo(trie, "arms");
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
        try (final InputStream inputStream = SuffixTrieDemo.class.getClassLoader().getResourceAsStream("shakespeare.txt")) {
            assert inputStream != null;
            final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            final long insertTime = System.currentTimeMillis();
            final SuffixTrie trie = new SuffixTrieImpl();
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

    private static void deleteDemo(SuffixTrie trie, String toDelete) {
        final String msg = trie.delete(toDelete) ? "true" : "false, reason: not present in tree";
        System.out.printf("deleting '%s' -> deleted:%s%n", toDelete, msg);
    }

    private static void startsWithDemo(SuffixTrie trie, String prefix) {
        System.out.printf("starts with '%s' %s%n", prefix, trie.startsWith(prefix, 4));
    }

    private static void ensWithDemo(SuffixTrie trie, String prefix) {
        System.out.printf("ends with '%s' %s%n", prefix, trie.endsWith(prefix, 4));
    }
}
