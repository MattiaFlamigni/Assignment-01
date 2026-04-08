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



    
    public Board(){} 
    
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

    	humanBall.updateState(dt, this);
        botBall.updateState(dt, this);
    	
    	for (var b: balls) {
    		b.updateState(dt, this);
    	}       	
    	
    	for (int i = 0; i < balls.size() - 1; i++) {
            for (int j = i + 1; j < balls.size(); j++) {
                Ball.resolveCollision(balls.get(i), balls.get(j));
            }
        }
        Ball.resolveCollision(humanBall, botBall);
    	for (var b: balls) {
    		Ball.resolveCollision(humanBall, b);
            Ball.resolveCollision(botBall, b);
    	}

        balls.removeIf(this::isInsideHole);
        //todo: aggiornare punteggio


    	   	    	
    }
    
    public List<Ball> getBalls(){
    	return balls;
    }
    
    public Ball getPlayerBall() {
    	return humanBall;
    }
    public Ball getBotBall() {
        return botBall;
    }
    
    public  Boundary getBounds(){
        return bounds;
    }

    public int getHumanScore() {
        return humanScore;
    }
    public int getBotScore() {
        return botScore;
    }
    private boolean isInsideHole(Ball b) {
        double dxLeft = b.getPos().x() - leftHole.x();
        double dyLeft = b.getPos().y() - leftHole.y();
        double distLeft = Math.hypot(dxLeft, dyLeft);

        double dxRight = b.getPos().x() - rightHole.x();
        double dyRight = b.getPos().y() - rightHole.y();
        double distRight = Math.hypot(dxRight, dyRight);

        return distLeft <= holeRadius || distRight <= holeRadius;
    }
}
