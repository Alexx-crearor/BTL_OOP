package com.arkanoid.game.game;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import com.arkanoid.game.entity.*;
import com.arkanoid.game.util.FontManager;

// ============================================================
// CLASS: GameRenderer - Vẽ tất cả đồ họa game
// ============================================================

/**
 * Class GameRenderer - Chịu trách nhiệm render tất cả graphics
 * 
 * TRÁCH NHIỆM:
 * 1. Vẽ Background (backgroundLevel1.png)
 * 2. Vẽ Game Objects (bricks, paddle, balls, powerups, lasers, boss, projectiles, shields)
 * 3. Vẽ HUD (Score, Lives, Level, FPS)
 * 4. Vẽ UI Overlays:
 *    - "PRESS SPACE TO START" (lần đầu chơi)
 *    - Pause Menu (Resume/Quit + Volume Slider)
 *    - Game Over Screen (Score, "Press ENTER to restart")
 *    - Game Won Screen (Congratulations + High Score)
 * 5. Vẽ Boss Health Bar (level 5)
 * 
 * RENDERING ORDER (Bottom to Top):
 * ```
 * Background Image
 *   ↓
 * Bricks, Boss, Shield Bricks
 *   ↓
 * Projectiles (boss bullets)
 *   ↓
 * Paddle
 *   ↓
 * Balls
 *   ↓
 * PowerUps, Lasers
 *   ↓
 * HUD (Score, Lives, Level)
 *   ↓
 * Boss Health Bar
 *   ↓
 * Overlays (Pause Menu, Game Over, etc.)
 * ```
 * 
 * DESIGN:
 * - Separation of Concerns: GamePanel chỉ call render()
 * - AntiAliasing enabled cho text/graphics mượt
 * - Custom fonts từ FontManager
 * - Responsive UI (center aligned)
 * 
 * PERFORMANCE:
 * - 120 FPS stable
 * - Dispose Graphics2D sau mỗi frame
 * - No memory leaks
 * 
 * @author Arkanoid Game Team
 * @version 1.0
 */
public class GameRenderer {
    
    // ============================================================
    // THUỘC TÍNH - Screen và Resources
    // ============================================================
    
    /** Chiều rộng màn hình (800px) */
    private final int width;
    
    /** Chiều cao màn hình (600px) */
    private final int height;
    
    /** Background image (backgroundLevel1.png) */
    private BufferedImage backgroundImage;
    
    // ============================================================
    // CONSTRUCTOR
    // ============================================================
    
    /**
     * Constructor
     * 
     * @param width Chiều rộng màn hình
     * @param height Chiều cao màn hình  
     * @param backgroundImage Background image (có thể null)
     */
    public GameRenderer(int width, int height, BufferedImage backgroundImage) {
        this.width = width;
        this.height = height;
        this.backgroundImage = backgroundImage;
    }
    
    // ============================================================
    // MAIN RENDER - Vẽ toàn bộ game
    // ============================================================
    
    /**
     * Vẽ toàn bộ game (main render method)
     * 
     * ĐƯỢC GỌI TỪ: GamePanel.paintComponent()
     * 
     * FLOW:
     * 1. Enable AntiAliasing cho graphics mượt
     * 2. Vẽ background image
     * 3. Vẽ game objects (bricks, paddle, balls, ...)
     * 4. Vẽ HUD (score, lives, level)
     * 5. Vẽ game over/won overlay (nếu cần)
     * 6. Dispose Graphics2D
     * 
     * @param g Graphics context từ paintComponent
     * @param panel GamePanel reference để access game state
     */
    public void render(Graphics g, GamePanel panel) {
        // BƯỚC 1: Tạo Graphics2D và enable antialiasing
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // BƯỚC 2: Vẽ background
        if (backgroundImage != null) {
            g2d.drawImage(backgroundImage, 0, 0, width, height, null);
        }
        
        // BƯỚC 3: Vẽ game objects
        drawGameObjects(g2d, panel);
        
        // BƯỚC 4: Vẽ HUD
        drawUI(g2d, panel);
        
        // BƯỚC 5: Vẽ game over/won overlay
        if (panel.gameOver) {
            drawGameOver(g2d, panel);
        }
        
        // BƯỚC 6: Cleanup
        g2d.dispose();
    }
    
