package com.arkanoid.game.entity;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;

/**
 * Lớp Paddle - Đại diện cho thanh chắn (paddle/vaus) do người chơi điều khiển
 * 
 * Kế thừa từ GameObject để có các thuộc tính cơ bản
 * 
 * Chức năng chính:
 * - Di chuyển trái/phải theo lệnh người chơi
 * - Hỗ trợ các power-up: Enlarge (to ra), Reduce (nhỏ lại), Laser (bắn laser)
 * - Va chạm với bóng để bật bóng lại
 * - Vẽ paddle với sprite image hoặc màu sắc
 */
public class Paddle extends GameObject {
    // ============================================================
    // THUỘC TÍNH KÍCH THƯỚC
    // ============================================================
    
    /** Chiều rộng bình thường của paddle (pixel) */
    private final int normalWidth = 120;
    
    /** Chiều rộng khi được power-up Enlarge (pixel) */
    private final int enlargedWidth = 180;
    
    /** Chiều rộng khi bị power-up Reduce (pixel) */
    private final int reducedWidth = 80;
    
    // ============================================================
    // THUỘC TÍNH POWER-UP
    // ============================================================
    
    /** Trạng thái Enlarge - paddle đang được to ra */
    private boolean enlarged = false;
    
    /** Trạng thái Reduce - paddle đang bị thu nhỏ */
    private boolean reduced = false;
    
    /** 
     * Bộ đếm thời gian cho Enlarge (frames)
     * Khi về 0, paddle trở lại kích thước bình thường
     */
    private int enlargeTimer = 0;
    
    /** 
     * Bộ đếm thời gian cho Reduce (frames)
     * Khi về 0, paddle trở lại kích thước bình thường
     */
    private int reduceTimer = 0;
    
    /** Trạng thái có khả năng bắn laser */
    private boolean hasLaser = false;
    
    /** 
     * Bộ đếm thời gian cho Laser (frames)
     * Khi về 0, khả năng bắn laser bị tắt
     */
    private int laserTimer = 0;
    
    // ============================================================
    // ĐỒ HỌA
    // ============================================================
    
    /** Hình ảnh paddle kích thước bình thường */
    private BufferedImage normalImage;
    
    /** Hình ảnh paddle kích thước to (enlarged) */
    private BufferedImage enlargedImage;
    
    // ============================================================
    // CONSTRUCTOR
    // ============================================================
    
    /**
     * Tạo paddle mới tại vị trí cho trước
     * 
     * @param x Tọa độ X ban đầu
     * @param y Tọa độ Y ban đầu
     * @param screenWidth Chiều rộng màn hình (để kiểm tra biên sau này)
     * @param screenHeight Chiều cao màn hình (không dùng trong constructor nhưng giữ để tương thích)
     */
    public Paddle(int x, int y, int screenWidth, int screenHeight) {
        // Gọi constructor GameObject với kích thước 120x20
        super(x, y, 120, 20);
        
        // Thiết lập tốc độ di chuyển (pixel/frame khi nhấn phím)
        this.dx = 5;
        
        // Load hình ảnh sprite
        loadImages();
    }
    
    /**
     * Load hình ảnh sprite cho paddle từ resources
     * 
     * Nhiệm vụ:
     * - Load ảnh paddle bình thường (VausII.png)
     * - Load ảnh paddle được to ra (VausIIwEnlarge.png)
     * - Nếu load lỗi, đặt null để dùng vẽ màu thay thế
     */
    private void loadImages() {
        try {
            // Tìm file ảnh từ classpath resources
            java.net.URL normalURL = getClass().getResource("/Image/VausII.png");
            java.net.URL enlargedURL = getClass().getResource("/Image/VausIIwEnlarge.png");
            
            // Load ảnh paddle bình thường
            if (normalURL != null) {
                normalImage = ImageIO.read(normalURL);
                System.out.println("Paddle normal image loaded: VausII.png");
            } else {
                System.out.println("Paddle normal image not found: /Image/VausII.png");
            }
            
            // Load ảnh paddle enlarged
            if (enlargedURL != null) {
                enlargedImage = ImageIO.read(enlargedURL);
                System.out.println("Paddle enlarged image loaded: VausIIwEnlarge.png");
            } else {
                System.out.println("Paddle enlarged image not found: /Image/VausIIwEnlarge.png");
            }
        } catch (Exception e) {
            // Lỗi khi đọc file -> dùng vẽ màu thay thế
            System.out.println("Error loading paddle images: " + e.getMessage());
            normalImage = null;
            enlargedImage = null;
        }
    }
    
