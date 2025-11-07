package com.arkanoid.game.entity;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

/**
 * Lớp Boss - Đại diện cho trùm cuối (Boss) ở level 5
 * 
 * Kế thừa từ GameObject để có các thuộc tính cơ bản
 * 
 * Chức năng chính:
 * - Di chuyển qua lại tự động (AI)
 * - Tấn công bằng Projectiles (đạn)
 * - Tạo shield (gạch bảo vệ) định kỳ
 * - Quản lý health (25 hits để thắng)
 * - Vẽ boss với thanh máu (health bar)
 * 
 * Cơ chế chiến đấu:
 * - Boss có 25 HP, mỗi lần bóng chạm giảm 1 HP
 * - Boss tấn công mỗi 3 giây (180 frames @ 60 FPS)
 * - Boss tạo shield mỗi 10 giây để tự bảo vệ
 * - Người chơi cần phá shield trước khi đánh boss
 */
public class Boss extends GameObject {
    // ============================================================
    // HẰNG SỐ VÀ CẤU HÌNH
    // ============================================================
    
    /** 
     * Máu tối đa của Boss
     * Cần 25 lần bóng đập trúng để đánh bại Boss
     */
    private final int MAX_HEALTH = 25;
    
    /** 
     * Khoảng thời gian giữa các lần tấn công (frames)
     * 180 frames @ 60 FPS = 3 giây
     * Boss sẽ bắn Projectile mỗi 3 giây
     */
    private final int ATTACK_INTERVAL = 180;
    
    /** 
     * Khoảng thời gian giữa các lần tạo shield (frames)
     * 600 frames @ 60 FPS = 10 giây
     * Boss sẽ tạo shield mỗi 10 giây
     */
    private final int SHIELD_INTERVAL = 600;
    
    // ============================================================
    // THUỘC TÍNH HEALTH
    // ============================================================
    
    /** 
     * Máu hiện tại của Boss
     * Bắt đầu = MAX_HEALTH (25)
     * Giảm xuống 0 -> Boss bị đánh bại
     */
    private int currentHealth;
    
    // ============================================================
    // THUỘC TÍNH ATTACK
    // ============================================================
    
    /** 
     * Bộ đếm cooldown cho tấn công (frames)
     * Giảm dần mỗi frame
     * Khi về 0 -> Boss có thể tấn công
     */
    private int attackCooldown;
    
    // ============================================================
    // THUỘC TÍNH SHIELD
    // ============================================================
    
    /** 
     * Bộ đếm cooldown cho tạo shield (frames)
     * Giảm dần mỗi frame khi shield không active
     * Khi về 0 -> Boss có thể tạo shield mới
     */
    private int shieldCooldown;
    
    /** 
     * Trạng thái shield có đang active không
     * true: Shield đang hoạt động (có gạch bảo vệ)
     * false: Không có shield
     * Public để GameUpdater có thể kiểm tra và tạo gạch shield
     */
    public boolean isShieldActive = false;

    // ============================================================
    // CONSTRUCTOR
    // ============================================================
    
    /**
     * Tạo Boss mới
     * 
     * Boss sẽ xuất hiện ở giữa màn hình phía trên
     * Kích thước: 150x50 pixels
     * Tốc độ di chuyển: 1 pixel/frame (chậm)
     * 
     * @param screenWidth Chiều rộng màn hình (để tính vị trí giữa)
     * @param screenHeight Chiều cao màn hình (không dùng nhưng giữ để tương thích)
     */
    public Boss(int screenWidth, int screenHeight) {
        // Gọi constructor GameObject mặc định
        super();
        
        // Thiết lập kích thước Boss (to hơn các entity khác)
        this.width = 150;
        this.height = 50;
        
        // Đặt Boss ở giữa màn hình phía trên
        this.x = screenWidth / 2 - this.width / 2; // Căn giữa theo chiều ngang
        this.y = 50;                                // 50 pixels từ trên xuống
        
        // Tốc độ di chuyển (chậm để dễ nhắm bắn)
        this.dx = 1;

        // Khởi tạo health = max
        this.currentHealth = MAX_HEALTH;
        
        // Khởi tạo cooldowns
        this.attackCooldown = ATTACK_INTERVAL;
        this.shieldCooldown = SHIELD_INTERVAL;
    }

    /**
     * Cập nhật logic của Trùm mỗi frame (override từ GameObject).
     * Di chuyển, đếm ngược tấn công và tạo khiên.
     */
    @Override
    public void update() {
        // 1. Di chuyển qua lại (cần screenWidth từ bên ngoài)
        x += dx;
        // Boundary check sẽ được xử lý trong updateWithBoundary()
        
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
     * Cập nhật với boundary checking
     * 
     * @param screenWidth Chiều rộng màn hình
     */
    public void updateWithBoundary(int screenWidth) {
        // Di chuyển
        x += dx;
        if (x <= 0 || x + width >= screenWidth) {
            dx = -dx; // Đảo chiều khi chạm biên
        }

        // Đếm ngược cooldowns
        if (attackCooldown > 0) {
            attackCooldown--;
        }

        if (!isShieldActive && shieldCooldown > 0) {
            shieldCooldown--;
        }
    }

    /**
     * Vẽ Trùm và thanh máu của nó (override từ GameObject).
     * 
     * @param g Graphics context
     */
    @Override
    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        
        // Vẽ thân trùm
        g2d.setColor(new Color(139, 0, 0)); // Màu đỏ sẫm
        g2d.fillRect(x, y, width, height);

        // Vẽ mắt
        g2d.setColor(Color.YELLOW);
        g2d.fillOval(x + width / 2 - 15, y + 15, 30, 20);

        // Vẽ thanh máu
        // Nền thanh máu
        g2d.setColor(Color.DARK_GRAY);
        g2d.fillRect(x, y - 20, width, 10);
        // Máu hiện tại
        g2d.setColor(Color.RED);
        double healthPercentage = (double) currentHealth / MAX_HEALTH;
        g2d.fillRect(x, y - 20, (int)(width * healthPercentage), 10);
        // Viền
        g2d.setColor(Color.WHITE);
        g2d.drawRect(x, y - 20, width, 10);
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

    /**
     * Override getBounds từ GameObject
     */
    @Override
    public java.awt.Rectangle getBounds() {
        return new java.awt.Rectangle(x, y, width, height);
    }
}