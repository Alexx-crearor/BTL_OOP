package com.arkanoid.game.game;

import java.awt.Rectangle;
import java.util.Random;

import com.arkanoid.game.entity.Ball;
import com.arkanoid.game.entity.Brick;
import com.arkanoid.game.entity.PowerUp;
import com.arkanoid.game.entity.Projectile;

/**
 * GameUpdater - Xử lý tất cả logic update game
 * Chịu trách nhiệm:
 * - Update paddle, balls, powerups, lasers
 * - Kiểm tra va chạm
 * - Kiểm tra điều kiện thắng/thua
 * - Apply power-up effects
 */
public class GameUpdater extends GameComponent {
    private static final int POWERUP_DROP_CHANCE = 30; // 30% cơ hội rơi PowerUp
    private final Random random = new Random();
    
    public GameUpdater(GamePanel panel) {
        super(panel); // Gọi constructor của GameComponent
    }
    
    /**
     * Update toàn bộ game state
     */
    public void update() {
        updatePaddle();
        updateBalls();
        updatePowerUps();
        updateLasers();
        updateBricks(); // Thêm update cho bricks (gạch tái sinh)

        // Cập nhật logic màn 5
        if (panel.levelNumber == 5  && panel.boss != null && panel.boss.isAlive() && panel.gameStarted) {
            updateBoss();
            updateProjectiles();
        }

        checkLevelComplete();
    }
    
    /**
     * Update bricks (đặc biệt cho gạch tái sinh)
     */
    private void updateBricks() {
        for (Brick brick : panel.bricks) {
            brick.update();
        }
    }
    
    /**
     * Update paddle movement
     */
    private void updatePaddle() {
        panel.paddle.update(getGameWidth());
        
        if (panel.movingLeft) {
            panel.paddle.moveLeft(getGameWidth());
        }
        if (panel.movingRight) {
            panel.paddle.moveRight(getGameWidth());
        }
        // Bóng chỉ bắt đầu bay khi nhấn SPACE, không tự động khi di chuyển paddle
    }
    
    /**
     * Update tất cả balls
     */
    private void updateBalls() {
        for (int i = panel.balls.size() - 1; i >= 0; i--) {
            Ball ball = panel.balls.get(i);
            
            // Xử lý ball bị catch
            if (panel.ballCaught && ball == panel.caughtBall) {
                ball.setPosition(panel.paddle.getCenterX() - ball.width / 2, panel.paddle.y - ball.height);
                if (panel.spacePressed) {
                    releaseBall(ball);
                }
                continue;
            }
            
            // Nếu bóng chưa di chuyển, giữ nó ở trên paddle
            if (!ball.isMoving()) {
                ball.setPosition(panel.paddle.getCenterX() - ball.width / 2, panel.paddle.y - ball.height);
            } else {
                ball.update();
                checkBallCollisions(ball);
            }
            
            // Xóa ball rơi xuống dưới
            if (ball.y > GamePanel.HEIGHT) {
                panel.balls.remove(i);
            }
        }
        
        // Kiểm tra mất mạng
        if (panel.balls.isEmpty()) {
            panel.lives--;
            if (panel.lives <= 0) {
                panel.gameOver = true;
                panel.checkAndSubmitHighScore();
            } else {
                panel.resetBall();
            }
        }
    }
    
    /**
     * Thả ball đã bị catch
     */
    private void releaseBall(Ball ball) {
        panel.ballCaught = false;
        panel.caughtBall = null;
        ball.setVelocity(random.nextInt(3) - 1, -2); // Tốc độ ngẫu nhiên
        ball.startMoving();
        panel.spacePressed = false;
    }
    
    /**
     * Update tất cả power-ups
     */
    private void updatePowerUps() {
        panel.powerUps.removeIf(p -> {
            p.update();
            // Kiểm tra collision với paddle
            if (p.getBounds().intersects(panel.paddle.getBounds())) {
                applyPowerUp(p);
                return true; // Remove power-up
            }
            return p.isOffScreen(GamePanel.HEIGHT);
        });
    }
    
    /**
     * Update tất cả lasers
     */
    private void updateLasers() {
        panel.lasers.removeIf(laser -> {
            laser.update();

            // Kiểm tra va chạm với khiên của trùm (chỉ ở màn 5)
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

            // Kiểm tra collision với bricks
            for (Brick brick : panel.bricks) {
                // Bỏ qua gạch đã phá hoặc đang ẩn (tái sinh)
                if (!brick.isDestroyed && !brick.isTemporarilyHidden() && laser.getBounds().intersects(brick.getBounds())) {
                    if (brick.hit()) {
                        panel.score += getScoreForBrick(brick);
                        checkPowerUpDrop(brick);
                    }
                    laser.setActive(false);
                    break; // Laser chỉ phá 1 gạch
                }
            }
            return !laser.isActive() || laser.isOffScreen();
        });
    }
    