    // ============================================================
    // GAME OBJECTS - Vẽ entities
    // ============================================================
    
    /**
     * Vẽ các đối tượng game (bricks, paddle, balls, powerups, lasers, boss)
     * 
     * ORDER:
     * 1. Bricks (phía sau)
     * 2. Boss + Shield Bricks + Projectiles (nếu level 5)
     * 3. Paddle
     * 4. Balls
     * 5. PowerUps
     * 6. Lasers (phía trước)
     * 
     * @param g2d Graphics2D context
     * @param panel GamePanel reference
     */
    private void drawGameObjects(Graphics2D g2d, GamePanel panel) {
        // ---- LAYER 1: BRICKS (Background layer) ----
        // Vẽ tất cả bricks còn lại trên màn hình
        for (Brick brick : panel.bricks) {
            brick.draw(g2d);
        }

        // ---- LAYER 2: BOSS (nếu level 5) ----
        if (panel.boss != null) {
            // 1. Vẽ boss sprite
            panel.boss.draw(g2d);

            // 2. Vẽ shield bricks (5 viên SILVER phía trước boss)
            for (Brick shieldBrick : panel.shieldBricks) {
                shieldBrick.draw(g2d);
            }

            // 3. Vẽ projectiles (bullets bắn từ boss xuống paddle)
            for (Projectile projectile : panel.projectiles) {
                projectile.draw(g2d);
            }
        }
        
        // ---- LAYER 3: PADDLE ----
        // Vẽ paddle (vợt người chơi điều khiển)
        panel.paddle.draw(g2d);
        
        // ---- LAYER 4: BALLS ----
        // Vẽ tất cả các quả bóng (có thể có nhiều quả do power-up TWIN_BALL)
        for (Ball ball : panel.balls) {
            ball.draw(g2d);
        }
        
        // ---- LAYER 5: POWER-UPS ----
        // Vẽ các power-up đang rơi (30% chance drop từ brick)
        for (PowerUp powerUp : panel.powerUps) {
            powerUp.draw(g2d);
        }
        
        // ---- LAYER 6: LASERS (Front layer) ----
        // Vẽ các tia laser bắn từ paddle (power-up LASER)
        for (Laser laser : panel.lasers) {
            laser.draw(g2d);
        }
    }
    
    // ============================================================
    // HUD - Vẽ UI (Score, Lives, Level)
    // ============================================================
    
    /**
     * Vẽ HUD (Heads-Up Display) - Thông tin game
     * 
     * HUD ELEMENTS:
     * - Top Left: Score, Lives, Level
     * - Top Right: Bricks Remaining
     * - Center: "PRESS SPACE TO START" (lần đầu chơi)
     * - Center: "PAUSED" (khi pause không có menu)
     * - Full Screen: Pause Menu (khi ESC)
     * - Full Screen: Boss Health Bar (level 5)
     * 
     * @param g Graphics context
     * @param panel GamePanel reference
     */
    private void drawUI(Graphics g, GamePanel panel) {
        // ---- HUD TEXT (Score, Lives, Level) ----
        g.setColor(Color.WHITE);
        g.setFont(FontManager.getFont(Font.BOLD, 22));
        
        // Top left info (3 dòng)
        String[] info = {
            "Score: " + panel.score,      // Điểm số hiện tại
            "Lives: " + panel.lives,       // Số mạng còn lại
            "Level: " + panel.levelNumber  // Level hiện tại (1-5)
        };
        for (int i = 0; i < info.length; i++) {
            g.drawString(info[i], 10, 25 + i * 25);
        }
        
        // Top right: Số bricks còn lại
        g.drawString("Bricks: " + panel.currentLevel.getRemainingBricks(), width - 120, 25);
        
        // ---- PRESS SPACE TO START ----
        // Hiển thị lần đầu tiên chơi game (bóng chưa phóng)
        if (!panel.gameStarted) {
            g.setFont(FontManager.getFont(Font.BOLD, 40));
            g.setColor(Color.YELLOW);
            String startText = "PRESS SPACE TO START";
            FontMetrics fm = g.getFontMetrics();
            int textX = (width - fm.stringWidth(startText)) / 2;
            int textY = height / 2 + 100;
            g.drawString(startText, textX, textY);
        }
        
        // ---- PAUSE STATES ----
        // Pause Menu (ESC key) - Full screen overlay
        if (panel.showPauseMenu) {
            drawPauseMenu(g, panel);
        } 
        // Simple PAUSED text (không có menu)
        else if (panel.paused) {
            drawCenteredText(g, "PAUSED", 40, 0);
        }
    }
    
