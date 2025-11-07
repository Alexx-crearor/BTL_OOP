package com.arkanoid.game.util;

import java.awt.Font;
import java.awt.FontFormatException;
import java.io.IOException;
import java.io.InputStream;

// ============================================================
// CLASS: FontManager - Quản lý custom fonts cho game
// ============================================================

/**
 * Class FontManager - Singleton utility class quản lý custom font
 * 
 * MỤC ĐÍCH:
 * - Load custom font "ByteBounce.ttf" từ resources
 * - Cung cấp font với size/style khác nhau cho toàn bộ game
 * - Fallback về Arial nếu load thất bại
 * 
 * CÁCH SỬ DỤNG:
 * ```java
 * Font titleFont = FontManager.getFont(Font.BOLD, 48);  // Bold 48pt
 * Font normalFont = FontManager.getFont(20);             // Plain 20pt
 * Font baseFont = FontManager.getFont();                 // Base font
 * ```
 * 
 * KIẾN TRÚC:
 * - Static initialization block: Load font 1 lần duy nhất khi class load
 * - Static methods: Truy cập font từ bất kỳ đâu mà không cần instance
 * - Immutable: customFont được load 1 lần, không thể thay đổi
 * 
 * DESIGN PATTERN: Singleton (thông qua static)
 * 
 * @author Arkanoid Game Team
 * @version 1.0
 */
public class FontManager {
    
    // ============================================================
    // THUỘC TÍNH - Custom Font
    // ============================================================
    
    /**
     * Custom font được load từ resources/Font/ByteBounce.ttf
     * 
     * CHI TIẾT:
     * - null nếu chưa load
     * - Non-null sau static initializer
     * - Fallback về Arial nếu ByteBounce.ttf không tìm thấy
     * - Được dùng để derive các font với size/style khác nhau
     */
    private static Font customFont;
    
    // ============================================================
    // STATIC INITIALIZER - Load font khi class được load
    // ============================================================
    
    /**
     * Static initializer block
     * 
     * CÁCH HOẠT ĐỘNG:
     * - Chạy 1 lần duy nhất khi class FontManager được load lần đầu
     * - Gọi loadCustomFont() để load ByteBounce.ttf
     * - Nếu thất bại: customFont = Arial (fallback)
     * 
     * LỢI ÍCH:
     * - Load sớm: Font sẵn sàng ngay khi cần
     * - Thread-safe: JVM đảm bảo static init chỉ chạy 1 lần
     * - Lazy loading: Chỉ load khi class được dùng
     */
    static {
        loadCustomFont();
    }
    
    // ============================================================
    // PHƯƠNG THỨC PRIVATE - Load Custom Font
    // ============================================================
    
    /**
     * Load custom font từ resources/Font/ByteBounce.ttf
     * 
     * LOGIC HOẠT ĐỘNG:
     * 1. Thử load từ "/Font/ByteBounce.ttf" (uppercase F)
     * 2. Nếu thất bại, thử "/font/ByteBounce.ttf" (lowercase f)
     * 3. Nếu tìm thấy: Tạo Font từ InputStream
     * 4. Nếu không tìm thấy: Fallback về Arial
     * 
     * XỬ LÝ LỖI:
     * - FontFormatException: File .ttf bị corrupt hoặc format sai
     * - IOException: Lỗi đọc file
     * - Cả 2 trường hợp đều fallback về Arial và log error
     * 
     * FALLBACK STRATEGY:
     * - Font name: "Arial"
     * - Style: PLAIN
     * - Size: 12pt (sẽ được derive lại khi dùng)
     * 
     * GHI CHÚ:
     * - Path case-sensitive: Unix/Linux phân biệt /Font/ vs /font/
     * - Windows không phân biệt nhưng JAR file có thể phân biệt
     * - Nên đảm bảo file nằm ở /Font/ (uppercase)
     */
    private static void loadCustomFont() {
        try {
            // BƯỚC 1: Thử load từ /Font/ByteBounce.ttf (uppercase F)
            InputStream fontStream = FontManager.class.getResourceAsStream("/Font/ByteBounce.ttf");
            
            // BƯỚC 2: Nếu không tìm thấy, thử /font/ByteBounce.ttf (lowercase f)
            // Để tương thích với các hệ thống khác nhau
            if (fontStream == null) {
                fontStream = FontManager.class.getResourceAsStream("/font/ByteBounce.ttf");
            }
            
            if (fontStream != null) {
                // BƯỚC 3: Tìm thấy file - tạo Font object
                customFont = Font.createFont(Font.TRUETYPE_FONT, fontStream);
                System.out.println("[FontManager] Custom font loaded successfully: ByteBounce");
                
                // BƯỚC 4: Đóng stream để giải phóng resources
                fontStream.close();
            } else {
                // BƯỚC 5: Không tìm thấy file - fallback về Arial
                System.err.println("[FontManager] Font file not found in JAR, using default font");
                customFont = new Font("Arial", Font.PLAIN, 12);
            }
        } catch (FontFormatException | IOException e) {
            // XỬ LÝ LỖI: Font corrupt hoặc lỗi I/O
            System.err.println("[FontManager] Error loading custom font: " + e.getMessage());
            e.printStackTrace();
            
            // Fallback về Arial để game vẫn chạy được
            customFont = new Font("Arial", Font.PLAIN, 12);
        }
    }
    
    // ============================================================
    // PHƯƠNG THỨC PUBLIC - Lấy Font với Style và Size
    // ============================================================
    
    /**
     * Lấy custom font với style và size tùy chỉnh
     * 
     * CÁCH SỬ DỤNG:
     * ```java
     * Font titleFont = FontManager.getFont(Font.BOLD, 48);
     * Font italicFont = FontManager.getFont(Font.ITALIC, 20);
     * Font boldItalic = FontManager.getFont(Font.BOLD | Font.ITALIC, 24);
     * ```
     * 
     * THAM SỐ:
     * - style: Font.PLAIN, Font.BOLD, Font.ITALIC, hoặc Font.BOLD | Font.ITALIC
     * - size: Kích thước font tính bằng points (thường 12-72)
     * 
     * RETURN:
     * - Font mới được derive từ customFont với style và size được chỉ định
     * - Không ảnh hưởng đến customFont gốc (immutable)
     * 
     * @param style Style của font (Font.PLAIN, BOLD, ITALIC)
     * @param size Kích thước font (points)
     * @return Font object mới với style và size được chỉ định
     */
    public static Font getFont(int style, int size) {
        return customFont.deriveFont(style, (float) size);
    }
    
    /**
     * Lấy custom font với size tùy chỉnh (PLAIN style)
     * 
     * CÁCH SỬ DỤNG:
     * ```java
     * Font normalFont = FontManager.getFont(20);  // Plain 20pt
     * ```
     * 
     * THAM SỐ:
     * - size: Kích thước font tính bằng points
     * 
     * RETURN:
     * - Font mới với style PLAIN và size được chỉ định
     * 
     * @param size Kích thước font (points)
     * @return Font object mới với style PLAIN
     */
    public static Font getFont(int size) {
        return customFont.deriveFont((float) size);
    }
    
    /**
     * Lấy base custom font (không thay đổi size/style)
     * 
     * CÁCH SỬ DỤNG:
     * ```java
     * Font baseFont = FontManager.getFont();
     * ```
     * 
     * RETURN:
     * - Custom font gốc (ByteBounce hoặc Arial nếu fallback)
     * - Size và style mặc định (12pt, PLAIN)
     * 
     * @return Custom font gốc
     */
    public static Font getFont() {
        return customFont;
    }
}
