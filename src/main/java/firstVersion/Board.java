package firstVersion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Board {

    private List<Ball> balls;
    private Ball humanBall;
    private Ball botBall;
    private Boundary bounds;
    private int humanScore;
    private int botScore;
    private P2d leftHole;
    private P2d rightHole;
    private double holeRadius;
    private boolean gameOver;
    private Ball.Type winner;
    private boolean useThreads = true;

    private int n_threads = 4;
    private Thread[] workers = new Thread[n_threads];
    private long movingBallsTotalNanos;
    private long movingBallsMeasurements;
    private long resolveSmallBallsCollisionTotalNanos;
    private long resolveSmallBallsCollisionMeasurements;
    private double collisionCellSize;

    public Board() {
    }

    public void init(BoardConf conf) {
        gameOver = false;
        winner = Ball.Type.BOT;
        humanScore = 0;
        botScore = 0;
        balls = conf.getSmallBalls();
        humanBall = conf.getPlayerBall();
        botBall = conf.getBotBall();
        bounds = conf.getBoardBoundary();
        leftHole = new P2d(bounds.x0(), bounds.y1());
        rightHole = new P2d(bounds.x1(), bounds.y1());
        holeRadius = 0.10;
        collisionCellSize = computeCollisionCellSize();
        resetPerformanceMetrics();
    }

    public void updateState(long dt) {
        long movingT0 = System.nanoTime();
        updateMovingBalls(dt);
        movingBallsTotalNanos += (System.nanoTime() - movingT0);
        movingBallsMeasurements++;

        long t0 = System.nanoTime();
        resolveSmallBallCollisions();
        resolveSmallBallsCollisionTotalNanos += (System.nanoTime() - t0);
        resolveSmallBallsCollisionMeasurements++;
        resolvePlayerCollisions();
        handlePocketedBalls();
        checkPlayerBallIsInHole();
        checkNoMoreSmallBalls();
    }

    private void updateMovingBalls(long dt) {
        humanBall.updateState(dt, this);
        botBall.updateState(dt, this);

        if(useThreads) {
            for (int t = 0; t < n_threads; t++) {
                int start = t * balls.size() / n_threads;
                int end = (t + 1) * balls.size() / n_threads;

                workers[t] = new Thread(() -> {
                    for (int i = start; i < end; i++) {
                        balls.get(i).updateState(dt, this);
                    }
                });

                workers[t].start();
            }

            for (int t = 0; t < n_threads; t++) {
                try {
                    workers[t].join();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException("Worker thread interrupted", e);
                }
            }
        } else {
            for (var b : balls) {
                b.updateState(dt, this);
            }
        }
    }

    private void resolveSmallBallCollisions() {
        Map<Long, List<Ball>> grid = buildCollisionGrid();
        List<Map.Entry<Long, List<Ball>>> entries = new ArrayList<>(grid.entrySet());

        if (!useThreads) {
            processCollisionCells(entries, grid, 0, entries.size());
            return;
        }

        for (int t = 0; t < n_threads; t++) {
            int start = t * entries.size() / n_threads;
            int end = (t + 1) * entries.size() / n_threads;
            workers[t] = new Thread(() -> processCollisionCells(entries, grid, start, end));
            workers[t].start();
        }

        for (int t = 0; t < n_threads; t++) {
            try {
                workers[t].join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Worker thread interrupted", e);
            }
        }
    }

    private void processCollisionCells(List<Map.Entry<Long, List<Ball>>> entries,
                                       Map<Long, List<Ball>> grid,
                                       int start,
                                       int end) {
        for (int entryIndex = start; entryIndex < end; entryIndex++) {
            Map.Entry<Long, List<Ball>> entry = entries.get(entryIndex);
            long cellKey = entry.getKey();
            List<Ball> cellBalls = entry.getValue();
            int cellX = unpackCellX(cellKey);
            int cellY = unpackCellY(cellKey);

            resolveBallPairs(cellBalls, cellBalls);

            for (int offsetX = -1; offsetX <= 1; offsetX++) {
                for (int offsetY = -1; offsetY <= 1; offsetY++) {
                    if (offsetX == 0 && offsetY == 0) {
                        continue;
                    }
                    int neighborX = cellX + offsetX;
                    int neighborY = cellY + offsetY;
                    if (!isCanonicalNeighbor(cellX, cellY, neighborX, neighborY)) {
                        continue;
                    }
                    List<Ball> neighborBalls = grid.get(packCell(neighborX, neighborY));
                    if (neighborBalls != null) {
                        resolveBallPairs(cellBalls, neighborBalls);
                    }
                }
            }
        }
    }

    private Map<Long, List<Ball>> buildCollisionGrid() {
        Map<Long, List<Ball>> grid = new HashMap<>();
        for (Ball ball : balls) {
            int cellX = (int) Math.floor((ball.getPos().x() - bounds.x0()) / collisionCellSize);
            int cellY = (int) Math.floor((ball.getPos().y() - bounds.y0()) / collisionCellSize);
            long key = packCell(cellX, cellY);
            grid.computeIfAbsent(key, ignored -> new ArrayList<>()).add(ball);
        }
        return grid;
    }

    private void resolveBallPairs(List<Ball> firstGroup, List<Ball> secondGroup) {
        if (firstGroup == secondGroup) {
            for (int i = 0; i < firstGroup.size() - 1; i++) {
                for (int j = i + 1; j < firstGroup.size(); j++) {
                    resolveBallPair(firstGroup.get(i), firstGroup.get(j));
                }
            }
            return;
        }
        for (Ball first : firstGroup) {
            for (Ball second : secondGroup) {
                resolveBallPair(first, second);
            }
        }
    }

    private void resolveBallPair(Ball first, Ball second) {
        Object firstLock = first;
        Object secondLock = second;

        if (System.identityHashCode(firstLock) > System.identityHashCode(secondLock)) {
            Object temp = firstLock;
            firstLock = secondLock;
            secondLock = temp;
        }

        synchronized (firstLock) {
            synchronized (secondLock) {
                if (areColliding(first, second)) {
                    Ball.resolveCollision(first, second);
                    first.setLastTouchedBy(Ball.LastTouchedBy.NONE);
                    second.setLastTouchedBy(Ball.LastTouchedBy.NONE);
                }
            }
        }
    }

    private boolean isCanonicalNeighbor(int cellX, int cellY, int neighborX, int neighborY) {
        return neighborX > cellX || (neighborX == cellX && neighborY > cellY);
    }

    private long packCell(int cellX, int cellY) {
        return (((long) cellX) << 32) ^ (cellY & 0xffffffffL);
    }

    private int unpackCellX(long cellKey) {
        return (int) (cellKey >> 32);
    }

    private int unpackCellY(long cellKey) {
        return (int) cellKey;
    }

    private double computeCollisionCellSize() {
        double maxRadius = 0.0;
        for (Ball ball : balls) {
            maxRadius = Math.max(maxRadius, ball.getRadius());
        }
        return Math.max(0.05, maxRadius * 4.0);
    }

    private void resolvePlayerCollisions() {
        if (areColliding(humanBall, botBall)) {
            Ball.resolveCollision(humanBall, botBall);
        }
        for (var b : balls) {
            if (areColliding(humanBall, b)) {
                b.setLastTouchedBy(Ball.LastTouchedBy.HUMAN);
                Ball.resolveCollision(humanBall, b);
            }
            if (areColliding(botBall, b)) {
                b.setLastTouchedBy(Ball.LastTouchedBy.BOT);
                Ball.resolveCollision(botBall, b);
            }
        }
    }

    private boolean checkPlayerBallIsInHole(){
        if(isInsideHole(humanBall)){
            gameOver = true;
            winner =  Ball.Type.BOT;
            return true;
        }
        if(isInsideHole(botBall)){
            gameOver = true;
            winner =  Ball.Type.HUMAN;
            return true;
        }
        return false;
    }

    private void handlePocketedBalls() {
        var ballsToRemove = new ArrayList<Ball>();
        for (var b : balls) {
            if (!isInsideHole(b)) {
                continue;
            }
            if (b.getLastTouchedBy() == Ball.LastTouchedBy.HUMAN) {
                humanScore++;
            } else if (b.getLastTouchedBy() == Ball.LastTouchedBy.BOT) {
                botScore++;
            }
            ballsToRemove.add(b);
        }
        balls.removeAll(ballsToRemove);
    }

    private boolean checkNoMoreSmallBalls(){
        if (balls.isEmpty()){
            gameOver = true;
            winner = humanScore>botScore ? humanBall.getType() : botBall.getType();

            return true;
        }
        return false;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public Ball.Type getWinner(){
        return winner;
    }

    public List<Ball> getBalls() {
        return balls;
    }

    public Ball getPlayerBall() {
        return humanBall;
    }

    public Ball getHumanBall() {
        return humanBall;
    }

    public Ball getBotBall() {
        return botBall;
    }

    public Boundary getBounds() {
        return bounds;
    }

    public int getHumanScore() {
        return humanScore;
    }

    public int getBotScore() {
        return botScore;
    }

    public P2d getLeftHole() {
        return leftHole;
    }

    public P2d getRightHole() {
        return rightHole;
    }

    public double getHoleRadius() {
        return holeRadius;
    }

    private boolean isInsideHole(Ball b) {
        return isInsideSpecificHole(b, leftHole) || isInsideSpecificHole(b, rightHole);
    }

    private boolean isInsideSpecificHole(Ball b, P2d holeCenter) {
        double dx = b.getPos().x() - holeCenter.x();
        double dy = b.getPos().y() - holeCenter.y();
        double dist = Math.hypot(dx, dy);
        return dist <= holeRadius;
    }

    private boolean areColliding(Ball a, Ball b) {
        double dx = b.getPos().x() - a.getPos().x();
        double dy = b.getPos().y() - a.getPos().y();
        double dist = Math.hypot(dx, dy);
        return dist < a.getRadius() + b.getRadius();
    }

    public void kickHumanBall(V2d impulse) {
        humanBall.kick(humanBall.getVel().sum(impulse));
    }

    public void setUseThreads(boolean useThreads) {
        this.useThreads = useThreads;
    }

    public boolean isUsingThreads() {
        return useThreads;
    }

    public void resetPerformanceMetrics() {
        movingBallsTotalNanos = 0L;
        movingBallsMeasurements = 0L;
        resolveSmallBallsCollisionTotalNanos = 0L;
        resolveSmallBallsCollisionMeasurements = 0L;
    }

    public long getMovingBallsTotalNanos() {
        return movingBallsTotalNanos;
    }

    public long getMovingBallsMeasurements() {
        return movingBallsMeasurements;
    }

    public double getAverageMovingBallsNanos() {
        if (movingBallsMeasurements == 0) {
            return 0.0;
        }
        return movingBallsTotalNanos / (double) movingBallsMeasurements;
    }

    public long getResolveSmallBallsCollisionTotalNanos() {
        return resolveSmallBallsCollisionTotalNanos;
    }

    public long getResolveSmallBallsCollisionMeasurements() {
        return resolveSmallBallsCollisionMeasurements;
    }

    public double getAverageResolveSmallBallsCollisionNanos() {
        if (resolveSmallBallsCollisionMeasurements == 0) {
            return 0.0;
        }
        return resolveSmallBallsCollisionTotalNanos / (double) resolveSmallBallsCollisionMeasurements;
    }
}
