package com.arkanoid.game.ui;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JButton;

// ============================================================
// CLASS: MenuInputHandler - Keyboard navigation cho Menu
// ============================================================

/**
 * Class MenuInputHandler - Xử lý keyboard navigation cho Menu
 * 
 * EXTENDS: BaseInputHandler (chứa common logic)
 * 
 * TRÁCH NHIỆM:
 * 1. Keyboard navigation giữa 4 buttons (UP/DOWN keys)
 * 2. Activate button với ENTER key
 * 3. Update visual focus (Yellow border cho selected button)
 * 4. Circular navigation (từ button cuối → button đầu và ngược lại)
 * 
 * KEYBOARD CONTROLS:
 * - UP: Move selection lên button trước
 * - DOWN: Move selection xuống button tiếp theo
 * - ENTER: Click button đang selected
 * 
 * VISUAL FEEDBACK:
 * - Selected button: Yellow border (3px) + brighter gradient
 * - Other buttons: Gray border (2px) + normal gradient
 * - Sử dụng setHovered(true/false) method từ MenuButtonFactory
 * 
 * NAVIGATION LOGIC:
 * ```
 * UP:   selectedIndex = (selectedIndex - 1 + buttonsLength) % buttonsLength
 * DOWN: selectedIndex = (selectedIndex + 1) % buttonsLength
 * ```
 * 
 * Example với 4 buttons:
 * - Tại button 0, UP → button 3 (wrap around)
 * - Tại button 3, DOWN → button 0 (wrap around)
 * 
 * @author Arkanoid Game Team
 * @version 1.0
 */
public class MenuInputHandler extends BaseInputHandler {
    
    // ============================================================
    // CONSTRUCTOR
    // ============================================================
    
    /**
     * Constructor - Khởi tạo handler với mảng buttons
     * 
     * @param buttons Mảng 4 buttons [PLAY, HIGH SCORE, CUSTOM, QUIT]
     */
    public MenuInputHandler(JButton[] buttons) {
        super(buttons); // Call BaseInputHandler constructor
    }
    
    // ============================================================
    // KEY ADAPTER - Tạo keyboard listener
    // ============================================================
    
    /**
     * Tạo KeyAdapter để xử lý keyboard input
     * 
     * USAGE:
     * ```java
     * MenuInputHandler handler = new MenuInputHandler(buttons);
     * menu.addKeyListener(handler.createKeyAdapter());
     * ```
     * 
     * KEYS HANDLED:
     * - VK_UP: Di chuyển selection lên (circular)
     * - VK_DOWN: Di chuyển selection xuống (circular)
     * - VK_ENTER: Click button đang selected
     * 
     * @return KeyAdapter configured với menu navigation logic
     */
    public KeyAdapter createKeyAdapter() {
        return new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_UP:
                        // Di chuyển lên (circular: 0 → 3)
                        selectedIndex = (selectedIndex - 1 + buttons.length) % buttons.length;
                        updateButtonFocus(); // Update visual feedback
                        break;
                        
                    case KeyEvent.VK_DOWN:
                        // Di chuyển xuống (circular: 3 → 0)
                        selectedIndex = (selectedIndex + 1) % buttons.length;
                        updateButtonFocus(); // Update visual feedback
                        break;
                        
                    case KeyEvent.VK_ENTER:
                        // Click button đang selected (trigger ActionListener)
                        clickSelectedButton();
                        break;
                }
            }
        };
    }
}

