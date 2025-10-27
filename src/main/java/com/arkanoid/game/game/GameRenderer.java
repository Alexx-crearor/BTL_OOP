package com.arkanoid.game.game;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import com.arkanoid.game.entity.Ball;
import com.arkanoid.game.entity.Brick;
import com.arkanoid.game.entity.Laser;
import com.arkanoid.game.entity.PowerUp;
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
        g.drawString(title, titleX, height / 2 - 80);
        
        // Các lựa chọn
        String[] options = {"RESUME", "QUIT TO MENU"};
        g.setFont(FontManager.getFont(Font.BOLD, 30));
        fm = g.getFontMetrics();
        
        for (int i = 0; i < options.length; i++) {
            if (i == panel.pauseMenuSelection) {
                // Lựa chọn đang được chọn (màu vàng + mũi tên)
                g.setColor(Color.YELLOW);
                String arrow = "> ";
                g.drawString(arrow + options[i], 
                    (width - fm.stringWidth(arrow + options[i])) / 2, 
                    height / 2 + i * 50);
            } else {
                // Lựa chọn không được chọn
                g.setColor(Color.LIGHT_GRAY);
                g.drawString(options[i], 
                    (width - fm.stringWidth(options[i])) / 2, 
                    height / 2 + i * 50);
            }
        }
        
        // Hướng dẫn
        g.setColor(Color.WHITE);
        g.setFont(FontManager.getFont(Font.PLAIN, 16));
        fm = g.getFontMetrics();
        String hint = "Use UP/DOWN to select, ENTER to confirm, ESC to resume";
        g.drawString(hint, (width - fm.stringWidth(hint)) / 2, height - 50);
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
