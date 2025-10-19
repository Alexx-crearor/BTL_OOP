package com.arkanoid.game.entity;

import java.awt.Graphics;
import java.awt.Rectangle;

/**
 * Lớp cha trừu tượng cho tất cả các đối tượng trong game
 * Áp dụng nguyên tắc đa hình và kế thừa
 */
public abstract class GameObject {
    public int x, y;
    public int width, height;
    public int dx, dy;
    
    // Constructor
    public GameObject() {
        this.x = 0;
        this.y = 0;
        this.width = 0;
        this.height = 0;
        this.dx = 0;
        this.dy = 0;
    }
    
    public GameObject(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.dx = 0;
        this.dy = 0;
    }
    
    // Abstract methods - phải được implement bởi các lớp con
    public abstract void draw(Graphics g);
    public abstract void update();
    
    // Template method - định nghĩa khung xử lý va chạm
    public boolean intersects(GameObject other) {
        return getBounds().intersects(other.getBounds());
    }
    
    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }
    
    // Getters và Setters
    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public int getDx() { return dx; }
    public int getDy() { return dy; }
    
    public void setX(int x) { this.x = x; }
    public void setY(int y) { this.y = y; }
    public void setWidth(int width) { this.width = width; }
    public void setHeight(int height) { this.height = height; }
    public void setDx(int dx) { this.dx = dx; }
    public void setDy(int dy) { this.dy = dy; }
    
    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }
    
    public void setVelocity(int dx, int dy) {
        this.dx = dx;
        this.dy = dy;
    }
}
