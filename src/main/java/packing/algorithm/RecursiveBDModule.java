package packing.algorithm;

import java.util.*;

public class RecursiveBDModule {
    private static XYGenerator xy_gen = null;
    private static int N, l, w;

    /**
     * A class to represent a 4-tuple of integers
     */
    public static class Tuple4<A, B, C, D> {
        private final A first;
        private final B second;
        private final C third;
        private final D fourth;

        public Tuple4(A first, B second, C third, D fourth) {
            this.first = first;
            this.second = second;
            this.third = third;
            this.fourth = fourth;
        }

        public A getFirst() { return first; }
        public B getSecond() { return second; }
        public C getThird() { return third; }
        public D getFourth() { return fourth; }
    }

    /**
     * A class to represent a result containing both count and blocks
     */
    public static class Result {
        private final int count;
        private final List<Tuple4<Integer, Integer, Integer, Integer>> blocks;

        public Result(int count, List<Tuple4<Integer, Integer, Integer, Integer>> blocks) {
            this.count = count;
            this.blocks = blocks;
        }

        public int getCount() { return count; }
        public List<Tuple4<Integer, Integer, Integer, Integer>> getBlocks() { return blocks; }
    }

    /**
     * Compute lower bound of box placements
     */
    public static int compute_zlb(int L, int W, int l, int w) {
        return Math.max((L / l) * (W / w), (L / w) * (W / l));
    }

    /**
     * Compute upper bound of box placements
     */
    public static int compute_zub(int L, int W, int l, int w) {
        return (L * W) / (l * w);
    }

    /**
     * Pack blocks into the area
     */
    public static List<Tuple4<Integer, Integer, Integer, Integer>> pack_blocks(
            int L0, int W0, int l, int w,
            List<Tuple4<Integer, Integer, Integer, Integer>> blocks) {

        List<Tuple4<Integer, Integer, Integer, Integer>> packing = new ArrayList<>();

        for (Tuple4<Integer, Integer, Integer, Integer> block : blocks) {
            int x_offset_i = block.getFirst();
            int y_offset_i = block.getSecond();
            int Li = block.getThird();
            int Wi = block.getFourth();

            // Decide orientation
            if ((Li / l) * (Wi / w) < (Li / w) * (Wi / l)) {
                int temp = l;
                l = w;
                w = temp;
            }

            // Pack within the block
            for (int i = 0; i < Li / l; ++i) {
                for (int j = 0; j < Wi / w; ++j) {
                    packing.add(new Tuple4<>(
                            i * l + x_offset_i,
                            j * w + y_offset_i,
                            l, w
                    ));
                }
            }
        }

        return packing;
    }

    /**
     * Sum a list of integers
     */
    public static int sum_vector(List<Integer> vec) {
        int sum = 0;
        for (int value : vec) {
            sum += value;
        }
        return sum;
    }

    /**
     * Check if a pattern is symmetrical
     */
    public static boolean isSymmetrical(int L0, int W0, int x1, int x2, int y1, int y2) {
        // 5-blocks symmetry check
        if (x1 > 0 && y1 > 0 && x2 > x1 && y2 > y1 &&
                ((x1 + x2 < L0) || (x1 + x2 == L0 && y1 + y2 <= W0))) {
            return false; // Not symmetrical, don't discard
        }

        // 4-blocks symmetry check
        if (x1 > 0 && y1 > 0 &&
                ((x2 == x1 && y2 > y1 && x1 <= L0 / 2) ||
                        (x2 > x1 && y2 == y1 && y1 <= W0 / 2) ||
                        (x2 == x1 && y2 == y1 && x1 <= L0 / 2 && y1 <= W0 / 2))) {
            return false; // Not symmetrical, don't discard
        }

        // 3-blocks symmetry check
        if ((x1 > 0 && y1 == 0 && x2 == x1 && 0 < y2 && y2 <= W0 / 2) ||
                (x1 == 0 && y1 > 0 && 0 < x2 && x2 <= L0 / 2 && y2 == y1)) {
            return false; // Not symmetrical, don't discard
        }

        // 2-blocks symmetry check
        if ((0 < x1 && x1 <= L0 / 2 && y1 == 0 && x2 == x1 && y2 == 0) ||
                (x1 == 0 && 0 < y1 && y1 <= W0 / 2 && x2 == 0 && y2 >= y1)) {
            return false; // Not symmetrical, don't discard
        }

        return true; // Symmetrical, discard
    }

