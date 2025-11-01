package com.arkanoid.game.entity;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;

public class Boss extends Item {
    // --- Thuộc tính của Trùm ---
    private final int MAX_HEALTH = 25; // Cần 25 lần bóng đập trúng để thắng
    private int currentHealth;

    // --- Điều khiển hành động ---
    private int attackCooldown;
    private final int ATTACK_INTERVAL = 180; // Tấn công mỗi 3 giây (180 frames @ 60FPS)

    private int shieldCooldown;
    private final int SHIELD_INTERVAL = 600; // Tạo khiên mỗi 10 giây
    public boolean isShieldActive = false;

    public Boss(int screenWidth, int screenHeight) {
        this.width = 150;
        this.height = 50;
        this.x = screenWidth / 2 - this.width / 2; // Bắt đầu ở giữa
        this.y = 50; // Ở gần phía trên màn hình
        this.dx = 1; // Tốc độ di chuyển ngang

        this.currentHealth = MAX_HEALTH;
        this.attackCooldown = ATTACK_INTERVAL;
        this.shieldCooldown = SHIELD_INTERVAL;
    }

    /**
     * Cập nhật logic của Trùm mỗi frame.
     * Di chuyển, đếm ngược tấn công và tạo khiên.
     */
    public void update(int screenWidth) {
        // 1. Di chuyển qua lại
        x += dx;
        if (x <= 0 || x + width >= screenWidth) {
            dx = -dx; // Đảo chiều khi chạm biên
        }

        // 2. Đếm ngược để tấn công
        if (attackCooldown > 0) {
            attackCooldown--;
        }

        // 3. Đếm ngược để tạo khiên (chỉ khi khiên không hoạt động)
        if (!isShieldActive && shieldCooldown > 0) {
            shieldCooldown--;
        }
    }

    /**
     * Vẽ Trùm và thanh máu của nó.
     */
    public void draw(Graphics2D g) {
        // Vẽ thân trùm (bạn có thể thay bằng ảnh)
        g.setColor(new Color(139, 0, 0)); // Màu đỏ sẫm
        g.fillRect(x, y, width, height);

        // Vẽ mắt (để trông ngầu hơn)
        g.setColor(Color.YELLOW);
        g.fillOval(x + width / 2 - 15, y + 15, 30, 20);

        // Vẽ thanh máu
        // Nền thanh máu
        g.setColor(Color.DARK_GRAY);
        g.fillRect(x, y - 20, width, 10);
        // Máu hiện tại
        g.setColor(Color.RED);
        double healthPercentage = (double) currentHealth / MAX_HEALTH;
        g.fillRect(x, y - 20, (int)(width * healthPercentage), 10);
        // Viền
        g.setColor(Color.WHITE);
        g.drawRect(x, y - 20, width, 10);
    }

    /**
     * Trùm nhận sát thương khi bóng chạm vào.
     */
    public void takeDamage() {
        if (currentHealth > 0) {
            currentHealth--;
        }
    }

    public boolean isAlive() {
        return currentHealth > 0;
    }

    /**
     * Kiểm tra xem trùm đã sẵn sàng tấn công chưa.
     * @return true nếu có thể tấn công, ngược lại false.
     */
    public boolean canAttack() {
        if (attackCooldown <= 0) {
            attackCooldown = ATTACK_INTERVAL; // Reset thời gian chờ
            return true;
        }
        return false;
    }

    /**
     * Kiểm tra xem trùm đã sẵn sàng tạo khiên chưa.
     * @return true nếu có thể tạo khiên, ngược lại false.
     */
    public boolean canCreateShield() {
        if (!isShieldActive && shieldCooldown <= 0) {
            shieldCooldown = SHIELD_INTERVAL; // Reset thời gian chờ
            isShieldActive = true; // Đánh dấu khiên đã được kích hoạt
            return true;
        }
        return false;
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }
}