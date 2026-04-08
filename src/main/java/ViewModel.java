import java.util.ArrayList;

record BallViewInfo(P2d pos, double radius) {}

public class ViewModel {

	private ArrayList<BallViewInfo> balls;
	private BallViewInfo player;
	private BallViewInfo humanBall;
	private BallViewInfo botBall;
	private int humanScore;
	private int botScore;
	private int framePerSec;
	
	public ViewModel() {
		balls = new ArrayList<BallViewInfo>();
		framePerSec = 0;
	}
	
	public synchronized void update(Board board, int framePerSec) {
		balls.clear();
		for (var b: board.getBalls()) {
			balls.add(new BallViewInfo(b.getPos(), b.getRadius()));
		}
		this.framePerSec = framePerSec;
		var p = board.getPlayerBall();
        var bot = board.getBotBall();
        player = new BallViewInfo(p.getPos(), p.getRadius());
        humanBall = player;
        botBall = new BallViewInfo(bot.getPos(), bot.getRadius());
		humanScore = board.getHumanScore();
		botScore = board.getBotScore();
	}
	
	public synchronized ArrayList<BallViewInfo> getBalls(){
		var copy = new ArrayList<BallViewInfo>();
		copy.addAll(balls);
		return copy;
		
	}

	public synchronized int getFramePerSec() {
		return framePerSec;
	}

	public synchronized BallViewInfo getPlayerBall() {
		return player;
	}

	public synchronized BallViewInfo getHumanBall() {
		return humanBall;
	}

	public synchronized BallViewInfo getBotBall() {
		return botBall;
	}

	public synchronized int getHumanScore() {
		return humanScore;
	}

	public synchronized int getBotScore() {
		return botScore;
	}

	public synchronized void setScores(int humanScore, int botScore) {
		this.humanScore = humanScore;
		this.botScore = botScore;
	}
	
}
