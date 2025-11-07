package com.arkanoid.game.game;

import java.util.ArrayList;
import java.util.Random;

import com.arkanoid.game.entity.Brick;

// ============================================================
// CLASS: Level - Tạo patterns gạch cho từng level
// ============================================================

/**
 * Class Level - Quản lý và tạo patterns gạch cho từng màn chơi
 * 
 * GAME CÓ 5 LEVELS:
 * 1. Level 1: Hàng ngang đơn giản (6 rows x 10 cols) - EASY
 * 2. Level 2: Kim cương (Diamond pattern) - MEDIUM
 * 3. Level 3: Hỗn hợp đầy thách thức - HARD
 * 4. Level 4: Pattern phức tạp với REGENERATING bricks
 * 5. Level 5: Boss fight (NO BRICKS - chỉ có boss)
 * 
 * BRICK TYPES SỬ DỤNG:
 * - RED, ORANGE, GREEN: 1 hit
 * - CYAN, BLUE, LIGHT_BLUE: 2 hits
 * - GOLD: 3 hits
 * - SILVER: 4 hits
 * - REGENERATING: Biến mất 10s rồi xuất hiện lại
 * 
 * WIN CONDITION:
 * - Level 1-4: Phá hết gạch (trừ REGENERATING)
 * - Level 5: Đánh bại boss (HP = 0)
 * 
 * PATTERN DESIGN:
 * - Symmetry: Nhiều patterns đối xứng để đẹp
 * - Color progression: Màu từ đơn giản → phức tạp
 * - Difficulty: Từ dễ → khó (số hit tăng dần)
 * 
 * @author Arkanoid Game Team
 * @version 1.0
 */
public class Level {
    
    // ============================================================
    // THUỘC TÍNH
    // ============================================================
    
    /** Số level hiện tại (1-5) */
    private int levelNumber;
    
    /** Danh sách gạch của level */
    private ArrayList<Brick> bricks;
    
    /** Random generator cho level random/variations */
    private Random random = new Random();
    
    // ============================================================
    // CONSTRUCTOR
    // ============================================================
    
    /**
     * Constructor - Tạo level với số thứ tự
     * 
     * @param levelNumber Số level (1-5)
     */
    public Level(int levelNumber) {
        this.levelNumber = levelNumber;
        this.bricks = new ArrayList<>();
        generateLevel(); // Tạo pattern gạch
    }
    
    // ============================================================
    // LEVEL GENERATION - Tạo patterns
    // ============================================================
    
    /**
     * Tạo pattern gạch theo level number
     * 
     * DISPATCH:
     * - Level 1 → generateLevel1()
     * - Level 2 → generateLevel2()
     * - Level 3 → generateLevel3()
     * - Level 4 → generateLevel4()
     * - Level 5 → generateLevel5() (empty - boss fight)
     * - Khác → generateRandomLevel()
     */
    private void generateLevel() {
        bricks.clear(); // Xóa bricks cũ nếu có
        
        switch (levelNumber) {
            case 1:
                generateLevel1();
                break;
            case 2:
                generateLevel2();
                break;
            case 3:
                generateLevel3();
                break;
            case 4:
                generateLevel4();
                break;
            case 5:
                generateLevel5();
                break;
            default:
                generateRandomLevel();
                break;
        }
    }
    
    // ============================================================
    // LEVEL 1 - Hàng ngang đơn giản
    // ============================================================
    
