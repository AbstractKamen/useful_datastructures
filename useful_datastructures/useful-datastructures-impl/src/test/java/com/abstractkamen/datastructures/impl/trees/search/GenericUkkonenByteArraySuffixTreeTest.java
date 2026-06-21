package com.abstractkamen.datastructures.impl.trees.search;

import com.abstractkamen.datastructures.impl.StopWatch;
import org.junit.BeforeClass;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.Assert.*;

public class GenericUkkonenByteArraySuffixTreeTest {
  private record WordPair(byte[] a, byte[] b) {
  }

  private static List<UkkonenByteArraySuffixTreeInput<String>> inputData;
  private static List<WordPair> assertMap;
  private static GenericUkkonenByteArraySuffixTree<String> tree;

  @BeforeClass
  public static void beforeAll() {
	inputData = new ArrayList<>();
	assertMap = new ArrayList<>();
	try {
	  try (var stream = Files.lines(Path.of("src/test/resources/shakespeare.txt"))) {
		stream.forEach(line -> {
		  final String[] words = line.split(" +|,+ *|\\. *|! *|\\? *|: *|\\( *");
		  for (String word : words) {
			if (word.isEmpty()) continue;
			assertMap.add(new WordPair(word.getBytes(), word.getBytes()));
			inputData.add(new UkkonenByteArraySuffixTreeInput<>(word.getBytes(), word));
		  }
		});
		final StopWatch treeConstruction = new StopWatch("Tree construction in ");
		tree = new GenericUkkonenByteArraySuffixTree<>(inputData);
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
	  final Collection<String> actual = tree.findAllOccurrences(target.getBytes());
	  System.out.println(treeLookup);
	  System.out.println(actual.stream().sorted().toList());
	  // assert
	  if (target.isEmpty()) {
		assertTrue(actual.isEmpty());
		// no need to compare because it will be wrong -> String.contains("") is always true
		continue;
	  }
	  final StopWatch assertMapLookup = new StopWatch("String contains lookup in ");
	  final List<String> expected = assertMap.stream()
			  .filter(e -> slowSearch(new String(e.a()), target))
			  .map(WordPair::b)
			  .map(String::new)
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

  private boolean slowSearch(String in, String search) {
	return in.contains(search);
  }

  @Test
  public void shakespeare_overlaps_with_all_no_duplicates() {
	var input = Stream.of("Called, Calls, Shall, all, all-eating, all-oblivious, all-tyrant, allayed, allege, allow, call, called, calls, effectually, fall, falls, hallowed, miscalled, parallels, shall, shallowest, small, swallowed, tall, tallies, thrall, thralled, valley-fountain, walls".split(", "))
			.map(s -> new UkkonenByteArraySuffixTreeInput<>(s.getBytes(), s))
			.toList();
	var tree = new GenericUkkonenByteArraySuffixTree<>(input);
	System.out.println(tree.prettyTreeString());
	assertEquals(input.size(), tree.findAllOccurrences("all".getBytes()).size());
  }

  @Test
  public void overlapping_suffix_and_prefixes() {
	var input = Stream.of("a, cdab, dab, do, doa".split(", "))
			.map(s -> new UkkonenByteArraySuffixTreeInput<>(s.getBytes(), s))
			.toList();
	var tree = new GenericUkkonenByteArraySuffixTree<>(input);
	System.out.println(tree.prettyTreeString());

	assertEquals(4, tree.findAllOccurrences("a".getBytes()).size());
	assertEquals(4, tree.findAllOccurrences("d".getBytes()).size());
	assertEquals(2, tree.findAllOccurrences("o".getBytes()).size());
	assertEquals(2, tree.findAllOccurrences("do".getBytes()).size());
	assertEquals(2, tree.findAllOccurrences("ab".getBytes()).size());
	assertEquals(2, tree.findAllOccurrences("dab".getBytes()).size());
	assertEquals(1, tree.findAllOccurrences("doa".getBytes()).size());
	assertEquals(1, tree.findAllOccurrences("cdab".getBytes()).size());
  }

  @Test
  public void overlapping_suffix_and_prefixes_many_duplicates() {
	var input = Stream.of("a, a, a, a, cdab, cdab, dab, dab, do, doa, dodab, d, d, d".split(", "))
			.map(s -> new UkkonenByteArraySuffixTreeInput<>(s.getBytes(), s))
			.toList();
	var tree = new GenericUkkonenByteArraySuffixTree<>(input);
	System.out.println(tree);
	System.out.println(tree.prettyTreeString());
	assertEquals(10, tree.findAllOccurrences("a".getBytes()).size());
	assertEquals(10, tree.findAllOccurrences("d".getBytes()).size());
	assertEquals(3, tree.findAllOccurrences("o".getBytes()).size());
	assertEquals(3, tree.findAllOccurrences("do".getBytes()).size());
	assertEquals(5, tree.findAllOccurrences("ab".getBytes()).size());
	assertEquals(5, tree.findAllOccurrences("dab".getBytes()).size());
	assertEquals(1, tree.findAllOccurrences("doa".getBytes()).size());
	assertEquals(2, tree.findAllOccurrences("cdab".getBytes()).size());
	assertEquals(1, tree.findAllOccurrences("dodab".getBytes()).size());
  }


  @Test
  public void testSingleCharacterStrings() {
	var input = List.of(
			new UkkonenByteArraySuffixTreeInput<>("a".getBytes(), "A"),
			new UkkonenByteArraySuffixTreeInput<>("b".getBytes(), "B"),
			new UkkonenByteArraySuffixTreeInput<>("c".getBytes(), "C")
	);
	var tree = new GenericUkkonenByteArraySuffixTree<>(input);

	assertTrue(tree.contains("a".getBytes()));
	assertTrue(tree.contains("b".getBytes()));
	assertTrue(tree.contains("c".getBytes()));
	assertFalse(tree.contains("d".getBytes()));

	assertEquals(Set.of("A"), Set.copyOf(tree.findAllOccurrences("a".getBytes())));
	assertEquals(Set.of("B"), Set.copyOf(tree.findAllOccurrences("b".getBytes())));
	assertEquals(Set.of("C"), Set.copyOf(tree.findAllOccurrences("c".getBytes())));
  }

  @Test
  public void testDuplicateStrings() {
	var input = List.of(
			new UkkonenByteArraySuffixTreeInput<>("a".getBytes(), "PLAYER_1"),
			new UkkonenByteArraySuffixTreeInput<>("a".getBytes(), "PLAYER_2")
	);
	var tree = new GenericUkkonenByteArraySuffixTree<>(input);

	assertTrue(tree.contains("a".getBytes()));
	var matches = tree.findAllOccurrences("a".getBytes());

	assertEquals(2, matches.size());
	assertTrue(matches.contains("PLAYER_1"));
	assertTrue(matches.contains("PLAYER_2"));
  }

  @Test
  public void testCaseSensitivity() {
	var input = List.of(
			new UkkonenByteArraySuffixTreeInput<>("Messi".getBytes(), "A"),
			new UkkonenByteArraySuffixTreeInput<>("messi".getBytes(), "B"),
			new UkkonenByteArraySuffixTreeInput<>("MESSI".getBytes(), "C")
	);
	var tree = new GenericUkkonenByteArraySuffixTree<>(input);

	assertTrue(tree.contains("Messi".getBytes()));
	assertTrue(tree.contains("messi".getBytes()));
	assertTrue(tree.contains("MESSI".getBytes()));

	assertEquals(Set.of("A"), Set.copyOf(tree.findAllOccurrences("Messi".getBytes())));
	assertEquals(Set.of("B"), Set.copyOf(tree.findAllOccurrences("messi".getBytes())));
	assertEquals(Set.of("C"), Set.copyOf(tree.findAllOccurrences("MESSI".getBytes())));
  }

  @Test
  public void testSpecialCharacters() {
	var input = List.of(
			new UkkonenByteArraySuffixTreeInput<>("a-b".getBytes(), "A"),
			new UkkonenByteArraySuffixTreeInput<>("a_b".getBytes(), "B"),
			new UkkonenByteArraySuffixTreeInput<>("a.b".getBytes(), "C")
	);
	var tree = new GenericUkkonenByteArraySuffixTree<>(input);

	assertEquals(1, tree.findAllOccurrences("a-b".getBytes()).size());
	assertEquals(1, tree.findAllOccurrences("a_b".getBytes()).size());
	assertEquals(1, tree.findAllOccurrences("a.b".getBytes()).size());
  }

  @Test
  public void testWhitespaceHandling() {
	var input = List.of(
			new UkkonenByteArraySuffixTreeInput<>("lionel messi".getBytes(), "A"),
			new UkkonenByteArraySuffixTreeInput<>("cristiano ronaldo".getBytes(), "B")
	);
	var tree = new GenericUkkonenByteArraySuffixTree<>(input);

	assertTrue(tree.contains("lionel".getBytes()));
	assertTrue(tree.contains("messi".getBytes()));
	assertTrue(tree.contains("lionel messi".getBytes()));

	assertEquals(Set.of("A"), Set.copyOf(tree.findAllOccurrences("lionel messi".getBytes())));
  }

  @Test
  public void testEmptyPattern() {
	var input = List.of(
			new UkkonenByteArraySuffixTreeInput<>("banana".getBytes(), "A")
	);
	var tree = new GenericUkkonenByteArraySuffixTree<>(input);

	assertNotNull(tree.findAllOccurrences("".getBytes()));
	assertNotNull(tree.findAllOccurrences(null));
  }

  @Test
  public void testPatternLongerThanIndexedStrings() {
	var input = List.of(
			new UkkonenByteArraySuffixTreeInput<>("cat".getBytes(), "A"),
			new UkkonenByteArraySuffixTreeInput<>("dog".getBytes(), "B")
	);
	var tree = new GenericUkkonenByteArraySuffixTree<>(input);

	assertFalse(tree.contains("cattle".getBytes()));
	assertFalse(tree.contains("doghouse".getBytes()));
	assertTrue(tree.findAllOccurrences("cattle".getBytes()).isEmpty());
  }

  @Test
  public void testPatterSimilarStrings() {
	var input = List.of(
			new UkkonenByteArraySuffixTreeInput<>("banana".getBytes(), "A"),
			new UkkonenByteArraySuffixTreeInput<>("bananabanana".getBytes(), "B"),
			new UkkonenByteArraySuffixTreeInput<>("bananabananabanana".getBytes(), "C")
	);
	var tree = new GenericUkkonenByteArraySuffixTree<>(input);

	assertTrue(tree.contains("banana".getBytes()));
	assertTrue(tree.contains("bananabanana".getBytes()));
	assertTrue(tree.contains("bananabananabanana".getBytes()));
	assertEquals(3, tree.findAllOccurrences("b".getBytes()).size());
	assertEquals(3, tree.findAllOccurrences("ba".getBytes()).size());
	assertEquals(3, tree.findAllOccurrences("ban".getBytes()).size());
	assertEquals(3, tree.findAllOccurrences("bana".getBytes()).size());
	assertEquals(3, tree.findAllOccurrences("banan".getBytes()).size());
	assertEquals(3, tree.findAllOccurrences("banana".getBytes()).size());
	assertEquals(2, tree.findAllOccurrences("bananab".getBytes()).size());
	assertEquals(2, tree.findAllOccurrences("bananaba".getBytes()).size());
	assertEquals(2, tree.findAllOccurrences("bananaban".getBytes()).size());
	assertEquals(2, tree.findAllOccurrences("bananabana".getBytes()).size());
	assertEquals(2, tree.findAllOccurrences("bananabanan".getBytes()).size());
	assertEquals(2, tree.findAllOccurrences("bananabanana".getBytes()).size());
	assertEquals(1, tree.findAllOccurrences("bananabananab".getBytes()).size());

	System.out.println(tree.prettyTreeString());
  }


  @Test
  public void testBulgarianCyrillicNames() {
	var input = List.of(
			new UkkonenByteArraySuffixTreeInput<>("Илия Груев".getBytes(), "BG_1"),
			new UkkonenByteArraySuffixTreeInput<>("Кирил Десподов".getBytes(), "BG_2"),
			new UkkonenByteArraySuffixTreeInput<>("Георги Минчев".getBytes(), "BG_3")
	);
	var tree = new GenericUkkonenByteArraySuffixTree<>(input);

	assertTrue(tree.contains("Илия".getBytes()));
	assertTrue(tree.contains("Груев".getBytes()));
	assertTrue(tree.contains("Кирил".getBytes()));
	assertTrue(tree.contains("Десподов".getBytes()));

	assertEquals(Set.of("BG_1"), Set.copyOf(tree.findAllOccurrences("Илия".getBytes())));
	assertEquals(Set.of("BG_2"), Set.copyOf(tree.findAllOccurrences("Десподов".getBytes())));
  }

  @Test
  public void testMixedLocaleTree() {
	var input = List.of(
			new UkkonenByteArraySuffixTreeInput<>("lionel messi".getBytes(), "EN_1"),
			new UkkonenByteArraySuffixTreeInput<>("Илия Груев".getBytes(), "BG_1"),
			new UkkonenByteArraySuffixTreeInput<>("cristiano ronaldo".getBytes(), "EN_2"),
			new UkkonenByteArraySuffixTreeInput<>("Кирил Десподов".getBytes(), "BG_2")
	);
	var tree = new GenericUkkonenByteArraySuffixTree<>(input);

	assertTrue(tree.contains("messi".getBytes()));
	assertTrue(tree.contains("ronaldo".getBytes()));

	assertTrue(tree.contains("Илия".getBytes()));
	assertTrue(tree.contains("Кирил".getBytes()));

	assertTrue(tree.findAllOccurrences("messi".getBytes()).stream()
			.allMatch(id -> id.startsWith("EN")));
	assertTrue(tree.findAllOccurrences("Илия".getBytes()).stream()
			.allMatch(id -> id.startsWith("BG")));
  }

  @Test
  public void testUnicodeBeyondCyrillic() {
	var input = List.of(
			new UkkonenByteArraySuffixTreeInput<>("García".getBytes(), "ES"),
			new UkkonenByteArraySuffixTreeInput<>("Müller".getBytes(), "DE"),
			new UkkonenByteArraySuffixTreeInput<>("Ødegaard".getBytes(), "NO")
	);
	var tree = new GenericUkkonenByteArraySuffixTree<>(input);

	assertTrue(tree.contains("Garc".getBytes()));
	assertTrue(tree.contains("Müll".getBytes()));
	assertTrue(tree.contains("Øde".getBytes()));

	assertEquals(Set.of("ES"), Set.copyOf(tree.findAllOccurrences("García".getBytes())));
	assertEquals(Set.of("DE"), Set.copyOf(tree.findAllOccurrences("Müller".getBytes())));
  }


  @Test
  public void testResultsAreUnique() {
	var input = List.of(
			new UkkonenByteArraySuffixTreeInput<>("banana".getBytes(), "A"),
			new UkkonenByteArraySuffixTreeInput<>("anabel".getBytes(), "B"),
			new UkkonenByteArraySuffixTreeInput<>("barnacle".getBytes(), "C")
	);
	var tree = new GenericUkkonenByteArraySuffixTree<>(input);

	var naMatches = tree.findAllOccurrences("na".getBytes());
	var uniqueMatches = naMatches.stream().distinct().count();

	assertEquals(naMatches.size(), uniqueMatches);
  }

  @Test
  public void testToString() {
	var input = List.of(
			new UkkonenByteArraySuffixTreeInput<>("test".getBytes(), "VALUE")
	);
	var tree = new GenericUkkonenByteArraySuffixTree<>(input);

	var toString = tree.toString();

	assertNotNull(toString);
	assertTrue(toString.contains("GenericUkkonenByteArraySuffixTree"));
	assertTrue(toString.contains("text.length"));
	assertTrue(toString.contains("valueCache.size"));
  }

  @Test
  public void testPrettyTreeString() {
	var input = List.of(
			new UkkonenByteArraySuffixTreeInput<>("banana".getBytes(), "A")
	);
	var tree = new GenericUkkonenByteArraySuffixTree<>(input);

	var prettyString = tree.prettyTreeString();

	assertNotNull(prettyString);
	assertFalse(prettyString.isEmpty());
	assertTrue(prettyString.contains("[root]"));
  }

  @Test
  public void testNullInputList() {
	assertThrows(NullPointerException.class, () -> new GenericUkkonenByteArraySuffixTree<>(null));
  }

  @Test
  public void testEmptyInputList() {
	var tree = new GenericUkkonenByteArraySuffixTree<>(List.of());

	assertNotNull(tree);
	assertFalse(tree.contains("anything".getBytes()));
	assertFalse(tree.contains(null));
	assertTrue(tree.findAllOccurrences("anything".getBytes()).isEmpty());
  }

  @Test
  public void testUnicodeSupplementaryCharacters() {
	var input = List.of(
			new UkkonenByteArraySuffixTreeInput<>("Hello 😀 World".getBytes(), "EMOJI_1"),
			new UkkonenByteArraySuffixTreeInput<>("Hello 😁 World".getBytes(), "EMOJI_2"),
			new UkkonenByteArraySuffixTreeInput<>("Hello 😂😂".getBytes(), "EMOJI_3"),
			new UkkonenByteArraySuffixTreeInput<>("🎉🎊🎈".getBytes(), "EMOJI_4"),
			new UkkonenByteArraySuffixTreeInput<>("Hello 😀 World".getBytes(), "DUPLICATE_1")  // same key, different value
	);
	var tree = new GenericUkkonenByteArraySuffixTree<>(input);
	System.out.println(tree.prettyTreeString());

	assertEquals(2, tree.findAllOccurrences("😀".getBytes()).size());  // EMOJI_1, DUPLICATE_1
	assertEquals(1, tree.findAllOccurrences("😁".getBytes()).size());  // EMOJI_2
	assertEquals(1, tree.findAllOccurrences("😂".getBytes()).size());  // EMOJI_3

	assertEquals(1, tree.findAllOccurrences("🎉".getBytes()).size());
	assertEquals(1, tree.findAllOccurrences("🎉🎊🎈".getBytes()).size());

	assertEquals(2, tree.findAllOccurrences("Hello 😀".getBytes()).size());
	assertEquals(2, tree.findAllOccurrences("😀 World".getBytes()).size());
	assertEquals(2, tree.findAllOccurrences("Hello 😀 World".getBytes()).size());

	assertTrue(tree.contains("😀".getBytes()));
	assertTrue(tree.contains("😂😂".getBytes()));
	assertTrue(tree.contains("🎊🎈".getBytes()));
	assertFalse(tree.contains("😍".getBytes()));

	assertFalse(tree.contains("😀 World!".getBytes()));

	var helloEmojiWorld = tree.findAllOccurrences("Hello 😀 World".getBytes());
	assertEquals(2, helloEmojiWorld.size());
	assertTrue(helloEmojiWorld.contains("EMOJI_1"));
	assertTrue(helloEmojiWorld.contains("DUPLICATE_1"));

  }

  @Test
  public void testUnicodeSupplementaryStress() {
	var input = List.of(
			new UkkonenByteArraySuffixTreeInput<>("Test 测试 😀 test".getBytes(), "MIXED_1"),
			new UkkonenByteArraySuffixTreeInput<>("Илия 😀 Груев".getBytes(), "MIXED_2"),
			new UkkonenByteArraySuffixTreeInput<>("🎉🎊🎈🎁🎀".getBytes(), "MIXED_3")
	);
	var tree = new GenericUkkonenByteArraySuffixTree<>(input);

	assertTrue(tree.contains("😀".getBytes()));
	assertTrue(tree.contains("测试".getBytes()));
	assertTrue(tree.contains("Илия".getBytes()));
	assertTrue(tree.contains("🎉🎊".getBytes()));

	System.out.println(tree);
  }
}