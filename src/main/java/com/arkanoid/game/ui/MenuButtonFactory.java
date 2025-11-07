package com.arkanoid.game.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;

import javax.swing.JButton;

// ============================================================
// CLASS: MenuButtonFactory - Factory tạo styled buttons
// ============================================================

/**
 * Class MenuButtonFactory - Factory pattern để tạo styled buttons
 * 
 * TRÁCH NHIỆM:
 * 1. Tạo styled button cho Menu chính (PLAY, HIGH SCORE, CUSTOM, QUIT)
 * 2. Tạo level selection button cho LevelSelectionDialog (LEVEL 1-5)
 * 3. Cung cấp consistent styling cho toàn bộ UI
 * 
 * STYLING FEATURES:
 * - Gradient background (top → bottom)
 * - Rounded corners (15px radius)
 * - Border với hover effect (Gray → Yellow)
 * - Text với drop shadow
 * - Hover animation (color transition)
 * - Hand cursor khi hover
 * 
 * BUTTON STATES:
 * - Normal: Darker gradient + Gray border (2px)
 * - Hovered/Selected: Brighter gradient + Yellow border (3px)
 * 
 * DESIGN PATTERN: Factory Method Pattern
 * - Static methods tạo buttons với configuration khác nhau
 * - Encapsulate button creation logic
 * - Ensure consistency across UI
 * 
 * @author Arkanoid Game Team
 * @version 1.0
 */
public class MenuButtonFactory {
    
    // ============================================================
    // FACTORY METHOD - Styled Button cho Menu
    // ============================================================
    
    /**
     * Tạo styled button cho menu chính với gradient và hover effect
     * 
     * USAGE:
     * ```java
     * JButton playButton = MenuButtonFactory.createStyledButton(
     *     "PLAY", 
     *     FontManager.getFont(Font.BOLD, 24),
     *     new Color(34, 139, 34),  // Forest Green
     *     new Color(0, 100, 0)      // Dark Green
     * );
     * ```
     * 
     * VISUAL STATES:
     * - Normal: normalColor.darker() → normalColor.darker().darker()
     * - Hovered: hoverColor.brighter().brighter() → hoverColor.brighter()
     * - Border: Gray 2px → Yellow 3px (when hovered)
     * 
     * CUSTOM PAINT:
     * - Override paintComponent() để vẽ gradient + border + text shadow
     * - Anti-aliasing enabled cho smooth graphics
     * 
     * @param text Text hiển thị trên button
     * @param font Font của text (usually ByteBounce BOLD 24)
     * @param normalColor Base color cho gradient (không hover)
     * @param hoverColor Color khi hover/selected
     * @return JButton đã được style với custom paint
     */
    public static JButton createStyledButton(String text, Font font, Color normalColor, Color hoverColor) {
        // Tạo anonymous JButton class với custom painting
        JButton button = new JButton(text) {
            // State tracking
            private boolean isHovered = false;
            private float alpha = 0.0f; // For future animation support
            
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                
                // ---- Enable AntiAliasing ----
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // ---- GRADIENT BACKGROUND ----
                Color startColor, endColor;
                if (isHovered) {
                    // Hover state: Sáng hơn rất nhiều (2x brighter)
                    startColor = hoverColor.brighter().brighter();
                    endColor = hoverColor.brighter();
                } else {
                    // Normal state: Tối hơn (darker)
                    startColor = normalColor.darker();
                    endColor = normalColor.darker().darker();
                }
                
                // Vẽ vertical gradient (top → bottom)
                GradientPaint gradient = new GradientPaint(
                    0, 0, startColor,
                    0, getHeight(), endColor
                );
                g2d.setPaint(gradient);
                g2d.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);
                
                // ---- BORDER ----
                if (isHovered) {
                    g2d.setColor(Color.YELLOW);    // Yellow border khi hover
                    g2d.setStroke(new BasicStroke(3)); // Dày hơn (3px)
                } else {
                    g2d.setColor(new Color(200, 200, 200)); // Gray border
                    g2d.setStroke(new BasicStroke(2));       // Thường (2px)
                }
                g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);
                
                // ---- TEXT với DROP SHADOW ----
                g2d.setFont(getFont());
                FontMetrics fm = g2d.getFontMetrics();
                int textWidth = fm.stringWidth(getText());
                
                // Center text
                int textX = (getWidth() - textWidth) / 2;
                int textY = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                
                // Shadow (offset 2px down-right, semi-transparent black)
                g2d.setColor(new Color(0, 0, 0, 100));
                g2d.drawString(getText(), textX + 2, textY + 2);
                
                // Main text (white)
                g2d.setColor(getForeground());
                g2d.drawString(getText(), textX, textY);
                
