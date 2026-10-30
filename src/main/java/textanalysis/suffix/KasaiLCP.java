package textanalysis.suffix;

import java.util.Objects;

public final class KasaiLCP {

    private long comparisons;

    public int[] build(String s, int[] suffixArray) {
        Objects.requireNonNull(s, "s");
        Objects.requireNonNull(suffixArray, "suffixArray");
        int n = s.length();
        if (suffixArray.length != n) {
            throw new IllegalArgumentException("suffix array length must equal string length");
        }
        int[] rank = new int[n];
        boolean[] seen = new boolean[n];
        for (int i = 0; i < n; i++) {
            int start = suffixArray[i];
            if (start < 0 || start >= n || seen[start]) {
                throw new IllegalArgumentException("suffix array must be a permutation of 0.." + (n - 1));
            }
            seen[start] = true;
            rank[start] = i;
        }
        int[] lcp = new int[n];
        comparisons = 0;
        int k = 0;
        for (int i = 0; i < n; i++) {
            if (rank[i] == 0) {
                k = 0;
                continue;
            }
            int j = suffixArray[rank[i] - 1];
            while (i + k < n && j + k < n) {
                comparisons++;
                if (s.charAt(i + k) != s.charAt(j + k)) {
                    break;
                }
                k++;
            }
            lcp[rank[i]] = k;
            if (k > 0) {
                k--;
            }
        }
        return lcp;
    }

    public long getComparisons() {
        return comparisons;
    }
}
