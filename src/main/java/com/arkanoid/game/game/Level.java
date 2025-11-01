package com.arkanoid.game.game;

import java.util.ArrayList;
import java.util.Random;

import com.arkanoid.game.entity.Brick;

public class Level {
    private int levelNumber;
    private ArrayList<Brick> bricks;
    private Random random = new Random();
    
    public Level(int levelNumber) {
        this.levelNumber = levelNumber;
        this.bricks = new ArrayList<>();
        generateLevel();
    }
    
    private void generateLevel() {
        bricks.clear();
        
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
    
    private void generateLevel2() {
        // Level 2: Pattern kim cương
        int centerX = 400;
        int startY = 50;
        
        int[][] pattern = {
            {4, 5},
            {3, 4, 5, 6},
            {2, 3, 4, 5, 6, 7},
            {1, 2, 3, 4, 5, 6, 7, 8},
            {2, 3, 4, 5, 6, 7},
            {3, 4, 5, 6},
            {4, 5}
        };
        
        Brick.BrickType[] types = {
            Brick.BrickType.GOLD,
            Brick.BrickType.ORANGE,
            Brick.BrickType.GREEN,
            Brick.BrickType.CYAN,
            Brick.BrickType.GREEN,
            Brick.BrickType.ORANGE,
            Brick.BrickType.GOLD
        };
        
        for (int row = 0; row < pattern.length; row++) {
            for (int col : pattern[row]) {
                int x = centerX - (pattern[row].length * Brick.BRICK_WIDTH / 2) + 
                        (col - pattern[row][0]) * Brick.BRICK_WIDTH;
                int y = startY + row * Brick.BRICK_HEIGHT;
                bricks.add(new Brick(x, y, types[row]));
            }
        }
    }
    
    private void generateLevel3() {
        // Level 3: Gạch cứng và regenerating
        int startX = 150;
        int startY = 50;
        
        for (int row = 0; row < 7; row++) {
            for (int col = 0; col < 10; col++) {
                int x = startX + col * Brick.BRICK_WIDTH;
                int y = startY + row * Brick.BRICK_HEIGHT;
                
                Brick.BrickType type;
                if (row == 0 || row == 6) {
                    type = Brick.BrickType.REGENERATING;
                } else if ((row + col) % 3 == 0) {
                    type = Brick.BrickType.GOLD;
                } else if ((row + col) % 2 == 0) {
                    type = Brick.BrickType.SILVER;
                } else {
                    type = Brick.BrickType.BLUE;
                }
                
                bricks.add(new Brick(x, y, type));
            }
        }
    }
    
    private void generateLevel4() {
        // Level 4: Pattern khó với nhiều gạch cứng và tái sinh
        int startX = 150;
        int startY = 30;
        
        for (int row = 0; row < 10; row++) {
            for (int col = 0; col < 10; col++) {
                int x = startX + col * Brick.BRICK_WIDTH;
                int y = startY + row * Brick.BRICK_HEIGHT;
                
                Brick.BrickType type;
                
                // Hàng đầu và cuối: gạch tái sinh
                if (row == 0 || row == 9) {
                    type = Brick.BrickType.REGENERATING;
                }
                // Hàng 1 và 8: gạch vàng (cứng nhất)
                else if (row == 1 || row == 8) {
                    type = Brick.BrickType.GOLD;
                }
                // Hàng 2 và 7: gạch bạc (cứng)
                else if (row == 2 || row == 7) {
                    type = Brick.BrickType.SILVER;
                }
                // Các cột biên: gạch vàng
                else if (col == 0 || col == 9) {
                    type = Brick.BrickType.GOLD;
                }
                // Pattern cờ vua ở giữa với gạch cứng
                else if ((row + col) % 2 == 0) {
                    type = (row % 3 == 0) ? Brick.BrickType.GOLD : Brick.BrickType.CYAN;
                }
                // Còn lại: gạch thường
                else {
                    type = Brick.BrickType.ORANGE;
                }
                
                bricks.add(new Brick(x, y, type));
            }
        }
    }
    
    private void generateLevel5() {
        // Level 5: Cực khó với nhiều gạch cứng, tái sinh và pattern phức tạp
        // Xóa sạch danh sách gạch để đảm bảo nó hoàn toàn trống.
        // Đây chính là "tín hiệu" cho GamePanel biết đây là màn trùm.
        bricks.clear();
    }
    
    private void generateRandomLevel() {
        // Level ngẫu nhiên cho các level cao hơn
        int startX = 150;
        int startY = 50;
        int rows = Math.min(6 + levelNumber / 2, 10);
        
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