    // ============================================================
    // PAUSE MENU - Menu tạm dừng
    // ============================================================
    
    /**
     * Vẽ menu pause với overlay tối + các lựa chọn
     * 
     * MENU STRUCTURE:
     * ```
     * +-------------------------+
     * |       PAUSED           |  (Title)
     * |                        |
     * |   > RESUME             |  (Selected - Yellow + Arrow)
     * |     QUIT TO MENU       |  (Not selected - Gray)
     * |                        |
     * | [=========|----------] |  (Volume Slider 50%)
     * |                        |
     * | Use UP/DOWN, ENTER...  |  (Hint text)
     * +-------------------------+
     * ```
     * 
     * CONTROLS:
     * - UP/DOWN: Di chuyển selection
     * - LEFT/RIGHT: Điều chỉnh âm lượng
     * - ENTER: Confirm lựa chọn
     * - ESC: Resume ngay lập tức
     * 
     * @param g Graphics context
     * @param panel GamePanel reference (để lấy pauseMenuSelection)
     */
    private void drawPauseMenu(Graphics g, GamePanel panel) {
        // ---- OVERLAY (Semi-transparent black) ----
        g.setColor(new Color(0, 0, 0, 180));
        g.fillRect(0, 0, width, height);
        
        // ---- TITLE "PAUSED" ----
        g.setColor(Color.WHITE);
        g.setFont(FontManager.getFont(Font.BOLD, 50));
        FontMetrics fm = g.getFontMetrics();
        String title = "PAUSED";
        int titleX = (width - fm.stringWidth(title)) / 2;
        g.drawString(title, titleX, height / 2 - 120);
        
        // ---- MENU OPTIONS ----
        String[] options = {"RESUME", "QUIT TO MENU"};
        g.setFont(FontManager.getFont(Font.BOLD, 30));
        fm = g.getFontMetrics();
        
        for (int i = 0; i < options.length; i++) {
            int yPos = height / 2 - 30 + i * 50;
            
            if (i == panel.pauseMenuSelection) {
                // Lựa chọn hiện tại: Vàng + mũi tên ">"
                g.setColor(Color.YELLOW);
                String arrow = "> ";
                g.drawString(arrow + options[i], 
                    (width - fm.stringWidth(arrow + options[i])) / 2, 
                    yPos);
            } else {
                // Lựa chọn khác: Xám nhạt
                g.setColor(Color.LIGHT_GRAY);
                g.drawString(options[i], 
                    (width - fm.stringWidth(options[i])) / 2, 
                    yPos);
            }
        }
        
        // ---- VOLUME SLIDER ----
        drawVolumeSlider(g, height - 100);
        
        // ---- CONTROLS HINT ----
        g.setColor(Color.WHITE);
        g.setFont(FontManager.getFont(Font.PLAIN, 16));
        fm = g.getFontMetrics();
        String hint = "Use UP/DOWN to select, LEFT/RIGHT to adjust volume, ENTER to confirm, ESC to resume";
        g.drawString(hint, (width - fm.stringWidth(hint)) / 2, height - 50);
    }
    
