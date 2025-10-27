package com.arkanoid.game.game;

import java.util.ArrayList;

import javax.swing.SwingUtilities;

import com.arkanoid.game.entity.Ball;
import com.arkanoid.game.entity.Brick;
import com.arkanoid.game.entity.Paddle;
import com.arkanoid.game.ui.HighScoreDialog;
import com.arkanoid.game.util.GameStateManager;
import com.arkanoid.game.util.GameStateManager.GameState;
import com.arkanoid.game.util.HighScoreManager;

/**
 * GameStateHandler - Xử lý save/load game state
 * Chịu trách nhiệm:
 * - Lưu game state khi quit
 * - Load game state từ file
 * - Quit to menu
 * - Kiểm tra và submit high score
 */
public class GameStateHandler extends GameComponent {
    private final HighScoreManager highScoreManager;
    
    public GameStateHandler(GamePanel panel, HighScoreManager highScoreManager) {
        super(panel);
        this.highScoreManager = highScoreManager;
    }
    
    /**
     * Lưu trạng thái game hiện tại
     */
    public void saveGameState() {
        GameState state = new GameState(
            panel.score, 
            panel.lives, 
            panel.levelNumber, 
            panel.paddle.x, 
            panel.paddle.width
        );
        
        // Lưu thông tin balls
        state.balls = new GameStateManager.BallState[panel.balls.size()];
        for (int i = 0; i < panel.balls.size(); i++) {
            Ball ball = panel.balls.get(i);
            state.balls[i] = new GameStateManager.BallState(
                ball.x, ball.y, ball.width, ball.dx, ball.dy, ball.isMoving()
            );
        }
        
        // Lưu thông tin bricks
        ArrayList<GameStateManager.BrickState> brickList = new ArrayList<>();
        for (Brick brick : panel.bricks) {
            if (!brick.isDestroyed) {
                brickList.add(new GameStateManager.BrickState(
                    brick.x, brick.y, brick.width, brick.height,
                    0, brick.getType().name(), brick.isDestroyed
                ));
            }
        }
        state.bricks = brickList.toArray(new GameStateManager.BrickState[0]);
        
        GameStateManager.getInstance().saveGameState(state);
    }
    
    /**
     * Load game từ saved state
     */
    public void loadGameFromState(GameState state) {
        // Restore game state
        panel.score = state.score;
        panel.lives = state.lives;
        panel.levelNumber = state.levelNumber;
        
        // Khởi tạo paddle
        panel.paddle = new Paddle(state.paddleX, getGameHeight() - 70, getGameWidth(), getGameHeight());
        panel.paddle.width = state.paddleWidth;
        
        // Khởi tạo balls từ saved state
        panel.balls = new ArrayList<>();
        if (state.balls != null) {
            for (GameStateManager.BallState ballState : state.balls) {
                Ball ball = new Ball(ballState.x, ballState.y, ballState.width);
                ball.dx = ballState.dx;
                ball.dy = ballState.dy;
                if (ballState.isMoving) {
                    ball.startMoving();
                }
                panel.balls.add(ball);
            }
        }
        
        // Load level để có bricks
        panel.loadLevel(panel.levelNumber);
        
        // Cập nhật trạng thái bricks từ saved state
        if (state.bricks != null) {
            for (GameStateManager.BrickState brickState : state.bricks) {
                // Tìm brick tương ứng và cập nhật
                for (Brick brick : panel.bricks) {
                    if (brick.x == brickState.x && brick.y == brickState.y) {
                        brick.isDestroyed = brickState.isDestroyed;
                        break;
                    }
                }
            }
            // Xóa các bricks đã bị phá hủy
            panel.bricks.removeIf(b -> b.isDestroyed);
        }
        
        // Khởi tạo collections
        panel.powerUps = new ArrayList<>();
        panel.lasers = new ArrayList<>();
        
        // Khi load game đã lưu, các ball đã moving thì gameStarted = true
        panel.gameStarted = state.balls != null && state.balls.length > 0 && state.balls[0].isMoving;
    }
    
    /**
     * Quit to menu - lưu game và trở về menu
     */
    public void quitToMenu(AudioManager audioManager) {
        // Lưu game trước khi thoát (nếu chưa game over)
        if (!panel.gameOver) {
            saveGameState();
        }
        
        // Dừng nhạc nền
        audioManager.stopLevelMusic();
        
        // Đóng cửa sổ game hiện tại và mở menu
        SwingUtilities.invokeLater(() -> {
            javax.swing.JFrame gameFrame = (javax.swing.JFrame) SwingUtilities.getWindowAncestor(panel);
            if (gameFrame != null) {
                gameFrame.dispose();
            }
            // Mở lại menu
            new com.arkanoid.game.ui.Menu().setVisible(true);
        });
    }
    
    /**
     * Kiểm tra và submit high score nếu đủ điểm
     */
    public void checkAndSubmitHighScore() {
        if (panel.highScoreSubmitted) {
            return; // Đã submit rồi
        }
        
        if (highScoreManager.isHighScore(panel.score)) {
            panel.highScoreSubmitted = true;
            
            // Chạy dialog trên EDT thread
            SwingUtilities.invokeLater(() -> {
                int rank = highScoreManager.getRank(panel.score);
                HighScoreDialog dialog = new HighScoreDialog(
                    (javax.swing.JFrame) SwingUtilities.getWindowAncestor(panel),
                    panel.score,
                    rank
                );
                
                String playerName = dialog.getPlayerName();
                if (playerName != null && !playerName.trim().isEmpty()) {
                    highScoreManager.addHighScore(playerName, panel.score);
                    System.out.println("High score saved: " + playerName + " - " + panel.score);
                }
            });
        }
    }
}
