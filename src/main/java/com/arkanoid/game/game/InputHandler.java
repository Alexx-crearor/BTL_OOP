package com.arkanoid.game.game;

import java.awt.event.KeyEvent;

import com.arkanoid.game.entity.Ball;
import com.arkanoid.game.entity.Laser;

// ============================================================
// CLASS: InputHandler - Xử lý tất cả keyboard input trong game
// ============================================================

/**
 * Class InputHandler - Xử lý keyboard input cho GamePanel
 * 
 * EXTENDS: GameComponent (có access to panel fields)
 * 
 * TRÁCH NHIỆM:
 * 1. Paddle movement (LEFT/RIGHT arrows)
 * 2. Space key actions (release ball, fire laser)
 * 3. Pause controls (P, ESC keys)
 * 4. Pause menu navigation (UP/DOWN/ENTER, volume LEFT/RIGHT)
 * 5. Game over input (ENTER to restart, ESC to quit)
 * 
 * KEY MAPPINGS:
 * ```
 * GAMEPLAY:
 *   LEFT/RIGHT → Paddle movement
 *   SPACE → Release ball (first time) / Fire laser (with power-up)
 *   P → Pause/Resume
 *   ESC → Open pause menu (hoặc resume nếu đang trong menu)
 * 
 * PAUSE MENU:
 *   UP/DOWN → Navigate options (Resume/Quit)
 *   LEFT/RIGHT → Adjust volume (-5% / +5%)
 *   ENTER → Confirm selection
 *   ESC → Resume game (close menu)
 * 
 * GAME OVER:
 *   ENTER → Restart game (level 1, 3 lives, score=0)
 *   ESC → Quit to menu
 * ```
 * 
 * @author Arkanoid Game Team
 * @version 1.0
 */
public class InputHandler extends GameComponent {
    
    public InputHandler(GamePanel panel) {
        super(panel);
    }
    
    // ============================================================
    // KEY PRESSED - Main input handler
    // ============================================================
    
    /**
     * Handle key pressed events
     * 
     * ROUTING:
     * 1. If pause menu visible → handlePauseMenuInput()
     * 2. Else if game over → handleGameOverInput()
     * 3. Else → gameplay input (paddle, space, pause)
     */
    public void handleKeyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        
        // ---- PAUSE MENU NAVIGATION ----
        if (panel.showPauseMenu && !panel.gameOver) {
            handlePauseMenuInput(key);
            return; // Don't process other keys
        }
        
        // ---- PADDLE MOVEMENT ----
        if (key == KeyEvent.VK_LEFT) {
            panel.movingLeft = true;
        }
        if (key == KeyEvent.VK_RIGHT) {
            panel.movingRight = true;
        }
        
        // ---- SPACE KEY (Release ball / Fire laser) ----
        if (key == KeyEvent.VK_SPACE) {
            handleSpaceKey();
        }
        
        // ---- PAUSE TOGGLE (P key) ----
        if (key == KeyEvent.VK_P) {
            togglePause();
        }
        
        // ---- ESC KEY (Open/close pause menu) ----
        if (!panel.gameOver && key == KeyEvent.VK_ESCAPE) {
            togglePause();
        }
        
        // ---- GAME OVER INPUT ----
        if (panel.gameOver) {
            handleGameOverInput(key);
        }
    }
    
    // ============================================================
    // KEY RELEASED - Handle key release
    // ============================================================
    
    /**
     * Handle key released events (stop paddle movement)
     */
    public void handleKeyReleased(KeyEvent e) {
        int key = e.getKeyCode();
        
        // Stop paddle movement
        if (key == KeyEvent.VK_LEFT) {
            panel.movingLeft = false;
        }
        if (key == KeyEvent.VK_RIGHT) {
            panel.movingRight = false;
        }
        
        // Reset space flag
        if (key == KeyEvent.VK_SPACE) {
            panel.spacePressed = false;
        }
    }
    
    // ============================================================
    // PAUSE MENU INPUT - Navigation và volume control
    // ============================================================
    
    /**
     * Handle input trong pause menu
     * 
     * CONTROLS:
     * - UP/DOWN: Toggle between Resume (0) ↔ Quit (1)
     * - LEFT/RIGHT: Volume -5% / +5%
     * - ENTER: Confirm selection (resume hoặc quit)
     * - ESC: Resume ngay lập tức
     */
    private void handlePauseMenuInput(int key) {
        if (key == KeyEvent.VK_UP || key == KeyEvent.VK_DOWN) {
            // Toggle giữa Resume (0) và Quit (1)
            panel.pauseMenuSelection = 1 - panel.pauseMenuSelection;
        } else if (key == KeyEvent.VK_LEFT) {
            // Giảm volume 5%
            adjustVolume(-0.05f);
        } else if (key == KeyEvent.VK_RIGHT) {
            // Tăng volume 5%
            adjustVolume(0.05f);
        } else if (key == KeyEvent.VK_ENTER) {
            if (panel.pauseMenuSelection == 0) {
                // Resume game
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