    // ============================================================
    // PHƯƠNG THỨC CẬP NHẬT (Override từ GameObject)
    // ============================================================
    
    /**
     * Cập nhật trạng thái paddle mỗi frame
     * 
     * Thực hiện:
     * - Giảm bộ đếm thời gian cho các power-up
     * - Tắt power-up khi hết thời gian
     * - Trả paddle về trạng thái bình thường
     */
    @Override
    public void update() {
        // Cập nhật timer cho Enlarge - trả về kích thước bình thường khi hết
        updateTimer(() -> { enlarged = false; width = normalWidth; }, enlargeTimer--);
        
        // Cập nhật timer cho Reduce - trả về kích thước bình thường khi hết
        updateTimer(() -> { reduced = false; width = normalWidth; }, reduceTimer--);
        
        // Cập nhật timer cho Laser - tắt laser khi hết
        updateTimer(() -> hasLaser = false, laserTimer--);
    }
    
    /**
     * Override của update() để tương thích với code cũ
     * @param screenWidth Chiều rộng màn hình (không sử dụng trong phiên bản này)
     */
    public void update(int screenWidth) {
        update();
    }
    
    /**
     * Helper method để cập nhật timer
     * Thực thi action khi timer về 1 (sắp hết)
     * 
     * @param action Hành động cần thực hiện khi timer hết
     * @param timer Giá trị timer hiện tại
     */
    private void updateTimer(Runnable action, int timer) {
        if (timer == 1) action.run();
    }
    
    // ============================================================
    // PHƯƠNG THỨC DI CHUYỂN
    // ============================================================
    
    /**
     * Di chuyển paddle sang trái
     * 
     * Giảm tọa độ X với tốc độ dx, đảm bảo không ra ngoài biên trái (x >= 0)
     * 
     * @param screenWidth Chiều rộng màn hình (tham số giữ lại để tương thích, không dùng trong logic)
     */
    public void moveLeft(int screenWidth) {
        // Math.max đảm bảo x không âm (không vượt qua biên trái)
        x = Math.max(0, x - dx);
    }
    
    /**
     * Di chuyển paddle sang phải
     * 
     * Tăng tọa độ X với tốc độ dx, đảm bảo không ra ngoài biên phải
     * 
     * @param screenWidth Chiều rộng màn hình để tính biên phải
     */
    public void moveRight(int screenWidth) {
        // Math.min đảm bảo paddle không vượt qua biên phải màn hình
        // screenWidth - width: vị trí X tối đa để paddle vừa khít màn hình
        x = Math.min(screenWidth - width, x + dx);
    }
    
    // ============================================================
    // PHƯƠNG THỨC VẼ (Override từ GameObject)
    // ============================================================
    
