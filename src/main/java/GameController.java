import java.util.Random;

public class GameController implements HumanInputListener {

    private final Board board;
    private final ViewModel viewModel;
    private final Random random;
    private View view;
    private long startTime;
    private long lastUpdateTime;
    private long lastKickTimeBot;
    private int frameCount;

    public GameController(Board board, ViewModel viewModel) {
        this.board = board;
        this.viewModel = viewModel;
        this.random = new Random(2);
    }

    public void attachView(View view) {
        this.view = view;
    }

    public void start() {
        if (this.view == null) {
            throw new IllegalStateException("View must be attached before starting the controller");
        }
        this.viewModel.update(this.board, 0);
        this.view.render();
        waitAbit();

        this.startTime = System.currentTimeMillis();
        this.lastUpdateTime = this.startTime;
        this.lastKickTimeBot = this.startTime;

        while (true) {
            updateBot();
            updateBoard();
            renderFrame();
        }
    }

    @Override
    public void onHumanImpulse(V2d impulse) {
        this.board.kickHumanBall(impulse);
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

    private static void waitAbit() {
        try {
            Thread.sleep(2000);
        } catch (Exception ex) {
        }
    }
}
