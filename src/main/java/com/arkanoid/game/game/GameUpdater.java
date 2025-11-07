package com.arkanoid.game.game;

import java.util.Random;

import com.arkanoid.game.entity.Ball;
import com.arkanoid.game.entity.Brick;
import com.arkanoid.game.entity.PowerUp;

// ============================================================
// CLASS: GameUpdater - Điều phối update logic (REFACTORED)
// ============================================================

/**
 * Class GameUpdater - Điều phối toàn bộ update logic của game
 * 
 * KIẾN TRÚC MỚI (Refactored):
 * GameUpdater đã được chia nhỏ thành 3 helper classes chuyên biệt:
 * 
 * 1. CollisionHandler: Xử lý tất cả va chạm
 *    - Ball vs Wall/Paddle/Brick/Boss
 *    - Laser vs Brick/Shield
 *    - Projectile vs Paddle
 * 
 * 2. PowerUpHandler: Xử lý power-ups
 *    - Drop power-up từ gạch (30% chance)
 *    - Apply effects (ENLARGE, LASER, TWIN, etc.)
 *    - Tạo thêm balls
 *    - Tính điểm
 * 
 * 3. BossHandler: Xử lý boss fight (Level 5)
 *    - Update boss movement
 *    - Tạo projectiles và shield
 *    - Update projectiles
 * 
 * TRÁCH NHIỆM CÒN LẠI:
 * - Điều phối update() cho tất cả entities
 * - Update paddle movement
 * - Update balls (movement + caught ball)
 * - Update power-ups (fall + collect)
 * - Update lasers (shoot + move)
 * - Kiểm tra level complete/game over
 * 
 * KẾT QUẢ REFACTORING:
 * - Từ 447 dòng → ~200 dòng (giảm 55%)
 * - Logic rõ ràng, dễ maintain
 * - Tách biệt concerns (SRP - Single Responsibility Principle)
 * 
 * @author Arkanoid Game Team
 * @version 2.0 (Refactored)
 */
public class GameUpdater extends GameComponent {
    
    // ============================================================
    // HELPER CLASSES - Các lớp trợ giúp
    // ============================================================
    
    /** Handler xử lý tất cả va chạm */
    private final CollisionHandler collisionHandler;
    
    /** Handler xử lý power-ups */
    private final PowerUpHandler powerUpHandler;
    
    /** Handler xử lý boss fight */
    private final BossHandler bossHandler;
    
    /** Random generator cho release ball */
    private final Random random = new Random();
    
    // ============================================================
    // CONSTRUCTOR
    // ============================================================
    
    /**
     * Constructor - Khởi tạo GameUpdater và các helper classes
     * 
     * @param panel GamePanel reference
     */
    public GameUpdater(GamePanel panel) {
        super(panel);
        
        // Khởi tạo helper classes
        this.collisionHandler = new CollisionHandler(panel);
        this.powerUpHandler = new PowerUpHandler(panel);
        this.bossHandler = new BossHandler(panel);
    }
    
    // ============================================================
    // MAIN UPDATE - Điều phối update cho tất cả entities
    // ============================================================
    
    /**
     * Update toàn bộ game state - Main update loop
     * 
     * FLOW:
     * 1. Update paddle (movement, timers)
     * 2. Update balls (movement, collision, caught ball)
     * 3. Update power-ups (fall, collect)
     * 4. Update lasers (shoot, hit)
     * 5. Update bricks (regenerating)
     * 6. Update boss fight (level 5 only)
     * 7. Check level complete/game over
     * 
     * GỌI TỪ: GamePanel.run() trong game loop
     */
    public void update() {
        // BƯỚC 1-5: Update entities cơ bản
        updatePaddle();
        updateBalls();
        updatePowerUps();
        updateLasers();
        updateBricks();

        // BƯỚC 6: Update boss fight (chỉ ở màn 5)
        if (panel.levelNumber == 5 && panel.boss != null && panel.boss.isAlive() && panel.gameStarted) {
            bossHandler.updateBoss();
            bossHandler.updateProjectiles();
        }

        // BƯỚC 7: Kiểm tra level complete
        checkLevelComplete();
    }
    
    // ============================================================
    // ENTITY UPDATES - Update từng loại entity
    // ============================================================
    
