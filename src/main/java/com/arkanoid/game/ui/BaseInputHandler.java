package com.arkanoid.game.ui;

import javax.swing.JButton;

// ============================================================
// ABSTRACT CLASS: BaseInputHandler - Base cho keyboard handlers
// ============================================================

/**
 * Abstract Class BaseInputHandler - Base class cho input handlers
 * 
 * SUBCLASSES:
 * - MenuInputHandler: Xử lý keyboard navigation cho Menu
 * - LevelSelectionInputHandler: Xử lý navigation cho LevelSelectionDialog
 * 
 * TRÁCH NHIỆM:
 * 1. Quản lý selected index (button nào đang được chọn)
 * 2. Update visual feedback (Yellow border cho selected button)
 * 3. Trigger button click (doClick() method)
 * 
 * COMMON PATTERN:
 * - Tất cả input handlers cần track selectedIndex
 * - Tất cả cần update visual feedback khi selection thay đổi
 * - Tất cả cần trigger click khi ENTER pressed
 * 
 * VISUAL FEEDBACK:
 * - Sử dụng reflection để call setHovered(boolean) trên buttons
 * - setHovered(true): Yellow border + brighter gradient
 * - setHovered(false): Gray border + normal gradient
 * 
 * WHY REFLECTION:
 * - Buttons được tạo bởi MenuButtonFactory là anonymous classes
 * - setHovered() method không có trong JButton interface
 * - Reflection cho phép dynamic method invocation
 * 
 * @author Arkanoid Game Team
 * @version 1.0
 */
public abstract class BaseInputHandler {
    
    // ============================================================
    // PROTECTED FIELDS - Được dùng bởi subclasses
    // ============================================================
    
    /** Mảng buttons cần xử lý navigation */
    protected final JButton[] buttons;
    
    /** Index của button đang được selected (0-based) */
    protected int selectedIndex = 0;
    
    // ============================================================
    // CONSTRUCTOR
    // ============================================================
    
    /**
     * Constructor - Khởi tạo với mảng buttons
     * 
     * @param buttons Mảng JButtons cần xử lý
     */
    public BaseInputHandler(JButton[] buttons) {
        this.buttons = buttons;
    }
    
    // ============================================================
    // VISUAL FEEDBACK - Update button appearance
    // ============================================================
    
    /**
     * Cập nhật visual feedback cho button đang selected
     * 
     * LOGIC:
     * - Loop qua tất cả buttons
     * - Button tại selectedIndex: setHovered(true) → Yellow border
     * - Buttons khác: setHovered(false) → Gray border
     * 
     * REFLECTION:
     * - buttons[i].getClass().getMethod("setHovered", boolean.class)
     * - method.invoke(buttons[i], isSelected)
     * - Catch Exception nếu method không tồn tại (fallback gracefully)
     */
    protected void updateButtonFocus() {
        for (int i = 0; i < buttons.length; i++) {
            try {
                // Get setHovered() method via reflection
                java.lang.reflect.Method method = buttons[i].getClass().getMethod("setHovered", boolean.class);
                
                // Call setHovered(true) nếu i == selectedIndex, else setHovered(false)
                method.invoke(buttons[i], i == selectedIndex);
            } catch (Exception e) {
                // Button không có setHovered() method → ignore
            }
        }
    }
    
    // ============================================================
    // GETTERS/SETTERS - Selection management
    // ============================================================
    
    /**
     * Lấy index của button đang được selected
     * 
     * @return selectedIndex (0-based)
     */
    public int getSelectedIndex() {
        return selectedIndex;
    }
    
    /**
     * Set index của button được selected (với validation)
     * 
     * VALIDATION: index phải trong range [0, buttons.length-1]
     * 
     * @param index New selected index
     */
    public void setSelectedIndex(int index) {
        if (index >= 0 && index < buttons.length) {
            this.selectedIndex = index;
            updateButtonFocus(); // Update visual feedback
        }
    }
    
    // ============================================================
    // INITIALIZATION - Setup initial state
    // ============================================================
    
    /**
     * Initialize focus cho button đầu tiên (index 0)
     * 
     * ĐƯỢC GỌI TỪ: Constructor của Menu/Dialog
     * 
     * EFFECT: Button đầu tiên sẽ có Yellow border ngay khi mở
     */
    public void initializeFocus() {
        updateButtonFocus();
    }
    
    // ============================================================
    // CLICK ACTION - Trigger button
    // ============================================================
    
    /**
     * Click button đang selected (trigger ActionListener)
     * 
     * ĐƯỢC GỌI TỪ: KeyAdapter khi ENTER pressed
     * 
     * EFFECT: Giống như user click chuột vào button
     * - Fire ActionEvent
     * - Execute ActionListener code
     */
    protected void clickSelectedButton() {
        buttons[selectedIndex].doClick();
    }
}

