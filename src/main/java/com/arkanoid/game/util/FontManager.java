package com.arkanoid.game.util;

import java.awt.Font;
import java.awt.FontFormatException;
import java.io.IOException;
import java.io.InputStream;

/**
 * FontManager - Quản lý custom fonts cho game
 */
public class FontManager {
    private static Font customFont;
    
    static {
        loadCustomFont();
    }
    
    /**
     * Load custom font từ resources
     */
    private static void loadCustomFont() {
        try {
            // Thử load với các path khác nhau (case-sensitive)
            InputStream fontStream = FontManager.class.getResourceAsStream("/Font/ByteBounce.ttf");
            
            // Nếu không tìm thấy, thử path chữ thường
            if (fontStream == null) {
                fontStream = FontManager.class.getResourceAsStream("/font/ByteBounce.ttf");
            }
            
            if (fontStream != null) {
                customFont = Font.createFont(Font.TRUETYPE_FONT, fontStream);
                System.out.println("[FontManager] Custom font loaded successfully: ByteBounce");
                fontStream.close();
            } else {
                System.err.println("[FontManager] Font file not found in JAR, using default font");
                customFont = new Font("Arial", Font.PLAIN, 12);
            }
        } catch (FontFormatException | IOException e) {
            System.err.println("[FontManager] Error loading custom font: " + e.getMessage());
            e.printStackTrace();
            customFont = new Font("Arial", Font.PLAIN, 12);
        }
    }
    
    /**
     * Lấy custom font với size và style tùy chỉnh
     */
    public static Font getFont(int style, int size) {
        return customFont.deriveFont(style, (float) size);
    }
    
    /**
     * Lấy custom font với size (PLAIN style)
     */
    public static Font getFont(int size) {
        return customFont.deriveFont((float) size);
    }
    
    /**
     * Lấy base custom font
     */
    public static Font getFont() {
        return customFont;
    }
}
