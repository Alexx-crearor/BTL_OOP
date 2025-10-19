package com.arkanoid.game.entity;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;

/**
 * Laser class - kế thừa từ GameObject
 */
public class Laser extends GameObject {
    private boolean active = true;
    
    public Laser(int x, int y) {
        super(x, y, 4, 15);
        this.dy = -8;
    }
    
    @Override
    public void update() {
        if (active) {
            y += dy;
        }
    }
    
    @Override
    public void draw(Graphics g) {
        if (!active) return;
        Graphics2D g2d = (Graphics2D) g;
        g2d.setPaint(new GradientPaint(x, y, new Color(255, 255, 100), x, y + height, new Color(255, 0, 0)));
        g2d.fillRect(x, y, width, height);
        g2d.setColor(new Color(255, 255, 255, 200));
        g2d.drawRect(x, y, width, height);
    }
    
    public boolean isActive() { 
        return active; 
    }
    
    public void setActive(boolean active) { 
        this.active = active; 
    }
    
    public boolean isOffScreen() { 
        return y + height < 0; 
    }
}

