package textanalysis.util;

import java.util.Random;

public final class RandomStrings {

    public static final String ALPHABET = "ACGT";
    public static final long SEED = 42L;

    private final Random random;
    private final String alphabet;

    public RandomStrings(long seed) {
        this(seed, ALPHABET);
    }

    public RandomStrings(long seed, String alphabet) {
        if (alphabet == null || alphabet.isEmpty()) {
            throw new IllegalArgumentException("alphabet must not be empty");
        }
        this.random = new Random(seed);
        this.alphabet = alphabet;
    }

    public String next(int length) {
        char[] chars = new char[length];
        for (int i = 0; i < length; i++) {
            chars[i] = alphabet.charAt(random.nextInt(alphabet.length()));
        }
        return new String(chars);
    }

    public int nextInt(int bound) {
        return random.nextInt(bound);
    }
}
