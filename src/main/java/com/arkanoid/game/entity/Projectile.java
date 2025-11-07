package com.arkanoid.game.entity;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

/**
 * Lớp Projectile đại diện cho viên đạn mà Boss bắn ra.
 * Kế thừa từ GameObject để có các thuộc tính và methods cơ bản.
 */
public class Projectile extends GameObject {
    
    private static final int PROJECTILE_WIDTH = 10;
    private static final int PROJECTILE_HEIGHT = 20;
    private static final int PROJECTILE_SPEED = 3;
    
    /**
     * Constructor tạo projectile tại vị trí cho trước
     * 
     * @param startX Tọa độ X ban đầu
     * @param startY Tọa độ Y ban đầu
     */
    public Projectile(int startX, int startY) {
        super(startX, startY, PROJECTILE_WIDTH, PROJECTILE_HEIGHT);
        this.dy = PROJECTILE_SPEED; // Tốc độ bay xuống
    }
    
    /**
     * Cập nhật vị trí của đạn mỗi frame
     * Override từ GameObject
     */
    @Override
    public void update() {
        y += dy;
    }
    
    /**
     * Vẽ viên đạn lên màn hình
     * Override từ GameObject
     * 
     * @param g Graphics context
     */
    @Override
    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        
        // Vẽ projectile màu cam
        g2d.setColor(Color.ORANGE);
        g2d.fillRect(x, y, width, height);
        
        // Vẽ highlight trắng ở giữa để có hiệu ứng 3D
        g2d.setColor(Color.WHITE);
        g2d.fillRect(x + width/2 - 1, y, 2, height);
    }
    
    /**
     * Kiểm tra xem đạn đã bay ra khỏi màn hình chưa
     * 
     * @param screenHeight Chiều cao màn hình
     * @return true nếu đạn đã ra ngoài màn hình
     */
    public boolean isOffScreen(int screenHeight) {
        return y > screenHeight;
    }
}