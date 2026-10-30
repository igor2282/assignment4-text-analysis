package textanalysis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import textanalysis.dp.EditDistance;
import textanalysis.dp.LongestCommonSubsequence;
import textanalysis.matching.KMPMatcher;
import textanalysis.matching.NaiveMatcher;
import textanalysis.matching.RabinKarpMatcher;
import textanalysis.matching.StringMatcher;
import textanalysis.suffix.KasaiLCP;
import textanalysis.suffix.SuffixArrayBuilder;
import textanalysis.util.RandomStrings;

public final class TestRunner {

    private static int passed;
    private static int failed;
    private static String currentSection = "";

    private TestRunner() {
    }

    public static void main(String[] args) {
        lcsTests();
        editDistanceTests();
        lpsTests();
        stringMatchingTests();
        rabinKarpCollisionTests();
        randomizedMatchingTests();
        kasaiTests();
        System.out.println();
        System.out.printf("Total: %d passed, %d failed%n", passed, failed);
        if (failed > 0) {
            System.exit(1);
        }
    }

    private static void lcsTests() {
        section("LCS");
        LongestCommonSubsequence lcs = new LongestCommonSubsequence();
        check("identical strings", 6, lcs.length("ABCDEF", "ABCDEF"));
        check("completely different strings", 0, lcs.length("ABC", "XYZ"));
        check("first string empty", 0, lcs.length("", "ABC"));
        check("second string empty", 0, lcs.length("ABC", ""));
        check("both strings empty", 0, lcs.length("", ""));
        check("classic ABCBDAB / BDCABA", 4, lcs.length("ABCBDAB", "BDCABA"));
        check("classic AGGTAB / GXTXAYB", 4, lcs.length("AGGTAB", "GXTXAYB"));
        check("one character in common", 1, lcs.length("AXX", "YYA"));
        check("symmetry", lcs.length("AGGTAB", "GXTXAYB"), lcs.length("GXTXAYB", "AGGTAB"));
        String sequence = lcs.subsequence("AGGTAB", "GXTXAYB");
        check("reconstructed LCS has the optimal length", 4, sequence.length());
        check("reconstructed LCS is a subsequence of both", true,
                isSubsequence(sequence, "AGGTAB") && isSubsequence(sequence, "GXTXAYB"));
        check("cells computed = n * m", 42L, runAndGetCells(lcs, "ABCBDAB", "BDCABA"));
        RandomStrings generator = new RandomStrings(7, "AB");
        boolean allMatch = true;
        for (int i = 0; i < 300; i++) {
            String a = generator.next(generator.nextInt(9));
            String b = generator.next(generator.nextInt(9));
            if (lcs.length(a, b) != bruteForceLcs(a, b)) {
                allMatch = false;
            }
        }
        check("300 random pairs agree with brute force", true, allMatch);
    }

    private static long runAndGetCells(LongestCommonSubsequence lcs, String a, String b) {
        lcs.length(a, b);
        return lcs.getCellsComputed();
    }

    private static void editDistanceTests() {
        section("Edit Distance");
        EditDistance ed = new EditDistance();
        check("identical strings", 0, ed.distance("algorithm", "algorithm"));
        check("source empty (only insertions)", 3, ed.distance("", "abc"));
        check("target empty (only deletions)", 3, ed.distance("abc", ""));
        check("both empty", 0, ed.distance("", ""));
        check("one substitution cat -> cut", 1, ed.distance("cat", "cut"));
        check("one insertion cat -> cart", 1, ed.distance("cat", "cart"));
        check("one deletion cart -> cat", 1, ed.distance("cart", "cat"));
        check("kitten -> sitting (2 subs + 1 insert)", 3, ed.distance("kitten", "sitting"));
        check("sunday -> saturday", 3, ed.distance("sunday", "saturday"));
        check("completely different, equal length", 3, ed.distance("abc", "xyz"));
        check("symmetry", ed.distance("intention", "execution"), ed.distance("execution", "intention"));
        check("intention -> execution", 5, ed.distance("intention", "execution"));
        RandomStrings generator = new RandomStrings(11, "ABC");
        boolean bounds = true;
        LongestCommonSubsequence lcs = new LongestCommonSubsequence();
        for (int i = 0; i < 300; i++) {
            String a = generator.next(generator.nextInt(12));
            String b = generator.next(generator.nextInt(12));
            int d = ed.distance(a, b);
            int lower = Math.abs(a.length() - b.length());
            int upper = Math.max(a.length(), b.length());
            int indelOnly = a.length() + b.length() - 2 * lcs.length(a, b);
            if (d < lower || d > upper || d > indelOnly) {
                bounds = false;
            }
        }
        check("300 random pairs respect |n-m| <= d <= min(max(n,m), n+m-2*LCS)", true, bounds);
    }

