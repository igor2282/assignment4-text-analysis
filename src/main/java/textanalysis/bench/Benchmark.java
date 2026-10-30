package textanalysis.bench;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.LongSupplier;
import textanalysis.dp.EditDistance;
import textanalysis.dp.LongestCommonSubsequence;
import textanalysis.matching.KMPMatcher;
import textanalysis.matching.NaiveMatcher;
import textanalysis.matching.RabinKarpMatcher;
import textanalysis.matching.StringMatcher;
import textanalysis.suffix.KasaiLCP;
import textanalysis.suffix.SuffixArrayBuilder;
import textanalysis.util.RandomStrings;

public final class Benchmark {

    public static final int WARMUP_RUNS = 10;
    public static final int MEASURED_RUNS = 5;
    public static final int[] DP_SIZES = {100, 500, 1_000};
    public static final int[] TEXT_SIZES = {10_000, 50_000, 100_000};
    public static final int[] KASAI_SIZES = {1_000, 5_000, 10_000};
    public static final int PATTERN_LENGTH = 16;

    private static long sink;

    private Benchmark() {
    }

    public static void main(String[] args) throws IOException {
        Locale.setDefault(Locale.US);
        Path outputDir = Paths.get(args.length > 0 ? args[0] : "results");
        Files.createDirectories(outputDir);
        System.out.printf("Java %s, %s %s, seed=%d, alphabet=%s, warmup=%d, runs=%d%n",
                System.getProperty("java.version"), System.getProperty("os.name"), System.getProperty("os.arch"),
                RandomStrings.SEED, RandomStrings.ALPHABET, WARMUP_RUNS, MEASURED_RUNS);

        globalWarmup();
        List<String> dpRows = runDynamicProgramming();
        List<String> matchingRows = runStringMatching();
        List<String> kasaiRows = runKasai();

        write(outputDir.resolve("dp_results.csv"), "algorithm,n,m,result,cells,avg_ns,avg_ms,min_ms,max_ms", dpRows);
        write(outputDir.resolve("matching_results.csv"),
                "scenario,algorithm,text_length,pattern_length,occurrences,comparisons,preprocessing_ops,hash_collisions,avg_ns,avg_ms,min_ms,max_ms",
                matchingRows);
        write(outputDir.resolve("kasai_results.csv"), "n,max_lcp,comparisons,avg_ns,avg_ms,min_ms,max_ms", kasaiRows);
        System.out.println("Results written to " + outputDir.toAbsolutePath());
        if (sink == 42) {
            System.out.println();
        }
    }

    private static void globalWarmup() {
        RandomStrings generator = new RandomStrings(RandomStrings.SEED + 100);
        LongestCommonSubsequence lcs = new LongestCommonSubsequence();
        EditDistance editDistance = new EditDistance();
        List<StringMatcher> matchers = List.of(new NaiveMatcher(), new KMPMatcher(), new RabinKarpMatcher());
        KasaiLCP kasai = new KasaiLCP();
        String a = generator.next(300);
        String b = generator.next(300);
        String text = generator.next(20_000);
        String pattern = text.substring(5_000, 5_000 + PATTERN_LENGTH);
        String repetitive = "A".repeat(20_000);
        String repetitivePattern = "A".repeat(PATTERN_LENGTH - 1) + "C";
        int[] suffixArray = SuffixArrayBuilder.build(text);
        for (int i = 0; i < 200; i++) {
            sink += lcs.length(a, b) + editDistance.distance(a, b);
            for (StringMatcher matcher : matchers) {
                sink += matcher.search(text, pattern).size() + matcher.search(repetitive, repetitivePattern).size();
            }
            sink += kasai.build(text, suffixArray).length;
        }
    }

    private static List<String> runDynamicProgramming() {
        List<String> rows = new ArrayList<>();
        RandomStrings generator = new RandomStrings(RandomStrings.SEED);
        LongestCommonSubsequence lcs = new LongestCommonSubsequence();
        EditDistance editDistance = new EditDistance();
        System.out.println();
        System.out.println("Part A: LCS and Edit Distance");
        for (int n : DP_SIZES) {
            String a = generator.next(n);
            String b = generator.next(n);
            Timing lcsTiming = measure(() -> lcs.length(a, b));
            int lcsResult = lcs.length(a, b);
            rows.add(dpRow("LCS", n, lcsResult, lcs.getCellsComputed(), lcsTiming));
            Timing edTiming = measure(() -> editDistance.distance(a, b));
            int edResult = editDistance.distance(a, b);
            rows.add(dpRow("EditDistance", n, edResult, editDistance.getCellsComputed(), edTiming));
            System.out.printf("  n=%-6d LCS=%-5d %8.3f ms   ED=%-5d %8.3f ms%n", n, lcsResult, lcsTiming.avgMs(), edResult, edTiming.avgMs());
        }
        return rows;
    }