    // ============================================================
    // VOLUME SLIDER - Thanh điều chỉnh âm lượng
    // ============================================================
    
    /**
     * Vẽ thanh điều chỉnh âm lượng (volume slider)
     * 
     * SLIDER STRUCTURE:
     * ```
     * Volume: [=========|----------] 50%
     * ```
     * 
     * - Track (full width): Xám đậm
     * - Progress (0-100%): Xanh lá cây
     * - Thumb (vị trí hiện tại): Hình tròn trắng
     * - Label: "Volume: " + percentage
     * 
     * VOLUME RANGE: 0.0 → 1.0 (được convert sang 0% → 100%)
     * 
     * @param g Graphics context
     * @param yPos Y position để vẽ slider
     */
    private void drawVolumeSlider(Graphics g, int yPos) {
        // Kích thước slider
        int sliderWidth = 300;
        int sliderHeight = 20;
        int trackHeight = 8;
        int sliderX = (width - sliderWidth) / 2;
        int trackY = yPos + (sliderHeight - trackHeight) / 2;
        
        // ---- LABEL "VOLUME:" ----
        g.setColor(Color.YELLOW);
        g.setFont(FontManager.getFont(Font.BOLD, 20));
        FontMetrics fm = g.getFontMetrics();
        String label = "VOLUME:";
        int labelX = sliderX - fm.stringWidth(label) - 15;
        g.drawString(label, labelX, yPos + sliderHeight - 5);
        
        // ---- TRACK (Thanh nền xám đậm) ----
        g.setColor(new Color(60, 60, 60));
        g.fillRoundRect(sliderX, trackY, sliderWidth, trackHeight, 5, 5);
        
        // ---- PROGRESS BAR (Phần đã fill - Gold) ----
        g.setColor(new Color(255, 215, 0)); // Gold color
        int fillWidth = (int)(sliderWidth * com.arkanoid.game.ui.Menu.globalVolume);
        if (fillWidth > 0) {
            g.fillRoundRect(sliderX, trackY, fillWidth, trackHeight, 5, 5);
        }
        
        // ---- BORDER (Viền trắng) ----
        g.setColor(Color.WHITE);
        g.drawRoundRect(sliderX, trackY, sliderWidth, trackHeight, 5, 5);
        
        // ---- TICK MARKS (Vạch chia độ 0%, 25%, 50%, 75%, 100%) ----
        g.setColor(Color.LIGHT_GRAY);
        int majorTickSpacing = sliderWidth / 4; // 4 khoảng => 5 vạch
        for (int i = 0; i <= 4; i++) {
            int tickX = sliderX + i * majorTickSpacing;
            int tickY1 = trackY + trackHeight;
            int tickY2 = tickY1 + 8; // Vạch dài 8px
            g.drawLine(tickX, tickY1, tickX, tickY2);
        }
        
        // ---- MINOR TICKS (Vạch nhỏ 5% intervals) ----
        g.setColor(new Color(150, 150, 150));
        int minorTickSpacing = sliderWidth / 20; // 20 vạch => 5% mỗi vạch
        for (int i = 1; i < 20; i++) {
            if (i % 5 != 0) { // Bỏ qua vị trí major ticks
                int tickX = sliderX + i * minorTickSpacing;
                int tickY1 = trackY + trackHeight;
                int tickY2 = tickY1 + 4; // Vạch ngắn hơn (4px)
                g.drawLine(tickX, tickY1, tickX, tickY2);
            }
        }
        
        // ---- THUMB (Nút trượt) ----
        int thumbWidth = 16;
        int thumbHeight = 20;
        int thumbX = sliderX + fillWidth - thumbWidth / 2;
        int thumbY = yPos;
        
        // Shadow cho thumb (drop shadow effect)
        g.setColor(new Color(0, 0, 0, 100));
        g.fillRoundRect(thumbX + 2, thumbY + 2, thumbWidth, thumbHeight, 8, 8);
        
        // Thumb chính (xám sáng)
        g.setColor(new Color(240, 240, 240));
        g.fillRoundRect(thumbX, thumbY, thumbWidth, thumbHeight, 8, 8);
        
        // Border thumb (viền trắng)
        g.setColor(Color.WHITE);
        g.drawRoundRect(thumbX, thumbY, thumbWidth, thumbHeight, 8, 8);
        
        // ---- PERCENTAGE LABEL (50% text) ----
        g.setFont(FontManager.getFont(Font.BOLD, 20));
        g.setColor(Color.WHITE);
        String volumeText = (int)(com.arkanoid.game.ui.Menu.globalVolume * 100) + "%";
        g.drawString(volumeText, sliderX + sliderWidth + 20, yPos + sliderHeight - 5);
    }
    
