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

/**
 * GameRenderer - Chịu trách nhiệm vẽ tất cả đồ họa của game
 * Bao gồm: background, game objects, UI, pause menu, game over screen
 */
public class GameRenderer {
    // Kích thước màn hình
    private final int width;
    private final int height;
    
    // Background image
    private BufferedImage backgroundImage;
    
    public GameRenderer(int width, int height, BufferedImage backgroundImage) {
        this.width = width;
        this.height = height;
        this.backgroundImage = backgroundImage;
    }
    
    /**
     * Vẽ toàn bộ game
     */
    public void render(Graphics g, GamePanel panel) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Vẽ background
        if (backgroundImage != null) {
            g2d.drawImage(backgroundImage, 0, 0, width, height, null);
        }
        
        // Vẽ game objects
        drawGameObjects(g2d, panel);
        
        // Vẽ UI
        drawUI(g2d, panel);
        
        // Vẽ game over/won nếu cần
        if (panel.gameOver) {
            drawGameOver(g2d, panel);
        }
        
        g2d.dispose();
    }
    
    /**
     * Vẽ các đối tượng game (bricks, paddle, balls, powerups, lasers)
     */
    private void drawGameObjects(Graphics2D g2d, GamePanel panel) {
        // Vẽ bricks
        for (Brick brick : panel.bricks) {
            brick.draw(g2d);
        }

        //Vẽ trùm
        if (panel.boss != null) {
            // 1. Vẽ trùm
            panel.boss.draw(g2d);

            // 2. Vẽ các viên gạch khiên của trùm
            for (Brick shieldBrick : panel.shieldBricks) {
                shieldBrick.draw(g2d);
            }

            // 3. Vẽ các viên đạn của trùm
            for (Projectile projectile : panel.projectiles) {
                projectile.draw(g2d);
            }
        }
        // Vẽ paddle
        panel.paddle.draw(g2d);
        
        // Vẽ balls
        for (Ball ball : panel.balls) {
            ball.draw(g2d);
        }
        
        // Vẽ power-ups
        for (PowerUp powerUp : panel.powerUps) {
            powerUp.draw(g2d);
        }
        
        // Vẽ lasers
        for (Laser laser : panel.lasers) {
            laser.draw(g2d);
        }
    }
    
    /**
     * Vẽ UI (score, lives, level, thông báo)
     */
    private void drawUI(Graphics g, GamePanel panel) {
        g.setColor(Color.WHITE);
        g.setFont(FontManager.getFont(Font.BOLD, 22));
        
        // Vẽ thông tin game
        String[] info = {
            "Score: " + panel.score, 
            "Lives: " + panel.lives, 
            "Level: " + panel.levelNumber
        };
        for (int i = 0; i < info.length; i++) {
            g.drawString(info[i], 10, 25 + i * 25);
        }
        g.drawString("Bricks: " + panel.currentLevel.getRemainingBricks(), width - 120, 25);
        
        // Hiển thị "PRESS SPACE TO START" khi chưa bắt đầu
        if (!panel.gameStarted) {
            g.setFont(FontManager.getFont(Font.BOLD, 40));
            g.setColor(Color.YELLOW);
            String startText = "PRESS SPACE TO START";
            FontMetrics fm = g.getFontMetrics();
            int textX = (width - fm.stringWidth(startText)) / 2;
            int textY = height / 2 + 100;
            g.drawString(startText, textX, textY);
        }
        
        // Vẽ pause menu hoặc text PAUSED
        if (panel.showPauseMenu) {
            drawPauseMenu(g, panel);
        } else if (panel.paused) {
            drawCenteredText(g, "PAUSED", 40, 0);
        }
    }
    
    /**
     * Vẽ menu pause với các lựa chọn
     */
    private void drawPauseMenu(Graphics g, GamePanel panel) {
        // Overlay tối
        g.setColor(new Color(0, 0, 0, 180));
        g.fillRect(0, 0, width, height);
        
        // Tiêu đề "PAUSED"
        g.setColor(Color.WHITE);
        g.setFont(FontManager.getFont(Font.BOLD, 50));
        FontMetrics fm = g.getFontMetrics();
        String title = "PAUSED";
        int titleX = (width - fm.stringWidth(title)) / 2;
        g.drawString(title, titleX, height / 2 - 120);
        
        // Các lựa chọn
        String[] options = {"RESUME", "QUIT TO MENU"};
        g.setFont(FontManager.getFont(Font.BOLD, 30));
        fm = g.getFontMetrics();
        
        for (int i = 0; i < options.length; i++) {
            int yPos = height / 2 - 30 + i * 50;
            
            if (i == panel.pauseMenuSelection) {
                // Lựa chọn đang được chọn (màu vàng + mũi tên)
                g.setColor(Color.YELLOW);
                String arrow = "> ";
                g.drawString(arrow + options[i], 
                    (width - fm.stringWidth(arrow + options[i])) / 2, 
                    yPos);
            } else {
                // Lựa chọn không được chọn
                g.setColor(Color.LIGHT_GRAY);
                g.drawString(options[i], 
                    (width - fm.stringWidth(options[i])) / 2, 
                    yPos);
            }
        }
        
        // Vẽ thanh điều chỉnh âm lượng ở dưới cùng
        drawVolumeSlider(g, height - 100);
        
        // Hướng dẫn
        g.setColor(Color.WHITE);
        g.setFont(FontManager.getFont(Font.PLAIN, 16));
        fm = g.getFontMetrics();
        String hint = "Use UP/DOWN to select, LEFT/RIGHT to adjust volume, ENTER to confirm, ESC to resume";
        g.drawString(hint, (width - fm.stringWidth(hint)) / 2, height - 50);
    }
    
    /**
     * Vẽ thanh điều chỉnh âm lượng
     */
    private void drawVolumeSlider(Graphics g, int yPos) {
        int sliderWidth = 300;
        int sliderHeight = 20;
        int trackHeight = 8;
        int sliderX = (width - sliderWidth) / 2;
        int trackY = yPos + (sliderHeight - trackHeight) / 2;
        
        // Label "VOLUME:"
        g.setColor(Color.YELLOW);
        g.setFont(FontManager.getFont(Font.BOLD, 20));
        FontMetrics fm = g.getFontMetrics();
        String label = "VOLUME:";
        int labelX = sliderX - fm.stringWidth(label) - 15;
        g.drawString(label, labelX, yPos + sliderHeight - 5);
        
        // Vẽ track (thanh nền) - bo tròn
        g.setColor(new Color(60, 60, 60));
        g.fillRoundRect(sliderX, trackY, sliderWidth, trackHeight, 5, 5);
        
        // Vẽ phần đã fill (theo volume) - bo tròn
        g.setColor(new Color(255, 215, 0)); // Gold color
        int fillWidth = (int)(sliderWidth * com.arkanoid.game.ui.Menu.globalVolume);
        if (fillWidth > 0) {
            g.fillRoundRect(sliderX, trackY, fillWidth, trackHeight, 5, 5);
        }
        
        // Vẽ border
        g.setColor(Color.WHITE);
        g.drawRoundRect(sliderX, trackY, sliderWidth, trackHeight, 5, 5);
        
        // Vẽ các tick marks (vạch chia độ)
        g.setColor(Color.LIGHT_GRAY);
        int majorTickSpacing = sliderWidth / 4; // 4 vạch lớn (0%, 25%, 50%, 75%, 100%)
        for (int i = 0; i <= 4; i++) {
            int tickX = sliderX + i * majorTickSpacing;
            int tickY1 = trackY + trackHeight;
            int tickY2 = tickY1 + 8; // Vạch dài 8 pixels
            g.drawLine(tickX, tickY1, tickX, tickY2);
        }
        
        // Vẽ các tick marks nhỏ (minor ticks)
        g.setColor(new Color(150, 150, 150));
        int minorTickSpacing = sliderWidth / 20; // 20 vạch nhỏ
        for (int i = 1; i < 20; i++) {
            if (i % 5 != 0) { // Không vẽ lại vị trí major ticks
                int tickX = sliderX + i * minorTickSpacing;
                int tickY1 = trackY + trackHeight;
                int tickY2 = tickY1 + 4; // Vạch ngắn hơn
                g.drawLine(tickX, tickY1, tickX, tickY2);
            }
        }
        
        // Vẽ thumb (nút trượt)
        int thumbWidth = 16;
        int thumbHeight = 20;
        int thumbX = sliderX + fillWidth - thumbWidth / 2;
        int thumbY = yPos;
        
        // Shadow cho thumb
        g.setColor(new Color(0, 0, 0, 100));
        g.fillRoundRect(thumbX + 2, thumbY + 2, thumbWidth, thumbHeight, 8, 8);
        
        // Thumb chính
        g.setColor(new Color(240, 240, 240));
        g.fillRoundRect(thumbX, thumbY, thumbWidth, thumbHeight, 8, 8);
        
        // Border thumb
        g.setColor(Color.WHITE);
        g.drawRoundRect(thumbX, thumbY, thumbWidth, thumbHeight, 8, 8);
        
        // Vẽ giá trị phần trăm
        g.setFont(FontManager.getFont(Font.BOLD, 20));
        g.setColor(Color.WHITE);
        String volumeText = (int)(com.arkanoid.game.ui.Menu.globalVolume * 100) + "%";
        g.drawString(volumeText, sliderX + sliderWidth + 20, yPos + sliderHeight - 5);
    }
    
    /**
     * Vẽ màn hình game over
     */
    private void drawGameOver(Graphics g, GamePanel panel) {
        // Overlay tối
        g.setColor(new Color(0, 0, 0, 180));
        g.fillRect(0, 0, width, height);
        
        // Text game over
        g.setColor(Color.WHITE);
        drawCenteredText(g, panel.gameWon ? "YOU WIN!" : "GAME OVER", 50, -50);
        drawCenteredText(g, "Final Score: " + panel.score, 30, 20);
        drawCenteredText(g, "Press ENTER to restart or ESC to quit to menu", 20, 70);
    }
    
    /**
     * Vẽ text ở giữa màn hình
     */
    private void drawCenteredText(Graphics g, String text, int fontSize, int yOffset) {
        g.setFont(FontManager.getFont(fontSize > 35 ? Font.BOLD : Font.PLAIN, fontSize));
        FontMetrics fm = g.getFontMetrics();
        int x = (width - fm.stringWidth(text)) / 2;
        int y = height / 2 + yOffset;
        g.drawString(text, x, y);
    }
    
    /**
     * Cập nhật background image
     */
    public void setBackgroundImage(BufferedImage image) {
        this.backgroundImage = image;
    }
}
