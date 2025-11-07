package com.arkanoid.game.entity;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;

/**
 * Lớp PowerUp - Đại diện cho các vật phẩm power-up rơi xuống từ gạch
 * 
 * Kế thừa từ GameObject để có các thuộc tính cơ bản
 * 
 * Chức năng chính:
 * - Rơi xuống từ gạch bị phá hủy
 * - 9 loại power-up khác nhau (ENLARGE, REDUCE, LASER, SLOW, CATCH, TWIN, DISRUPT, MEGABALL, INCANDESCENCE)
 * - Vẽ với sprite image hoặc màu sắc
 * - Tự động biến mất khi ra ngoài màn hình
 * - Va chạm với paddle để kích hoạt hiệu ứng
 * 
 * Các loại Power-Up:
 * - ENLARGE: To paddle ra (dễ hơn)
 * - REDUCE: Thu nhỏ paddle (khó hơn) 
 * - LASER: Cho phép paddle bắn laser
 * - SLOW: Làm chậm bóng
 * - CATCH: Paddle có thể bắt bóng
 * - TWIN: Nhân đôi số bóng
 * - DISRUPT: Tạo nhiều bóng
 * - MEGABALL: Bóng to và mạnh hơn
 * - INCANDESCENCE: Bóng xuyên thủng gạch
 */
public class PowerUp extends GameObject {
    // ============================================================
    // HẰNG SỐ KÍCH THƯỚC
    // ============================================================
    
    /** Chiều rộng chuẩn của power-up (pixel) */
    public static final int POWERUP_WIDTH = 40;
    
    /** Chiều cao chuẩn của power-up (pixel) */
    public static final int POWERUP_HEIGHT = 20;
    
    // ============================================================
    // ENUM: POWER-UP TYPES
    // ============================================================
    
    /**
     * Enum định nghĩa các loại power-up trong game
     * 
     * Mỗi loại có:
     * - imagePath: Đường dẫn đến file sprite (GIF animation)
     * - description: Mô tả hiệu ứng
     */
    public enum PowerUpType {
        /** To paddle ra - Dễ hơn khi bắt bóng */
        ENLARGE("/Image/Enlarge.gif", "Enlarge paddle"),
        
        /** Thu nhỏ paddle - Khó hơn khi bắt bóng (power-up xấu) */
        REDUCE("/Image/Reduce.gif", "Reduce paddle"),
        
        /** Cho phép paddle bắn laser để phá gạch */
        LASER("/Image/Laser.gif", "Laser shot"),
        
        /** Làm chậm tốc độ bóng - Dễ điều khiển hơn */
        SLOW("/Image/Slow.gif", "Slow ball"),
        
        /** Paddle có thể bắt và giữ bóng */
        CATCH("/Image/Catch.gif", "Catch ball"),
        
        /** Nhân đôi số lượng bóng (2 bóng) */
        TWIN("/Image/Twin.gif", "Twin ball"),
        
        /** Tạo nhiều bóng (3+ bóng) */
        DISRUPT("/Image/Disrupt.gif", "Multiple balls"),
        
        /** Bóng to ra và có thể phá gạch cứng dễ hơn */
        MEGABALL("/Image/Megaball.gif", "Mega ball"),
        
        /** Bóng xuyên thủng gạch (không bị bật lại) */
        INCANDESCENCE("/Image/Incandescence.gif", "Pierce ball");
        
        /** Đường dẫn đến file sprite image */
        public final String imagePath;
        
        /** Mô tả hiệu ứng của power-up */
        public final String description;
        
        /**
         * Constructor cho PowerUpType enum
         * 
         * @param imagePath Đường dẫn file sprite
         * @param description Mô tả hiệu ứng
         */
        PowerUpType(String imagePath, String description) {
            this.imagePath = imagePath;
            this.description = description;
        }
    }
    
    // ============================================================
    // THUỘC TÍNH INSTANCE
    // ============================================================
    