    /**
     * Level 1: Pattern hàng ngang đơn giản
     * 
     * PATTERN:
     * - 6 hàng x 10 cột = 60 gạch
     * - Mỗi hàng 1 màu (RED → ORANGE → GREEN → CYAN → BLUE → LIGHT_BLUE)
     * - Tất cả 1-hit bricks (dễ phá)
     * 
     * LAYOUT:
     * ```
     * R R R R R R R R R R   (RED row)
     * O O O O O O O O O O   (ORANGE row)
     * G G G G G G G G G G   (GREEN row)
     * C C C C C C C C C C   (CYAN row)
     * B B B B B B B B B B   (BLUE row)
     * L L L L L L L L L L   (LIGHT_BLUE row)
     * ```
     * 
     * DIFFICULTY: ⭐ (EASY)
     */
    private void generateLevel1() {
        // Level 1: Pattern đơn giản - hàng ngang
        int startX = 150;
        int startY = 50;
        
        for (int row = 0; row < 6; row++) {
            Brick.BrickType type;
            switch (row) {
                case 0: type = Brick.BrickType.RED; break;
                case 1: type = Brick.BrickType.ORANGE; break;
                case 2: type = Brick.BrickType.GREEN; break;
                case 3: type = Brick.BrickType.CYAN; break;
                case 4: type = Brick.BrickType.BLUE; break;
                default: type = Brick.BrickType.LIGHT_BLUE; break;
            }
            
            for (int col = 0; col < 10; col++) {
                int x = startX + col * Brick.BRICK_WIDTH;
                int y = startY + row * Brick.BRICK_HEIGHT;
                bricks.add(new Brick(x, y, type));
            }
        }
    }
    
    // ============================================================
    // LEVEL 2 - Diamond Pattern (Kim cương)
    // ============================================================
    
    /**
     * Level 2: Diamond pattern (hình kim cương)
     * 
     * PATTERN:
     * - 7 hàng tạo thành hình kim cương
     * - Sử dụng 2D array để define vị trí columns cho mỗi row
     * - Màu sắc: GOLD (viền) → ORANGE → GREEN → CYAN (trung tâm)
     * - Symmetry: Mirror từ center outward
     * 
     * LAYOUT:
     * ```
     *         G G           (Row 0: 2 bricks)
     *       O O O O         (Row 1: 4 bricks)
     *     G G G G G G       (Row 2: 6 bricks)
     *   C C C C C C C C     (Row 3: 8 bricks - widest)
     *     G G G G G G       (Row 4: 6 bricks)
     *       O O O O         (Row 5: 4 bricks)
     *         G G           (Row 6: 2 bricks)
     * ```
     * 
     * DIFFICULTY: ⭐⭐ (MEDIUM)
     * 
     * TECHNIQUE:
     * - 2D array pattern[row] = {col indices}
     * - Brick X = centerX + (col - 4.5) * BRICK_WIDTH
     */
    private void generateLevel2() {
        // Level 2: Diamond pattern
        int centerX = 400;
        int startY = 50;
        
        // Pattern definition: Mỗi row có các column indices
        int[][] pattern = {
            {4, 5},                      // Row 0: Center 2 bricks
            {3, 4, 5, 6},                // Row 1: 4 bricks
            {2, 3, 4, 5, 6, 7},          // Row 2: 6 bricks
            {1, 2, 3, 4, 5, 6, 7, 8},    // Row 3: 8 bricks (widest)
            {2, 3, 4, 5, 6, 7},          // Row 4: 6 bricks (mirror row 2)
            {3, 4, 5, 6},                // Row 5: 4 bricks (mirror row 1)
            {4, 5}                       // Row 6: 2 bricks (mirror row 0)
        };
        
        // Colors: GOLD borders, ORANGE/GREEN middle, CYAN center
        Brick.BrickType[] types = {
            Brick.BrickType.GOLD,        // Row 0 (top)
            Brick.BrickType.ORANGE,      // Row 1
            Brick.BrickType.GREEN,       // Row 2
            Brick.BrickType.CYAN,        // Row 3 (center/widest)
            Brick.BrickType.GREEN,       // Row 4 (mirror)
            Brick.BrickType.ORANGE,      // Row 5 (mirror)
            Brick.BrickType.GOLD
        };
        
        // Tạo bricks theo pattern
        for (int row = 0; row < pattern.length; row++) {
            for (int col : pattern[row]) {
                // Calculate X để center diamond
                int x = centerX - (pattern[row].length * Brick.BRICK_WIDTH / 2) + 
                        (col - pattern[row][0]) * Brick.BRICK_WIDTH;
                int y = startY + row * Brick.BRICK_HEIGHT;
                bricks.add(new Brick(x, y, types[row]));
            }
        }
    }
    
