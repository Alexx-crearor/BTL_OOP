package com.arkanoid.game.ui;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JButton;

/**
 * Xử lý keyboard input cho level selection dialog
 * Hỗ trợ navigation 2D với cả 4 phím mũi tên
 * Layout: 3 hàng x 2 cột
 */
public class LevelSelectionInputHandler extends BaseInputHandler {
    private final int rows = 3;
    private final int cols = 2;
    
    /**
     * Constructor
     * @param buttons Mảng 6 nút (5 levels + 1 cancel) theo thứ tự từ trái sang phải, trên xuống dưới
     */
    public LevelSelectionInputHandler(JButton[] buttons) {
        super(buttons);
    }
    
    /**
     * Tạo KeyAdapter để xử lý phím với navigation 2D
     * @return KeyAdapter đã được configure
     */
    public KeyAdapter createKeyAdapter() {
        return new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int currentRow = selectedIndex / cols;
                int currentCol = selectedIndex % cols;
                
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_UP:
                        // Di chuyển lên trên
                        currentRow = (currentRow - 1 + rows) % rows;
                        selectedIndex = currentRow * cols + currentCol;
                        updateButtonFocus();
                        break;
                        
                    case KeyEvent.VK_DOWN:
                        // Di chuyển xuống dưới
                        currentRow = (currentRow + 1) % rows;
                        selectedIndex = currentRow * cols + currentCol;
                        updateButtonFocus();
                        break;
                        
                    case KeyEvent.VK_LEFT:
                        // Di chuyển sang trái
                        currentCol = (currentCol - 1 + cols) % cols;
                        selectedIndex = currentRow * cols + currentCol;
                        updateButtonFocus();
                        break;
                        
                    case KeyEvent.VK_RIGHT:
                        // Di chuyển sang phải
                        currentCol = (currentCol + 1) % cols;
                        selectedIndex = currentRow * cols + currentCol;
                        updateButtonFocus();
                        break;
                        
                    case KeyEvent.VK_ENTER:
                        // Chọn button hiện tại
                        clickSelectedButton();
                        break;
                }
            }
        };
    }
}
