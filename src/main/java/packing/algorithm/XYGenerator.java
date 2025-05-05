package packing.algorithm;

import java.util.*;

public class XYGenerator {
    private int L;
    private int W;
    private int l;
    private int w;
    private boolean initialized;
    private List<Integer> full_X;
    private List<Integer> full_Y;
    private Map<Pair<Integer, Integer>, Pair<List<Integer>, List<Integer>>> memo;

    // Constructor
    public XYGenerator(int maxL, int maxW, int palletL, int palletW) {
        this.L = maxL;
        this.W = maxW;
        this.l = palletL;
        this.w = palletW;
        this.initialized = false;
        this.full_X = new ArrayList<>();
        this.full_Y = new ArrayList<>();
        this.memo = new HashMap<>();
        initialize_XY();  // Precompute full_X and full_Y once
    }

    // Private method to initialize full_X and full_Y
    private void initialize_XY() {
        if (initialized) return;

        Set<Integer> unique_X = new TreeSet<>();
        Set<Integer> unique_Y = new TreeSet<>();

        // Compute all possible X coordinates
        for (int r = 0; r <= L / l; ++r) {
            for (int s = 0; s <= L / w; ++s) {
                int value = r * l + s * w;
                if (value <= L - w) unique_X.add(value);
            }
        }

        // Compute all possible Y coordinates
        for (int t = 0; t <= W / l; ++t) {
            for (int u = 0; u <= W / w; ++u) {
                int value = t * l + u * w;
                if (value <= W - w) unique_Y.add(value);
            }
        }

        // Convert to sorted lists
        full_X = new ArrayList<>(unique_X);
        full_Y = new ArrayList<>(unique_Y);
        Collections.sort(full_X);
        Collections.sort(full_Y);

        initialized = true;
    }

    // Public method to get filtered X and Y
    public Pair<List<Integer>, List<Integer>> get_XY(int L, int W) {
        if (L < w || W < w)
            return new Pair<>(new ArrayList<>(), new ArrayList<>());  // Return a pair of empty lists

        // Check memo table
        Pair<Integer, Integer> key = new Pair<>(L, W);
        if (memo.containsKey(key)) return memo.get(key);

        initialize_XY();  // Ensure full_X and full_Y are initialized

        // Use binary search to extract subset
        List<Integer> X = new ArrayList<>();
        List<Integer> Y = new ArrayList<>();

        int xEndIndex = Collections.binarySearch(full_X, L - w + 1);
        if (xEndIndex < 0) xEndIndex = -xEndIndex - 1;
        X.addAll(full_X.subList(0, xEndIndex));

        int yEndIndex = Collections.binarySearch(full_Y, W - w + 1);
        if (yEndIndex < 0) yEndIndex = -yEndIndex - 1;
        Y.addAll(full_Y.subList(0, yEndIndex));

        // Store result in memoization table
        Pair<List<Integer>, List<Integer>> result = new Pair<>(X, Y);
        memo.put(key, result);
        return result;
    }

    public int get_zub(int L0, int W0) {
        if (L0 < w || W0 < w)
            return 0;

        // Initialize if needed
        initialize_XY();

        // Use binary search to find appropriate bounds
        int xEndIndex = Collections.binarySearch(full_X, L0 + 1);
        if (xEndIndex < 0) xEndIndex = -xEndIndex - 1;
        if (xEndIndex == 0) return 0;

        int yEndIndex = Collections.binarySearch(full_Y, W0 + 1);
        if (yEndIndex < 0) yEndIndex = -yEndIndex - 1;
        if (yEndIndex == 0) return 0;

        // Get the largest valid coordinates
        int L_star = full_X.get(xEndIndex - 1);
        int W_star = full_Y.get(yEndIndex - 1);

        return (L_star * W_star) / (l * w);
    }

    // Utility class for pairs
    public static class Pair<K, V> {
        private final K first;
        private final V second;

        public Pair(K first, V second) {
            this.first = first;
            this.second = second;
        }

        public K getFirst() {
            return first;
        }

        public V getSecond() {
            return second;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Pair<?, ?> pair = (Pair<?, ?>) o;
            return Objects.equals(first, pair.first) && Objects.equals(second, pair.second);
        }

        @Override
        public int hashCode() {
            return Objects.hash(first, second);
        }
    }
}
