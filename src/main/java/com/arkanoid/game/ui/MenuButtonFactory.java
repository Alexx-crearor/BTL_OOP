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

/**
 * Factory class để tạo các button với style nhất quán
 * Chức năng:
 * - Tạo styled button cho menu chính
 * - Tạo button cho level selection dialog
 * - Xử lý hover effects và animations
 */
public class MenuButtonFactory {
    
    /**
     * Tạo nút menu chính với gradient và hover effect
     * @param text Text hiển thị trên nút
     * @param font Font của text
     * @param normalColor Màu bình thường
     * @param hoverColor Màu khi hover
     * @return JButton đã được style
     */
    public static JButton createStyledButton(String text, Font font, Color normalColor, Color hoverColor) {
        JButton button = new JButton(text) {
            private boolean isHovered = false;
            private float alpha = 0.0f;
            
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Vẽ background gradient với độ tương phản cao hơn
                Color startColor, endColor;
                if (isHovered) {
                    // Khi hover/selected: Sáng hơn NHIỀU
                    startColor = hoverColor.brighter().brighter();
                    endColor = hoverColor.brighter();
                } else {
                    // Khi không hover: Tối hơn
                    startColor = normalColor.darker();
                    endColor = normalColor.darker().darker();
                }
                
                GradientPaint gradient = new GradientPaint(
                    0, 0, startColor,
                    0, getHeight(), endColor
                );
                g2d.setPaint(gradient);
                g2d.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);
                
                // Vẽ border - VÀNG SÁNG khi hover/selected
                if (isHovered) {
                    g2d.setColor(Color.YELLOW); // Đổi sang vàng rõ ràng
                    g2d.setStroke(new BasicStroke(3)); // Border dày hơn
                } else {
                    g2d.setColor(new Color(200, 200, 200));
                    g2d.setStroke(new BasicStroke(2));
                }
                g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);
                
                // Vẽ text với shadow
                g2d.setFont(getFont());
                FontMetrics fm = g2d.getFontMetrics();
                int textWidth = fm.stringWidth(getText());
                int textX = (getWidth() - textWidth) / 2;
                int textY = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                
                // Shadow
                g2d.setColor(new Color(0, 0, 0, 100));
                g2d.drawString(getText(), textX + 2, textY + 2);
                
                // Text
                g2d.setColor(getForeground());
                g2d.drawString(getText(), textX, textY);
                
                g2d.dispose();
            }
            
            public void setHovered(boolean hovered) {
                if (this.isHovered != hovered) {
                    this.isHovered = hovered;
                    repaint();
                }
            }
        };
        
        button.setFont(font);
        button.setForeground(Color.WHITE);
        button.setPreferredSize(new Dimension(250, 50));
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setFocusable(false); // Quan trọng: Không cho button chiếm focus
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Hover effects
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                try {
                    java.lang.reflect.Method method = button.getClass().getMethod("setHovered", boolean.class);
                    method.invoke(button, true);
                } catch (Exception e) {
                    // Ignore
                }
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                try {
                    java.lang.reflect.Method method = button.getClass().getMethod("setHovered", boolean.class);
                    method.invoke(button, false);
                } catch (Exception e) {
                    // Ignore
                }
            }
        });
        
        return button;
    }
    
    /**
     * Tạo nút cho level selection dialog
     * @param text Text hiển thị (VD: "LEVEL 1")
     * @param font Font của text
     * @return JButton đã được style
     */
    public static JButton createLevelButton(String text, Font font) {
        JButton button = new JButton(text) {
            private boolean isHovered = false;
            
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Background gradient (Purple theme)
                Color startColor = isHovered ? new Color(138, 43, 226) : new Color(75, 0, 130);
                Color endColor = isHovered ? new Color(75, 0, 130) : new Color(50, 0, 80);
                
                GradientPaint gradient = new GradientPaint(
                    0, 0, startColor,
                    0, getHeight(), endColor
                );
                g2d.setPaint(gradient);
                g2d.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                
                // Border
                g2d.setColor(isHovered ? Color.YELLOW : Color.WHITE);
                g2d.setStroke(new BasicStroke(2));
                g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                
                // Text với shadow
                g2d.setFont(getFont());
                FontMetrics fm = g2d.getFontMetrics();
                int textWidth = fm.stringWidth(getText());
                int textX = (getWidth() - textWidth) / 2;
                int textY = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                
                g2d.setColor(new Color(0, 0, 0, 150));
                g2d.drawString(getText(), textX + 1, textY + 1);
                
                g2d.setColor(getForeground());
                g2d.drawString(getText(), textX, textY);
                
                g2d.dispose();
            }
            
            public void setHovered(boolean hovered) {
                if (this.isHovered != hovered) {
                    this.isHovered = hovered;
                    repaint();
                }
            }
        };
        
        button.setFont(font);
        button.setForeground(Color.WHITE);
        button.setPreferredSize(new Dimension(150, 60));
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setFocusable(false); // Quan trọng: Không cho button chiếm focus
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Hover effects
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                try {
                    java.lang.reflect.Method method = button.getClass().getMethod("setHovered", boolean.class);
                    method.invoke(button, true);
                } catch (Exception e) {
                    // Ignore
                }
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                try {
                    java.lang.reflect.Method method = button.getClass().getMethod("setHovered", boolean.class);
                    method.invoke(button, false);
                } catch (Exception e) {
                    // Ignore
                }
            }
        });
        
        return button;
    }
}