    /**
     * Recursive implementation of the B&D heuristic
     */
    public static Result recursive_block_decomposition(
            int L0, int W0, int n,
            int x_offset, int y_offset) {

        // Compute lower and upper bounds
        int zlb = compute_zlb(L0, W0, l, w);
        int zub = xy_gen.get_zub(L0, W0);

        // Base cases
        if (zlb == 0) return new Result(zlb, new ArrayList<>());
        if (zlb == zub) {
            List<Tuple4<Integer, Integer, Integer, Integer>> blocks = new ArrayList<>();
            blocks.add(new Tuple4<>(x_offset, y_offset, L0, W0));
            return new Result(zlb, blocks);
        }

        // Initial best blocks
        List<Tuple4<Integer, Integer, Integer, Integer>> best_blocks = new ArrayList<>();
        best_blocks.add(new Tuple4<>(x_offset, y_offset, L0, W0));

        XYGenerator.Pair<List<Integer>, List<Integer>> xyPair = xy_gen.get_XY(L0, W0);
        List<Integer> X = xyPair.getFirst();
        List<Integer> Y = xyPair.getSecond();

        // Try different block divisions
        for (int x1 : X) {
            for (int x2 : X) {
                if (x1 > x2) continue;
                for (int y1 : Y) {
                    for (int y2 : Y) {
                        if (y1 > y2) continue;

                        // Reduce the search by discarding symmetrical patterns
                        if (isSymmetrical(L0, W0, x1, x2, y1, y2))
                            continue;

                        // Define sub-blocks
                        int L1 = x1, W1 = W0 - y1;
                        int L2 = L0 - x1, W2 = W0 - y2;
                        int L3 = x2 - x1, W3 = y2 - y1;
                        int L4 = x2, W4 = y1;
                        int L5 = L0 - x2, W5 = y2;

                        // Create block list
                        List<Tuple4<Integer, Integer, Integer, Integer>> blocks = new ArrayList<>();
                        blocks.add(new Tuple4<>(x_offset, W4 + y_offset, L1, W1));
                        blocks.add(new Tuple4<>(L1 + x_offset, W3 + W4 + y_offset, L2, W2));
                        blocks.add(new Tuple4<>(L1 + x_offset, W4 + y_offset, L3, W3));
                        blocks.add(new Tuple4<>(x_offset, y_offset, L4, W4));
                        blocks.add(new Tuple4<>(L1 + L3 + x_offset, y_offset, L5, W5));

                        int z_0 = 0;
                        List<Tuple4<Integer, Integer, Integer, Integer>> temp_blocks = new ArrayList<>();
                        List<Integer> zi = new ArrayList<>();

                        // Recursive exploration
                        for (Tuple4<Integer, Integer, Integer, Integer> block : blocks) {
                            int Li = block.getThird();
                            int Wi = block.getFourth();
                            zi.add(compute_zub(Li, Wi, l, w)); // z_i are now all z_ub_i
                        }

                        if (n < N) {
                            z_0 = sum_vector(zi);
                            for (int i = 0; i < 5; i++) {
                                if (zlb >= z_0) {
                                    break;
                                } else {
                                    Tuple4<Integer, Integer, Integer, Integer> block = blocks.get(i);
                                    int x_offset_i = block.getFirst();
                                    int y_offset_i = block.getSecond();
                                    int Li = block.getThird();
                                    int Wi = block.getFourth();

                                    Result recursiveResult = recursive_block_decomposition(
                                            Li, Wi, n + 1, x_offset_i, y_offset_i
                                    );

                                    temp_blocks.addAll(recursiveResult.getBlocks());
                                    zi.set(i, recursiveResult.getCount());
                                    z_0 = sum_vector(zi);
                                }
                            }
                        } else {
                            // At max recursion, compute lower bounds
                            for (Tuple4<Integer, Integer, Integer, Integer> block : blocks) {
                                int Li = block.getThird();
                                int Wi = block.getFourth();
                                z_0 += compute_zlb(Li, Wi, l, w);
                            }
                        }

                        // Update best solution
                        if (z_0 > zlb) {
                            zlb = z_0;
                            best_blocks = (n == N) ? blocks : temp_blocks;

                            // Early exit if upper bound reached
                            if (zlb == zub) return new Result(zlb, best_blocks);
                        }
                    }
                }
            }
        }

        return new Result(zlb, best_blocks);
    }

    /**
     * Main pallet loading function
     */
    public static Result palletLoading(
            int L0, int W0, int n, int N0, int l0, int w0,
            int x_offset, int y_offset) {

        N = N0;
        l = l0;
        w = w0;
        xy_gen = new XYGenerator(L0, W0, l, w);

        return recursive_block_decomposition(L0, W0, n, x_offset, y_offset);
    }

    /**
     * Main pallet loading function with default offsets
     */
    public static Result palletLoading(int L0, int W0, int n, int N0, int l0, int w0) {
        return palletLoading(L0, W0, n, N0, l0, w0, 0, 0);
    }

    /**
     * Main method for testing
     */
    public static void main(String[] args) {
        // Example usage
        int L0 = 100;
        int W0 = 100;
        int n = 0;
        int N0 = 3;
        int l0 = 7;
        int w0 = 5;

        Result result = palletLoading(L0, W0, n, N0, l0, w0);

        System.out.println("Optimal packing count: " + result.getCount());
        System.out.println("Blocks: ");
        for (Tuple4<Integer, Integer, Integer, Integer> block : result.getBlocks()) {
            System.out.println("(" + block.getFirst() + ", " + block.getSecond() + ", " +
                    block.getThird() + ", " + block.getFourth() + ")");
        }
    }
}