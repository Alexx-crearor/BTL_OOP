package com.arkanoid.game.ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import com.arkanoid.game.game.GamePanel;
import com.arkanoid.game.util.FontManager;

/**
 * Dialog để chọn level
 * Hiển thị 5 nút level (LEVEL 1-5) và nút CANCEL
 */
public class LevelSelectionDialog {
    private final JFrame parentFrame;
    private final Menu parentMenu;
    private JDialog dialog;
    private JButton[] levelButtons;
    private LevelSelectionInputHandler inputHandler;
    
    /**
     * Constructor
     * @param parentFrame Frame cha (Menu)
     * @param parentMenu Menu instance để stop nhạc
     */
    public LevelSelectionDialog(JFrame parentFrame, Menu parentMenu) {
        this.parentFrame = parentFrame;
        this.parentMenu = parentMenu;
    }
    
    /**
     * Hiển thị dialog chọn level
     */
    public void show() {
        dialog = new JDialog(parentFrame, "Chọn Level", true);
        dialog.setLayout(new GridLayout(3, 2, 10, 10));
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(parentFrame);
        dialog.getContentPane().setBackground(Color.BLACK);
        dialog.getContentPane().setFocusable(true);
        
        Font buttonFont = FontManager.getFont(20);
        
        // Tạo array để lưu tất cả buttons (5 levels + 1 cancel)
        levelButtons = new JButton[6];
        
        // Tạo 5 nút level
        for (int i = 1; i <= 5; i++) {
            final int level = i;
            JButton levelButton = MenuButtonFactory.createLevelButton("LEVEL " + i, buttonFont);
            levelButton.addActionListener(e -> {
                startGameAtLevel(level);
                dialog.dispose();
            });
            levelButtons[i - 1] = levelButton;
            dialog.add(levelButton);
        }
        
        // Nút CANCEL
        JButton cancelButton = MenuButtonFactory.createLevelButton("CANCEL", buttonFont);
        cancelButton.addActionListener(e -> dialog.dispose());
        levelButtons[5] = cancelButton;
        dialog.add(cancelButton);
        
        // Khởi tạo input handler với navigation 2D (hỗ trợ 4 phím mũi tên)
        inputHandler = new LevelSelectionInputHandler(levelButtons);
        inputHandler.initializeFocus();
        
        // Thêm KeyListener vào dialog
        dialog.addKeyListener(inputHandler.createKeyAdapter());
        dialog.getContentPane().addKeyListener(inputHandler.createKeyAdapter());
        
        // Request focus khi dialog mở
        dialog.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowOpened(java.awt.event.WindowEvent e) {
                dialog.getContentPane().requestFocusInWindow();
            }
        });
        
        dialog.setVisible(true);
    }
    
    /**
     * Bắt đầu game ở level cụ thể
     * @param level Level muốn chơi (1-5)
     */
    private void startGameAtLevel(int level) {
        parentMenu.stopMenuMusic(); // Dừng nhạc menu trước khi vào game
        parentFrame.dispose(); // Đóng menu
        
        SwingUtilities.invokeLater(() -> {
            JFrame gameFrame = new JFrame("Arkanoid - Level " + level);
            gameFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            gameFrame.setResizable(false);
            
            GamePanel gamePanel = new GamePanel();
            gamePanel.loadLevel(level); // Load level được chọn
            gamePanel.resetBall(); // Reset ball về vị trí ban đầu
            
            gameFrame.add(gamePanel);
            gameFrame.pack();
            gameFrame.setLocationRelativeTo(null);
            gameFrame.setVisible(true);
            
            gamePanel.requestFocusInWindow();
        });
    }
    
    /**
     * Đóng dialog nếu đang mở
     */
    public void close() {
        if (dialog != null && dialog.isVisible()) {
            dialog.dispose();
        }
    }
}
