package com.arkanoid.game.entity;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;

/**
 * Lớp Ball - Đại diện cho quả bóng trong game Arkanoid
 * 
 * Kế thừa từ GameObject để có các thuộc tính cơ bản (vị trí, kích thước, vận tốc)
 * 
 * Chức năng chính:
 * - Di chuyển và va chạm với paddle, gạch, tường
 * - Hỗ trợ các power-up: Mega Ball (bóng khổng lồ), Incandescent (bóng xuyên thủng)
 * - Quản lý trạng thái chuyển động (moving/stopped)
 * - Vẽ bóng với các hiệu ứng đồ họa khác nhau
 */
public class Ball extends GameObject {
    // ============================================================
    // THUỘC TÍNH CƠ BẢN
    // ============================================================
    
    /** 
     * Tốc độ cơ bản của bóng (pixel/frame)
     * Giá trị 2 được chọn để bóng di chuyển vừa phải, không quá nhanh
     */
    private int baseSpeed = 2;
    
    /** 
     * Trạng thái di chuyển của bóng
     * true: bóng đang di chuyển
     * false: bóng đang dừng (chờ người chơi nhấn Space)
     */
    private boolean isMoving = false;
    
    // ============================================================
    // THUỘC TÍNH POWER-UP
    // ============================================================
    
    /** 
     * Trạng thái Mega Ball - bóng khổng lồ
     * Khi active, bóng sẽ lớn hơn và có thể phá gạch mạnh hơn
     */
    private boolean megaBall = false;
    
    /** 
     * Bộ đếm thời gian cho Mega Ball (đơn vị: frames)
     * Khi về 0, hiệu ứng Mega Ball sẽ hết
     */
    private int megaBallTimer = 0;
    
    /** 
     * Trạng thái Incandescent - bóng xuyên thủng
     * Khi active, bóng có thể xuyên qua gạch mà không bị bật lại
     */
    private boolean incandescent = false;
    
    /** 
     * Bộ đếm thời gian cho Incandescent (đơn vị: frames)
     * Khi về 0, hiệu ứng Incandescent sẽ hết
     */
    private int incandescentTimer = 0;
    
    // ============================================================
    // ĐỒ HỌA
    // ============================================================
    
    /** 
     * Hình ảnh sprite cho bóng
     * Static để tất cả các Ball instance dùng chung 1 ảnh (tiết kiệm bộ nhớ)
     * Null nếu không load được - sẽ dùng vẽ màu thay thế
     */
    private static BufferedImage ballSprite = null;
    
    // ============================================================
    // STATIC INITIALIZER BLOCK
    // ============================================================
    
    /**
     * Static initializer - chạy 1 lần khi class được load
     * Nhiệm vụ: Load hình ảnh sprite cho bóng từ resources
     * 
     * Lý do dùng static block:
     * - Chỉ cần load 1 lần cho tất cả Ball instances
     * - Tiết kiệm bộ nhớ và thời gian load
     */
    static {
        try {
            // Tìm file ảnh từ classpath resources
            java.net.URL imgURL = Ball.class.getResource("/Image/sprite-ball.png");
            
            if (imgURL != null) {
                // Load ảnh vào BufferedImage
                ballSprite = ImageIO.read(imgURL);
                System.out.println("Ball sprite loaded successfully!");
            } else {
                // Không tìm thấy file
                System.out.println("Ball sprite not found: /Image/sprite-ball.png");
            }
        } catch (Exception e) {
            // Lỗi khi đọc file (file bị hỏng, format không đúng, v.v.)
            System.out.println("Error loading ball sprite: " + e.getMessage());
        }
    }
    
    // ============================================================
    // CONSTRUCTOR
    // ============================================================
    
    /**
     * Tạo bóng mới tại vị trí và kích thước cho trước
     * 
     * @param x Tọa độ X ban đầu
     * @param y Tọa độ Y ban đầu
     * @param size Kích thước bóng (cả width và height vì bóng là hình vuông)
     */
    public Ball(int x, int y, int size) {
        // Gọi constructor của GameObject với width = height = size (bóng hình vuông)
        super(x, y, size, size);
        
        // Bóng mới không di chuyển cho đến khi người chơi nhấn Space
        this.dx = 0;
        this.dy = 0;
    }
    
    // ============================================================
    // PHƯƠNG THỨC CẬP NHẬT (Override từ GameObject)
    // ============================================================
    
