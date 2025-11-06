package com.arkanoid.game.game;

import java.awt.event.KeyEvent;

import com.arkanoid.game.entity.Ball;
import com.arkanoid.game.entity.Laser;

/**
 * InputHandler - Xử lý tất cả keyboard input
 * Chịu trách nhiệm:
 * - Xử lý pause menu navigation
 * - Di chuyển paddle (trái/phải)
 * - Bắn laser, bắt đầu game (space)
 * - Pause/Resume (P, ESC)
 * - Game over actions (Enter, ESC)
 */
public class InputHandler extends GameComponent {
    
    public InputHandler(GamePanel panel) {
        super(panel);
    }
    
    /**
     * Xử lý khi phím được nhấn
     */
    public void handleKeyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        
        // Xử lý pause menu navigation
        if (panel.showPauseMenu && !panel.gameOver) {
            handlePauseMenuInput(key);
            return;
        }
        
        // Xử lý di chuyển paddle
        if (key == KeyEvent.VK_LEFT) {
            panel.movingLeft = true;
        }
        if (key == KeyEvent.VK_RIGHT) {
            panel.movingRight = true;
        }
        
        // Xử lý phím Space
        if (key == KeyEvent.VK_SPACE) {
            handleSpaceKey();
        }
        
        // Xử lý phím P (pause)
        if (key == KeyEvent.VK_P) {
            togglePause();
        }
        
        // Xử lý phím ESC (pause/unpause)
        if (!panel.gameOver && key == KeyEvent.VK_ESCAPE) {
            togglePause();
        }
        
        // Xử lý game over screen
        if (panel.gameOver) {
            handleGameOverInput(key);
        }
    }
    
    /**
     * Xử lý khi phím được thả
     */
    public void handleKeyReleased(KeyEvent e) {
        int key = e.getKeyCode();
        
        if (key == KeyEvent.VK_LEFT) {
            panel.movingLeft = false;
        }
        if (key == KeyEvent.VK_RIGHT) {
            panel.movingRight = false;
        }
        if (key == KeyEvent.VK_SPACE) {
            panel.spacePressed = false;
        }
    }
    
    /**
     * Xử lý input trong pause menu
     */
    private void handlePauseMenuInput(int key) {
        if (key == KeyEvent.VK_UP || key == KeyEvent.VK_DOWN) {
            // Toggle giữa Resume (0) và Quit (1)
            panel.pauseMenuSelection = 1 - panel.pauseMenuSelection;
        } else if (key == KeyEvent.VK_LEFT) {
            // Giảm âm lượng
            adjustVolume(-0.05f);
        } else if (key == KeyEvent.VK_RIGHT) {
            // Tăng âm lượng
            adjustVolume(0.05f);
        } else if (key == KeyEvent.VK_ENTER) {
            if (panel.pauseMenuSelection == 0) {
                // Resume
                resumeGame();
            } else {
                // Quit to Menu
                panel.quitToMenu();
            }
        } else if (key == KeyEvent.VK_ESCAPE) {
            // Resume khi bấm ESC
            resumeGame();
        }
    }
    
    /**
     * Điều chỉnh âm lượng toàn cục
     */
    private void adjustVolume(float delta) {
        com.arkanoid.game.ui.Menu.globalVolume += delta;
        // Giới hạn trong khoảng [0.0, 1.0]
        if (com.arkanoid.game.ui.Menu.globalVolume < 0.0f) {
            com.arkanoid.game.ui.Menu.globalVolume = 0.0f;
        }
        if (com.arkanoid.game.ui.Menu.globalVolume > 1.0f) {
            com.arkanoid.game.ui.Menu.globalVolume = 1.0f;
        }
        // Cập nhật âm lượng của audio manager trong game
        AudioManager audioManager = panel.getAudioManager();
        if (audioManager != null) {
            audioManager.updateVolume();
        }
    }
    
    /**
     * Xử lý phím Space
     */
    private void handleSpaceKey() {
        panel.spacePressed = true;
        
        // Bắt đầu game khi bấm space lần đầu
        if (!panel.gameStarted) {
            panel.gameStarted = true;
            for (Ball ball : panel.balls) {
                ball.startMoving();
            }
        }
        
        // Bắn laser nếu có power-up
        if (panel.paddle.hasLaser()) {
            fireLaser();
        }
    }
    
    /**
     * Toggle pause/unpause
     */
    private void togglePause() {
        panel.paused = !panel.paused;
        if (panel.paused) {
            panel.showPauseMenu = true;
            panel.pauseMenuSelection = 0;
        } else {
            panel.showPauseMenu = false;
        }
    }
    
    /**
     * Resume game
     */
    private void resumeGame() {
        panel.showPauseMenu = false;
        panel.paused = false;
    }
    
    /**
     * Xử lý input khi game over
     */
    private void handleGameOverInput(int key) {
        if (key == KeyEvent.VK_ENTER) {
            panel.restartGame();
        } else if (key == KeyEvent.VK_ESCAPE) {
            panel.quitToMenu();
        }
    }
    
    /**
     * Bắn laser từ paddle
     */
    private void fireLaser() {
        panel.lasers.add(new Laser(panel.paddle.x + 10, panel.paddle.y));
        panel.lasers.add(new Laser(panel.paddle.x + panel.paddle.width - 14, panel.paddle.y));
    }
}
