package textanalysis;

import java.util.Objects;
import textanalysis.dp.EditDistance;
import textanalysis.dp.LongestCommonSubsequence;
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
