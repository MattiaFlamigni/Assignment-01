import java.util.List;

public class Board {

    private List<Ball> balls;    
    private Ball humanBall;
    private Ball botBall;
    private Boundary bounds;


    
    public Board(){} 
    
    public void init(BoardConf conf) {
    	balls = conf.getSmallBalls();
    	humanBall = conf.getPlayerBall();
        botBall = conf.getBotBall();
    	bounds = conf.getBoardBoundary();
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
}
