package com.arkanoid.game.entity;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;

/**
 * PowerUp class - kế thừa từ GameObject
 * Áp dụng kế thừa: PowerUp kế thừa từ GameObject
 */
public class PowerUp extends GameObject {
    public static final int POWERUP_WIDTH = 40;
    public static final int POWERUP_HEIGHT = 20;
    
    // Loại power-up
    public enum PowerUpType {
        ENLARGE("/Image/Enlarge.gif", "Mở rộng paddle"),
        REDUCE("/Image/Reduce.gif", "Thu nhỏ paddle"),
        LASER("/Image/Laser.gif", "Laser bắn"),
        SLOW("/Image/Slow.gif", "Làm chậm bóng"),
        CATCH("/Image/Catch.gif", "Bắt bóng"),
        TWIN("/Image/Twin.gif", "Chia đôi bóng"),
        DISRUPT("/Image/Disrupt.gif", "Nhiều bóng"),
        MEGABALL("/Image/Megaball.gif", "Bóng khổng lồ"),
        INCANDESCENCE("/Image/Incandescence.gif", "Bóng xuyên thủng");
        
        public final String imagePath;
        public final String description;
        
        PowerUpType(String imagePath, String description) {
            this.imagePath = imagePath;
            this.description = description;
        }
    }
    
    private final PowerUpType type;
    private BufferedImage image;
    private boolean active = true;
    
    public PowerUp(int x, int y, PowerUpType type) {
        super(x, y, POWERUP_WIDTH, POWERUP_HEIGHT);
        this.type = type;
        this.dy = 2; // Tốc độ rơi
        
        // Load image
        loadImage();
    }
    
    private void loadImage() {
        try {
            // Load từ classpath resources
            java.net.URL imgURL = getClass().getResource(type.imagePath);
            if (imgURL != null) {
                image = ImageIO.read(imgURL);
            }
        } catch (Exception e) {
            // Nếu không load được image, sẽ dùng màu sắc thay thế
            image = null;
        }
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
        
        if (image != null) {
            g.drawImage(image, x, y, width, height, null);
        } else {
            // Vẽ bằng màu sắc nếu không có image
            Color color = getColorForType();
            g.setColor(color);
            g.fillRoundRect(x, y, width, height, 10, 10);
            g.setColor(Color.WHITE);
            g.drawRoundRect(x, y, width, height, 10, 10);
            
            // Vẽ chữ viết tắt
            g.setFont(new Font("Arial", Font.BOLD, 10));
            String text = getShortName();
            FontMetrics fm = g.getFontMetrics();
            int textX = x + (width - fm.stringWidth(text)) / 2;
            int textY = y + (height + fm.getAscent()) / 2 - 2;
            g.drawString(text, textX, textY);
        }
    }
    
    private Color getColorForType() {
        Color[] colors = {
            new Color(0, 255, 0), new Color(255, 0, 0), new Color(255, 255, 0),
            new Color(0, 255, 255), new Color(255, 165, 0), new Color(255, 0, 255),
            new Color(128, 0, 255), new Color(255, 100, 100), new Color(255, 215, 0)
        };
        return type.ordinal() < colors.length ? colors[type.ordinal()] : Color.GRAY;
    }
    
    private String getShortName() {
        return type.name().substring(0, 1);
    }
    
    public PowerUpType getType() {
        return type;
    }
    
    public boolean isActive() {
        return active;
    }
    
    public void setActive(boolean active) {
        this.active = active;
    }
    
    public boolean isOffScreen(int screenHeight) {
        return y > screenHeight;
    }
}

