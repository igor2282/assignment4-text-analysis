package textanalysis.dp;

import java.util.Objects;

public final class LongestCommonSubsequence {

    private long cellsComputed;

    public int length(String a, String b) {
        int[][] dp = buildTable(a, b);
        return dp[a.length()][b.length()];
    }

    public String subsequence(String a, String b) {
        int[][] dp = buildTable(a, b);
        StringBuilder result = new StringBuilder();
        int i = a.length();
        int j = b.length();
        while (i > 0 && j > 0) {
            if (a.charAt(i - 1) == b.charAt(j - 1)) {
                result.append(a.charAt(i - 1));
                i--;
                j--;
            } else if (dp[i - 1][j] >= dp[i][j - 1]) {
                i--;
            } else {
                j--;
            }
        }
        return result.reverse().toString();
    }

    public int[][] buildTable(String a, String b) {
        Objects.requireNonNull(a, "a");
        Objects.requireNonNull(b, "b");
        int n = a.length();
        int m = b.length();
        int[][] dp = new int[n + 1][m + 1];
        cellsComputed = 0;
        for (int i = 1; i <= n; i++) {
            char ca = a.charAt(i - 1);
            for (int j = 1; j <= m; j++) {
                if (ca == b.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1] + 1;
                } else {
                    dp[i][j] = Math.max(dp[i - 1][j], dp[i][j - 1]);
                }
                cellsComputed++;
            }
        }
        return dp;
    }

    public long getCellsComputed() {
        return cellsComputed;
    }
}