    /**
     * Vẽ paddle lên màn hình
     * 
     * Thực hiện:
     * 1. Chọn hình ảnh phù hợp (enlarged hoặc normal)
     * 2. Vẽ paddle bằng sprite image nếu có, nếu không dùng màu sắc
     * 3. Vẽ laser (nếu có power-up laser)
     * 
     * @param g Graphics context để vẽ
     */
    @Override
    public void draw(Graphics g) {
        // Cast sang Graphics2D để có nhiều tính năng vẽ hơn
        Graphics2D g2d = (Graphics2D) g;
        
        // Bật antialiasing để vẽ mượt mà
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                            RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Chọn hình ảnh phù hợp dựa vào trạng thái
        BufferedImage currentImage = null;
        if (enlarged && enlargedImage != null) {
            // Đang enlarged và có ảnh enlarged
            currentImage = enlargedImage;
        } else if (normalImage != null) {
            // Trường hợp còn lại: dùng ảnh normal
            currentImage = normalImage;
        }
        
        if (currentImage != null) {
            // === VẼ BẰNG SPRITE IMAGE ===
            g2d.drawImage(currentImage, x, y, width, height, null);
            
        } else {
            // === VẼ BẰNG MÀU SẮC (Fallback) ===
            
            // Chọn màu dựa vào trạng thái
            Color paddleColor;
            if (enlarged) {
                paddleColor = new Color(0, 200, 0); // Xanh lá - to ra
            } else if (reduced) {
                paddleColor = new Color(200, 0, 0); // Đỏ - nhỏ lại
            } else {
                paddleColor = new Color(70, 130, 180); // Xanh dương - bình thường
            }
            
            // Vẽ gradient từ sáng xuống tối
            GradientPaint gp = new GradientPaint(
                x, y, paddleColor.brighter(),        // Phía trên: màu sáng hơn
                x, y + height, paddleColor.darker()); // Phía dưới: màu tối hơn
            g2d.setPaint(gp);
            
            // Vẽ hình chữ nhật bo góc
            g2d.fillRoundRect(x, y, width, height, 10, 10);
            
            // Vẽ viền trắng
            g2d.setColor(Color.WHITE);
            g2d.drawRoundRect(x, y, width, height, 10, 10);
        }
        
        // Vẽ laser indicators (nếu có power-up laser)
        if (hasLaser) {
            g2d.setColor(new Color(255, 255, 0)); // Màu vàng
            int laserWidth = 4;
            
            // Vẽ 2 thanh laser ở 2 bên paddle
            g2d.fillRect(x + 10, y - 5, laserWidth, 5);                    // Laser bên trái
            g2d.fillRect(x + width - 14, y - 5, laserWidth, 5);           // Laser bên phải
        }
    }
    
    // ============================================================
    // PHƯƠNG THỨC POWER-UP
    // ============================================================
    
    /**
     * Kích hoạt power-up Enlarge - to paddle ra
     * 
     * @param duration Thời lượng hiệu ứng (frames)
     */
    public void activateEnlarge(int duration) {
        enlarged = true;        // Đánh dấu trạng thái enlarged
        reduced = false;        // Tắt reduce nếu đang active
        reduceTimer = 0;        // Reset timer của reduce
        enlargeTimer = duration; // Thiết lập timer
        width = enlargedWidth;  // Thay đổi kích thước
    }
    
    /**
     * Kích hoạt power-up Reduce - thu nhỏ paddle lại
     * 
     * @param duration Thời lượng hiệu ứng (frames)
     */
    public void activateReduce(int duration) {
        reduced = true;         // Đánh dấu trạng thái reduced
        enlarged = false;       // Tắt enlarge nếu đang active
        enlargeTimer = 0;       // Reset timer của enlarge
        reduceTimer = duration; // Thiết lập timer
        width = reducedWidth;   // Thay đổi kích thước
    }
    
    /**
     * Kích hoạt power-up Laser - cho phép paddle bắn laser
     * 
     * @param duration Thời lượng hiệu ứng (frames)
     */
    public void activateLaser(int duration) {
        hasLaser = true;        // Đánh dấu có laser
        laserTimer = duration;  // Thiết lập timer
    }
    
    // ============================================================
    // GETTERS
    // ============================================================
    
    /**
     * Kiểm tra paddle có khả năng bắn laser không
     * 
     * @return true nếu có laser power-up, false nếu không
     */
    public boolean hasLaser() {
        return hasLaser;
    }
    
    /**
     * Lấy tọa độ X của trung tâm paddle
     * Dùng để đặt vị trí bóng hoặc laser
     * 
     * @return Tọa độ X của điểm giữa paddle
     */
    public int getCenterX() {
        return x + width / 2;
    }
}