    /**
     * Update bricks (đặc biệt cho gạch tái sinh)
     * 
     * LOGIC:
     * - Gọi update() cho mỗi brick
     * - Regenerating brick tự động reappear sau 10 giây
     */
    private void updateBricks() {
        for (Brick brick : panel.bricks) {
            brick.update();
        }
    }
    
    /**
     * Update paddle movement và timers
     * 
     * LOGIC:
     * - Update timers (enlarge, reduce, laser)
     * - Di chuyển left/right theo input
     * - Bóng chỉ bay khi nhấn SPACE, không tự động
     */
    private void updatePaddle() {
        // Update timers và power-up states
        panel.paddle.update(getGameWidth());
        
        // Di chuyển theo input
        if (panel.movingLeft) {
            panel.paddle.moveLeft(getGameWidth());
        }
        if (panel.movingRight) {
            panel.paddle.moveRight(getGameWidth());
        }
    }
    
    /**
     * Update tất cả balls
     * 
     * LOGIC:
     * 1. Xử lý caught ball (giữ trên paddle, release khi nhấn Space)
     * 2. Xử lý ball chưa moving (giữ trên paddle)
     * 3. Xử lý ball đang moving (update position + check collision)
     * 4. Xóa ball rơi ra ngoài
     * 5. Kiểm tra mất mạng (khi hết balls)
     */
    private void updateBalls() {
        // Loop ngược để có thể xóa an toàn
        for (int i = panel.balls.size() - 1; i >= 0; i--) {
            Ball ball = panel.balls.get(i);
            
            // CASE 1: Ball bị catch (power-up CATCH)
            if (panel.ballCaught && ball == panel.caughtBall) {
                // Giữ ball trên paddle
                ball.setPosition(
                    panel.paddle.getCenterX() - ball.width / 2,
                    panel.paddle.y - ball.height
                );
                
                // Release khi nhấn Space
                if (panel.spacePressed) {
                    releaseBall(ball);
                }
                continue; // Skip collision check
            }
            
            // CASE 2: Ball chưa moving (lần đầu chơi)
            if (!ball.isMoving()) {
                // Giữ ball trên paddle
                ball.setPosition(
                    panel.paddle.getCenterX() - ball.width / 2,
                    panel.paddle.y - ball.height
                );
            } else {
                // CASE 3: Ball đang moving
                ball.update(); // Cập nhật vị trí
                
                // Kiểm tra va chạm (ủy quyền cho CollisionHandler)
                checkBallCollisions(ball);
            }
            
            // CASE 4: Xóa ball rơi ra ngoài
            if (ball.y > GamePanel.HEIGHT) {
                panel.balls.remove(i);
            }
        }
        
        // CASE 5: Kiểm tra mất mạng (hết balls)
        if (panel.balls.isEmpty()) {
            panel.lives--;
            
            if (panel.lives <= 0) {
                // Game Over
                panel.gameOver = true;
                panel.checkAndSubmitHighScore();
            } else {
                // Còn mạng - Reset ball
                panel.resetBall();
            }
        }
    }
    
    /**
     * Thả ball đã bị catch (CATCH power-up)
     * 
     * LOGIC:
     * - Tắt caught state
     * - Set velocity ngẫu nhiên cho ball
     * - Bắt đầu di chuyển
     */
    private void releaseBall(Ball ball) {
        panel.ballCaught = false;
        panel.caughtBall = null;
        
        // Velocity ngẫu nhiên: dx = [-1, 0, 1], dy = -2
        ball.setVelocity(random.nextInt(3) - 1, -2);
        ball.startMoving();
        
        panel.spacePressed = false; // Reset Space flag
    }
    
    /**
     * Update tất cả power-ups
     * 
     * LOGIC:
     * 1. Update vị trí (rơi xuống)
     * 2. Kiểm tra va chạm với paddle → Thu thập
     * 3. Apply hiệu ứng (ủy quyền cho PowerUpHandler)
     * 4. Xóa power-up off-screen
     */
    private void updatePowerUps() {
        panel.powerUps.removeIf(p -> {
            // BƯỚC 1: Update vị trí (rơi xuống)
            p.update();
            
            // BƯỚC 2 + 3: Kiểm tra collision và apply effect
            if (p.getBounds().intersects(panel.paddle.getBounds())) {
                // Ủy quyền apply effect cho PowerUpHandler
                powerUpHandler.applyPowerUp(p);
                return true; // Remove power-up sau khi collect
            }
            
            // BƯỚC 4: Remove nếu off-screen
            return p.isOffScreen(GamePanel.HEIGHT);
        });
    }
    
