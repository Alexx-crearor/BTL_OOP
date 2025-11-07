package com.arkanoid.game.entity;

import java.awt.Graphics;
import java.awt.Rectangle;

/**
 * Lớp cha trừu tượng cho tất cả các đối tượng trong game
 * Áp dụng nguyên tắc OOP: Kế thừa và Đa hình (Inheritance & Polymorphism)
 * 
 * Lớp này định nghĩa các thuộc tính và phương thức cơ bản mà tất cả các
 * game object (Ball, Paddle, Brick, PowerUp, v.v.) đều có chung.
 */
public abstract class GameObject {
    // ============================================================
    // THUỘC TÍNH (PROPERTIES)
    // ============================================================
    
    /** Tọa độ X (ngang) của đối tượng trên màn hình (pixel) */
    public int x;
    
    /** Tọa độ Y (dọc) của đối tượng trên màn hình (pixel) */
    public int y;
    
    /** Chiều rộng của đối tượng (pixel) */
    public int width;
    
    /** Chiều cao của đối tượng (pixel) */
    public int height;
    
    /** 
     * Vận tốc theo trục X (dx = delta x)
     * Giá trị dương: di chuyển sang phải
     * Giá trị âm: di chuyển sang trái
     * Giá trị 0: không di chuyển theo trục X
     */
    public int dx;
    
    /** 
     * Vận tốc theo trục Y (dy = delta y)
     * Giá trị dương: di chuyển xuống dưới
     * Giá trị âm: di chuyển lên trên
     * Giá trị 0: không di chuyển theo trục Y
     */
    public int dy;
    
    // ============================================================
    // CONSTRUCTORS
    // ============================================================
    
    /**
     * Constructor mặc định
     * Khởi tạo đối tượng tại vị trí (0,0) với kích thước 0x0 và không có vận tốc
     */
    public GameObject() {
        this.x = 0;
        this.y = 0;
        this.width = 0;
        this.height = 0;
        this.dx = 0;
        this.dy = 0;
    }
    
    /**
     * Constructor với tham số vị trí và kích thước
     * 
     * @param x Tọa độ X ban đầu
     * @param y Tọa độ Y ban đầu
     * @param width Chiều rộng của đối tượng
     * @param height Chiều cao của đối tượng
     */
    public GameObject(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.dx = 0; // Vận tốc ban đầu = 0
        this.dy = 0;
    }
    
    // ============================================================
    // ABSTRACT METHODS - Phương thức trừu tượng
    // ============================================================
    
    /**
     * Vẽ đối tượng lên màn hình
     * Phương thức trừu tượng - phải được implement bởi các lớp con
     * Mỗi loại đối tượng sẽ có cách vẽ riêng
     * 
     * @param g Graphics context để vẽ
     */
    public abstract void draw(Graphics g);
    
    /**
     * Cập nhật trạng thái của đối tượng mỗi frame
     * Phương thức trừu tượng - phải được implement bởi các lớp con
     * Mỗi loại đối tượng sẽ có logic cập nhật riêng
     * (ví dụ: di chuyển, thay đổi trạng thái, kiểm tra điều kiện, v.v.)
     */
    public abstract void update();
    
    // ============================================================
    // CONCRETE METHODS - Phương thức cụ thể
    // ============================================================
    
    /**
     * Kiểm tra va chạm với đối tượng khác
     * Sử dụng Rectangle intersection để phát hiện va chạm
     * 
     * @param other Đối tượng khác cần kiểm tra va chạm
     * @return true nếu có va chạm, false nếu không
     */
    public boolean intersects(GameObject other) {
        return getBounds().intersects(other.getBounds());
    }
    
    /**
     * Lấy hình chữ nhật bao quanh đối tượng
     * Được sử dụng cho collision detection (phát hiện va chạm)
     * 
     * @return Rectangle đại diện cho vùng của đối tượng
     */
    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }
    
    // ============================================================
    // GETTERS - Phương thức lấy giá trị
    // ============================================================
    
    /** @return Tọa độ X hiện tại của đối tượng */
    public int getX() { return x; }
    
    /** @return Tọa độ Y hiện tại của đối tượng */
    public int getY() { return y; }
    
    /** @return Chiều rộng của đối tượng */
    public int getWidth() { return width; }
    
    /** @return Chiều cao của đối tượng */
    public int getHeight() { return height; }
    
    /** @return Vận tốc theo trục X */
    public int getDx() { return dx; }
    
    /** @return Vận tốc theo trục Y */
    public int getDy() { return dy; }
    
    // ============================================================
    // SETTERS - Phương thức thiết lập giá trị
    // ============================================================
    
    /** 
     * Thiết lập tọa độ X
     * @param x Tọa độ X mới
     */
    public void setX(int x) { this.x = x; }
    
    /** 
     * Thiết lập tọa độ Y
     * @param y Tọa độ Y mới
     */
    public void setY(int y) { this.y = y; }
    
    /** 
     * Thiết lập chiều rộng
     * @param width Chiều rộng mới
     */
    public void setWidth(int width) { this.width = width; }
    
    /** 
     * Thiết lập chiều cao
     * @param height Chiều cao mới
     */
    public void setHeight(int height) { this.height = height; }
    
    /** 
     * Thiết lập vận tốc X
     * @param dx Vận tốc X mới
     */
    public void setDx(int dx) { this.dx = dx; }
    
    /** 
     * Thiết lập vận tốc Y
     * @param dy Vận tốc Y mới
     */
    public void setDy(int dy) { this.dy = dy; }
    
    /**
     * Thiết lập vị trí của đối tượng
     * 
     * @param x Tọa độ X mới
     * @param y Tọa độ Y mới
     */
    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }
    
    /**
     * Thiết lập vận tốc của đối tượng
     * 
     * @param dx Vận tốc theo trục X
     * @param dy Vận tốc theo trục Y
     */
    public void setVelocity(int dx, int dy) {
        this.dx = dx;
        this.dy = dy;
    }
}
