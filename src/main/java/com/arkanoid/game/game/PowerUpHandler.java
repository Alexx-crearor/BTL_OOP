package com.arkanoid.game.game;

import java.util.Random;

import com.arkanoid.game.entity.Ball;
import com.arkanoid.game.entity.Brick;
import com.arkanoid.game.entity.PowerUp;

// ============================================================
// CLASS: PowerUpHandler - Xử lý logic power-ups
// ============================================================

/**
 * Class PowerUpHandler - Helper class quản lý power-ups
 * 
 * TRÁCH NHIỆM:
 * - Kiểm tra và tạo power-up drop từ gạch (30% chance)
 * - Apply hiệu ứng power-up lên paddle/ball
 * - Tạo thêm balls (Twin, Disrupt)
 * - Tính điểm thưởng cho từng loại gạch
 * 
 * POWER-UP TYPES (9 loại):
 * 1. ENLARGE: To paddle (600 frames)
 * 2. REDUCE: Nhỏ paddle (600 frames)
 * 3. LASER: Bắn laser (600 frames)
 * 4. SLOW: Giảm tốc bóng (speed = 2)
 * 5. CATCH: Bắt bóng
 * 6. TWIN: Tạo 1 bóng clone
 * 7. DISRUPT: Tạo 2 bóng random angle
 * 8. MEGABALL: Xuyên gạch (600 frames)
 * 9. INCANDESCENCE: Xuyên gạch không phá (600 frames)
 * 
 * ĐƯỢC GỌI TỪ:
 * - GameUpdater.checkBallCollisions() → checkPowerUpDrop()
 * - GameUpdater.updatePowerUps() → applyPowerUp()
 * 
 * @author Arkanoid Game Team
 * @version 1.0
 */
public class PowerUpHandler extends GameComponent {
    
    // ============================================================
    // HẰNG SỐ
    // ============================================================
    
    /** Tỷ lệ drop power-up từ gạch (30%) */
    private static final int POWERUP_DROP_CHANCE = 30;
    
    /** Duration cho các power-up có thời hạn (600 frames = 5 giây @ 120 FPS) */
    private static final int POWER_UP_DURATION = 600;
    
    // ============================================================
    // THUỘC TÍNH
    // ============================================================
    
    /** Random generator cho power-up drop */
    private final Random random = new Random();
    
    /**
     * Constructor
     * @param panel GamePanel reference
     */
    public PowerUpHandler(GamePanel panel) {
        super(panel);
    }
    
    // ============================================================
    // POWER-UP DROP - Tạo power-up từ gạch
    // ============================================================
    
    /**
     * Kiểm tra và tạo power-up ngẫu nhiên từ gạch bị phá
     * 
     * LOGIC:
     * 1. Kiểm tra gạch có thể drop power-up không (canDropPowerUp)
     * 2. Random 30% chance
     * 3. Random 1 trong 9 loại power-up
     * 4. Tạo power-up ở vị trí giữa gạch
     * 5. Đánh dấu gạch đã drop (tránh drop nhiều lần)
     * 
     * @param brick Gạch vừa bị phá
     */
    public void checkPowerUpDrop(Brick brick) {
        // BƯỚC 1: Kiểm tra gạch có thể drop không
        if (!brick.canDropPowerUp()) {
            return;
        }
        
        // BƯỚC 2: Random 30% chance
        if (random.nextInt(100) >= POWERUP_DROP_CHANCE) {
            return;
        }
        
        // BƯỚC 3: Random 1 loại power-up
        PowerUp.PowerUpType[] types = PowerUp.PowerUpType.values();
        PowerUp.PowerUpType type = types[random.nextInt(types.length)];
        
        // BƯỚC 4: Tạo power-up ở giữa gạch
        int powerUpX = brick.x + brick.width / 2 - 20; // 20 = half of powerup width
        int powerUpY = brick.y;
        PowerUp powerUp = new PowerUp(powerUpX, powerUpY, type);
        panel.powerUps.add(powerUp);
        
        // BƯỚC 5: Đánh dấu đã drop
        brick.setDroppedPowerUp();
    }
    
    // ============================================================
    // POWER-UP EFFECTS - Apply hiệu ứng
    // ============================================================
    
