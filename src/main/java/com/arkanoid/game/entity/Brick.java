package com.arkanoid.game.entity;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.HashMap;

import javax.imageio.ImageIO;

/**
 * Lớp Brick - Đại diện cho các viên gạch trong game Arkanoid
 * 
 * Kế thừa từ GameObject để có các thuộc tính cơ bản
 * 
 * Chức năng chính:
 * - Quản lý nhiều loại gạch với độ bền khác nhau (1-4 hits)
 * - Hỗ trợ gạch tái sinh (regenerating) - ẩn 10 giây rồi xuất hiện lại
 * - Vẽ gạch với sprite image hoặc màu sắc
 * - Quản lý việc drop power-up khi bị phá hủy
 * - Tính điểm dựa trên loại gạch
 * 
 * Design Pattern: Flyweight (dùng imageCache static để share images)
 */
public class Brick extends GameObject {
    // ============================================================
    // HẰNG SỐ KÍCH THƯỚC
    // ============================================================
    
    /** Chiều rộng chuẩn của mỗi viên gạch (pixel) */
    public static final int BRICK_WIDTH = 48;
    
    /** Chiều cao chuẩn của mỗi viên gạch (pixel) */
    public static final int BRICK_HEIGHT = 20;
    
    // ============================================================
    // IMAGE CACHE (STATIC) - Design Pattern: Flyweight
    // ============================================================
    
    /**
     * Cache hình ảnh gạch - static để tất cả Brick instances dùng chung
     * 
     * Key: BrickType (loại gạch)
     * Value: BufferedImage (hình ảnh) hoặc null nếu không load được
     * 
     * Lợi ích:
     * - Tiết kiệm bộ nhớ (không load trùng lặp)
     * - Tăng hiệu suất (load 1 lần, dùng nhiều lần)
     */
    private static HashMap<BrickType, BufferedImage> imageCache = new HashMap<>();
    
    // ============================================================
    // ENUM: BRICK TYPES
    // ============================================================
    
    /**
     * Enum định nghĩa các loại gạch trong game
     * 
     * Mỗi loại gạch có:
     * - hits: Số lần đập để phá hủy
     * - color: Màu sắc (dùng khi không có sprite)
     * - imagePath: Đường dẫn đến file sprite
     */
    public enum BrickType {
        // === GẠCH YẾU (1 hit) ===
        /** Gạch đỏ - 1 hit để phá, 10 điểm */
        RED(1, new Color(220, 20, 60), "/Image/RedWall.png"),
        
        /** Gạch cam - 1 hit để phá, 20 điểm */
        ORANGE(1, new Color(255, 140, 0), "/Image/OrangeWall.png"),
        
        /** Gạch xanh lá - 1 hit để phá, 30 điểm */
        GREEN(1, new Color(34, 139, 34), "/Image/GreenWall.png"),
        
        // === GẠCH TRUNG BÌNH (2 hits) ===
        /** Gạch cyan - 2 hits để phá, 40 điểm */
        CYAN(2, new Color(0, 206, 209), "/Image/CyanWall.png"),
        
        /** Gạch xanh dương đậm - 2 hits để phá, 50 điểm */
        BLUE(2, new Color(30, 144, 255), "/Image/BlueWall.png"),
        
        /** Gạch xanh dương nhạt - 2 hits để phá, 60 điểm */
        LIGHT_BLUE(2, new Color(135, 206, 250), "/Image/LightBlueWall.png"),
        
        // === GẠCH CỨNG ===
        /** Gạch vàng - 3 hits để phá, 100 điểm */
        GOLD(3, new Color(255, 215, 0), "/Image/GoldWall.png"),
        
        /** Gạch bạc - 4 hits để phá, 150 điểm */
        SILVER(4, new Color(192, 192, 192), "/Image/SilverWall.png"),
        
        // === GẠCH ĐặC BIỆT ===
        /**
         * Gạch tái sinh - Không thể phá hủy hoàn toàn
         * Khi bị đập sẽ ẩn 10 giây rồi xuất hiện lại
         * Màu hồng neon để dễ nhận biết
         * 999 hits = gần như bất tử
         */
        REGENERATING(999, new Color(255, 0, 255), "/Image/RegeneratingWall.png");
        
        /** Số lần đập cần thiết để phá hủy gạch */
        public final int hits;
        
        /** Màu sắc gạch (dùng khi không có sprite image) */
        public final Color color;
        
        /** Đường dẫn đến file sprite image */
        public final String imagePath;
        
        /**
         * Constructor cho BrickType enum
         * 
         * @param hits Số lần đập để phá hủy
         * @param color Màu sắc của gạch
         * @param imagePath Đường dẫn file sprite
         */
        BrickType(int hits, Color color, String imagePath) {
            this.hits = hits;
            this.color = color;
            this.imagePath = imagePath;
        }
    }
    