    // ============================================================
    // LEVEL 3 - Hard Pattern với REGENERATING bricks
    // ============================================================
    
    /**
     * Level 3: Hard pattern với REGENERATING bricks
     * 
     * PATTERN:
     * - 7 hàng x 10 cột = 70 gạch
     * - Hàng đầu + hàng cuối: REGENERATING (tự hồi máu sau 3s nếu chưa phá hết)
     * - Hàng giữa: Mix GOLD (2-hit), SILVER (3-hit), BLUE (1-hit)
     * - Logic: Sử dụng modulo để tạo pattern
     * 
     * BRICK DISTRIBUTION:
     * - Row 0: R R R R R R R R R R  (REGENERATING - 10 bricks)
     * - Row 1-5: Mix pattern (GOLD/SILVER/BLUE based on modulo)
     * - Row 6: R R R R R R R R R R  (REGENERATING - 10 bricks)
     * 
     * MODULO LOGIC:
     * - (row + col) % 3 == 0 → GOLD (2-hit)
     * - (row + col) % 2 == 0 → SILVER (3-hit)
     * - else → BLUE (1-hit)
     * 
     * REGENERATING MECHANIC:
     * - Nếu brick bị hit nhưng không phá hết trong 3 giây → regen về max hits
     * - Gây khó khăn: Player phải focus phá dần hoặc phá nhanh
     * 
     * DIFFICULTY: ⭐⭐⭐⭐ (HARD)
     */
    private void generateLevel3() {
        // Level 3: Hard pattern với REGENERATING bricks
        int startX = 150;
        int startY = 50;
        
        for (int row = 0; row < 7; row++) {
            for (int col = 0; col < 10; col++) {
                int x = startX + col * Brick.BRICK_WIDTH;
                int y = startY + row * Brick.BRICK_HEIGHT;
                
                Brick.BrickType type;
                
                // Hàng đầu và cuối: REGENERATING
                if (row == 0 || row == 6) {
                    type = Brick.BrickType.REGENERATING;
                } 
                // Hàng giữa: Mix pattern dựa trên modulo
                else if ((row + col) % 3 == 0) {
                    type = Brick.BrickType.GOLD;      // 2-hit
                } else if ((row + col) % 2 == 0) {
                    type = Brick.BrickType.SILVER;    // 3-hit
                } else {
                    type = Brick.BrickType.BLUE;      // 1-hit
                }
                
                bricks.add(new Brick(x, y, type));
            }
        }
    }
    
    // ============================================================
    // LEVEL 4 - Complex Pattern (Phức tạp nhất)
    // ============================================================
    
    /**
     * Level 4: Complex pattern - Level khó nhất trước boss
     * 
     * PATTERN:
     * - Mix tất cả brick types (except REGENERATING)
     * - Multiple zones với difficulty khác nhau
     * - Strategic placement: GOLD ở vị trí khó hit
     * - Checkerboard pattern ở giữa
     * 
     * LAYOUT STRATEGY:
     * ```
     * G G G G G G G G G G    (Row 0: GOLD wall - 2-hit barrier)
     * S - S - S - S - S -    (Row 1: SILVER checkerboard - 3-hit)
     * - S - S - S - S - S    (Row 2: SILVER checkerboard inverted)
     * B B B B B B B B B B    (Row 3: BLUE - 1-hit breather)
     * O O O O O O O O O O    (Row 4: ORANGE - 1-hit)
     * Mix pattern...          (Rows 5-7: Complex mix)
     * ```
     * 
     * DIFFICULTY: ⭐⭐⭐⭐⭐ (VERY HARD)
     */
    private void generateLevel4() {
        // Level 4: Most complex pattern before boss
        int startX = 150;
        int startY = 30;
        
        for (int row = 0; row < 10; row++) {
            for (int col = 0; col < 10; col++) {
                int x = startX + col * Brick.BRICK_WIDTH;
                int y = startY + row * Brick.BRICK_HEIGHT;
                
                Brick.BrickType type;
                
                // Top và bottom walls: REGENERATING (barriers)
                if (row == 0 || row == 9) {
                    type = Brick.BrickType.REGENERATING;
                }
                // Rows 1 & 8: GOLD wall (2-hit barrier)
                else if (row == 1 || row == 8) {
                    type = Brick.BrickType.GOLD;
                }
                // Rows 2 & 7: SILVER wall (3-hit barrier)
                else if (row == 2 || row == 7) {
                    type = Brick.BrickType.SILVER;
                }
                // Side columns: GOLD pillars (strategic placement)
                else if (col == 0 || col == 9) {
                    type = Brick.BrickType.GOLD;
                }
                // Checkerboard pattern trong center (alternating difficulty)
                else if ((row + col) % 2 == 0) {
                    type = (row % 3 == 0) ? Brick.BrickType.GOLD : Brick.BrickType.CYAN;
                }
                // Fill: ORANGE bricks
                else {
                    type = Brick.BrickType.ORANGE;
                }
                
                bricks.add(new Brick(x, y, type));
            }
        }
    }
    
