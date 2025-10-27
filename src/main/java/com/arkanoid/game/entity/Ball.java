package com.arkanoid.game.entity;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;

/**
 * Ball class - kế thừa từ GameObject
 * Áp dụng kế thừa: Ball kế thừa các thuộc tính và phương thức từ GameObject
 */
public class Ball extends GameObject {
    private int baseSpeed = 2; // Giảm từ 3 xuống 2 để chậm hơn
    private boolean megaBall = false;
    private boolean incandescent = false; // Xuyên thủng gạch
    private int megaBallTimer = 0;
    private int incandescentTimer = 0;
    private boolean isMoving = false; // Bóng chỉ di chuyển khi paddle di chuyển
    
    // Sprite image cho bóng
    private static BufferedImage ballSprite = null;
    
    static {
        try {
            java.net.URL imgURL = Ball.class.getResource("/Image/sprite-ball.png");
            if (imgURL != null) {
                ballSprite = ImageIO.read(imgURL);
                System.out.println("Ball sprite loaded successfully!");
            } else {
                System.out.println("Ball sprite not found: /Image/sprite-ball.png");
            }
        } catch (Exception e) {
            System.out.println("Error loading ball sprite: " + e.getMessage());
        }
    }
    
    public Ball(int x, int y, int size) {
        super(x, y, size, size);
        this.dx = 0;
        this.dy = 0;
    }
    
    @Override
    public void update() {
        if (isMoving) {
            x += dx;
            y += dy;
        }
        
        // Cập nhật timers
        if (megaBallTimer > 0 && --megaBallTimer == 0) { 
            megaBall = false; 
            width = height = 20; 
        }
        if (incandescentTimer > 0 && --incandescentTimer == 0) {
            incandescent = false;
        }
    }
    
    public void setSpeed(int speed) {
        baseSpeed = speed;
        // Giữ hướng hiện tại nhưng thay đổi tốc độ
        dx = dx > 0 ? speed : (dx < 0 ? -speed : dx);
        dy = dy > 0 ? speed : (dy < 0 ? -speed : dy);
    }
    
    public void startMoving() {
        if (!isMoving) {
            isMoving = true;
            // Bắt đầu di chuyển với hướng ngẫu nhiên nhẹ
            dx = (int)(Math.random() * 3) - 1; // -1, 0, hoặc 1
            dy = -baseSpeed;
        }
    }
    
    public boolean isMoving() {
        return isMoving;
    }
    
    public void stopMoving() {
        isMoving = false;
        dx = 0;
        dy = 0;
    }
    
    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        if (ballSprite != null) {
            // Vẽ bằng sprite image
            if (megaBall) {
                // Bóng khổng lồ - vẽ lớn hơn với hiệu ứng đỏ
                g2d.setColor(new Color(255, 100, 100, 100));
                g2d.fillOval(x - 5, y - 5, width + 10, height + 10); // Aura đỏ
                g2d.drawImage(ballSprite, x, y, width, height, null);
            } else if (incandescent) {
                // Bóng xuyên thấu - màu CAM với aura cam sáng
                g2d.setColor(new Color(255, 140, 0, 120)); // Aura cam
                g2d.fillOval(x - 4, y - 4, width + 8, height + 8);
                // Vẽ bóng với màu cam overlay
                g2d.setColor(new Color(255, 140, 0)); // Màu cam đậm
                g2d.fillOval(x, y, width, height);
                // Thêm highlight
                g2d.setColor(new Color(255, 200, 100, 200));
                g2d.fillOval(x + width/4, y + height/4, width/2, height/2);
            } else {
                // Bóng thường
                g2d.drawImage(ballSprite, x, y, width, height, null);
            }
        } else {
            // Fallback: vẽ bằng gradient nếu không load được sprite
            if (megaBall) {
                // Vẽ bóng khổng lồ với hiệu ứng
                GradientPaint gp = new GradientPaint(x, y, new Color(255, 100, 100), 
                                                     x + width, y + height, new Color(255, 50, 50));
                g2d.setPaint(gp);
            } else if (incandescent) {
                // Vẽ bóng xuyên thấu với màu cam
                GradientPaint gp = new GradientPaint(x, y, new Color(255, 180, 0), 
                                                     x + width, y + height, new Color(255, 100, 0));
                g2d.setPaint(gp);
            } else {
                // Bóng thường
                GradientPaint gp = new GradientPaint(x, y, Color.WHITE, 
                                                     x + width, y + height, Color.RED);
                g2d.setPaint(gp);
            }
            
            g2d.fillOval(x, y, width, height);
            
            // Highlight
            g2d.setColor(new Color(255, 255, 255, 150));
            g2d.fillOval(x + width/4, y + height/4, width/3, height/3);
        }
    }
    
    public void reverseX() {
        dx = -dx;
    }
    
    public void reverseY() {
        dy = -dy;
    }
    
    public void activateMegaBall(int duration) {
        megaBall = true;
        megaBallTimer = duration;
        width = 40;
        height = 40;
    }
    
    public void activateIncandescent(int duration) {
        incandescent = true;
        incandescentTimer = duration;
    }
    
    public boolean isMegaBall() {
        return megaBall;
    }
    
    public boolean isIncandescent() {
        return incandescent;
    }
}
