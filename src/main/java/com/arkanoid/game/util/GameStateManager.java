package com.arkanoid.game.util;

import java.io.Serializable;

/**
 * Quản lý trạng thái game (lưu và load game)
 */
public class GameStateManager {
    private static GameStateManager instance;
    private GameState savedState;
    
    private GameStateManager() {
        savedState = null;
    }
    
    public static GameStateManager getInstance() {
        if (instance == null) {
            instance = new GameStateManager();
        }
        return instance;
    }
    
    public void saveGameState(GameState state) {
        this.savedState = state;
    }
    
    public GameState loadGameState() {
        return savedState;
    }
    
    public boolean hasSavedGame() {
        return savedState != null;
    }
    
    public void clearSavedGame() {
        savedState = null;
    }
    
    /**
     * Class chứa thông tin trạng thái game
     */
    public static class GameState implements Serializable {
        private static final long serialVersionUID = 1L;
        
        public int score;
        public int lives;
        public int levelNumber;
        
        // Thông tin paddle
        public int paddleX;
        public int paddleWidth;
        
        // Thông tin balls
        public BallState[] balls;
        
        // Thông tin bricks
        public BrickState[] bricks;
        
        public GameState(int score, int lives, int levelNumber, int paddleX, int paddleWidth) {
            this.score = score;
            this.lives = lives;
            this.levelNumber = levelNumber;
            this.paddleX = paddleX;
            this.paddleWidth = paddleWidth;
        }
    }
    
    public static class BallState implements Serializable {
        private static final long serialVersionUID = 1L;
        
        public int x, y, width;
        public int dx, dy;
        public boolean isMoving;
        
        public BallState(int x, int y, int width, int dx, int dy, boolean isMoving) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.dx = dx;
            this.dy = dy;
            this.isMoving = isMoving;
        }
    }
    
    public static class BrickState implements Serializable {
        private static final long serialVersionUID = 1L;
        
        public int x, y, width, height;
        public int hitCount;
        public String type;
        public boolean isDestroyed;
        
        public BrickState(int x, int y, int width, int height, int hitCount, String type, boolean isDestroyed) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.hitCount = hitCount;
            this.type = type;
            this.isDestroyed = isDestroyed;
        }
    }
}
