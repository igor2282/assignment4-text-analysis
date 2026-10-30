package textanalysis.dp;

import java.util.Objects;

public final class EditDistance {

    private long cellsComputed;

    public int distance(String source, String target) {
        int[][] dp = buildTable(source, target);
        return dp[source.length()][target.length()];
    }

    public int[][] buildTable(String source, String target) {
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(target, "target");
        int n = source.length();
        int m = target.length();
        int[][] dp = new int[n + 1][m + 1];
        for (int i = 0; i <= n; i++) {
            dp[i][0] = i;
        }
        for (int j = 0; j <= m; j++) {
            dp[0][j] = j;
        }
        cellsComputed = 0;
        for (int i = 1; i <= n; i++) {
            char cs = source.charAt(i - 1);
            for (int j = 1; j <= m; j++) {
                int substitutionCost = cs == target.charAt(j - 1) ? 0 : 1;
                int substitute = dp[i - 1][j - 1] + substitutionCost;
                int delete = dp[i - 1][j] + 1;
                int insert = dp[i][j - 1] + 1;
                dp[i][j] = Math.min(substitute, Math.min(delete, insert));
                cellsComputed++;
            }
        }
        return dp;
    }

    public long getCellsComputed() {
        return cellsComputed;
    }
}