    /**
     * Update tất cả lasers
     * 
     * LOGIC:
     * 1. Update vị trí (bay lên)
     * 2. Kiểm tra va chạm với bricks/shield (ủy quyền cho CollisionHandler)
     * 3. Tính điểm và drop power-up khi phá gạch
     * 4. Xóa laser inactive hoặc off-screen
     */
    private void updateLasers() {
        panel.lasers.removeIf(laser -> {
            // BƯỚC 1: Update vị trí
            laser.update();

            // BƯỚC 2: Kiểm tra va chạm (ủy quyền cho CollisionHandler)
            // CollisionHandler sẽ return true nếu laser hit something
            boolean hitSomething = collisionHandler.checkLaserCollisions(laser, brick -> {
                // BƯỚC 3: Callback khi phá gạch
                panel.score += powerUpHandler.getScoreForBrick(brick);
                powerUpHandler.checkPowerUpDrop(brick);
            });
            
            if (hitSomething) {
                return true; // Remove laser
            }
            
            // BƯỚC 4: Remove nếu inactive hoặc off-screen
            return !laser.isActive() || laser.isOffScreen();
        });
    }
    
    // ============================================================
    // COLLISION CHECK - Kiểm tra va chạm (Delegate to CollisionHandler)
    // ============================================================
    
    /**
     * Kiểm tra va chạm của ball (ủy quyền cho CollisionHandler)
     * 
     * REFACTORED:
     * - Logic va chạm được chuyển sang CollisionHandler
     * - GameUpdater chỉ xử lý callback (tính điểm, drop power-up)
     * 
     * @param ball Ball cần kiểm tra
     */
    private void checkBallCollisions(Ball ball) {
        // Ủy quyền cho CollisionHandler với 2 callbacks:
        
        // Callback 1: Khi phá gạch
        CollisionHandler.BrickHitCallback onBrickHit = brick -> {
            panel.score += powerUpHandler.getScoreForBrick(brick);
            powerUpHandler.checkPowerUpDrop(brick);
        };
        
        // Callback 2: Khi đánh boss
        CollisionHandler.BossHitCallback onBossHit = boss -> {
            boss.takeDamage();
        };
        
        // Gọi CollisionHandler để xử lý tất cả va chạm
        collisionHandler.checkBallCollisions(ball, onBrickHit, onBossHit);
    }
    
    // ============================================================
    // LEVEL CHECK - Kiểm tra hoàn thành level
    // ============================================================
    
    /**
     * Kiểm tra xem level đã hoàn thành chưa
     * 
     * LOGIC:
     * - Level 5 (Boss): Thắng khi boss chết
     * - Level 1-4: Thắng khi phá hết gạch (trừ regenerating)
     * - Chuyển sang level tiếp theo hoặc kết thúc game
     */
    private void checkLevelComplete() {
        // TRƯỜNG HỢP 1: Màn boss (Level 5)
        // Điều kiện thắng duy nhất: Boss bị đánh bại
        if (panel.levelNumber == 5) {
            if (panel.boss != null && !panel.boss.isAlive()) {
                // THẮNG GAME!
                panel.gameWon = true;
                panel.gameOver = true;
                panel.checkAndSubmitHighScore();
            }
            return; // Không kiểm tra gạch ở màn 5
        }

        // TRƯỜNG HỢP 2: Màn thường (Level 1-4)
        // Điều kiện thắng: Phá hết gạch (trừ regenerating)
        if (panel.currentLevel.isCompleted()) {
            panel.levelNumber++;
            
            if (panel.levelNumber > 5) {
                // Hoàn thành tất cả levels
                panel.gameWon = true;
                panel.gameOver = true;
            } else {
                // Chuyển sang level tiếp theo
                panel.loadLevel(panel.levelNumber);
                panel.resetBall();
            }
        }
    }
}
