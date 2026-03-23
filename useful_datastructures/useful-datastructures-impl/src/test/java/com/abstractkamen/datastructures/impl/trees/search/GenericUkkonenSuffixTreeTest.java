package com.abstractkamen.datastructures.impl.trees.search;

import com.abstractkamen.datastructures.impl.StopWatch;
import org.junit.BeforeClass;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class GenericUkkonenSuffixTreeTest {
    private record WordPair(String a, String b) {
    }

    private static List<UkkonenSuffixTreeInput<String>> inputData;
    private static List<WordPair> assertMap;
    private static GenericUkkonenSuffixTree<String> tree;

    @BeforeClass
    public static void beforeAll() {
        inputData = new ArrayList<>();
        assertMap = new ArrayList<>();
        try {
            final Map<String, Integer> countMap = new HashMap<>();
            try (var stream = Files.lines(Path.of("src/test/resources/shakespeare.txt"))) {
                stream.forEach(line -> {
                    final String[] words = line.split(" +|,+ *|\\. *|! *|\\? *|: *|\\( *");
                    for (String word : words) {
                        if (word.isEmpty()) continue;
                        final Integer c = countMap.compute(word, (k, v) -> v == null ? 1 : v + 1);
                        final String wordWithCount = word + "-" + c;
                        assertMap.add(new WordPair(word, word));
                        inputData.add(new UkkonenSuffixTreeInput<>(word, word));
                    }
                });
                final StopWatch treeConstruction = new StopWatch("Tree construction in ");
                tree = new GenericUkkonenSuffixTree<>(inputData);
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
                    .filter(e -> slowSearch(e.a(), target))
                    .map(WordPair::b)
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
    public void shakespeare_overlaps_with_all_no_duplicates() {
        var input = Stream.of("Called, Calls, Shall, all, all-eating, all-oblivious, all-tyrant, allayed, allege, allow, call, called, calls, effectually, fall, falls, hallowed, miscalled, parallels, shall, shallowest, small, swallowed, tall, tallies, thrall, thralled, valley-fountain, walls".split(", "))
                .map(s -> new UkkonenSuffixTreeInput<>(s, s))
                .toList();
        var tree = new GenericUkkonenSuffixTree<>(input);
       System.out.println(tree.prettyTreeString());
        assertEquals(input.size(), tree.findAllOccurrences("all").size());
    }

    @Test
    public void cacao() {
        var input = Stream.of("cacao")
                .map(s -> new UkkonenSuffixTreeInput<>(s, s))
                .toList();
        var tree = new GenericUkkonenSuffixTree<>(input);
        System.out.println(tree.prettyTreeString());
        assertEquals(input.size(), tree.findAllOccurrences("c").size());
    }


    @Test
    public void shakespeare_overlaps_with_all_many_duplicates() {
        var input = Stream.of("Called, Calls, Shall, Shall, Shall, Shall, Shall, Shall, Shall, Shall, Shall, Shall, all, all, all, all, all, all, all, all, all, all, all, all, all, all, all, all, all, all, all, all, all, all, all, all, all, all, all, all, all, all, all, all, all, all, all, all, all, all, all, all, all, all, all, all, all, all, all, all, all, all, all, all, all, all, all, all, all, all, all, all, all, all, all, all, all, all, all, all, all, all, all, all, all, all, all, all, all, all, all, all, all, all, all, all, all, all, all, all, all, all, all, all, all, all, all, all, all, all, all, all, all, all, all, all, all, all, all, all-eating, all-oblivious, all-tyrant, allayed, allege, allow, allow, call, call, call, call, call, call, call, call, call, call, called, calls, calls, calls, calls, effectually, fall, fall, fall, falls, hallowed, miscalled, parallels, shall, shall, shall, shall, shall, shall, shall, shall, shall, shall, shall, shall, shall, shall, shall, shall, shall, shall, shall, shall, shall, shall, shall, shall, shall, shall, shall, shall, shall, shall, shall, shall, shall, shall, shall, shall, shall, shall, shall, shall, shall, shall, shall, shall, shall, shall, shall, shall, shall, shallowest, small, small, swallowed, tall, tallies, thrall, thralled, valley-fountain, walls, walls".split(", "))
                .map(s -> new UkkonenSuffixTreeInput<>(s, s))
                .toList();
        var tree = new GenericUkkonenSuffixTree<>(input);
        System.out.println(tree.prettyTreeString());
        assertEquals(input.size(), tree.findAllOccurrences("all").size());
    }

    @Test
    public void overlapping_suffix_and_prefixes() {
        var input = Stream.of("a, cdab, dab, do, doa".split(", "))
                .map(s -> new UkkonenSuffixTreeInput<>(s, s))
                .toList();
        var tree = new GenericUkkonenSuffixTree<>(input);
        System.out.println(tree.prettyTreeString());

        assertEquals(4, tree.findAllOccurrences("a").size());
        assertEquals(4, tree.findAllOccurrences("d").size());
        assertEquals(2, tree.findAllOccurrences("o").size());
        assertEquals(2, tree.findAllOccurrences("do").size());
        assertEquals(2, tree.findAllOccurrences("ab").size());
        assertEquals(2, tree.findAllOccurrences("dab").size());
        assertEquals(1, tree.findAllOccurrences("doa").size());
        assertEquals(1, tree.findAllOccurrences("cdab").size());
    }

    @Test
    public void overlapping_suffix_and_prefixes_many_duplicates() {
        var input = Stream.of("a, a, a, a, cdab, cdab, dab, dab, do, doa, dodab, d, d, d".split(", "))
                .map(s -> new UkkonenSuffixTreeInput<>(s, s))
                .toList();
        var tree = new GenericUkkonenSuffixTree<>(input);
        System.out.println(tree);
        System.out.println(tree.prettyTreeString());
        assertEquals(10, tree.findAllOccurrences("a").size());
        assertEquals(10, tree.findAllOccurrences("d").size());
        assertEquals(3, tree.findAllOccurrences("o").size());
        assertEquals(3, tree.findAllOccurrences("do").size());
        assertEquals(5, tree.findAllOccurrences("ab").size());
        assertEquals(5, tree.findAllOccurrences("dab").size());
        assertEquals(1, tree.findAllOccurrences("doa").size());
        assertEquals(2, tree.findAllOccurrences("cdab").size());
        assertEquals(1, tree.findAllOccurrences("dodab").size());
    }

    private boolean slowSearch(String in, String search) {
        return in.contains(search);
    }

    @Test
    public void testSingleCharacterStrings() {
        var input = List.of(
                new UkkonenSuffixTreeInput<>("a", "A"),
                new UkkonenSuffixTreeInput<>("b", "B"),
                new UkkonenSuffixTreeInput<>("c", "C")
        );
        var tree = new GenericUkkonenSuffixTree<>(input);

        assertTrue(tree.contains("a"));
        assertTrue(tree.contains("b"));
        assertTrue(tree.contains("c"));
        assertFalse(tree.contains("d"));

        assertEquals(Set.of("A"), Set.copyOf(tree.findAllOccurrences("a")));
        assertEquals(Set.of("B"), Set.copyOf(tree.findAllOccurrences("b")));
        assertEquals(Set.of("C"), Set.copyOf(tree.findAllOccurrences("c")));
    }

    @Test
    public void testDuplicateStrings() {
        var input = List.of(
                new UkkonenSuffixTreeInput<>("a", "PLAYER_1"),
                new UkkonenSuffixTreeInput<>("a", "PLAYER_2")
        );
        var tree = new GenericUkkonenSuffixTree<>(input);

        assertTrue(tree.contains("a"));
        var matches = tree.findAllOccurrences("a");

        // Both entries should be indexed separately
        assertEquals(2, matches.size());
        assertTrue(matches.contains("PLAYER_1"));
        assertTrue(matches.contains("PLAYER_2"));
    }

    @Test
    public void testCaseSensitivity() {
        var input = List.of(
                new UkkonenSuffixTreeInput<>("Messi", "A"),
                new UkkonenSuffixTreeInput<>("messi", "B"),
                new UkkonenSuffixTreeInput<>("MESSI", "C")
        );
        var tree = new GenericUkkonenSuffixTree<>(input);

        // Tree is case-sensitive by design
        assertTrue(tree.contains("Messi"));
        assertTrue(tree.contains("messi"));
        assertTrue(tree.contains("MESSI"));

        assertEquals(Set.of("A"), Set.copyOf(tree.findAllOccurrences("Messi")));
        assertEquals(Set.of("B"), Set.copyOf(tree.findAllOccurrences("messi")));
        assertEquals(Set.of("C"), Set.copyOf(tree.findAllOccurrences("MESSI")));
    }

    @Test
    public void testSpecialCharacters() {
        var input = List.of(
                new UkkonenSuffixTreeInput<>("a-b", "A"),
                new UkkonenSuffixTreeInput<>("a_b", "B"),
                new UkkonenSuffixTreeInput<>("a.b", "C")
        );
        var tree = new GenericUkkonenSuffixTree<>(input);

        assertEquals(1, tree.findAllOccurrences("a-b").size());
        assertEquals(1, tree.findAllOccurrences("a_b").size());
        assertEquals(1, tree.findAllOccurrences("a.b").size());
    }

    @Test
    public void testWhitespaceHandling() {
        var input = List.of(
                new UkkonenSuffixTreeInput<>("lionel messi", "A"),
                new UkkonenSuffixTreeInput<>("cristiano ronaldo", "B")
        );
        var tree = new GenericUkkonenSuffixTree<>(input);

        assertTrue(tree.contains("lionel"));
        assertTrue(tree.contains("messi"));
        assertTrue(tree.contains("lionel messi"));

        assertEquals(Set.of("A"), Set.copyOf(tree.findAllOccurrences("lionel messi")));
    }

    @Test
    public void testEmptyPattern() {
        var input = List.of(
                new UkkonenSuffixTreeInput<>("banana", "A")
        );
        var tree = new GenericUkkonenSuffixTree<>(input);

        assertNotNull(tree.findAllOccurrences(""));
        assertNotNull(tree.findAllOccurrences(null));
    }

    @Test
    public void testPatternLongerThanIndexedStrings() {
        var input = List.of(
                new UkkonenSuffixTreeInput<>("cat", "A"),
                new UkkonenSuffixTreeInput<>("dog", "B")
        );
        var tree = new GenericUkkonenSuffixTree<>(input);

        assertFalse(tree.contains("cattle"));
        assertFalse(tree.contains("doghouse"));
        assertTrue(tree.findAllOccurrences("cattle").isEmpty());
    }

    @Test
    public void testPatterSimilarStrings() {
        var input = List.of(
                new UkkonenSuffixTreeInput<>("banana", "A"),
                new UkkonenSuffixTreeInput<>("bananabanana", "B"),
                new UkkonenSuffixTreeInput<>("bananabananabanana", "C")
        );
        var tree = new GenericUkkonenSuffixTree<>(input);

        assertTrue(tree.contains("banana"));
        assertTrue(tree.contains("bananabanana"));
        assertTrue(tree.contains("bananabananabanana"));
        assertEquals(3, tree.findAllOccurrences("b").size());
        assertEquals(3, tree.findAllOccurrences("ba").size());
        assertEquals(3, tree.findAllOccurrences("ban").size());
        assertEquals(3, tree.findAllOccurrences("bana").size());
        assertEquals(3, tree.findAllOccurrences("banan").size());
        assertEquals(3, tree.findAllOccurrences("banana").size());
        assertEquals(2, tree.findAllOccurrences("bananab").size());
        assertEquals(2, tree.findAllOccurrences("bananaba").size());
        assertEquals(2, tree.findAllOccurrences("bananaban").size());
        assertEquals(2, tree.findAllOccurrences("bananabana").size());
        assertEquals(2, tree.findAllOccurrences("bananabanan").size());
        assertEquals(2, tree.findAllOccurrences("bananabanana").size());
        assertEquals(1, tree.findAllOccurrences("bananabananab").size());

        System.out.println(tree.prettyTreeString());
    }


    @Test
    public void testBulgarianCyrillicNames() {
        var input = List.of(
                new UkkonenSuffixTreeInput<>("Илия Груев", "BG_1"),
                new UkkonenSuffixTreeInput<>("Кирил Десподов", "BG_2"),
                new UkkonenSuffixTreeInput<>("Георги Минчев", "BG_3")
        );
        var tree = new GenericUkkonenSuffixTree<>(input);

        assertTrue(tree.contains("Илия"));
        assertTrue(tree.contains("Груев"));
        assertTrue(tree.contains("Кирил"));
        assertTrue(tree.contains("Десподов"));

        assertEquals(Set.of("BG_1"), Set.copyOf(tree.findAllOccurrences("Илия")));
        assertEquals(Set.of("BG_2"), Set.copyOf(tree.findAllOccurrences("Десподов")));
    }

    @Test
    public void testMixedLocaleTree() {
        var input = List.of(
                new UkkonenSuffixTreeInput<>("lionel messi", "EN_1"),
                new UkkonenSuffixTreeInput<>("Илия Груев", "BG_1"),
                new UkkonenSuffixTreeInput<>("cristiano ronaldo", "EN_2"),
                new UkkonenSuffixTreeInput<>("Кирил Десподов", "BG_2")
        );
        var tree = new GenericUkkonenSuffixTree<>(input);

        // English names
        assertTrue(tree.contains("messi"));
        assertTrue(tree.contains("ronaldo"));

        // Bulgarian names
        assertTrue(tree.contains("Илия"));
        assertTrue(tree.contains("Кирил"));

        // Verify locale separation
        assertTrue(tree.findAllOccurrences("messi").stream()
                .allMatch(id -> id.startsWith("EN")));
        assertTrue(tree.findAllOccurrences("Илия").stream()
                .allMatch(id -> id.startsWith("BG")));
    }

    @Test
    public void testUnicodeBeyondCyrillic() {
        var input = List.of(
                new UkkonenSuffixTreeInput<>("García", "ES"),
                new UkkonenSuffixTreeInput<>("Müller", "DE"),
                new UkkonenSuffixTreeInput<>("Ødegaard", "NO")
        );
        var tree = new GenericUkkonenSuffixTree<>(input);

        assertTrue(tree.contains("Garc"));
        assertTrue(tree.contains("Müll"));
        assertTrue(tree.contains("Øde"));

        assertEquals(Set.of("ES"), Set.copyOf(tree.findAllOccurrences("García")));
        assertEquals(Set.of("DE"), Set.copyOf(tree.findAllOccurrences("Müller")));
    }


    @Test
    public void testResultsAreUnique() {
        var input = List.of(
                new UkkonenSuffixTreeInput<>("banana", "A"),
                new UkkonenSuffixTreeInput<>("anabel", "B"),
                new UkkonenSuffixTreeInput<>("barnacle", "C")
        );
        var tree = new GenericUkkonenSuffixTree<>(input);

        var naMatches = tree.findAllOccurrences("na");
        var uniqueMatches = naMatches.stream().distinct().count();

        assertEquals(naMatches.size(), uniqueMatches);
    }

    @Test
    public void testToString() {
        var input = List.of(
                new UkkonenSuffixTreeInput<>("test", "VALUE")
        );
        var tree = new GenericUkkonenSuffixTree<>(input);

        var toString = tree.toString();

        assertNotNull(toString);
        assertTrue(toString.contains("GenericUkkonenSuffixTree"));
        assertTrue(toString.contains("text.length"));
        assertTrue(toString.contains("valueCache.size"));
    }

    @Test
    public void testPrettyTreeString() {
        var input = List.of(
                new UkkonenSuffixTreeInput<>("banana", "A")
        );
        var tree = new GenericUkkonenSuffixTree<>(input);

        var prettyString = tree.prettyTreeString();

        assertNotNull(prettyString);
        assertFalse(prettyString.isEmpty());
        assertTrue(prettyString.contains("[root]"));
    }

    @Test
    public void testNullInputList() {
        assertThrows(NullPointerException.class, () -> new GenericUkkonenSuffixTree<>(null));
    }

    @Test
    public void testEmptyInputList() {
        var tree = new GenericUkkonenSuffixTree<>(List.of());

        assertNotNull(tree);
        assertFalse(tree.contains("anything"));
        assertFalse(tree.contains(null));
        assertTrue(tree.findAllOccurrences("anything").isEmpty());
    }

    @Test
    public void testImmutableAfterConstruction() {
        var input = List.of(
                new UkkonenSuffixTreeInput<>("test", "VALUE")
        );
        var tree = new GenericUkkonenSuffixTree<>(input);

        // Multiple queries should return consistent results
        assertEquals(tree.findAllOccurrences("test"), tree.findAllOccurrences("test"));
        assertEquals(tree.contains("test"), tree.contains("test"));
    }
}