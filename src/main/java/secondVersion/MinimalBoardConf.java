package secondVersion;

import java.util.ArrayList;
import java.util.List;

public class MinimalBoardConf implements BoardConf {

    @Override
    public Ball getPlayerBall() {
        return new Ball(new P2d(-0.35, -0.75), 0.06, 1, new V2d(0,0.5), Ball.Type.HUMAN);
    }

    @Override
    public Ball getBotBall() {
        return  new Ball(new P2d(0.35, -0.75), 0.06, 1, new V2d(0,0.5), Ball.Type.BOT);
    }

    @Override
    public List<Ball> getSmallBalls() {
        var balls = new ArrayList<Ball>();
        var b1 = new Ball(new P2d(0, 0.5), 0.05, 0.75, new V2d(0,0), Ball.Type.SMALL);
        var b2 = new Ball(new P2d(0.05, 0.55), 0.025, 0.25, new V2d(0,0), Ball.Type.SMALL);
        balls.add(b1);
        balls.add(b2);
        return balls;
    }

    @Override
    public Boundary getBoardBoundary() {
        return new Boundary(-1.5,-1.0,1.5,1.0);
    }

}