    // ============================================================
    // THUỘC TÍNH INSTANCE
    // ============================================================
    
    /** Loại gạch (RED, ORANGE, GREEN, v.v.) */
    private BrickType type;
    
    /** Số lần đập còn lại trước khi gạch bị phá hủy */
    private int currentHits;
    
    /** Trạng thái gạch đã bị phá hủy chưa */
    public boolean isDestroyed = false;
    
    /** Đánh dấu gạch đã drop power-up chưa (tránh drop nhiều lần) */
    private boolean hasDroppedPowerUp = false;
    
    // ============================================================
    // THUỘC TÍNH REGENERATING BRICK
    // ============================================================
    
    /** 
     * Trạng thái gạch tái sinh đang ẩn tạm thời
     * true: Gạch đang ẩn (không vẽ, không va chạm)
     * false: Gạch đang hiển thị bình thường
     */
    private boolean isTemporarilyHidden = false;
    
    /** 
     * Thời điểm bắt đầu ẩn (milliseconds)
     * Dùng System.currentTimeMillis() để lấy timestamp
     */
    private long hiddenStartTime = 0;
    
    /** 
     * Thời gian tái sinh (milliseconds)
     * 10000ms = 10 giây
     * Sau 10 giây ẩn, gạch sẽ xuất hiện lại
     */
    private static final long REGENERATION_TIME = 10000;
    
    // ============================================================
    // CONSTRUCTOR
    // ============================================================
    
    /**
     * Tạo gạch mới tại vị trí và loại cho trước
     * 
     * @param x Tọa độ X
     * @param y Tọa độ Y
     * @param type Loại gạch (RED, ORANGE, GOLD, v.v.)
     */
    public Brick(int x, int y, BrickType type) {
        // Gọi constructor GameObject với kích thước chuẩn
        super(x, y, BRICK_WIDTH, BRICK_HEIGHT);
        
        // Thiết lập loại gạch
        this.type = type;
        
        // Thiết lập số hits ban đầu từ BrickType
        this.currentHits = type.hits;
        
        // Load hình ảnh nếu chưa có trong cache
        // (chỉ load 1 lần cho mỗi loại gạch)
        loadImageIfNeeded(type);
    }
    
    // ============================================================
    // PHƯƠNG THỨC CẬP NHẬT (Override từ GameObject)
    // ============================================================
    
    /**
     * Cập nhật trạng thái gạch mỗi frame
     * 
     * Nhiệm vụ chính: Kiểm tra gạch tái sinh có cần xuất hiện lại không
     * 
     * Logic:
     * 1. Chỉ xử lý nếu là gạch REGENERATING và đang ẩn
     * 2. So sánh thời gian hiện tại với thời điểm bắt đầu ẩn
     * 3. Nếu đã qua 10 giây (REGENERATION_TIME) -> xuất hiện lại
     */
    @Override
    public void update() {
        // Chỉ xử lý gạch tái sinh đang ẩn
        if (type == BrickType.REGENERATING && isTemporarilyHidden) {
            // Lấy thời gian hiện tại
            long currentTime = System.currentTimeMillis();
            
            // Kiểm tra đã đủ thời gian tái sinh chưa
            if (currentTime - hiddenStartTime >= REGENERATION_TIME) {
                // Đủ 10 giây -> xuất hiện lại
                isTemporarilyHidden = false;
            }
        }
    }
    
    // ============================================================
    // PHƯƠNG THỨC LOAD IMAGE (STATIC)
    // ============================================================
    
    /**
     * Load hình ảnh sprite cho loại gạch nếu chưa có trong cache
     * 
     * Static method vì hoạt động trên static imageCache
     * 
     * Flow:
     * 1. Kiểm tra đã có trong cache chưa
     * 2. Nếu chưa có, thử load từ resources
     * 3. Nếu load thành công -> thêm vào cache
     * 4. Nếu load thất bại -> thêm null vào cache (để không thử lại)
     * 
     * @param type Loại gạch cần load image
     */
    private static void loadImageIfNeeded(BrickType type) {
        // Kiểm tra đã có trong cache chưa
        if (!imageCache.containsKey(type)) {
            try {
                // Tìm file ảnh từ classpath resources
                java.net.URL imgURL = Brick.class.getResource(type.imagePath);
                
                if (imgURL != null) {
                    // Load ảnh vào BufferedImage
                    BufferedImage img = ImageIO.read(imgURL);
                    
                    // Thêm vào cache để dùng sau
                    imageCache.put(type, img);
                    
                    System.out.println("Loaded brick image: " + type.name() + " from " + type.imagePath);
                } else {
                    // Không tìm thấy resource -> dùng màu thay thế
                    System.out.println("Image not found for " + type.name() + ": " + type.imagePath);
                    
                    // Thêm null vào cache để không thử load lại
                    imageCache.put(type, null);
                }
            } catch (Exception e) {
                // Lỗi khi load (file bị hỏng, format không đúng, v.v.)
                System.out.println("Error loading image for " + type.name() + ": " + e.getMessage());
                
                // Thêm null vào cache để không thử load lại
                imageCache.put(type, null);
            }
        }
    }
    