    private static String dpRow(String name, int n, int result, long cells, Timing timing) {
        return String.format("%s,%d,%d,%d,%d,%s", name, n, n, result, cells, timing.csv());
    }

    private static List<String> runStringMatching() {
        List<String> rows = new ArrayList<>();
        RandomStrings generator = new RandomStrings(RandomStrings.SEED + 1);
        NaiveMatcher naive = new NaiveMatcher();
        KMPMatcher kmp = new KMPMatcher();
        RabinKarpMatcher rabinKarp = new RabinKarpMatcher();
        List<StringMatcher> matchers = List.of(naive, kmp, rabinKarp);
        System.out.println();
        System.out.println("Part B: Naive vs KMP vs Rabin-Karp (pattern length " + PATTERN_LENGTH + ")");
        for (int n : TEXT_SIZES) {
            String text = generator.next(n);
            String present = text.substring(n / 2, n / 2 + PATTERN_LENGTH);
            String absent = absentPattern(generator, naive, text);
            String repetitiveText = "A".repeat(n);
            String repetitivePattern = "A".repeat(PATTERN_LENGTH - 1) + "C";
            runScenario(rows, matchers, "present", text, present);
            runScenario(rows, matchers, "absent", text, absent);
            runScenario(rows, matchers, "worst-case", repetitiveText, repetitivePattern);
        }
        return rows;
    }

    private static String absentPattern(RandomStrings generator, NaiveMatcher naive, String text) {
        while (true) {
            String candidate = generator.next(PATTERN_LENGTH);
            if (naive.search(text, candidate).isEmpty()) {
                return candidate;
            }
        }
    }

    private static void runScenario(List<String> rows, List<StringMatcher> matchers, String scenario, String text, String pattern) {
        List<Integer> reference = null;
        StringBuilder line = new StringBuilder(String.format("  %-10s n=%-7d", scenario, text.length()));
        for (StringMatcher matcher : matchers) {
            Timing timing = measure(() -> matcher.search(text, pattern).size());
            List<Integer> occurrences = matcher.search(text, pattern);
            if (reference == null) {
                reference = occurrences;
            } else if (!reference.equals(occurrences)) {
                throw new IllegalStateException(matcher.name() + " disagrees with Naive on scenario " + scenario);
            }
            long collisions = matcher instanceof RabinKarpMatcher rk ? rk.getHashCollisions() : 0;
            rows.add(String.format("%s,%s,%d,%d,%d,%d,%d,%d,%s", scenario, matcher.name(), text.length(), pattern.length(),
                    occurrences.size(), matcher.getComparisons(), matcher.getPreprocessingOperations(), collisions, timing.csv()));
            line.append(String.format("  %s=%.3f ms", matcher.name(), timing.avgMs()));
        }
        System.out.println(line);
    }

    private static List<String> runKasai() {
        List<String> rows = new ArrayList<>();
        RandomStrings generator = new RandomStrings(RandomStrings.SEED + 2);
        KasaiLCP kasai = new KasaiLCP();
        System.out.println();
        System.out.println("Part C: Kasai LCP");
        for (int n : KASAI_SIZES) {
            String s = generator.next(n);
            int[] suffixArray = SuffixArrayBuilder.build(s);
            Timing timing = measure(() -> kasai.build(s, suffixArray).length);
            int[] lcp = kasai.build(s, suffixArray);
            int maxLcp = 0;
            for (int value : lcp) {
                maxLcp = Math.max(maxLcp, value);
            }
            rows.add(String.format("%d,%d,%d,%s", n, maxLcp, kasai.getComparisons(), timing.csv()));
            System.out.printf("  n=%-6d comparisons=%-7d max LCP=%-3d %8.4f ms%n", n, kasai.getComparisons(), maxLcp, timing.avgMs());
        }
        return rows;
    }

    private static Timing measure(LongSupplier task) {
        for (int i = 0; i < WARMUP_RUNS; i++) {
            sink += task.getAsLong();
        }
        long total = 0;
        long min = Long.MAX_VALUE;
        long max = Long.MIN_VALUE;
        for (int i = 0; i < MEASURED_RUNS; i++) {
            long start = System.nanoTime();
            long value = task.getAsLong();
            long elapsed = System.nanoTime() - start;
            sink += value;
            total += elapsed;
            min = Math.min(min, elapsed);
            max = Math.max(max, elapsed);
        }
        return new Timing(total / MEASURED_RUNS, min, max);
    }

    private static void write(Path file, String header, List<String> rows) throws IOException {
        List<String> lines = new ArrayList<>();
        lines.add(header);
        lines.addAll(rows);
        Files.write(file, lines, StandardCharsets.UTF_8);
    }

    private record Timing(long avgNs, long minNs, long maxNs) {

        double avgMs() {
            return avgNs / 1_000_000.0;
        }

        String csv() {
            return String.format("%d,%.4f,%.4f,%.4f", avgNs, avgNs / 1_000_000.0, minNs / 1_000_000.0, maxNs / 1_000_000.0);
        }
    }
}