    /**
     * Cập nhật trạng thái bóng mỗi frame
     * 
     * Thực hiện:
     * 1. Di chuyển bóng (nếu đang trong trạng thái moving)
     * 2. Cập nhật các bộ đếm thời gian power-up
     * 3. Tắt power-up khi hết thời gian
     */
    @Override
    public void update() {
        // Bước 1: Di chuyển bóng nếu đang active
        if (isMoving) {
            x += dx; // Cập nhật vị trí X
            y += dy; // Cập nhật vị trí Y
        }
        
        // Bước 2: Cập nhật timer cho Mega Ball
        if (megaBallTimer > 0 && --megaBallTimer == 0) {
            // Timer đã hết -> tắt Mega Ball
            megaBall = false;
            // Trả bóng về kích thước bình thường (20x20 pixels)
            width = height = 20;
        }
        
        // Bước 3: Cập nhật timer cho Incandescent
        if (incandescentTimer > 0 && --incandescentTimer == 0) {
            // Timer đã hết -> tắt Incandescent
            incandescent = false;
        }
    }
    
    // ============================================================
    // PHƯƠNG THỨC ĐIỀU KHIỂN TỐC ĐỘ
    // ============================================================
    
    /**
     * Thay đổi tốc độ bóng trong khi giữ nguyên hướng di chuyển
     * 
     * Ví dụ: Nếu bóng đang di chuyển sang phải với dx=3,
     * sau khi setSpeed(5), bóng sẽ di chuyển sang phải với dx=5
     * 
     * @param speed Tốc độ mới (pixel/frame)
     */
    public void setSpeed(int speed) {
        baseSpeed = speed;
        
        // Giữ hướng hiện tại (dương/âm) nhưng thay đổi độ lớn
        // dx > 0: đang đi phải -> giữ dương với tốc độ mới
        // dx < 0: đang đi trái -> giữ âm với tốc độ mới
        // dx = 0: đứng yên -> giữ nguyên 0
        dx = dx > 0 ? speed : (dx < 0 ? -speed : dx);
        dy = dy > 0 ? speed : (dy < 0 ? -speed : dy);
    }
    
    // ============================================================
    // PHƯƠNG THỨC ĐIỀU KHIỂN CHUYỂN ĐỘNG
    // ============================================================
    
    /**
     * Bắt đầu di chuyển bóng
     * 
     * Được gọi khi người chơi nhấn Space lần đầu tiên
     * Bóng sẽ bắt đầu di chuyển lên trên với góc ngẫu nhiên nhẹ
     */
    public void startMoving() {
        if (!isMoving) {
            isMoving = true;
            
            // Tạo góc di chuyển ngẫu nhiên nhẹ theo trục X
            // Math.random() * 3 cho giá trị 0.0 đến 2.999
            // Trừ 1 để có giá trị -1, 0, hoặc 1
            dx = (int)(Math.random() * 3) - 1;
            
            // Di chuyển lên trên với tốc độ cơ bản
            dy = -baseSpeed; // Âm vì trục Y tăng xuống dưới
        }
    }
    
    /**
     * Kiểm tra bóng có đang di chuyển không
     * 
     * @return true nếu bóng đang di chuyển, false nếu đang dừng
     */
    public boolean isMoving() {
        return isMoving;
    }
    
    /**
     * Dừng bóng lại
     * 
     * Đặt vận tốc về 0 và đánh dấu trạng thái không di chuyển
     */
    public void stopMoving() {
        isMoving = false;
        dx = 0;
        dy = 0;
    }
    
    // ============================================================
    // PHƯƠNG THỨC VẼ (Override từ GameObject)
    // ============================================================
    