    /**
     * Kiểm tra xem level đã hoàn thành chưa
     */
    private void checkLevelComplete() {
        // Nếu đang ở màn 5, điều kiện thắng duy nhất là khi trùm bị đánh bại.
        if (panel.levelNumber == 5) {
            if (panel.boss != null && !panel.boss.isAlive()) {
                // Nếu trùm không còn sống -> THẮNG GAME!
                panel.gameWon = panel.gameOver = true;
                panel.checkAndSubmitHighScore();
            }
            // QUAN TRỌNG: Dừng lại ở đây, không kiểm tra gạch nữa.
            return;
        }

        if (panel.currentLevel.isCompleted()) {
            panel.levelNumber++;
            if (panel.levelNumber > 5) {
                panel.gameWon = panel.gameOver = true;
            } else {
                panel.loadLevel(panel.levelNumber);
                panel.resetBall();
            }
        }
    }
    
    /**
     * Kiểm tra va chạm của ball với tường, paddle, và bricks
     */
    private void checkBallCollisions(Ball ball) {
        Rectangle ballRect = ball.getBounds();

        // Va chạm với tường trái
        if (ball.x <= 0) {
            ball.x = 0;
            ball.reverseX();
        }

        // Va chạm với tường phải
        if (ball.x + ball.width >= getGameWidth()) {
            ball.x = getGameWidth() - ball.width;
            ball.reverseX();
        }

        // Va chạm với tường trên
        if (ball.y <= 0) {
            ball.y = 0;
            ball.reverseY();
        }

        // Va chạm với paddle
        if (ballRect.intersects(panel.paddle.getBounds())) {
            ball.y = panel.paddle.y - ball.height; // Đẩy bóng lên trên paddle
            ball.reverseY();

            // Thêm hiệu ứng góc phản xạ dựa vào vị trí chạm
            int paddleCenter = panel.paddle.x + panel.paddle.width / 2;
            int ballCenter = ball.x + ball.width / 2;
            int diff = ballCenter - paddleCenter;
            ball.dx = diff / 15; // Điều chỉnh góc phản xạ
        }

        // Nếu là màn trùm, xử lý va chạm riêng và dừng lại.
        if (panel.levelNumber == 5 && panel.boss != null && panel.boss.isAlive()) {
            // Va chạm với trùm
            if (ballRect.intersects(panel.boss.getBounds())) {
                panel.boss.takeDamage();
                ball.reverseY();
                // Đẩy bóng ra khỏi trùm để tránh bị dính.
                ball.y = panel.boss.y + panel.boss.height;
            }
            // Va chạm với gạch khiên
            panel.shieldBricks.removeIf(brick -> {
                if (ballRect.intersects(brick.getBounds())) {
                    ball.reverseY();
                    // Đẩy bóng ra khỏi gạch khiên.
                    ball.y = brick.y + brick.height;
                    return brick.hit();
                }
                return false;
            });
        } else {
            // Va chạm với bricks
            for (Brick brick : panel.bricks) {
                // Bỏ qua gạch đã phá hoặc đang ẩn (tái sinh)
                if (!brick.isDestroyed && !brick.isTemporarilyHidden() && ballRect.intersects(brick.getBounds())) {
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

                    // Đánh gạch và nhận điểm
                    if (brick.hit()) {
                        panel.score += getScoreForBrick(brick);
                        checkPowerUpDrop(brick);
                    }

                    // Chỉ phá 1 gạch mỗi frame (trừ khi có Incandescent)
                    if (!ball.isIncandescent()) {
                        break;
                    }
                }
            }
        }
    }
    
    /**
     * Kiểm tra và tạo PowerUp ngẫu nhiên từ gạch
     */
    private void checkPowerUpDrop(Brick brick) {
        if (brick.canDropPowerUp() && random.nextInt(100) < POWERUP_DROP_CHANCE) {
            PowerUp.PowerUpType[] types = PowerUp.PowerUpType.values();
            PowerUp.PowerUpType type = types[random.nextInt(types.length)];
            
            // Tạo PowerUp ở giữa gạch
            int powerUpX = brick.x + brick.width / 2 - 20;
            int powerUpY = brick.y;
            PowerUp powerUp = new PowerUp(powerUpX, powerUpY, type);
            panel.powerUps.add(powerUp);
            
            brick.setDroppedPowerUp(); // Đánh dấu đã drop
        }
    }
    
