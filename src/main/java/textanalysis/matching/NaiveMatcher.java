package textanalysis.matching;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class NaiveMatcher implements StringMatcher {

    private long comparisons;

    @Override
    public String name() {
        return "Naive";
    }

    @Override
    public List<Integer> search(String text, String pattern) {
        Objects.requireNonNull(text, "text");
        Objects.requireNonNull(pattern, "pattern");
        comparisons = 0;
        List<Integer> occurrences = new ArrayList<>();
        int n = text.length();
        int m = pattern.length();
        if (m == 0 || m > n) {
            return occurrences;
        }
        for (int shift = 0; shift <= n - m; shift++) {
            int j = 0;
            while (j < m) {
                comparisons++;
                if (text.charAt(shift + j) != pattern.charAt(j)) {
                    break;
                }
                j++;
            }
            if (j == m) {
                occurrences.add(shift);
            }
        }
        return occurrences;
    }

    @Override
    public long getComparisons() {
        return comparisons;
    }
}