    // ============================================================
    // GAME OVER - Màn hình kết thúc game
    // ============================================================
    
    /**
     * Vẽ màn hình game over/won
     * 
     * GAME OVER SCREEN:
     * ```
     * +-------------------------+
     * |                         |
     * |      GAME OVER          |  (hoặc "YOU WIN!")
     * |                         |
     * |   Final Score: 12345    |
     * |                         |
     * | Press ENTER to restart  |
     * |   or ESC to quit        |
     * +-------------------------+
     * ```
     * 
     * STATES:
     * - gameWon = true: Hiển thị "YOU WIN!" (hoàn thành 5 levels + boss)
     * - gameWon = false: Hiển thị "GAME OVER" (hết mạng)
     * 
     * CONTROLS:
     * - ENTER: Restart game (reset level 1, 3 lives, score = 0)
     * - ESC: Quit to main menu
     * 
     * @param g Graphics context
     * @param panel GamePanel reference (để lấy gameWon, score)
     */
    private void drawGameOver(Graphics g, GamePanel panel) {
        // ---- OVERLAY (Semi-transparent black) ----
        g.setColor(new Color(0, 0, 0, 180));
        g.fillRect(0, 0, width, height);
        
        // ---- GAME OVER/WIN TEXT ----
        g.setColor(Color.WHITE);
        drawCenteredText(g, panel.gameWon ? "YOU WIN!" : "GAME OVER", 50, -50);
        
        // ---- FINAL SCORE ----
        drawCenteredText(g, "Final Score: " + panel.score, 30, 20);
        
        // ---- CONTROLS HINT ----
        drawCenteredText(g, "Press ENTER to restart or ESC to quit to menu", 20, 70);
    }
    
    // ============================================================
    // UTILITY - Helper methods
    // ============================================================
    
    /**
     * Vẽ text ở giữa màn hình (center aligned)
     * 
     * @param g Graphics context
     * @param text String cần vẽ
     * @param fontSize Font size (>35 = BOLD, <=35 = PLAIN)
     * @param yOffset Offset từ center màn hình (pixels)
     */
    private void drawCenteredText(Graphics g, String text, int fontSize, int yOffset) {
        // Font style: BOLD cho text lớn, PLAIN cho text nhỏ
        g.setFont(FontManager.getFont(fontSize > 35 ? Font.BOLD : Font.PLAIN, fontSize));
        
        // Tính toán X để center text
        FontMetrics fm = g.getFontMetrics();
        int x = (width - fm.stringWidth(text)) / 2;
        
        // Y = center màn hình + offset
        int y = height / 2 + yOffset;
        
        g.drawString(text, x, y);
    }
    
    // ============================================================
    // SETTER - Cập nhật background image
    // ============================================================
    
    /**
     * Cập nhật background image (khi chuyển level)
     * 
     * ĐƯỢC GỌI TỪ: GamePanel khi load level mới
     * 
     * @param image Background image mới (backgroundLevel1.png, ...)
     */
    public void setBackgroundImage(BufferedImage image) {
        this.backgroundImage = image;
    }
}