    /**
     * Apply hiệu ứng của power-up lên game
     * 
     * LOGIC:
     * - Switch-case theo từng loại power-up
     * - Gọi method tương ứng trên paddle/ball
     * - Tạo thêm balls nếu cần (TWIN, DISRUPT)
     * 
     * @param powerUp Power-up đã thu thập
     */
    public void applyPowerUp(PowerUp powerUp) {
        switch (powerUp.getType()) {
            case ENLARGE:
                // To paddle 600 frames (5 giây)
                panel.paddle.activateEnlarge(POWER_UP_DURATION);
                break;
                
            case REDUCE:
                // Nhỏ paddle 600 frames
                panel.paddle.activateReduce(POWER_UP_DURATION);
                break;
                
            case LASER:
                // Bắn laser 600 frames
                panel.paddle.activateLaser(POWER_UP_DURATION);
                break;
                
            case SLOW:
                // Giảm tốc độ tất cả balls về 2
                panel.balls.forEach(b -> b.setSpeed(2));
                break;
                
            case CATCH:
                // Bắt ball đầu tiên
                panel.ballCaught = true;
                if (!panel.balls.isEmpty()) {
                    panel.caughtBall = panel.balls.get(0);
                }
                break;
                
            case TWIN:
                // Tạo 1 ball clone
                addTwinBall();
                break;
                
            case DISRUPT:
                // Tạo 2 balls với góc ngẫu nhiên
                addMultipleBalls(2);
                break;
                
            case MEGABALL:
                // Mega Ball: Xuyên gạch 600 frames
                panel.balls.forEach(b -> b.activateMegaBall(POWER_UP_DURATION));
                break;
                
            case INCANDESCENCE:
                // Incandescent: Xuyên gạch không phá 600 frames
                panel.balls.forEach(b -> b.activateIncandescent(POWER_UP_DURATION));
                break;
        }
    }
    
    // ============================================================
    // BALL CREATION - Tạo thêm balls
    // ============================================================
    
    /**
     * Thêm 1 ball giống hệt (Twin power-up)
     * 
     * LOGIC:
     * - Clone ball đầu tiên
     * - Đổi hướng dx ngược lại (để tách ra)
     * - Bắt đầu di chuyển ngay
     */
    private void addTwinBall() {
        if (panel.balls.isEmpty()) {
            return;
        }
        
        Ball orig = panel.balls.get(0);
        Ball twin = new Ball(orig.x, orig.y, orig.width);
        twin.setVelocity(-orig.dx, orig.dy); // Hướng ngược lại
        twin.startMoving();
        panel.balls.add(twin);
    }
    
    /**
     * Thêm nhiều balls với hướng ngẫu nhiên (Disrupt power-up)
     * 
     * LOGIC:
     * - Tạo count balls mới
     * - Mỗi ball có góc ngẫu nhiên (0-360°)
     * - Tốc độ = 2 (chậm hơn ball gốc)
     * - Tất cả bắt đầu di chuyển ngay
     * 
     * @param count Số lượng balls cần tạo
     */
    private void addMultipleBalls(int count) {
        if (panel.balls.isEmpty()) {
            return;
        }
        
        Ball orig = panel.balls.get(0);
        for (int i = 0; i < count; i++) {
            Ball newBall = new Ball(orig.x, orig.y, orig.width);
            
            // Random góc 0-360°
            int angle = random.nextInt(360);
            
            // Tính velocity từ góc (tốc độ = 2)
            newBall.setVelocity(
                (int)(2 * Math.cos(Math.toRadians(angle))),
                (int)(2 * Math.sin(Math.toRadians(angle)))
            );
            
            newBall.startMoving();
            panel.balls.add(newBall);
        }
    }
    
    // ============================================================
    // SCORING - Tính điểm cho gạch
    // ============================================================
    
    /**
     * Tính điểm thưởng cho từng loại gạch
     * 
     * BẢNG ĐIỂM:
     * - RED: 90 điểm (1 hit)
     * - ORANGE: 60 điểm (1 hit)
     * - GREEN: 80 điểm (1 hit)
     * - CYAN: 70 điểm (2 hits)
     * - BLUE: 100 điểm (2 hits)
     * - LIGHT_BLUE: 110 điểm (2 hits)
     * - GOLD: 100 điểm (3 hits)
     * - SILVER: 50 điểm (4 hits)
     * - REGENERATING: 120 điểm (special)
     * 
     * @param brick Gạch vừa bị phá
     * @return Điểm thưởng
     */
    public int getScoreForBrick(Brick brick) {
        switch (brick.getType()) {
            case RED:
                return 90;
            case ORANGE:
                return 60;
            case GREEN:
                return 80;
            case CYAN:
                return 70;
            case BLUE:
                return 100;
            case LIGHT_BLUE:
                return 110;
            case GOLD:
                return 100;
            case SILVER:
                return 50;
            case REGENERATING:
                return 120;
            default:
                return 50; // Fallback
        }
    }
}
