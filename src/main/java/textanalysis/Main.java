package textanalysis;

import java.util.Arrays;
import java.util.List;
import textanalysis.dp.EditDistance;
import textanalysis.dp.LongestCommonSubsequence;
import textanalysis.matching.KMPMatcher;
import textanalysis.matching.NaiveMatcher;
import textanalysis.matching.RabinKarpMatcher;
import textanalysis.matching.StringMatcher;
import textanalysis.suffix.KasaiLCP;
import textanalysis.suffix.SuffixArrayBuilder;

public final class Main {

    public static void main(String[] args) {
        System.out.println("=== Part A: Dynamic Programming ===");
        LongestCommonSubsequence lcs = new LongestCommonSubsequence();
        String a = "ABCBDAB";
        String b = "BDCABA";
        System.out.printf("LCS(%s, %s) length = %d, one LCS = %s%n", a, b, lcs.length(a, b), lcs.subsequence(a, b));

        EditDistance editDistance = new EditDistance();
        String source = "kitten";
        String target = "sitting";
        System.out.printf("EditDistance(%s, %s) = %d%n", source, target, editDistance.distance(source, target));

        System.out.println();
        System.out.println("=== Part B: String Matching ===");
        String text = "ABABDABACDABABCABABABABCABAB";
        String pattern = "ABABCABAB";
        List<StringMatcher> matchers = List.of(new NaiveMatcher(), new KMPMatcher(), new RabinKarpMatcher());
        System.out.printf("text = %s, pattern = %s%n", text, pattern);
        for (StringMatcher matcher : matchers) {
            List<Integer> occurrences = matcher.search(text, pattern);
            System.out.printf("%-10s -> %s (comparisons = %d)%n", matcher.name(), occurrences, matcher.getComparisons());
        }
        System.out.println("KMP LPS table of " + pattern + " = " + Arrays.toString(new KMPMatcher().buildLps(pattern)));

        System.out.println();
        System.out.println("=== Part C: Kasai LCP ===");
        String word = "banana";
        int[] suffixArray = SuffixArrayBuilder.build(word);
        int[] lcp = new KasaiLCP().build(word, suffixArray);
        System.out.println("string       = " + word);
        System.out.println("suffix array = " + Arrays.toString(suffixArray));
        System.out.println("LCP array    = " + Arrays.toString(lcp));
        for (int i = 0; i < suffixArray.length; i++) {
            System.out.printf("  SA[%d] = %d  LCP = %d  %s%n", i, suffixArray[i], lcp[i], word.substring(suffixArray[i]));
        }
    }
}
