package firstVersion;

public class PerformanceBenchmark {

    public static void main(String[] args) {
        BoardConf conf = new MassiveBoardConf();
        runBenchmark(conf, false, 150, 16L);
        runBenchmark(conf, true, 150, 16L);
    }

    private static void runBenchmark(BoardConf conf, boolean useThreads, int frames, long dt) {
        Board board = new Board();
        board.init(conf);
        board.setUseThreads(useThreads);

        // Warm-up.
        for (int i = 0; i < 20; i++) {
            board.updateState(dt);
        }

        board.resetPerformanceMetrics();
        long totalUpdateStateNanos = 0L;

        for (int i = 0; i < frames; i++) {
            long t0 = System.nanoTime();
            board.updateState(dt);
            totalUpdateStateNanos += (System.nanoTime() - t0);
        }

        double avgMovingMs = board.getAverageMovingBallsNanos() / 1_000_000.0;
        double avgCollisionMs = board.getAverageResolveSmallBallsCollisionNanos() / 1_000_000.0;
        double avgTotalMs = (totalUpdateStateNanos / (double) frames) / 1_000_000.0;
        String mode = useThreads ? "platform-threads" : "sequential";

        System.out.println("Mode: " + mode);
        System.out.println("Average updateMovingBalls: " + avgMovingMs + " ms");
        System.out.println("Average resolveSmallBallCollisions: " + avgCollisionMs + " ms");
        System.out.println("Average updateState: " + avgTotalMs + " ms");
        System.out.println();
    }
}
