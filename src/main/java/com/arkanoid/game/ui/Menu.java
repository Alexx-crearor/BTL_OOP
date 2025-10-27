package com.arkanoid.game.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;

import com.arkanoid.game.game.GamePanel;
import com.arkanoid.game.util.FontManager;
import com.arkanoid.game.util.GameStateManager;

/**
 * Menu chính của game Arkanoid
 * Được refactor thành các helper class để dễ maintain:
 * - MenuAudioManager: Quản lý audio menu
 * - MenuButtonFactory: Tạo các nút với style đẹp
 * - MenuInputHandler: Xử lý keyboard input
 * - LevelSelectionDialog: Dialog chọn level
 */
public class Menu extends JFrame {
    // UI Components
    private BufferedImage backgroundImage;
    private JButton[] buttons;
    private JPanel mainPanel;
    
    // Helpers
    private MenuAudioManager audioManager;
    private MenuInputHandler inputHandler;
    
    // Global volume (0.0 - 1.0) - dùng cho cả menu và game
    public static float globalVolume = 0.8f;
    
    public Menu() {
        // Cấu hình cửa sổ
        setTitle("ARKANOID");
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setFocusable(true);
        setFocusableWindowState(true);
        
        // Khởi tạo helpers
        audioManager = new MenuAudioManager();
        
        loadBackgroundImage();
        audioManager.loadAndPlayMenuMusic();
        
        // Tạo panel chính với ảnh nền
        mainPanel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (backgroundImage != null) {
                    g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
                }
            }
        };
        mainPanel.setOpaque(false);
        mainPanel.setFocusable(true);
        setContentPane(mainPanel);

        Font btnFont = FontManager.getFont(Font.BOLD, 24);
        
        // Kiểm tra xem có game đã lưu không
        GameStateManager stateManager = GameStateManager.getInstance();
        boolean hasSavedGame = stateManager.hasSavedGame();
        
        // Nút Play/Resume
        String playButtonText = hasSavedGame ? "RESUME" : "PLAY";
        JButton playButton = MenuButtonFactory.createStyledButton(playButtonText, btnFont, 
            new Color(34, 139, 34), new Color(0, 100, 0));
        
        JButton highScoreButton = MenuButtonFactory.createStyledButton("HIGH SCORE", btnFont,
            new Color(255, 185, 15), new Color(218, 165, 32));
        
        JButton customButton = MenuButtonFactory.createStyledButton("CUSTOM", btnFont,
            new Color(30, 144, 255), new Color(0, 100, 200));
        
        JButton quitButton = MenuButtonFactory.createStyledButton("QUIT", btnFont,
            new Color(220, 20, 60), new Color(139, 0, 0));

        // Action listeners
        playButton.addActionListener(e -> {
            audioManager.stopMenuMusic();
            
            JFrame frame = new JFrame("Arkanoid");
            GamePanel gamePanel;
            
            if (hasSavedGame) {
                gamePanel = new GamePanel(stateManager.loadGameState());
                stateManager.clearSavedGame();
            } else {
                gamePanel = new GamePanel();
            }
            
            frame.add(gamePanel);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setResizable(false);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
            gamePanel.requestFocusInWindow();
            dispose();
        });
        
        highScoreButton.addActionListener(e -> {
            new HighScorePanel().setVisible(true);
        });
        
        customButton.addActionListener(e -> {
            LevelSelectionDialog dialog = new LevelSelectionDialog(this, this);
            dialog.show();
        });
        
        quitButton.addActionListener(e -> {
            audioManager.stopMenuMusic();
            System.exit(0);
        });
        
        // Lưu các nút vào mảng
        buttons = new JButton[]{playButton, highScoreButton, customButton, quitButton};
        
        // Khởi tạo input handler
        inputHandler = new MenuInputHandler(buttons);
        inputHandler.initializeFocus();
        
        // Thêm KeyListener vào cả JFrame và mainPanel để đảm bảo luôn nhận được phím
        addKeyListener(inputHandler.createKeyAdapter());
        mainPanel.addKeyListener(inputHandler.createKeyAdapter());

        // Layout
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        
        // Add spacing at top
        gbc.gridy = 0;
        gbc.weighty = 0.4;
        gbc.insets = new Insets(160, 0, 0, 0);
        add(Box.createVerticalGlue(), gbc);
        
        // Play button
        gbc.gridy = 1;
        gbc.weighty = 0;
        gbc.insets = new Insets(8, 20, 10, 20);
        add(playButton, gbc);
        
        // High Score button
        gbc.gridy = 2;
        gbc.insets = new Insets(8, 20, 10, 20);
        add(highScoreButton, gbc);
        
        // Custom button
        gbc.gridy = 3;
        gbc.insets = new Insets(8, 20, 10, 20);
        add(customButton, gbc);
        
        // Quit button
        gbc.gridy = 4;
        gbc.insets = new Insets(8, 20, 10, 20);
        add(quitButton, gbc);
        
        // Volume control
        gbc.gridy = 5;
        gbc.insets = new Insets(30, 20, 10, 20);
        add(createVolumeControl(), gbc);
        
        // Add spacing at bottom
        gbc.gridy = 6;
        gbc.weighty = 0.6;
        add(Box.createVerticalGlue(), gbc);
        
        // Request focus
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowOpened(java.awt.event.WindowEvent e) {
                mainPanel.requestFocusInWindow();
            }
        });
    }
    
    /**
     * Tạo panel điều chỉnh âm lượng
     */
    private JPanel createVolumeControl() {
        JPanel volumePanel = new JPanel();
        volumePanel.setOpaque(false);
        volumePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 5));
        volumePanel.setPreferredSize(new Dimension(400, 50));
        
        // Label
        JLabel volumeLabel = new JLabel("VOLUME:");
        volumeLabel.setFont(FontManager.getFont(Font.BOLD, 18));
        volumeLabel.setForeground(Color.YELLOW);
        
        // Slider
        JSlider volumeSlider = new JSlider(0, 100, (int)(globalVolume * 100));
        volumeSlider.setPreferredSize(new Dimension(200, 35));
        volumeSlider.setOpaque(false);
        volumeSlider.setForeground(Color.WHITE);
        volumeSlider.setMajorTickSpacing(25);
        volumeSlider.setMinorTickSpacing(5);
        volumeSlider.setPaintTicks(true);
        volumeSlider.setFocusable(false); // Không cho slider chiếm focus
        
        // Value label
        JLabel valueLabel = new JLabel((int)(globalVolume * 100) + "%");
        valueLabel.setFont(FontManager.getFont(Font.BOLD, 16));
        valueLabel.setForeground(Color.WHITE);
        valueLabel.setPreferredSize(new Dimension(50, 30));
        
        // Listener
        volumeSlider.addChangeListener(e -> {
            int volume = volumeSlider.getValue();
            valueLabel.setText(volume + "%");
            globalVolume = volume / 100.0f;
            audioManager.setVolume(globalVolume);
        });
        
        // Set âm lượng ban đầu
        audioManager.setVolume(globalVolume);
        
        volumePanel.add(volumeLabel);
        volumePanel.add(volumeSlider);
        volumePanel.add(valueLabel);
        
        return volumePanel;
    }
    
    /**
     * Load ảnh nền menu
     */
    private void loadBackgroundImage() {
        try {
            java.net.URL imgURL = getClass().getResource("/Image/menu.png");
            if (imgURL != null) {
                backgroundImage = ImageIO.read(imgURL);
                System.out.println("Menu background loaded successfully!");
            } else {
                System.out.println("Menu background not found: /Image/menu.png");
            }
        } catch (Exception e) {
            System.out.println("Error loading menu background: " + e.getMessage());
            backgroundImage = null;
        }
    }
    
    /**
     * Dừng nhạc menu - được gọi từ LevelSelectionDialog
     */
    public void stopMenuMusic() {
        audioManager.stopMenuMusic();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Menu().setVisible(true));
    }
}
