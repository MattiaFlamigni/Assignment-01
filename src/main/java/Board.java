import java.util.ArrayList;
import java.util.List;

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
    private boolean useThreads = false;

    private int n_threads = 4;
    private Thread[] workers = new Thread[n_threads];
    private long movingBallsTotalNanos;
    private long movingBallsMeasurements;

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
        resetMovingBallsMetrics();
    }

    public void updateState(long dt) {
        long t0 = System.nanoTime();
        updateMovingBalls(dt);
        movingBallsTotalNanos += (System.nanoTime() - t0);
        movingBallsMeasurements++;
        resolveSmallBallCollisions();
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
        for (int i = 0; i < balls.size() - 1; i++) {
            for (int j = i + 1; j < balls.size(); j++) {
                Ball first = balls.get(i);
                Ball second = balls.get(j);
                if (areColliding(first, second)) {
                    Ball.resolveCollision(first, second);
                    first.setLastTouchedBy(Ball.LastTouchedBy.NONE);
                    second.setLastTouchedBy(Ball.LastTouchedBy.NONE);
                }
            }
        }
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

    public void resetMovingBallsMetrics() {
        movingBallsTotalNanos = 0L;
        movingBallsMeasurements = 0L;
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
}
