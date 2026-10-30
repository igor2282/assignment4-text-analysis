package textanalysis.suffix;

import java.util.Arrays;
import java.util.Objects;

public final class SuffixArrayBuilder {

    private SuffixArrayBuilder() {
    }

    public static int[] build(String s) {
        Objects.requireNonNull(s, "s");
        int n = s.length();
        Integer[] order = new Integer[n];
        for (int i = 0; i < n; i++) {
            order[i] = i;
        }
        Arrays.sort(order, (x, y) -> compareSuffixes(s, x, y));
        int[] sa = new int[n];
        for (int i = 0; i < n; i++) {
            sa[i] = order[i];
        }
        return sa;
    }

    static int compareSuffixes(String s, int x, int y) {
        int n = s.length();
        while (x < n && y < n) {
            char a = s.charAt(x);
            char b = s.charAt(y);
            if (a != b) {
                return Character.compare(a, b);
            }
            x++;
            y++;
        }
        return Integer.compare(n - x, n - y);
    }
}
