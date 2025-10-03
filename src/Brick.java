import java.awt.*;

public class Brick extends Item {
    public static final int BRICK_WIDTH = 48;
    public static final int BRICK_HEIGHT = 20;
    
    // Loại gạch
    public enum BrickType {
        RED(1, new Color(220, 20, 60)),
        ORANGE(1, new Color(255, 140, 0)),
        GREEN(1, new Color(34, 139, 34)),
        CYAN(2, new Color(0, 206, 209)),
        BLUE(2, new Color(30, 144, 255)),
        LIGHT_BLUE(2, new Color(135, 206, 250)),
        GOLD(3, new Color(255, 215, 0)),
        SILVER(4, new Color(192, 192, 192)),
        REGENERATING(999, new Color(138, 43, 226)); // Gạch tái sinh
        
        public final int hits;
        public final Color color;
        
        BrickType(int hits, Color color) {
            this.hits = hits;
            this.color = color;
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
        
        // Vẽ gạch với màu sáng hơn khi bị đập
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