    /**
     * Apply hiệu ứng của power-up
     */
    private void applyPowerUp(PowerUp powerUp) {
        switch (powerUp.getType()) {
            case ENLARGE:
                panel.paddle.activateEnlarge(600);
                break;
            case REDUCE:
                panel.paddle.activateReduce(600);
                break;
            case LASER:
                panel.paddle.activateLaser(600);
                break;
            case SLOW:
                panel.balls.forEach(b -> b.setSpeed(2));
                break;
            case CATCH:
                panel.ballCaught = true;
                if (!panel.balls.isEmpty()) {
                    panel.caughtBall = panel.balls.get(0);
                }
                break;
            case TWIN:
                addTwinBall();
                break;
            case DISRUPT:
                addMultipleBalls(2);
                break;
            case MEGABALL:
                panel.balls.forEach(b -> b.activateMegaBall(600));
                break;
            case INCANDESCENCE:
                panel.balls.forEach(b -> b.activateIncandescent(600));
                break;
        }
    }
    
    /**
     * Thêm 1 ball giống hệt (Twin power-up)
     */
    private void addTwinBall() {
        if (!panel.balls.isEmpty()) {
            Ball orig = panel.balls.get(0);
            Ball twin = new Ball(orig.x, orig.y, orig.width);
            twin.setVelocity(-orig.dx, orig.dy); // Hướng ngược lại
            twin.startMoving();
            panel.balls.add(twin);
        }
    }
    
    /**
     * Thêm nhiều balls với hướng ngẫu nhiên (Disrupt power-up)
     */
    private void addMultipleBalls(int count) {
        if (!panel.balls.isEmpty()) {
            Ball orig = panel.balls.get(0);
            for (int i = 0; i < count; i++) {
                Ball newBall = new Ball(orig.x, orig.y, orig.width);
                int angle = random.nextInt(360);
                // Tốc độ giảm từ 3 xuống 2
                newBall.setVelocity(
                    (int)(2 * Math.cos(Math.toRadians(angle))),
                    (int)(2 * Math.sin(Math.toRadians(angle)))
                );
                newBall.startMoving();
                panel.balls.add(newBall);
            }
        }
    }
    
    /**
     * Tính điểm thưởng cho từng loại gạch
     */
    private int getScoreForBrick(Brick brick) {
        switch (brick.getType()) {
            case RED: return 90;
            case ORANGE: return 60;
            case GREEN: return 80;
            case CYAN: return 70;
            case BLUE: return 100;
            case LIGHT_BLUE: return 110;
            case GOLD: return 100;
            case SILVER: return 50;
            case REGENERATING: return 120;
            default: return 50;
        }
    }


    /**
     * Cập nhật toàn bộ logic cho trùm, bao gồm di chuyển, tấn công và tạo khiên.
     */
    private void updateBoss() {
        // 1. Cập nhật di chuyển của trùm
        panel.boss.updateWithBoundary(getGameWidth());

        // 2. Xử lý bắn đạn
        if (panel.boss.canAttack()) {
            int projectileX = panel.boss.x + panel.boss.width / 2 - 5; // Căn giữa viên đạn
            int projectileY = panel.boss.y + panel.boss.height;
            panel.projectiles.add(new Projectile(projectileX, projectileY));
        }

        // 3. Xử lý tạo khiên gạch
        if (panel.boss.canCreateShield()) {
            panel.shieldBricks.clear(); // Xóa khiên cũ
            int shieldY = panel.boss.y + panel.boss.height + 10;
            // Tạo một hàng 5 viên gạch cứng làm khiên
            for (int i = 0; i < 5; i++) {
                int brickX = panel.boss.x + i * (Brick.BRICK_WIDTH + 5);
                panel.shieldBricks.add(new Brick(brickX, shieldY, Brick.BrickType.SILVER));
            }
        }

        // 4. Nếu khiên đã bị phá hủy hết, tắt trạng thái khiên của trùm
        if (panel.boss.isShieldActive && panel.shieldBricks.isEmpty()) {
            panel.boss.isShieldActive = false;
        }
    }

    /**
     * Cập nhật các viên đạn của trùm và kiểm tra va chạm với paddle.
     */
    private void updateProjectiles() {
        // Cập nhật vị trí và xóa đạn bay ra khỏi màn hình.
        panel.projectiles.removeIf(projectile -> {
            projectile.update();
            // Kiểm tra va chạm với paddle
            if (projectile.getBounds().intersects(panel.paddle.getBounds())) {
                panel.lives--; // Người chơi mất một mạng
                if (panel.lives <= 0) {
                    panel.gameOver = true;
                    panel.checkAndSubmitHighScore();
                } else {
                    panel.resetBall(); // Reset lại bóng
                }
                return true; // Xóa viên đạn sau khi va chạm
            }
            return projectile.isOffScreen(getGameHeight());
        });
    }
}
