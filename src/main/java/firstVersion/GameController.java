package firstVersion;

import java.util.Random;

public class GameController implements HumanInputListener {

    private final Board board;
    private final BoardConf boardConf;
    private final ViewModel viewModel;
    private final Random random;
    private View view;
    private long startTime;
    private long lastUpdateTime;
    private long lastKickTimeBot;
    private int frameCount;
    private volatile boolean restartRequested;

    public GameController(Board board, BoardConf boardConf, ViewModel viewModel) {
        this.board = board;
        this.boardConf = boardConf;
        this.viewModel = viewModel;
        this.random = new Random(2);
    }

    public void attachView(View view) {
        this.view = view;
    }

    public void start() {
        if (this.view == null) {
            throw new IllegalStateException("firstVersion.View must be attached before starting the controller");
        }
        resetGame();

        while (true) {
            if (this.restartRequested) {
                resetGame();
                this.restartRequested = false;
                continue;
            }
            if (!board.isGameOver()) {
                updateBot();
                updateBoard();
                renderFrame();
                continue;
            }
            renderFrame();
            sleepBriefly();
        }
    }

    @Override
    public void onHumanImpulse(V2d impulse) {
        if (this.board.isGameOver()) {
            return;
        }
        this.board.kickHumanBall(impulse);
    }

    @Override
    public void onRestartRequested() {
        this.restartRequested = true;
    }

    private void updateBot() {
        var botBall = this.board.getBotBall();
        long now = System.currentTimeMillis();
        if (botBall.getVel().abs() < 0.05 && now - this.lastKickTimeBot > 2000) {
            var angle = this.random.nextDouble() * Math.PI * 0.25;
            var velocity = new V2d(Math.cos(angle), Math.sin(angle)).mul(1.5);
            botBall.kick(velocity);
            this.lastKickTimeBot = now;
        }
    }

    private void updateBoard() {
        long now = System.currentTimeMillis();
        long elapsed = now - this.lastUpdateTime;
        this.lastUpdateTime = now;
        this.board.updateState(elapsed);
    }

    private void renderFrame() {
        this.frameCount++;
        int framePerSec = 0;
        long dt = System.currentTimeMillis() - this.startTime;
        if (dt > 0) {
            framePerSec = (int) (this.frameCount * 1000L / dt);
        }
        this.viewModel.update(this.board, framePerSec);
        this.view.render();
    }

    private void resetGame() {
        this.board.init(this.boardConf);
        this.viewModel.update(this.board, 0);
        this.view.render();
        waitAbit();
        this.startTime = System.currentTimeMillis();
        this.lastUpdateTime = this.startTime;
        this.lastKickTimeBot = this.startTime;
        this.frameCount = 0;
    }

    private static void waitAbit() {
        try {
            Thread.sleep(2000);
        } catch (Exception ex) {
        }
    }

    private static void sleepBriefly() {
        try {
            Thread.sleep(40);
        } catch (Exception ex) {
        }
    }
}
