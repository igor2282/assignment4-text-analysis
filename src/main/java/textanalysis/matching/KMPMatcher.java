package textanalysis.matching;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class KMPMatcher implements StringMatcher {

    private long comparisons;
    private long preprocessingComparisons;

    @Override
    public String name() {
        return "KMP";
    }

    public int[] buildLps(String pattern) {
        Objects.requireNonNull(pattern, "pattern");
        int m = pattern.length();
        int[] lps = new int[m];
        preprocessingComparisons = 0;
        int length = 0;
        int i = 1;
        while (i < m) {
            preprocessingComparisons++;
            if (pattern.charAt(i) == pattern.charAt(length)) {
                length++;
                lps[i] = length;
                i++;
            } else if (length > 0) {
                length = lps[length - 1];
            } else {
                lps[i] = 0;
                i++;
            }
        }
        return lps;
    }

    @Override
    public List<Integer> search(String text, String pattern) {
        Objects.requireNonNull(text, "text");
        Objects.requireNonNull(pattern, "pattern");
        comparisons = 0;
        preprocessingComparisons = 0;
        List<Integer> occurrences = new ArrayList<>();
        int n = text.length();
        int m = pattern.length();
        if (m == 0 || m > n) {
            return occurrences;
        }
        int[] lps = buildLps(pattern);
        int j = 0;
        for (int i = 0; i < n; i++) {
            char c = text.charAt(i);
            while (true) {
                comparisons++;
                if (c == pattern.charAt(j)) {
                    j++;
                    break;
                }
                if (j == 0) {
                    break;
                }
                j = lps[j - 1];
            }
            if (j == m) {
                occurrences.add(i - m + 1);
                j = lps[j - 1];
            }
        }
        return occurrences;
    }

    @Override
    public long getComparisons() {
        return comparisons;
    }

    @Override
    public long getPreprocessingOperations() {
        return preprocessingComparisons;
    }
}