                g2d.dispose();
            }
            
            /**
             * Set hover state và trigger repaint
             * ĐƯỢC GỌI TỪ: MenuInputHandler (keyboard navigation)
             */
            public void setHovered(boolean hovered) {
                if (this.isHovered != hovered) {
                    this.isHovered = hovered;
                    repaint(); // Redraw với state mới
                }
            }
        };
        
        // ---- Button Configuration ----
        button.setFont(font);
        button.setForeground(Color.WHITE); // Text color
        button.setPreferredSize(new Dimension(250, 50));
        button.setContentAreaFilled(false); // No default background
        button.setBorderPainted(false);     // No default border
        button.setFocusPainted(false);      // No focus indicator
        button.setFocusable(false);         // QUAN TRỌNG: Không chiếm focus (MenuInputHandler xử lý)
        button.setCursor(new Cursor(Cursor.HAND_CURSOR)); // Hand cursor khi hover
        
        // ---- MOUSE LISTENERS (Hover Effects) ----
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                // Call setHovered(true) via reflection (vì method trong anonymous class)
                try {
                    java.lang.reflect.Method method = button.getClass().getMethod("setHovered", boolean.class);
                    method.invoke(button, true);
                } catch (Exception e) {
                    // Ignore reflection errors
                }
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                // Call setHovered(false)
                try {
                    java.lang.reflect.Method method = button.getClass().getMethod("setHovered", boolean.class);
                    method.invoke(button, false);
                } catch (Exception e) {
                    // Ignore reflection errors
                }
            }
        });
        
        return button;
    }
    
    // ============================================================
    // FACTORY METHOD - Level Selection Button
    // ============================================================
    
    /**
     * Tạo button cho level selection dialog
     * 
     * USAGE:
     * ```java
     * JButton level1Btn = MenuButtonFactory.createLevelButton(
     *     "LEVEL 1", 
     *     FontManager.getFont(Font.BOLD, 20)
     * );
     * ```
     * 
     * STYLING:
     * - Purple gradient theme (Indigo → Dark Purple)
     * - Smaller size: 180x60 (vs 250x50 for menu buttons)
     * - Yellow border khi hover (vs White normally)
     * - Round corners: 10px (vs 15px for menu buttons)
     * 
     * @param text Text hiển thị (e.g. "LEVEL 1", "LEVEL 2", ...)
     * @param font Font của text (usually ByteBounce BOLD 20)
     * @return JButton styled cho level selection
     */
    public static JButton createLevelButton(String text, Font font) {
        // Tạo anonymous JButton với custom painting
        JButton button = new JButton(text) {
            private boolean isHovered = false;
            
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // ---- GRADIENT BACKGROUND (Purple Theme) ----
                Color startColor = isHovered ? new Color(138, 43, 226) : new Color(75, 0, 130);  // Blue Violet : Indigo
                Color endColor = isHovered ? new Color(75, 0, 130) : new Color(50, 0, 80);       // Indigo : Dark Purple
                
                GradientPaint gradient = new GradientPaint(
                    0, 0, startColor,
                    0, getHeight(), endColor
                );
                g2d.setPaint(gradient);
                g2d.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                
                // ---- BORDER ----
                g2d.setColor(isHovered ? Color.YELLOW : Color.WHITE);
                g2d.setStroke(new BasicStroke(2));
                g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                
                // ---- TEXT với DROP SHADOW ----
                g2d.setFont(getFont());
                FontMetrics fm = g2d.getFontMetrics();
                int textWidth = fm.stringWidth(getText());
                int textX = (getWidth() - textWidth) / 2;
                int textY = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                
                // Shadow (darker than menu buttons)
                g2d.setColor(new Color(0, 0, 0, 150));
                g2d.drawString(getText(), textX + 1, textY + 1);
                
                // Main text
                g2d.setColor(getForeground());
                g2d.drawString(getText(), textX, textY);
                
                g2d.dispose();
            }
            
            /**
             * Set hover state và trigger repaint
             */
            public void setHovered(boolean hovered) {
                if (this.isHovered != hovered) {
                    this.isHovered = hovered;
                    repaint();
                }
            }
        };
        
        // ---- Button Configuration (Smaller than menu buttons) ----
        button.setFont(font);
        button.setForeground(Color.WHITE);
        button.setPreferredSize(new Dimension(150, 60)); // 150x60 (vs 250x50)
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setFocusable(false); // Không chiếm focus
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // ---- MOUSE LISTENERS (Hover Effects) ----
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                // Call setHovered(true) via reflection
                try {
                    java.lang.reflect.Method method = button.getClass().getMethod("setHovered", boolean.class);
                    method.invoke(button, true);
                } catch (Exception e) {
                    // Ignore reflection errors
                }
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                // Call setHovered(false)
                try {
                    java.lang.reflect.Method method = button.getClass().getMethod("setHovered", boolean.class);
                    method.invoke(button, false);
                } catch (Exception e) {
                    // Ignore reflection errors
                }
            }
        });
        
        return button;
    }
}

