package com.arkanoid.game.entity;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.HashMap;

import javax.imageio.ImageIO;

/**
 * Brick class - kế thừa từ GameObject
 * Áp dụng kế thừa: Brick kế thừa tất cả từ GameObject
 */
public class Brick extends GameObject {
    public static final int BRICK_WIDTH = 48;
    public static final int BRICK_HEIGHT = 20;
    
    // Cache hình ảnh gạch (static để chia sẻ giữa các instance)
    private static HashMap<BrickType, BufferedImage> imageCache = new HashMap<>();
    
    // Loại gạch
    public enum BrickType {
        RED(1, new Color(220, 20, 60), "/Image/RedWall.png"),
        ORANGE(1, new Color(255, 140, 0), "/Image/OrangeWall.png"),
        GREEN(1, new Color(34, 139, 34), "/Image/GreenWall.png"),
        CYAN(2, new Color(0, 206, 209), "/Image/CyanWall.png"),
        BLUE(2, new Color(30, 144, 255), "/Image/BlueWall.png"),
        LIGHT_BLUE(2, new Color(135, 206, 250), "/Image/LightBlueWall.png"),
        GOLD(3, new Color(255, 215, 0), "/Image/GoldWall.png"),
        SILVER(4, new Color(192, 192, 192), "/Image/SilverWall.png"),
        REGENERATING(999, new Color(138, 43, 226), "/Image/RegeneratingWall.png");
        
        public final int hits;
        public final Color color;
        public final String imagePath;
        
        BrickType(int hits, Color color, String imagePath) {
            this.hits = hits;
            this.color = color;
            this.imagePath = imagePath;
        }
    }
    
    private BrickType type;
    private int currentHits;
    public boolean isDestroyed = false;
    private boolean hasDroppedPowerUp = false;
    
    public Brick(int x, int y, BrickType type) {
        super(x, y, BRICK_WIDTH, BRICK_HEIGHT);
        this.type = type;
        this.currentHits = type.hits;
        
        // Load hình ảnh nếu chưa có trong cache
        loadImageIfNeeded(type);
    }
    
    @Override
    public void update() {
        // Brick không cần update mỗi frame
    }
    
    private static void loadImageIfNeeded(BrickType type) {
        if (!imageCache.containsKey(type)) {
            try {
                // Load từ classpath resources
                java.net.URL imgURL = Brick.class.getResource(type.imagePath);
                if (imgURL != null) {
                    BufferedImage img = ImageIO.read(imgURL);
                    imageCache.put(type, img);
                } else {
                    // Không tìm thấy resource, dùng màu thay thế
                    imageCache.put(type, null);
                }
            } catch (Exception e) {
                // Không load được, sẽ dùng màu thay thế
                imageCache.put(type, null);
            }
        }
    }
    
    public boolean hit() {
        if (type == BrickType.REGENERATING) {
            return false; // Gạch không thể phá hủy
        }
        
        currentHits--;
        if (currentHits <= 0) {
            isDestroyed = true;
            return true;
        }
        return false;
    }
    
    @Override
    public void draw(Graphics g) {
        if (isDestroyed) return;
        
        BufferedImage image = imageCache.get(type);
        
        if (image != null) {
            // Vẽ bằng hình ảnh
            g.drawImage(image, x, y, width, height, null);
            
            // Thêm hiệu ứng sáng hơn khi bị đập
            if (currentHits < type.hits) {
                g.setColor(new Color(255, 255, 255, 80));
                g.fillRect(x, y, width, height);
            }
            
            // Hiển thị số lần đập còn lại
            if (type.hits > 1 && type != BrickType.REGENERATING) {
                g.setColor(Color.WHITE);
                g.setFont(new Font("Arial", Font.BOLD, 14));
                drawCenteredText(g, String.valueOf(currentHits));
            }
        } else {
            // Dự phòng: vẽ bằng màu sắc nếu không có image
            Color color = currentHits < type.hits ? brighten(type.color, 50) : type.color;
            g.setColor(color);
            g.fillRect(x, y, width, height);
            
            // Vẽ viền và highlight
            g.setColor(Color.WHITE);
            g.drawRect(x, y, width, height);
            g.drawRect(x + 1, y + 1, width - 2, height - 2);
            g.setColor(new Color(255, 255, 255, 100));
            g.fillRect(x + 2, y + 2, width - 4, 3);
            
            // Hiển thị số lần đập còn lại
            if (type.hits > 1) {
                g.setColor(Color.WHITE);
                g.setFont(new Font("Arial", Font.BOLD, 12));
                drawCenteredText(g, String.valueOf(currentHits));
            }
        }
    }
    
    private Color brighten(Color c, int amount) {
        return new Color(
            Math.min(255, c.getRed() + amount),
            Math.min(255, c.getGreen() + amount),
            Math.min(255, c.getBlue() + amount)
        );
    }
    
    private void drawCenteredText(Graphics g, String text) {
        FontMetrics fm = g.getFontMetrics();
        int textX = x + (width - fm.stringWidth(text)) / 2;
        int textY = y + (height + fm.getAscent()) / 2 - 2;
        g.drawString(text, textX, textY);
    }
    
    public BrickType getType() {
        return type;
    }
    
    public boolean canDropPowerUp() {
        return !hasDroppedPowerUp;
    }
    
    public void setDroppedPowerUp() {
        hasDroppedPowerUp = true;
    }
    
    public int getScoreValue() {
        int[] scores = {10, 20, 30, 40, 50, 60, 100, 150, 50};
        int idx = type.ordinal();
        return idx < scores.length ? scores[idx] : 50;
    }
}
