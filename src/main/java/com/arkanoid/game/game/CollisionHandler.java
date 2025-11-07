package com.arkanoid.game.game;

import java.awt.Rectangle;

import com.arkanoid.game.entity.Ball;
import com.arkanoid.game.entity.Brick;
import com.arkanoid.game.entity.Laser;

// ============================================================
// CLASS: CollisionHandler - Xử lý tất cả va chạm trong game
// ============================================================

/**
 * Class CollisionHandler - Helper class xử lý collision detection
 * 
 * TRÁCH NHIỆM:
 * - Kiểm tra va chạm Ball với Wall/Paddle/Brick
 * - Kiểm tra va chạm Laser với Brick/Shield
 * - Kiểm tra va chạm Projectile với Paddle
 * - Xử lý phản xạ và đổi hướng
 * 
 * ĐƯỢC GỌI TỪ:
 * - GameUpdater.updateBalls() → checkBallCollisions()
 * - GameUpdater.updateLasers() → checkLaserCollisions()
 * - BossHandler.updateProjectiles() → checkProjectileCollisions()
 * 
 * DESIGN PATTERN: Helper/Utility Class
 * - Extends GameComponent để truy cập panel
 * - Các method static để dễ gọi
 * 
 * @author Arkanoid Game Team
 * @version 1.0
 */
public class CollisionHandler extends GameComponent {
    
    /**
     * Constructor
     * @param panel GamePanel reference
     */
    public CollisionHandler(GamePanel panel) {
        super(panel);
    }
    
    // ============================================================
    // BALL COLLISION - Va chạm của Ball
    // ============================================================
    
    /**
     * Kiểm tra và xử lý tất cả va chạm của ball
     * 
     * LOGIC:
     * 1. Va chạm với tường (trái, phải, trên)
     * 2. Va chạm với paddle (có hiệu ứng góc phản xạ)
     * 3. Va chạm với boss (nếu level 5)
     * 4. Va chạm với shield bricks (nếu boss có khiên)
     * 5. Va chạm với bricks thường
     * 
     * @param ball Ball cần kiểm tra
     * @param onBrickHit Callback khi phá gạch (để tính điểm, drop power-up)
     * @param onBossHit Callback khi đánh boss
     */
    public void checkBallCollisions(Ball ball, BrickHitCallback onBrickHit, BossHitCallback onBossHit) {
        Rectangle ballRect = ball.getBounds();

        // BƯỚC 1: Va chạm với tường trái
        if (ball.x <= 0) {
            ball.x = 0;
            ball.reverseX();
        }

        // BƯỚC 2: Va chạm với tường phải
        if (ball.x + ball.width >= getGameWidth()) {
            ball.x = getGameWidth() - ball.width;
            ball.reverseX();
        }

        // BƯỚC 3: Va chạm với tường trên
        if (ball.y <= 0) {
            ball.y = 0;
            ball.reverseY();
        }

        // BƯỚC 4: Va chạm với paddle
        if (ballRect.intersects(panel.paddle.getBounds())) {
            // Đẩy bóng lên trên paddle
            ball.y = panel.paddle.y - ball.height;
            ball.reverseY();

            // Thêm hiệu ứng góc phản xạ dựa vào vị trí chạm
            // Càng xa center, góc phản xạ càng lớn
            int paddleCenter = panel.paddle.x + panel.paddle.width / 2;
            int ballCenter = ball.x + ball.width / 2;
            int diff = ballCenter - paddleCenter;
            ball.dx = diff / 15; // Điều chỉnh góc phản xạ
        }

        // BƯỚC 5: Nếu là màn boss (level 5), xử lý va chạm riêng
        if (panel.levelNumber == 5 && panel.boss != null && panel.boss.isAlive()) {
            checkBallBossCollision(ball, ballRect, onBossHit);
        } else {
            // BƯỚC 6: Va chạm với bricks thường (level 1-4)
            checkBallBrickCollision(ball, ballRect, onBrickHit);
        }
    }
    
    /**
     * Kiểm tra va chạm ball với boss và shield bricks
     * 
     * @param ball Ball object
     * @param ballRect Ball bounds
     * @param onBossHit Callback khi đánh boss
     */
    private void checkBallBossCollision(Ball ball, Rectangle ballRect, BossHitCallback onBossHit) {
        // Va chạm với boss
        if (ballRect.intersects(panel.boss.getBounds())) {
            if (onBossHit != null) {
                onBossHit.onHit(panel.boss);
            }
            ball.reverseY();
            // Đẩy bóng ra khỏi boss để tránh bị dính
            ball.y = panel.boss.y + panel.boss.height;
        }
        
        // Va chạm với shield bricks (gạch khiên bảo vệ boss)
        panel.shieldBricks.removeIf(brick -> {
            if (ballRect.intersects(brick.getBounds())) {
                ball.reverseY();
                // Đẩy bóng ra khỏi gạch khiên
                ball.y = brick.y + brick.height;
                return brick.hit(); // Phá gạch khiên
            }
            return false;
        });
    }
    
