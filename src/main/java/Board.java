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

    public Board() {
    }

    public void init(BoardConf conf) {
        balls = conf.getSmallBalls();
        humanBall = conf.getPlayerBall();
        botBall = conf.getBotBall();
        bounds = conf.getBoardBoundary();
        leftHole = new P2d(bounds.x0(), bounds.y1());
        rightHole = new P2d(bounds.x1(), bounds.y1());
        holeRadius = 0.10;
    }

    public void updateState(long dt) {
        updateMovingBalls(dt);
        resolveSmallBallCollisions();
        resolvePlayerCollisions();
        handlePocketedBalls();
    }

    private void updateMovingBalls(long dt) {
        humanBall.updateState(dt, this);
        botBall.updateState(dt, this);
        for (var b : balls) {
            b.updateState(dt, this);
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
}
