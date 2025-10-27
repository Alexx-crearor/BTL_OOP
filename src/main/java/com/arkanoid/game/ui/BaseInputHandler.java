package com.arkanoid.game.ui;

import javax.swing.JButton;

/**
 * Base class cho các input handler
 * Cung cấp chức năng chung để update visual feedback cho buttons
 */
public abstract class BaseInputHandler {
    protected final JButton[] buttons;
    protected int selectedIndex = 0;
    
    /**
     * Constructor
     * @param buttons Mảng các nút cần xử lý
     */
    public BaseInputHandler(JButton[] buttons) {
        this.buttons = buttons;
    }
    
    /**
     * Cập nhật visual feedback cho nút đang được chọn
     * Sử dụng reflection để gọi setHovered() method
     */
    protected void updateButtonFocus() {
        for (int i = 0; i < buttons.length; i++) {
            try {
                java.lang.reflect.Method method = buttons[i].getClass().getMethod("setHovered", boolean.class);
                method.invoke(buttons[i], i == selectedIndex);
            } catch (Exception e) {
                // Nếu không có setHovered(), không làm gì
            }
        }
    }
    
    /**
     * Lấy index của nút đang được chọn
     */
    public int getSelectedIndex() {
        return selectedIndex;
    }
    
    /**
     * Set index của nút được chọn
     */
    public void setSelectedIndex(int index) {
        if (index >= 0 && index < buttons.length) {
            this.selectedIndex = index;
            updateButtonFocus();
        }
    }
    
    /**
     * Cập nhật focus cho nút đầu tiên
     */
    public void initializeFocus() {
        updateButtonFocus();
    }
    
    /**
     * Click button hiện tại
     */
    protected void clickSelectedButton() {
        buttons[selectedIndex].doClick();
    }
}