    /** Loại power-up (ENLARGE, LASER, MEGABALL, v.v.) */
    private final PowerUpType type;
    
    /** Hình ảnh sprite cho power-up (GIF animation) */
    private BufferedImage image;
    
    /** 
     * Trạng thái active của power-up
     * true: Đang rơi và có thể va chạm với paddle
     * false: Đã bị thu thập hoặc ra ngoài màn hình
     */
    private boolean active = true;
    
    // ============================================================
    // CONSTRUCTOR
    // ============================================================
    
    /**
     * Tạo power-up mới tại vị trí cho trước
     * 
     * @param x Tọa độ X ban đầu (thường là vị trí gạch)
     * @param y Tọa độ Y ban đầu (thường là vị trí gạch)
     * @param type Loại power-up
     */
    public PowerUp(int x, int y, PowerUpType type) {
        // Gọi constructor GameObject với kích thước chuẩn
        super(x, y, POWERUP_WIDTH, POWERUP_HEIGHT);
        
        // Lưu loại power-up
        this.type = type;
        
        // Thiết lập tốc độ rơi (2 pixel/frame)
        this.dy = 2;
        
        // Load hình ảnh sprite
        loadImage();
    }
    
    // ============================================================
    // PHƯƠNG THỨC LOAD IMAGE
    // ============================================================
    
    /**
     * Load hình ảnh sprite cho power-up từ resources
     * 
     * Nếu load thất bại, image = null và sẽ dùng màu sắc thay thế
     */
    private void loadImage() {
        try {
            // Tìm file ảnh từ classpath resources
            java.net.URL imgURL = getClass().getResource(type.imagePath);
            
            if (imgURL != null) {
                // Load ảnh vào BufferedImage (hỗ trợ GIF)
                image = ImageIO.read(imgURL);
            }
        } catch (Exception e) {
            // Lỗi khi load -> dùng màu sắc thay thế
            // Không in lỗi để tránh spam console
            image = null;
        }
    }
    
    // ============================================================
    // PHƯƠNG THỨC CẬP NHẬT (Override từ GameObject)
    // ============================================================
    
    /**
     * Cập nhật trạng thái power-up mỗi frame
     * 
     * Nhiệm vụ: Di chuyển power-up xuống dưới nếu đang active
     * 
     * Power-up sẽ tự động rơi xuống với tốc độ dy = 2 pixel/frame
     */
    @Override
    public void update() {
        if (active) {
            // Di chuyển xuống dưới
            y += dy;
        }
    }
    
    // ============================================================
    // PHƯƠNG THỨC VẼ (Override từ GameObject)
    // ============================================================
    
    /**
     * Vẽ power-up lên màn hình
     * 
     * Logic vẽ:
     * 1. Kiểm tra active -> không vẽ nếu không active
     * 2. Nếu có sprite image:
     *    - Vẽ bằng sprite (GIF animation)
     * 3. Nếu không có sprite image:
     *    - Vẽ bằng màu sắc (fallback)
     *    - Vẽ hình chữ nhật bo góc
     *    - Hiển thị chữ cái đầu của power-up
     * 
     * @param g Graphics context để vẽ
     */
    @Override
    public void draw(Graphics g) {
        // Không vẽ nếu không active
        if (!active) return;
        
        if (image != null) {
            // ===== VẼ BẰNG SPRITE IMAGE =====
            // Vẽ GIF animation
            g.drawImage(image, x, y, width, height, null);
            
        } else {
            // ===== VẼ BẰNG MÀU SẮC (Fallback) =====
            
            // Lấy màu tương ứng với loại power-up
            Color color = getColorForType();
            g.setColor(color);
            
            // Vẽ hình chữ nhật bo góc (radius = 10)
            g.fillRoundRect(x, y, width, height, 10, 10);
            
            // Vẽ viền trắng
            g.setColor(Color.WHITE);
            g.drawRoundRect(x, y, width, height, 10, 10);
            
            // Vẽ chữ viết tắt (chữ cái đầu của tên power-up)
            g.setFont(new Font("Arial", Font.BOLD, 10));
            String text = getShortName();
            
            // Tính toán vị trí để text căn giữa
            FontMetrics fm = g.getFontMetrics();
            int textX = x + (width - fm.stringWidth(text)) / 2;
            int textY = y + (height + fm.getAscent()) / 2 - 2;
            
            // Vẽ text
            g.drawString(text, textX, textY);
        }
    }
    
