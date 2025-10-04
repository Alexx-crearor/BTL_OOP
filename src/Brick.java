import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import javax.imageio.ImageIO;

public class Brick extends Item {
    public static final int BRICK_WIDTH = 48;
    public static final int BRICK_HEIGHT = 20;
    
    // Cache hình ảnh gạch (static để chia sẻ giữa các instance)
    private static HashMap<BrickType, BufferedImage> imageCache = new HashMap<>();
    
    // Loại gạch
    public enum BrickType {
        RED(1, new Color(220, 20, 60), "../Image/RedWall.png"),
        ORANGE(1, new Color(255, 140, 0), "../Image/OrangeWall.png"),
        GREEN(1, new Color(34, 139, 34), "../Image/GreenWall.png"),
        CYAN(2, new Color(0, 206, 209), "../Image/CyanWall.png"),
        BLUE(2, new Color(30, 144, 255), "../Image/BlueWall.png"),
        LIGHT_BLUE(2, new Color(135, 206, 250), "../Image/LightBlueWall.png"),
        GOLD(3, new Color(255, 215, 0), "../Image/GoldWall.png"),
        SILVER(4, new Color(192, 192, 192), "../Image/SilverWall.png"),
        REGENERATING(999, new Color(138, 43, 226), "../Image/RegeneratingWall.png");
        
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
        this.x = x;
        this.y = y;
        this.width = BRICK_WIDTH;
        this.height = BRICK_HEIGHT;
        this.type = type;
        this.currentHits = type.hits;
        
        // Load hình ảnh nếu chưa có trong cache
        loadImageIfNeeded(type);
    }
    
    private static void loadImageIfNeeded(BrickType type) {
        if (!imageCache.containsKey(type)) {
            try {
                File imageFile = new File(type.imagePath);
                if (imageFile.exists()) {
                    BufferedImage img = ImageIO.read(imageFile);
                    imageCache.put(type, img);
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
    
    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }
}