    /**
     * Vẽ bóng lên màn hình với các hiệu ứng visual khác nhau
     * 
     * Thực hiện:
     * 1. Bật antialiasing để vẽ mượt mà hơn
     * 2. Kiểm tra có sprite image không
     * 3. Vẽ bóng với hiệu ứng tùy thuộc vào power-up đang active
     *    - Mega Ball: Aura đỏ + bóng to
     *    - Incandescent: Aura cam + highlight sáng
     *    - Normal: Vẽ sprite bình thường hoặc gradient
     * 
     * @param g Graphics context để vẽ
     */
    @Override
    public void draw(Graphics g) {
        // Cast Graphics sang Graphics2D để có nhiều tính năng vẽ hơn
        Graphics2D g2d = (Graphics2D) g;
        
        // Bật antialiasing để vẽ mượt mà (không bị răng cưa)
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                            RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Kiểm tra có sprite image không
        if (ballSprite != null) {
            // CÓ SPRITE IMAGE - Vẽ bằng hình ảnh
            
            if (megaBall) {
                // === MEGA BALL MODE ===
                // Vẽ aura đỏ phía sau bóng
                g2d.setColor(new Color(255, 100, 100, 100)); // RGBA: R=255, G=100, B=100, Alpha=100 (trong suốt)
                g2d.fillOval(x - 5, y - 5, width + 10, height + 10); // Vẽ hình tròn lớn hơn bóng 10 pixel
                
                // Vẽ bóng chính
                g2d.drawImage(ballSprite, x, y, width, height, null);
                
            } else if (incandescent) {
                // === INCANDESCENT MODE (Bóng xuyên thủng) ===
                // Vẽ aura màu cam
                g2d.setColor(new Color(255, 140, 0, 120)); // Aura cam trong suốt
                g2d.fillOval(x - 4, y - 4, width + 8, height + 8); // Aura rộng hơn bóng 8 pixel
                
                // Vẽ bóng chính với màu cam overlay
                g2d.setColor(new Color(255, 140, 0)); // Màu cam đậm
                g2d.fillOval(x, y, width, height);
                
                // Thêm highlight sáng ở trung tâm
                g2d.setColor(new Color(255, 200, 100, 200)); // Màu vàng cam sáng
                g2d.fillOval(x + width/4, y + height/4, width/2, height/2);
                
            } else {
                // === NORMAL MODE ===
                // Vẽ sprite bình thường
                g2d.drawImage(ballSprite, x, y, width, height, null);
            }
            
        } else {
            // KHÔNG CÓ SPRITE IMAGE - Vẽ bằng màu sắc (fallback)
            
            if (megaBall) {
                // Mega Ball: Gradient đỏ
                GradientPaint gp = new GradientPaint(
                    x, y, new Color(255, 100, 100),                    // Màu bắt đầu (đỏ nhạt)
                    x + width, y + height, new Color(255, 50, 50));   // Màu kết thúc (đỏ đậm)
                g2d.setPaint(gp);
                
            } else if (incandescent) {
                // Incandescent: Gradient cam
                GradientPaint gp = new GradientPaint(
                    x, y, new Color(255, 180, 0),                     // Màu bắt đầu (cam sáng)
                    x + width, y + height, new Color(255, 100, 0));  // Màu kết thúc (cam đậm)
                g2d.setPaint(gp);
                
            } else {
                // Normal: Gradient trắng-đỏ
                GradientPaint gp = new GradientPaint(
                    x, y, Color.WHITE,                                // Màu bắt đầu (trắng)
                    x + width, y + height, Color.RED);               // Màu kết thúc (đỏ)
                g2d.setPaint(gp);
            }
            
            // Vẽ hình tròn với gradient
            g2d.fillOval(x, y, width, height);
            
            // Thêm highlight (vùng sáng) để bóng trông có chiều sâu hơn
            g2d.setColor(new Color(255, 255, 255, 150)); // Trắng trong suốt
            g2d.fillOval(x + width/4, y + height/4, width/3, height/3); // Vẽ ở 1/4 từ góc trên trái
        }
    }
    
    // ============================================================
    // PHƯƠNG THỨC VA CHẠM
    // ============================================================
    
    /**
     * Đảo chiều di chuyển theo trục X
     * Được gọi khi bóng va chạm với tường trái/phải hoặc cạnh bên của paddle
     */
    public void reverseX() {
        dx = -dx; // Đảo dấu vận tốc X
    }
    
    /**
     * Đảo chiều di chuyển theo trục Y
     * Được gọi khi bóng va chạm với tường trên hoặc paddle/gạch
     */
    public void reverseY() {
        dy = -dy; // Đảo dấu vận tốc Y
    }
    
    // ============================================================
    // PHƯƠNG THỨC POWER-UP
    // ============================================================
    
    /**
     * Kích hoạt power-up Mega Ball
     * Bóng sẽ to ra và có thể phá gạch mạnh hơn
     * 
     * @param duration Thời lượng hiệu ứng (đơn vị: frames, ví dụ 300 frames = 5 giây @ 60 FPS)
     */
    public void activateMegaBall(int duration) {
        megaBall = true;
        megaBallTimer = duration;
        
        // Tăng kích thước bóng lên 40x40 (từ 20x20)
        width = 40;
        height = 40;
    }
    
    /**
     * Kích hoạt power-up Incandescent (bóng xuyên thủng)
     * Bóng có thể xuyên qua gạch mà không bị bật lại
     * 
     * @param duration Thời lượng hiệu ứng (đơn vị: frames)
     */
    public void activateIncandescent(int duration) {
        incandescent = true;
        incandescentTimer = duration;
    }
    
    /**
     * Kiểm tra bóng có đang ở trạng thái Mega Ball không
     * 
     * @return true nếu Mega Ball đang active, false nếu không
     */
    public boolean isMegaBall() {
        return megaBall;
    }
    
    /**
     * Kiểm tra bóng có đang ở trạng thái Incandescent không
     * 
     * @return true nếu Incandescent đang active, false nếu không
     */
    public boolean isIncandescent() {
        return incandescent;
    }
}