    // ============================================================
    // PHƯƠNG THỨC VA CHẠM
    // ============================================================
    
    /**
     * Xử lý khi gạch bị bóng/laser đập trúng
     * 
     * Logic xử lý khác nhau tùy loại gạch:
     * 
     * REGENERATING BRICK:
     * - Không bị phá hủy
     * - Ẩn đi 10 giây rồi xuất hiện lại
     * - Trả về false (không destroyed)
     * 
     * GẠCH THƯỜNG:
     * - Giảm currentHits xuống 1
     * - Nếu currentHits <= 0 -> đánh dấu destroyed
     * - Trả về true nếu destroyed, false nếu còn hits
     * 
     * @return true nếu gạch bị phá hủy hoàn toàn, false nếu còn hits hoặc là regenerating
     */
    public boolean hit() {
        // === XỬ LÝ GẠCH TÁI SINH ===
        if (type == BrickType.REGENERATING) {
            // Chỉ ẩn nếu chưa ẩn (tránh reset timer nhiều lần)
            if (!isTemporarilyHidden) {
                // Đánh dấu đang ẩn
                isTemporarilyHidden = true;
                
                // Lưu thời điểm bắt đầu ẩn
                hiddenStartTime = System.currentTimeMillis();
            }
            
            // Gạch tái sinh không bao giờ bị phá hủy hoàn toàn
            return false;
        }
        
        // === XỬ LÝ GẠCH THƯỜNG ===
        // Giảm số hits còn lại
        currentHits--;
        
        // Kiểm tra đã hết hits chưa
        if (currentHits <= 0) {
            // Hết hits -> đánh dấu destroyed
            isDestroyed = true;
            return true; // Gạch bị phá hủy
        }
        
        // Còn hits -> chưa destroyed
        return false;
    }
    
    // ============================================================
    // PHƯƠNG THỨC VẼ (Override từ GameObject)
    // ============================================================
    
    /**
     * Vẽ gạch lên màn hình
     * 
     * Logic vẽ:
     * 1. Kiểm tra gạch đã destroyed -> không vẽ
     * 2. Kiểm tra gạch regenerating đang ẩn -> không vẽ
     * 3. Lấy image từ cache
     * 4. Nếu có image:
     *    - Vẽ bằng sprite
     *    - Thêm hiệu ứng sáng khi bị đập
     *    - Hiển thị số hits còn lại (nếu > 1 hit)
     * 5. Nếu không có image:
     *    - Vẽ bằng màu sắc (fallback)
     *    - Vẽ viền và highlight
     *    - Hiển thị số hits còn lại
     * 
     * @param g Graphics context để vẽ
     */
    @Override
    public void draw(Graphics g) {
        // === BƯỚC 1: Kiểm tra destroyed ===
        if (isDestroyed) return; // Không vẽ gạch đã bị phá hủy
        
        // === BƯỚC 2: Kiểm tra regenerating đang ẩn ===
        if (type == BrickType.REGENERATING && isTemporarilyHidden) {
            return; // Không vẽ gạch đang ẩn
        }
        
        // === BƯỚC 3: Lấy image từ cache ===
        BufferedImage image = imageCache.get(type);
        
        if (image != null) {
            // ===== VẼ BẰNG SPRITE IMAGE =====
            
            // Vẽ sprite image chính
            g.drawImage(image, x, y, width, height, null);
            
            // Thêm hiệu ứng sáng khi gạch bị đập (currentHits < max hits)
            if (currentHits < type.hits) {
                // Overlay trắng trong suốt (alpha=80)
                g.setColor(new Color(255, 255, 255, 80));
                g.fillRect(x, y, width, height);
            }
            
            // Hiển thị số lần đập còn lại (chỉ với gạch > 1 hit, không phải regenerating)
            if (type.hits > 1 && type != BrickType.REGENERATING) {
                g.setColor(Color.WHITE);
                g.setFont(new Font("Arial", Font.BOLD, 14));
                drawCenteredText(g, String.valueOf(currentHits));
            }
            
        } else {
            // ===== VẼ BẰNG MÀU SẮC (Fallback) =====
            
            // Chọn màu: sáng hơn nếu bị đập, màu gốc nếu chưa đập
            Color color = currentHits < type.hits ? brighten(type.color, 50) : type.color;
            g.setColor(color);
            
            // Vẽ hình chữ nhật màu
            g.fillRect(x, y, width, height);
            
            // Vẽ viền trắng (double border cho đẹp)
            g.setColor(Color.WHITE);
            g.drawRect(x, y, width, height);                    // Viền ngoài
            g.drawRect(x + 1, y + 1, width - 2, height - 2);   // Viền trong
            
            // Vẽ highlight phía trên (tạo hiệu ứng 3D)
            g.setColor(new Color(255, 255, 255, 100)); // Trắng trong suốt
            g.fillRect(x + 2, y + 2, width - 4, 3);    // Thanh sáng phía trên
            
            // Hiển thị số lần đập còn lại
            if (type.hits > 1) {
                g.setColor(Color.WHITE);
                g.setFont(new Font("Arial", Font.BOLD, 12));
                drawCenteredText(g, String.valueOf(currentHits));
            }
        }
    }
    
