package poool;

import org.junit.jupiter.api.Test;
import poool.engine.SequentialPhysicsEngine;
import poool.model.Ball;
import poool.model.BallType;
import poool.model.GameConfig;
import poool.model.GameState;
import poool.model.PlayerId;
import poool.model.Vector2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

final class GameStateTest {
    @Test
    void directPocketIncrementsHumanScore() {
        final GameConfig config = GameConfig.defaultConfig().withBallCount(1);
        final GameState state = GameState.createDefault(config);
        final Ball target = state.balls().stream().filter(ball -> ball.type() == BallType.SMALL).findFirst().orElseThrow();
        target.setPosition(new Vector2(34.0, 30.0));
        target.setLastDirectTouch(PlayerId.HUMAN);

        new SequentialPhysicsEngine(config).step(state, config.timeStepSeconds());

        assertEquals(1, state.humanScore());
        assertFalse(target.active());
    }
}
