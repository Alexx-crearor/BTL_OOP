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
            InputStream fontStream = FontManager.class.getResourceAsStream("/font/ByteBounce.ttf");
            if (fontStream != null) {
                customFont = Font.createFont(Font.TRUETYPE_FONT, fontStream);
                System.out.println("Custom font loaded successfully: ByteBounce");
            } else {
                System.out.println("Font file not found, using default font");
                customFont = new Font("Arial", Font.PLAIN, 12);
            }
        } catch (FontFormatException | IOException e) {
            System.err.println("Error loading custom font: " + e.getMessage());
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
