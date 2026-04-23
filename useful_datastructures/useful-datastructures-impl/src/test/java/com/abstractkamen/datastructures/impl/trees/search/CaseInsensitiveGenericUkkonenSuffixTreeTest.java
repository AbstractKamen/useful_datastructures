package com.abstractkamen.datastructures.impl.trees.search;

import com.abstractkamen.datastructures.impl.StopWatch;
import com.abstractkamen.datastructures.impl.utils.Pair;
import org.junit.BeforeClass;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CaseInsensitiveGenericUkkonenSuffixTreeTest {

    private static final Locale BULGARIAN = new Locale("bg");
    private static final Locale TURKISH = new Locale("tr");

    private static List<LocalisedUkkonenSuffixTreeInput<String>> inputData;
    private static List<Pair<String, String>> assertMap;
    private static CaseInsensitiveGenericUkkonenSuffixTree<String> tree;

    @BeforeClass
    public static void beforeAll() {
        inputData = new ArrayList<>();
        assertMap = new ArrayList<>();
        try {
            final Map<String, Integer> countMap = new HashMap<>();
            try (var stream = Files.lines(Path.of("src/test/resources/shakespeare.txt"))) {
                stream.forEach(line -> {
                    final String[] words = line.split(" +|,+ *|\\. *|! *|\\? *|: *|\\( |\\) *");
                    for (String word : words) {
                        if (word.isEmpty()) continue;
                        final Integer c = countMap.compute(word, (k, v) -> v == null ? 1 : v + 1);
                        assertMap.add(Pair.of(word, word));
                        inputData.add(new LocalisedUkkonenSuffixTreeInput<>(List.of(Pair.of(word, Locale.ENGLISH)), word));
                    }
                });
                final StopWatch treeConstruction = new StopWatch("Tree construction in ");
                tree = new CaseInsensitiveGenericUkkonenSuffixTree<>(inputData);
                System.out.printf("Total input %d - %s %s%n", inputData.size(), tree, treeConstruction);
            }
        } catch (Exception e) {
            throw new AssertionError("could not load test data", e);
        }
    }

    @Test
    public void shakespear_word_search() {
        // arrange
        // tree is static
        final List<String> targets = List.of("all", "thou", "refuse", "I", "love", "crack", "shouldst", "st", "ise", "a", "e", "i", "o", "u", "z");
        for (String target : targets) {
            // act
            final StopWatch treeLookup = new StopWatch("Tree lookup in ");
            final Collection<String> actual = tree.findAllOccurrences(target);
            System.out.println(treeLookup);
            System.out.println(actual.stream().sorted().toList());
            // assert
            if (target.isEmpty()) {
                assertTrue(actual.isEmpty());
                // no need to compare because it will be wrong -> String.contains("") is always true
                return;
            }
            final StopWatch assertMapLookup = new StopWatch("String contains lookup in ");
            final List<String> expected = assertMap.stream()
                    .filter(e -> slowCaseInsensitiveSearch(e.first(), target, Locale.ENGLISH))
                    .map(Pair::second)
                    .toList();
            System.out.println(assertMapLookup);
            System.out.println(expected.stream().sorted().toList());
            assertEquals("Unexpected size for input " + target, expected.size(), actual.size());
            // System.out.println(expected);
            for (String values : actual) {
                assertTrue(expected.contains(values));
            }
        }
    }

    @Test
    public void shakespear_word_search_shouldBeCaseInsensitive() {
        // arrange
        // tree is static
        final List<List<String>> allCasePermutations = List.of(
                getAllCasePermutations("all", Locale.ENGLISH),
                getAllCasePermutations("thou", Locale.ENGLISH),
                getAllCasePermutations("aight", Locale.ENGLISH),
                getAllCasePermutations("umph", Locale.ENGLISH),
                getAllCasePermutations("ph", Locale.ENGLISH),
                getAllCasePermutations("ee", Locale.ENGLISH),
                getAllCasePermutations("ou", Locale.ENGLISH),
                getAllCasePermutations("eth", Locale.ENGLISH),
                getAllCasePermutations("ith", Locale.ENGLISH),
                getAllCasePermutations("ish", Locale.ENGLISH),
                getAllCasePermutations("sh", Locale.ENGLISH)
        );
        for (List<String> targetPermutations : allCasePermutations) {
            // assert lowercase search is expected
            final Iterator<String> it = targetPermutations.iterator();
            final String lowercaseTarget = it.next();
            final Collection<String> actualLowerCase = tree.findAllOccurrences(lowercaseTarget);
            final List<String> expected = assertMap.stream()
                    .filter(e -> slowCaseInsensitiveSearch(e.first(), lowercaseTarget, Locale.ENGLISH))
                    .map(Pair::second)
                    .toList();
            assertEquals("Unexpected size for input " + lowercaseTarget, expected.size(), actualLowerCase.size());
            // System.out.println(expected);
            for (String values : actualLowerCase) {
                assertTrue(expected.contains(values));
            }

            // after base case assertion - assert all case permutations match lowercase
            while (it.hasNext()) {
                String target = it.next();
                // act
                final StopWatch treeLookup = new StopWatch("Tree lookup in ");
                final Collection<String> actual = tree.findAllOccurrences(target);
                System.out.println(treeLookup);
                System.out.println(actual.stream().sorted().toList());
                // assert
                if (target.isEmpty()) {
                    assertTrue(actual.isEmpty());
                    // no need to compare because it will be wrong -> String.contains("") is always true
                    return;
                }
                final StopWatch assertMapLookup = new StopWatch("String contains lookup in ");

                System.out.println(assertMapLookup);
                System.out.println(expected.stream().sorted().toList());
                assertEquals("Unexpected size for input " + target, expected.size(), actual.size());
                for (String values : actual) {
                    assertTrue(expected.contains(values));
                }
            }
        }
    }

    @Test
    public void localised_input_should_be_case_insensitive() {
        // arrange
        final List<LocalisedUkkonenSuffixTreeInput<String>> input = List.of(
                new LocalisedUkkonenSuffixTreeInput<>(List.of(
                        Pair.of("Messi", Locale.ENGLISH),
                        Pair.of("Меси", BULGARIAN),
                        Pair.of("Messi", TURKISH)
                ), "ID_1"),
                new LocalisedUkkonenSuffixTreeInput<>(List.of(
                        Pair.of("Ronaldo", Locale.ENGLISH),
                        Pair.of("Роналдо", BULGARIAN),
                        Pair.of("Ronaldo", TURKISH)
                ), "ID_2"),
                new LocalisedUkkonenSuffixTreeInput<>(List.of(
                        Pair.of("Neymar", Locale.ENGLISH),
                        Pair.of("Неймар", BULGARIAN),
                        Pair.of("Neymar", TURKISH)
                ), "ID_3"),
                new LocalisedUkkonenSuffixTreeInput<>(List.of(
                        Pair.of("Mbappe", Locale.ENGLISH),
                        Pair.of("Мбапе", BULGARIAN),
                        Pair.of("Mbappé", TURKISH)
                ), "ID_4"),
                new LocalisedUkkonenSuffixTreeInput<>(List.of(
                        Pair.of("Hazard", Locale.ENGLISH),
                        Pair.of("Хазард", BULGARIAN),
                        Pair.of("Hazard", TURKISH)
                ), "ID_5"),
                new LocalisedUkkonenSuffixTreeInput<>(List.of(
                        Pair.of("Salah", Locale.ENGLISH),
                        Pair.of("Салах", BULGARIAN),
                        Pair.of("Salah", TURKISH)
                ), "ID_6"),
                new LocalisedUkkonenSuffixTreeInput<>(List.of(
                        Pair.of("De Bruyne", Locale.ENGLISH),
                        Pair.of("Дьойнбрюне", BULGARIAN),
                        Pair.of("De Bruyne", TURKISH)
                ), "ID_7"),
                new LocalisedUkkonenSuffixTreeInput<>(List.of(
                        Pair.of("Modric", Locale.ENGLISH),
                        Pair.of("Модрич", BULGARIAN),
                        Pair.of("Modrić", TURKISH)
                ), "ID_8"),
                new LocalisedUkkonenSuffixTreeInput<>(List.of(
                        Pair.of("Lewandowski", Locale.ENGLISH),
                        Pair.of("Левандовски", BULGARIAN),
                        Pair.of("Lewandowski", TURKISH)
                ), "ID_9"),
                new LocalisedUkkonenSuffixTreeInput<>(List.of(
                        Pair.of("Haaland", Locale.ENGLISH),
                        Pair.of("Холанд", BULGARIAN),
                        Pair.of("Haaland", TURKISH)
                ), "ID_10"),
                new LocalisedUkkonenSuffixTreeInput<>(List.of(
                        Pair.of("Vinicius", Locale.ENGLISH),
                        Pair.of("Винисиус", BULGARIAN),
                        Pair.of("Vinícius", TURKISH)
                ), "ID_11"),
                new LocalisedUkkonenSuffixTreeInput<>(List.of(
                        Pair.of("Istanbul", Locale.ENGLISH),
                        Pair.of("Истанбул", BULGARIAN),
                        Pair.of("İstanbul", TURKISH)
                ), "ID_12"),
                new LocalisedUkkonenSuffixTreeInput<>(List.of(
                        Pair.of("Sofia", Locale.ENGLISH),
                        Pair.of("София", BULGARIAN),
                        Pair.of("Sofya", TURKISH)
                ), "ID_13"),
                new LocalisedUkkonenSuffixTreeInput<>(List.of(
                        Pair.of("Plovdiv", Locale.ENGLISH),
                        Pair.of("Пловдив", BULGARIAN),
                        Pair.of("Filibe", TURKISH)
                ), "ID_14"),
                new LocalisedUkkonenSuffixTreeInput<>(List.of(
                        Pair.of("Burgas", Locale.ENGLISH),
                        Pair.of("Бургас", BULGARIAN),
                        Pair.of("Burgaz", TURKISH)
                ), "ID_15")
        );
        // act
        final CaseInsensitiveGenericUkkonenSuffixTree<String> tree = new CaseInsensitiveGenericUkkonenSuffixTree<>(input);
        System.out.println(tree);
        final Collection<String> actual1 = tree.findAllOccurrences("i"); // en
        final Collection<String> actual2 = tree.findAllOccurrences("I"); // en
        final Collection<String> actual3 = tree.findAllOccurrences("İ"); // tr

        final Collection<String> actual4 = tree.findAllOccurrences("С"); // bg
        final Collection<String> actual5 = tree.findAllOccurrences("с"); // bg
        // assert
        assertEquals(List.of("ID_1", "ID_8", "ID_9", "ID_11", "ID_12", "ID_13", "ID_14"), actual1); // expected order
        assertEquals(actual1, actual2);
        assertEquals(actual1, actual3);

        assertEquals(List.of("ID_1", "ID_6", "ID_9", "ID_11", "ID_12", "ID_13", "ID_15"), actual4); // expected order
        assertEquals(actual4, actual5);

    }

    private List<String> getAllCasePermutations(String s, Locale l) {
        final String lowerCase = s.toLowerCase(l);
        final String upperCase = s.toUpperCase(l);
        final List<String> res = new ArrayList<>();
        new Object() {
            void dfs(int i, StringBuilder sb) {
                if (i >= s.length()) {
                    res.add(sb.toString());
                    return;
                }
                dfs(i + 1, new StringBuilder(sb).append(lowerCase.charAt(i)));
                dfs(i + 1, new StringBuilder(sb).append(upperCase.charAt(i)));
            }
        }.dfs(0, new StringBuilder());
        return res;
    }


    private boolean slowCaseInsensitiveSearch(String in, String search, Locale locale) {
        return in.toLowerCase(locale).contains(search.toLowerCase(locale));
    }
}