    private static void lpsTests() {
        section("KMP LPS table");
        KMPMatcher kmp = new KMPMatcher();
        check("AABAACAABAA", "[0, 1, 0, 1, 2, 0, 1, 2, 3, 4, 5]", Arrays.toString(kmp.buildLps("AABAACAABAA")));
        check("ABABCABAB", "[0, 0, 1, 2, 0, 1, 2, 3, 4]", Arrays.toString(kmp.buildLps("ABABCABAB")));
        check("AAAA", "[0, 1, 2, 3]", Arrays.toString(kmp.buildLps("AAAA")));
        check("ABCD", "[0, 0, 0, 0]", Arrays.toString(kmp.buildLps("ABCD")));
    }

    private static void stringMatchingTests() {
        section("String matching (Naive, KMP, Rabin-Karp agree)");
        matchCase("pattern at the beginning", "abcxyzxyz", "abc", List.of(0));
        matchCase("pattern in the middle", "xxxabcxxx", "abc", List.of(3));
        matchCase("pattern at the end", "xyzxyzabc", "abc", List.of(6));
        matchCase("multiple occurrences", "abcxabcxabc", "abc", List.of(0, 4, 8));
        matchCase("overlapping occurrences", "aaaaa", "aa", List.of(0, 1, 2, 3));
        matchCase("overlapping periodic pattern", "abababab", "abab", List.of(0, 2, 4));
        matchCase("pattern absent", "abcdefgh", "xyz", List.of());
        matchCase("pattern longer than text", "abc", "abcdef", List.of());
        matchCase("empty pattern", "abc", "", List.of());
        matchCase("empty text and empty pattern", "", "", List.of());
        matchCase("empty text", "", "a", List.of());
        matchCase("pattern equals text", "pattern", "pattern", List.of(0));
        matchCase("single character pattern", "banana", "a", List.of(1, 3, 5));
        matchCase("near miss at the end", "aaaaaaab", "aab", List.of(5));
    }

    private static void matchCase(String name, String text, String pattern, List<Integer> expected) {
        List<StringMatcher> matchers = List.of(new NaiveMatcher(), new KMPMatcher(), new RabinKarpMatcher());
        List<List<Integer>> results = new ArrayList<>();
        for (StringMatcher matcher : matchers) {
            results.add(matcher.search(text, pattern));
        }
        boolean agree = results.get(0).equals(results.get(1)) && results.get(1).equals(results.get(2));
        check(name + " -> " + expected, true, agree && results.get(0).equals(expected));
    }

    private static void rabinKarpCollisionTests() {
        section("Rabin-Karp hash collisions");
        RabinKarpMatcher tiny = new RabinKarpMatcher(256, 3);
        NaiveMatcher naive = new NaiveMatcher();
        RandomStrings generator = new RandomStrings(5, "ACGT");
        String text = generator.next(2_000);
        String pattern = text.substring(700, 708);
        List<Integer> expected = naive.search(text, pattern);
        List<Integer> actual = tiny.search(text, pattern);
        check("modulus 3 still returns the correct occurrences", expected, actual);
        check("modulus 3 produces spurious hash hits", true, tiny.getHashCollisions() > 0);
        check("hash hits = true matches + collisions", tiny.getHashHits(), actual.size() + tiny.getHashCollisions());
        RabinKarpMatcher large = new RabinKarpMatcher();
        large.search(text, pattern);
        check("large modulus has far fewer collisions", true, large.getHashCollisions() < tiny.getHashCollisions());
    }

    private static void randomizedMatchingTests() {
        section("Randomized cross-check");
        RandomStrings generator = new RandomStrings(99, "AB");
        List<StringMatcher> matchers = List.of(new NaiveMatcher(), new KMPMatcher(), new RabinKarpMatcher(), new RabinKarpMatcher(31, 7));
        boolean allAgree = true;
        for (int i = 0; i < 500; i++) {
            String text = generator.next(generator.nextInt(60));
            String pattern = generator.next(1 + generator.nextInt(6));
            List<Integer> oracle = new ArrayList<>();
            for (int s = 0; s + pattern.length() <= text.length(); s++) {
                if (text.startsWith(pattern, s)) {
                    oracle.add(s);
                }
            }
            for (StringMatcher matcher : matchers) {
                if (!matcher.search(text, pattern).equals(oracle)) {
                    allAgree = false;
                }
            }
        }
        check("500 random text/pattern pairs, all matchers equal the oracle", true, allAgree);
    }

