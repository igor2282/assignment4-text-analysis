package textanalysis.matching;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class RabinKarpMatcher implements StringMatcher {

    public static final long DEFAULT_BASE = 256;
    public static final long DEFAULT_MODULUS = 1_000_000_007L;

    private final long base;
    private final long modulus;
    private long comparisons;
    private long hashCollisions;
    private long hashHits;
    private long preprocessingOperations;

    public RabinKarpMatcher() {
        this(DEFAULT_BASE, DEFAULT_MODULUS);
    }

    public RabinKarpMatcher(long base, long modulus) {
        if (base < 2) {
            throw new IllegalArgumentException("base must be at least 2");
        }
        if (modulus < 2 || modulus > 2_147_483_647L) {
            throw new IllegalArgumentException("modulus must be in [2, 2^31 - 1]");
        }
        this.base = base % modulus;
        this.modulus = modulus;
    }

    @Override
    public String name() {
        return "Rabin-Karp";
    }

    @Override
    public List<Integer> search(String text, String pattern) {
        Objects.requireNonNull(text, "text");
        Objects.requireNonNull(pattern, "pattern");
        comparisons = 0;
        hashCollisions = 0;
        hashHits = 0;
        preprocessingOperations = 0;
        List<Integer> occurrences = new ArrayList<>();
        int n = text.length();
        int m = pattern.length();
        if (m == 0 || m > n) {
            return occurrences;
        }
        long highPower = 1;
        for (int i = 0; i < m - 1; i++) {
            highPower = (highPower * base) % modulus;
        }
        long patternHash = 0;
        long windowHash = 0;
        for (int i = 0; i < m; i++) {
            patternHash = (patternHash * base + pattern.charAt(i)) % modulus;
            windowHash = (windowHash * base + text.charAt(i)) % modulus;
            preprocessingOperations++;
        }
        for (int shift = 0; shift <= n - m; shift++) {
            if (windowHash == patternHash) {
                hashHits++;
                if (verify(text, pattern, shift)) {
                    occurrences.add(shift);
                } else {
                    hashCollisions++;
                }
            }
            if (shift < n - m) {
                windowHash = roll(windowHash, text.charAt(shift), text.charAt(shift + m), highPower);
            }
        }
        return occurrences;
    }

    private long roll(long hash, char outgoing, char incoming, long highPower) {
        long withoutOutgoing = (hash - (outgoing * highPower) % modulus + modulus) % modulus;
        return (withoutOutgoing * base + incoming) % modulus;
    }

    private boolean verify(String text, String pattern, int shift) {
        for (int j = 0; j < pattern.length(); j++) {
            comparisons++;
            if (text.charAt(shift + j) != pattern.charAt(j)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public long getComparisons() {
        return comparisons;
    }

    @Override
    public long getPreprocessingOperations() {
        return preprocessingOperations;
    }

    public long getHashCollisions() {
        return hashCollisions;
    }

    public long getHashHits() {
        return hashHits;
    }

    public long getModulus() {
        return modulus;
    }
}