    // ============================================================
    // HELPER METHODS - VẼ
    // ============================================================
    
    /**
     * Lấy màu sắc tương ứng với từng loại power-up
     * 
     * Mỗi power-up có màu riêng để dễ phân biệt:
     * - ENLARGE: Xanh lá (tốt)
     * - REDUCE: Đỏ (xấu)
     * - LASER: Vàng
     * - SLOW: Cyan
     * - CATCH: Cam
     * - TWIN: Hồng
     * - DISRUPT: Tím
     * - MEGABALL: Đỏ nhạt
     * - INCANDESCENCE: Vàng gold
     * 
     * @return Màu sắc của power-up
     */
    private Color getColorForType() {
        // Mảng màu tương ứng với thứ tự enum PowerUpType
        Color[] colors = {
            new Color(0, 255, 0),      // ENLARGE - Xanh lá
            new Color(255, 0, 0),      // REDUCE - Đỏ
            new Color(255, 255, 0),    // LASER - Vàng
            new Color(0, 255, 255),    // SLOW - Cyan
            new Color(255, 165, 0),    // CATCH - Cam
            new Color(255, 0, 255),    // TWIN - Hồng
            new Color(128, 0, 255),    // DISRUPT - Tím
            new Color(255, 100, 100),  // MEGABALL - Đỏ nhạt
            new Color(255, 215, 0)     // INCANDESCENCE - Vàng gold
        };
        
        // Lấy màu theo index, hoặc GRAY nếu index không hợp lệ
        return type.ordinal() < colors.length ? colors[type.ordinal()] : Color.GRAY;
    }
    
    /**
     * Lấy tên viết tắt của power-up (chữ cái đầu)
     * 
     * Ví dụ:
     * - ENLARGE -> "E"
     * - LASER -> "L"
     * - MEGABALL -> "M"
     * 
     * @return Chữ cái đầu của tên power-up
     */
    private String getShortName() {
        return type.name().substring(0, 1);
    }
    
    // ============================================================
    // GETTERS & SETTERS
    // ============================================================
    
    /**
     * Lấy loại power-up
     * 
     * @return PowerUpType (ENLARGE, LASER, MEGABALL, v.v.)
     */
    public PowerUpType getType() {
        return type;
    }
    
    /**
     * Kiểm tra power-up có đang active không
     * 
     * @return true nếu đang active (đang rơi), false nếu đã thu thập hoặc ra ngoài màn hình
     */
    public boolean isActive() {
        return active;
    }
    
    /**
     * Thiết lập trạng thái active
     * 
     * Thường dùng để đánh dấu power-up đã bị thu thập:
     * - setActive(false) khi paddle chạm vào
     * - setActive(false) khi ra ngoài màn hình
     * 
     * @param active true = active, false = inactive
     */
    public void setActive(boolean active) {
        this.active = active;
    }
    
    /**
     * Kiểm tra power-up đã ra ngoài màn hình chưa
     * 
     * Power-up ra ngoài màn hình khi y > screenHeight
     * Sẽ bị xóa khỏi danh sách để tiết kiệm bộ nhớ
     * 
     * @param screenHeight Chiều cao màn hình
     * @return true nếu đã ra ngoài, false nếu còn trong màn hình
     */
    public boolean isOffScreen(int screenHeight) {
        return y > screenHeight;
    }
}