    /**
     * Kiểm tra va chạm ball với bricks thường
     * 
     * LOGIC:
     * - Xác định hướng va chạm (ngang/dọc) để đổi hướng chính xác
     * - Ball Incandescent: Xuyên qua không đổi hướng
     * - Ball Mega Ball: Xuyên qua nhiều gạch
     * - Ball thường: Chỉ phá 1 gạch rồi dừng
     * 
     * @param ball Ball object
     * @param ballRect Ball bounds
     * @param onBrickHit Callback khi phá gạch
     */
    private void checkBallBrickCollision(Ball ball, Rectangle ballRect, BrickHitCallback onBrickHit) {
        for (Brick brick : panel.bricks) {
            // Bỏ qua gạch đã phá hoặc đang ẩn (tái sinh)
            if (brick.isDestroyed || brick.isTemporarilyHidden()) {
                continue;
            }
            
            if (ballRect.intersects(brick.getBounds())) {
                // Nếu không có Incandescent, phải đổi hướng
                if (!ball.isIncandescent()) {
                    // Xác định hướng va chạm: ngang hay dọc
                    Rectangle intersection = ballRect.intersection(brick.getBounds());
                    if (intersection.width < intersection.height) {
                        ball.reverseX(); // Va chạm từ trái/phải
                    } else {
                        ball.reverseY(); // Va chạm từ trên/dưới
                    }
                }

                // Đánh gạch và gọi callback
                if (brick.hit()) {
                    if (onBrickHit != null) {
                        onBrickHit.onHit(brick);
                    }
                }

                // Chỉ phá 1 gạch mỗi frame (trừ khi có Incandescent)
                if (!ball.isIncandescent()) {
                    break;
                }
            }
        }
    }
    
    // ============================================================
    // LASER COLLISION - Va chạm của Laser
    // ============================================================
    
    /**
     * Kiểm tra va chạm của laser với bricks và shield
     * 
     * RETURN:
     * - true: Laser hit something → should be removed
     * - false: Laser không hit gì → keep it
     * 
     * @param laser Laser cần kiểm tra
     * @param onBrickHit Callback khi laser phá gạch
     * @return true nếu laser hit và cần xóa
     */
    public boolean checkLaserCollisions(Laser laser, BrickHitCallback onBrickHit) {
        // Kiểm tra va chạm với shield bricks (chỉ ở màn 5)
        if (panel.levelNumber == 5) {
            for (Brick shieldBrick : panel.shieldBricks) {
                if (laser.getBounds().intersects(shieldBrick.getBounds())) {
                    shieldBrick.hit(); // Gạch khiên nhận sát thương
                    laser.setActive(false);
                    return true; // Xóa laser ngay lập tức
                }
            }
            // Xóa các gạch khiên đã bị phá hủy
            panel.shieldBricks.removeIf(b -> b.isDestroyed);
        }

        // Kiểm tra collision với bricks thường
        for (Brick brick : panel.bricks) {
            // Bỏ qua gạch đã phá hoặc đang ẩn (tái sinh)
            if (brick.isDestroyed || brick.isTemporarilyHidden()) {
                continue;
            }
            
            if (laser.getBounds().intersects(brick.getBounds())) {
                if (brick.hit()) {
                    if (onBrickHit != null) {
                        onBrickHit.onHit(brick);
                    }
                }
                laser.setActive(false);
                return true; // Laser chỉ phá 1 gạch
            }
        }
        
        return false; // Laser không hit gì
    }
    
    // ============================================================
    // CALLBACK INTERFACES - Để GameUpdater xử lý logic
    // ============================================================
    
    /**
     * Interface callback khi brick bị hit
     * Cho phép GameUpdater xử lý tính điểm và drop power-up
     */
    public interface BrickHitCallback {
        void onHit(Brick brick);
    }
    
    /**
     * Interface callback khi boss bị hit
     * Cho phép GameUpdater xử lý damage và effects
     */
    public interface BossHitCallback {
        void onHit(com.arkanoid.game.entity.Boss boss);
    }
}