    // ============================================================
    // HELPER METHODS - VẼ
    // ============================================================
    
    /**
     * Làm sáng màu lên một lượng cho trước
     * 
     * Cộng thêm amount vào từng thành phần RGB
     * Math.min(255, ...) để đảm bảo không vượt quá 255
     * 
     * @param c Màu gốc
     * @param amount Lượng tăng (0-255)
     * @return Màu mới sáng hơn
     */
    private Color brighten(Color c, int amount) {
        return new Color(
            Math.min(255, c.getRed() + amount),    // Red + amount, tối đa 255
            Math.min(255, c.getGreen() + amount),  // Green + amount, tối đa 255
            Math.min(255, c.getBlue() + amount)    // Blue + amount, tối đa 255
        );
    }
    
    /**
     * Vẽ text căn giữa trong gạch
     * 
     * Tính toán vị trí X, Y để text nằm chính giữa gạch
     * 
     * @param g Graphics context
     * @param text Text cần vẽ (thường là số hits)
     */
    private void drawCenteredText(Graphics g, String text) {
        // Lấy metrics của font hiện tại
        FontMetrics fm = g.getFontMetrics();
        
        // Tính tọa độ X để text căn giữa theo chiều ngang
        int textX = x + (width - fm.stringWidth(text)) / 2;
        
        // Tính tọa độ Y để text căn giữa theo chiều dọc
        // getAscent() = khoảng cách từ baseline lên trên
        int textY = y + (height + fm.getAscent()) / 2 - 2;
        
        // Vẽ text
        g.drawString(text, textX, textY);
    }
    
    // ============================================================
    // GETTERS
    // ============================================================
    
    /**
     * Lấy loại gạch
     * 
     * @return BrickType (RED, ORANGE, GOLD, v.v.)
     */
    public BrickType getType() {
        return type;
    }
    
    /**
     * Kiểm tra gạch có đang ẩn tạm thời không
     * 
     * Chỉ áp dụng cho gạch REGENERATING
     * true = đang ẩn (không vẽ, không va chạm)
     * false = đang hiển thị bình thường
     * 
     * @return true nếu đang ẩn, false nếu không
     */
    public boolean isTemporarilyHidden() {
        return isTemporarilyHidden;
    }
    
    /**
     * Kiểm tra gạch có thể drop power-up không
     * 
     * Mỗi gạch chỉ được drop power-up 1 lần duy nhất
     * Tránh trường hợp gạch regenerating drop nhiều lần
     * 
     * @return true nếu chưa drop, false nếu đã drop rồi
     */
    public boolean canDropPowerUp() {
        return !hasDroppedPowerUp;
    }
    
    /**
     * Đánh dấu gạch đã drop power-up
     * 
     * Được gọi khi GamePanel tạo power-up từ gạch này
     * Đảm bảo mỗi gạch chỉ drop 1 lần
     */
    public void setDroppedPowerUp() {
        hasDroppedPowerUp = true;
    }
    
    /**
     * Lấy điểm số khi phá hủy gạch này
     * 
     * Điểm số tăng dần theo độ khó của gạch:
     * - RED: 10 điểm (1 hit)
     * - ORANGE: 20 điểm (1 hit)
     * - GREEN: 30 điểm (1 hit)
     * - CYAN: 40 điểm (2 hits)
     * - BLUE: 50 điểm (2 hits)
     * - LIGHT_BLUE: 60 điểm (2 hits)
     * - GOLD: 100 điểm (3 hits)
     * - SILVER: 150 điểm (4 hits)
     * - REGENERATING: 50 điểm (không phá được, nhưng cho điểm khi ẩn)
     * 
     * @return Số điểm (10-150)
     */
    public int getScoreValue() {
        // Mảng điểm tương ứng với thứ tự enum BrickType
        int[] scores = {10, 20, 30, 40, 50, 60, 100, 150, 50};
        
        // Lấy index của type trong enum (0-8)
        int idx = type.ordinal();
        
        // Trả về điểm tương ứng, hoặc 50 nếu index không hợp lệ
        return idx < scores.length ? scores[idx] : 50;
    }
}
