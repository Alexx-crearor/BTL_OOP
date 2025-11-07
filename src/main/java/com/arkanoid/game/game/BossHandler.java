package com.arkanoid.game.game;

import com.arkanoid.game.entity.Brick;
import com.arkanoid.game.entity.Projectile;

// ============================================================
// CLASS: BossHandler - Xử lý logic boss fight (Level 5)
// ============================================================

/**
 * Class BossHandler - Helper class quản lý boss fight
 * 
 * TRÁCH NHIỆM:
 * - Update boss movement và boundaries
 * - Tạo projectiles (đạn boss) mỗi 3 giây
 * - Tạo shield bricks (khiên) mỗi 10 giây
 * - Update projectiles và kiểm tra va chạm với paddle
 * 
 * BOSS MECHANICS:
 * - HP: 25
 * - Attack Interval: 180 frames (3 giây @ 120 FPS)
 * - Shield Interval: 600 frames (10 giây @ 120 FPS)
 * - Movement: Di chuyển ngang, bounce khi chạm tường
 * - Shield: 5 SILVER bricks bảo vệ
 * 
 * WIN CONDITION:
 * - Boss HP = 0 → Game Won!
 * 
 * ĐƯỢC GỌI TỪ:
 * - GameUpdater.update() → updateBoss()
 * - GameUpdater.update() → updateProjectiles()
 * 
 * @author Arkanoid Game Team
 * @version 1.0
 */
public class BossHandler extends GameComponent {
    
    // ============================================================
    // HẰNG SỐ
    // ============================================================
    
    /** Số lượng gạch trong shield (5 gạch SILVER) */
    private static final int SHIELD_BRICK_COUNT = 5;
    
    /** Khoảng cách giữa các gạch shield */
    private static final int SHIELD_BRICK_SPACING = 5;
    
    /** Khoảng cách shield dưới boss */
    private static final int SHIELD_OFFSET_Y = 10;
    
    /**
     * Constructor
     * @param panel GamePanel reference
     */
    public BossHandler(GamePanel panel) {
        super(panel);
    }
    
    // ============================================================
    // BOSS UPDATE - Cập nhật boss
    // ============================================================
    
    /**
     * Cập nhật toàn bộ logic boss
     * 
     * BƯỚC THỰC HIỆN:
     * 1. Update di chuyển boss (bounce tại tường)
     * 2. Kiểm tra và bắn projectile (mỗi 3 giây)
     * 3. Kiểm tra và tạo shield (mỗi 10 giây)
     * 4. Vô hiệu hóa shield khi hết gạch
     * 
     * GỌI TỪ: GameUpdater.update()
     */
    public void updateBoss() {
        // Kiểm tra boss có tồn tại và còn sống không
        if (panel.boss == null || !panel.boss.isAlive()) {
            return;
        }
        
        // BƯỚC 1: Cập nhật di chuyển boss
        // Boss di chuyển ngang, bounce khi chạm tường
        panel.boss.updateWithBoundary(getGameWidth());

        // BƯỚC 2: Xử lý bắn đạn (mỗi 3 giây)
        if (panel.boss.canAttack()) {
            createProjectile();
        }

        // BƯỚC 3: Xử lý tạo khiên gạch (mỗi 10 giây)
        if (panel.boss.canCreateShield()) {
            createShield();
        }

        // BƯỚC 4: Nếu khiên đã bị phá hủy hết, tắt trạng thái khiên
        if (panel.boss.isShieldActive && panel.shieldBricks.isEmpty()) {
            panel.boss.isShieldActive = false;
        }
    }
    
    /**
     * Tạo projectile (viên đạn) từ boss
     * 
     * LOGIC:
     * - Vị trí X: Giữa boss (boss.x + boss.width/2 - 5)
     * - Vị trí Y: Dưới boss (boss.y + boss.height)
     * - Projectile di chuyển thẳng xuống
     */
    private void createProjectile() {
        // Tính vị trí căn giữa boss
        int projectileX = panel.boss.x + panel.boss.width / 2 - 5; // 5 = half of projectile width
        int projectileY = panel.boss.y + panel.boss.height;
        
        // Tạo và thêm projectile vào danh sách
        panel.projectiles.add(new Projectile(projectileX, projectileY));
    }
    
    /**
     * Tạo shield bricks (khiên gạch) bảo vệ boss
     * 
     * LOGIC:
     * - Xóa shield cũ (nếu có)
     * - Tạo 5 gạch SILVER làm khiên
     * - Vị trí: Dưới boss, cách 10 pixels
     * - Bố trí: Ngang, cách nhau 5 pixels
     */
    private void createShield() {
        // Xóa khiên cũ
        panel.shieldBricks.clear();
        
        // Tính vị trí Y của shield (dưới boss)
        int shieldY = panel.boss.y + panel.boss.height + SHIELD_OFFSET_Y;
        
        // Tạo 5 viên gạch SILVER làm khiên
        for (int i = 0; i < SHIELD_BRICK_COUNT; i++) {
            int brickX = panel.boss.x + i * (Brick.BRICK_WIDTH + SHIELD_BRICK_SPACING);
            panel.shieldBricks.add(new Brick(brickX, shieldY, Brick.BrickType.SILVER));
        }
    }
    
    // ============================================================
    // PROJECTILE UPDATE - Cập nhật đạn boss
    // ============================================================
    
    /**
     * Cập nhật các viên đạn của boss
     * 
     * LOGIC:
     * 1. Update vị trí projectile (di chuyển xuống)
     * 2. Kiểm tra va chạm với paddle
     *    - Nếu chạm: Trừ 1 mạng, reset ball
     *    - Nếu hết mạng: Game Over
     * 3. Xóa projectile ra khỏi màn hình
     * 
     * GỌI TỪ: GameUpdater.update()
     */
    public void updateProjectiles() {
        // Dùng removeIf để update và remove trong 1 loop
        panel.projectiles.removeIf(projectile -> {
            // BƯỚC 1: Update vị trí
            projectile.update();
            
            // BƯỚC 2: Kiểm tra va chạm với paddle
            if (projectile.getBounds().intersects(panel.paddle.getBounds())) {
                // Trừ 1 mạng
                panel.lives--;
                
                // Kiểm tra game over
                if (panel.lives <= 0) {
                    panel.gameOver = true;
                    panel.checkAndSubmitHighScore();
                } else {
                    // Reset ball nếu còn mạng
                    panel.resetBall();
                }
                
                return true; // Xóa projectile sau khi va chạm
            }
            
            // BƯỚC 3: Xóa projectile ra khỏi màn hình
            return projectile.isOffScreen(getGameHeight());
        });
    }
}