    private static void kasaiTests() {
        section("Kasai LCP");
        KasaiLCP kasai = new KasaiLCP();
        int[] bananaSa = {5, 3, 1, 0, 4, 2};
        int[] bananaLcp = {0, 1, 3, 0, 0, 2};
        check("banana: suffix array builder matches manual SA", Arrays.toString(bananaSa), Arrays.toString(SuffixArrayBuilder.build("banana")));
        check("banana: LCP from manual SA", Arrays.toString(bananaLcp), Arrays.toString(kasai.build("banana", bananaSa)));
        int[] mississippiSa = {10, 7, 4, 1, 0, 9, 8, 6, 3, 5, 2};
        int[] mississippiLcp = {0, 1, 1, 4, 0, 0, 1, 0, 2, 1, 3};
        check("mississippi: suffix array builder matches manual SA", Arrays.toString(mississippiSa), Arrays.toString(SuffixArrayBuilder.build("mississippi")));
        check("mississippi: LCP from manual SA", Arrays.toString(mississippiLcp), Arrays.toString(kasai.build("mississippi", mississippiSa)));
        check("single character", "[0]", Arrays.toString(kasai.build("a", new int[] {0})));
        check("empty string", "[]", Arrays.toString(kasai.build("", new int[0])));
        check("aaaa", "[0, 1, 2, 3]", Arrays.toString(kasai.build("aaaa", SuffixArrayBuilder.build("aaaa"))));
        check("rejects invalid suffix array", true, throwsIllegalArgument(() -> kasai.build("abc", new int[] {0, 0, 1})));
        RandomStrings generator = new RandomStrings(3, "AB");
        boolean allMatch = true;
        boolean linear = true;
        for (int i = 0; i < 200; i++) {
            String s = generator.next(1 + generator.nextInt(80));
            int[] sa = SuffixArrayBuilder.build(s);
            int[] lcp = kasai.build(s, sa);
            if (!Arrays.equals(lcp, bruteForceLcp(s, sa))) {
                allMatch = false;
            }
            if (kasai.getComparisons() > 3L * s.length()) {
                linear = false;
            }
        }
        check("200 random strings agree with brute-force LCP", true, allMatch);
        check("character comparisons never exceed 3n", true, linear);
    }

    private static int bruteForceLcs(String a, String b) {
        int best = 0;
        for (int mask = 0; mask < (1 << a.length()); mask++) {
            StringBuilder candidate = new StringBuilder();
            for (int i = 0; i < a.length(); i++) {
                if ((mask & (1 << i)) != 0) {
                    candidate.append(a.charAt(i));
                }
            }
            if (candidate.length() > best && isSubsequence(candidate.toString(), b)) {
                best = candidate.length();
            }
        }
        return best;
    }

    private static boolean isSubsequence(String small, String big) {
        int i = 0;
        for (int j = 0; j < big.length() && i < small.length(); j++) {
            if (small.charAt(i) == big.charAt(j)) {
                i++;
            }
        }
        return i == small.length();
    }

    private static int[] bruteForceLcp(String s, int[] sa) {
        int[] lcp = new int[sa.length];
        for (int i = 1; i < sa.length; i++) {
            int k = 0;
            while (sa[i] + k < s.length() && sa[i - 1] + k < s.length() && s.charAt(sa[i] + k) == s.charAt(sa[i - 1] + k)) {
                k++;
            }
            lcp[i] = k;
        }
        return lcp;
    }

    private static boolean throwsIllegalArgument(Runnable action) {
        try {
            action.run();
            return false;
        } catch (IllegalArgumentException e) {
            return true;
        }
    }

    private static void section(String name) {
        currentSection = name;
        System.out.println();
        System.out.println("[" + name + "]");
    }

    private static void check(String name, Object expected, Object actual) {
        if (Objects.equals(expected, actual)) {
            passed++;
            System.out.println("  PASS  " + name);
        } else {
            failed++;
            System.out.println("  FAIL  " + currentSection + " / " + name + ": expected " + expected + " but was " + actual);
        }
    }
}
