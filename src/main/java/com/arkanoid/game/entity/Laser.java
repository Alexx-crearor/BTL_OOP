package com.arkanoid.game.entity;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

public class Laser extends Item {
    private boolean active = true;
    
    public Laser(int x, int y) {
        this.x = x;
        this.y = y;
        this.width = 4;
        this.height = 15;
        this.dy = -8;
    }
    
    public void update() {
        if (active) y += dy;
    }
    
    public void draw(Graphics g) {
        if (!active) return;
        Graphics2D g2d = (Graphics2D) g;
        g2d.setPaint(new GradientPaint(x, y, new Color(255, 255, 100), x, y + height, new Color(255, 0, 0)));
        g2d.fillRect(x, y, width, height);
        g2d.setColor(new Color(255, 255, 255, 200));
        g2d.drawRect(x, y, width, height);
    }
    
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public Rectangle getBounds() { return new Rectangle(x, y, width, height); }
    public boolean isOffScreen() { return y + height < 0; }
}

