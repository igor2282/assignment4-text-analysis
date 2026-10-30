package textanalysis.matching;

import java.util.List;

public interface StringMatcher {

    String name();

    List<Integer> search(String text, String pattern);

    long getComparisons();

    default long getPreprocessingOperations() {
        return 0;
    }
}
