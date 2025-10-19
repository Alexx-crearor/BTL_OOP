package com.arkanoid.game.entity;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;

/**
 * Paddle class - kế thừa từ GameObject
 * Áp dụng kế thừa: Paddle kế thừa tất cả thuộc tính và phương thức từ GameObject
 */
public class Paddle extends GameObject {
    private final int normalWidth = 120;
    private final int enlargedWidth = 180;
    private final int reducedWidth = 80;
    private boolean enlarged = false;
    private boolean reduced = false;
    private int enlargeTimer = 0;
    private int reduceTimer = 0;
    private boolean hasLaser = false;
    private int laserTimer = 0;
    private BufferedImage normalImage;
    private BufferedImage enlargedImage;
    
    public Paddle(int x, int y, int screenWidth, int screenHeight) {
        super(x, y, 120, 20);
        this.dx = 5;
        loadImages();
    }
    
    private void loadImages() {
        try {
            // Load từ classpath resources
            java.net.URL normalURL = getClass().getResource("/Image/VausII.png");
            java.net.URL enlargedURL = getClass().getResource("/Image/VausIIwEnlarge.png");
            
            if (normalURL != null) {
                normalImage = ImageIO.read(normalURL);
            }
            if (enlargedURL != null) {
                enlargedImage = ImageIO.read(enlargedURL);
            }
        } catch (Exception e) {
            // Sử dụng hình vẽ mặc định nếu không load được
            normalImage = null;
            enlargedImage = null;
        }
    }
    
    @Override
    public void update() {
        updateTimer(() -> { enlarged = false; width = normalWidth; }, enlargeTimer--);
        updateTimer(() -> { reduced = false; width = normalWidth; }, reduceTimer--);
        updateTimer(() -> hasLaser = false, laserTimer--);
    }
    
    public void update(int screenWidth) {
        update();
    }
    
    private void updateTimer(Runnable action, int timer) {
        if (timer == 1) action.run();
    }
    
    public void moveLeft(int screenWidth) {
        x = Math.max(0, x - dx);
    }
    
    public void moveRight(int screenWidth) {
        x = Math.min(screenWidth - width, x + dx);
    }
    
    @Override
    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        BufferedImage currentImage = null;
        if (enlarged && enlargedImage != null) {
            currentImage = enlargedImage;
        } else if (normalImage != null) {
            currentImage = normalImage;
        }
        
        if (currentImage != null) {
            g2d.drawImage(currentImage, x, y, width, height, null);
        } else {
            // Vẽ paddle bằng màu nếu không có image
            Color paddleColor;
            if (enlarged) {
                paddleColor = new Color(0, 200, 0);
            } else if (reduced) {
                paddleColor = new Color(200, 0, 0);
            } else {
                paddleColor = new Color(70, 130, 180);
            }
            
            // Gradient
            GradientPaint gp = new GradientPaint(x, y, paddleColor.brighter(), 
                                                 x, y + height, paddleColor.darker());
            g2d.setPaint(gp);
            g2d.fillRoundRect(x, y, width, height, 10, 10);
            
            // Viền
            g2d.setColor(Color.WHITE);
            g2d.drawRoundRect(x, y, width, height, 10, 10);
        }
        
        // Vẽ laser nếu có
        if (hasLaser) {
            g2d.setColor(new Color(255, 255, 0));
            int laserWidth = 4;
            g2d.fillRect(x + 10, y - 5, laserWidth, 5);
            g2d.fillRect(x + width - 14, y - 5, laserWidth, 5);
        }
    }
    
    public void activateEnlarge(int duration) {
        enlarged = true;
        reduced = false;
        reduceTimer = 0;
        enlargeTimer = duration;
        width = enlargedWidth;
    }
    
    public void activateReduce(int duration) {
        reduced = true;
        enlarged = false;
        enlargeTimer = 0;
        reduceTimer = duration;
        width = reducedWidth;
    }
    
    public void activateLaser(int duration) {
        hasLaser = true;
        laserTimer = duration;
    }
    
    public boolean hasLaser() {
        return hasLaser;
    }
    
    public int getCenterX() {
        return x + width / 2;
    }
}

