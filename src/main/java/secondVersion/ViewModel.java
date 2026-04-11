package secondVersion;

import java.util.ArrayList;

record BallViewInfo(P2d pos, double radius) {}
record HoleViewInfo(P2d pos, double radius) {}

public class ViewModel {

	private final ArrayList<BallViewInfo> balls;
	private BallViewInfo humanBall;
	private BallViewInfo botBall;
	private HoleViewInfo leftHole;
	private HoleViewInfo rightHole;
	private int humanScore;
	private int botScore;
	private int framePerSec;
    private boolean gameOver;
    private Ball.Type winner;
	
	public ViewModel() {
        gameOver = false;
        winner = Ball.Type.BOT;

		balls = new ArrayList<BallViewInfo>();
		framePerSec = 0;
	}
	
	public synchronized void update(Board board, int framePerSec) {
        //noinspection DuplicatedCode
        gameOver = board.isGameOver();
        winner = board.getWinner();

		balls.clear();
		for (var b: board.getBalls()) {
			balls.add(new BallViewInfo(b.getPos(), b.getRadius()));
		}
		this.framePerSec = framePerSec;
		var p = board.getHumanBall();
        var bot = board.getBotBall();
        humanBall = new BallViewInfo(p.getPos(), p.getRadius());
        botBall = new BallViewInfo(bot.getPos(), bot.getRadius());
		leftHole = new HoleViewInfo(board.getLeftHole(), board.getHoleRadius());
		rightHole = new HoleViewInfo(board.getRightHole(), board.getHoleRadius());
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

	public synchronized HoleViewInfo getLeftHole() {
		return leftHole;
	}

	public synchronized HoleViewInfo getRightHole() {
		return rightHole;
	}

    public boolean isGameOver() {
        return gameOver;
    }

    public Ball.Type getWinner() {
        return winner;
    }
	
}
