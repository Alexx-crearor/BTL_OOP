package com.arkanoid.game.game;

/**
 * Base class cho tất cả các component của game
 * Giúp giảm code lặp lại bằng cách cung cấp truy cập chung đến GamePanel
 */
public abstract class GameComponent {
    protected final GamePanel panel;
    
    /**
     * Constructor chung cho tất cả component
     * @param panel GamePanel chứa tất cả game state
     */
    public GameComponent(GamePanel panel) {
        this.panel = panel;
    }
    
    // Các phương thức tiện ích chung
    protected int getGameWidth() {
        return GamePanel.WIDTH;
    }
    
    protected int getGameHeight() {
        return GamePanel.HEIGHT;
    }
}
