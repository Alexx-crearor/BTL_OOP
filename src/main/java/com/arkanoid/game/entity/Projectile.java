package com.arkanoid.game.entity;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;

// Lớp Projectile đại diện cho viên đạn mà Trùm bắn ra.
// Nó cũng kế thừa từ Item để có các thuộc tính cơ bản.
public class Projectile extends Item {

    public Projectile(int startX, int startY) {
        this.x = startX;
        this.y = startY;
        this.width = 10;
        this.height = 20;
        this.dy = 3; // Tốc độ bay xuống
    }

    // Cập nhật vị trí của đạn mỗi frame
    public void update() {
        y += dy;
    }

    // Vẽ viên đạn
    public void draw(Graphics2D g) {
        g.setColor(Color.ORANGE);
        g.fillRect(x, y, width, height);
    }

    // Kiểm tra xem đạn đã bay ra khỏi màn hình chưa
    public boolean isOffScreen(int screenHeight) {
        return y > screenHeight;
    }

    // Lấy hình chữ nhật bao quanh để xét va chạm
    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }
}