package com.arkanoid.game.ui;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JButton;

/**
 * Xử lý keyboard input cho menu
 * Chức năng:
 * - Di chuyển qua lại giữa các nút bằng UP/DOWN
 * - Nhấn ENTER để chọn nút
 * - Cập nhật focus và border của nút
 */
public class MenuInputHandler extends BaseInputHandler {
    
    /**
     * Constructor
     * @param buttons Mảng các nút menu
     */
    public MenuInputHandler(JButton[] buttons) {
        super(buttons);
    }
    
    /**
     * Tạo KeyAdapter để xử lý phím
     * @return KeyAdapter đã được configure
     */
    public KeyAdapter createKeyAdapter() {
        return new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_UP:
                        selectedIndex = (selectedIndex - 1 + buttons.length) % buttons.length;
                        updateButtonFocus();
                        break;
                    case KeyEvent.VK_DOWN:
                        selectedIndex = (selectedIndex + 1) % buttons.length;
                        updateButtonFocus();
                        break;
                    case KeyEvent.VK_ENTER:
                        clickSelectedButton();
                        break;
                }
            }
        };
    }
}