    // ============================================================
    // LEVEL 5 - Boss Fight (Không có bricks)
    // ============================================================
    
    /**
     * Level 5: BOSS FIGHT - Level cuối cùng
     * 
     * PATTERN:
     * - **KHÔNG CÓ BRICKS** (empty level)
     * - Clear bricks list → Signal cho GamePanel spawn Boss
     * 
     * BOSS MECHANICS:
     * - Boss xuất hiện ở top center (300 HP)
     * - Bắn projectiles xuống paddle
     * - Tạo shield bricks (5 SILVER) phía trước
     * - Di chuyển trái-phải
     * 
     * WIN CONDITION:
     * - Phá hết 300 HP của boss
     * - Boss explodes → Game Won!
     * 
     * ĐƯỢC DETECT BỞI:
     * ```java
     * // Trong GamePanel.java:
     * if (currentLevel.getBricks().isEmpty() && levelNumber == 5) {
     *     spawnBoss(); // Create boss entity
     * }
     * ```
     * 
     * DIFFICULTY: ⭐⭐⭐⭐⭐⭐ (BOSS)
     */
    private void generateLevel5() {
        // Level 5: Boss fight - Không có bricks
        // Clear list → GamePanel sẽ detect và spawn boss
        bricks.clear();
    }
    
    // ============================================================
    // RANDOM LEVEL - Fallback pattern
    // ============================================================
    
    /**
     * Generate random level pattern
     * 
     * ĐƯỢC GỌI KHI: levelNumber > 5 (nếu có)
     * 
     * LOGIC:
     * - Rows tăng dần theo level: 6 + levelNumber/2 (max 10)
     * - Random brick types từ BrickType.values()
     * - 10 columns mỗi row
     * 
     * NOTE: Hiện tại game chỉ có 5 levels, method này là fallback
     */
    private void generateRandomLevel() {
        // Random level cho levels > 5
        int startX = 150;
        int startY = 50;
        int rows = Math.min(6 + levelNumber / 2, 10); // Scale with level
        
        Brick.BrickType[] types = Brick.BrickType.values();
        
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < 10; col++) {
                if (random.nextInt(100) < 80) { // 80% chance có gạch
                    int x = startX + col * Brick.BRICK_WIDTH;
                    int y = startY + row * Brick.BRICK_HEIGHT;
                    
                    Brick.BrickType type = types[random.nextInt(types.length)];
                    bricks.add(new Brick(x, y, type));
                }
            }
        }
    }
    
    public ArrayList<Brick> getBricks() {
        return bricks;
    }
    
    public int getLevelNumber() {
        return levelNumber;
    }
    
    public boolean isCompleted() {
        return getRemainingBricks() == 0;
    }
    
    public int getRemainingBricks() {
        return (int) bricks.stream()
            .filter(b -> !b.isDestroyed && b.getType() != Brick.BrickType.REGENERATING)
            .count();
    }
}

