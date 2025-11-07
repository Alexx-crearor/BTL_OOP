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

// ============================================================
// CLASS: Menu - Main Menu của Arkanoid Game
// ============================================================

/**
 * Class Menu - Màn hình menu chính của game Arkanoid
 * 
 * TRÁCH NHIỆM:
 * 1. Hiển thị menu chính với 4 buttons:
 *    - PLAY/RESUME: Bắt đầu game mới hoặc resume từ save
 *    - HIGH SCORE: Xem bảng xếp hạng top 10
 *    - CUSTOM: Chọn level tùy chỉnh (1-5)
 *    - QUIT: Thoát game
 * 
 * 2. Quản lý background music (menuMusic.wav)
 * 3. Volume control (slider + LEFT/RIGHT keys)
 * 4. Keyboard navigation (UP/DOWN/ENTER/ESC)
 * 5. Load/save game state
 * 
 * ARCHITECTURE (Delegation Pattern):
 * ```
 * Menu (Main Controller)
 *   ├─> MenuAudioManager (Background music + volume)
 *   ├─> MenuButtonFactory (Tạo styled buttons)
 *   ├─> MenuInputHandler (Keyboard navigation)
 *   └─> LevelSelectionDialog (Custom level selection)
 * ```
 * 
 * UI LAYOUT (GridBagLayout):
 * ```
 * +-----------------------------+
 * |                             |
 * |    [Background Image]       |
 * |                             |
 * |      [PLAY/RESUME]          |  <- Green button
 * |                             |
 * |      [HIGH SCORE]           |  <- Gold button
 * |                             |
 * |      [CUSTOM]               |  <- Blue button
 * |                             |
 * |      [QUIT]                 |  <- Red button
 * |                             |
 * |  Volume: [====|------] 80%  |  <- Volume slider
 * |                             |
 * +-----------------------------+
 * ```
 * 
 * KEYBOARD CONTROLS:
 * - UP/DOWN: Di chuyển giữa các buttons
 * - ENTER: Chọn button hiện tại
 * - ESC: Quit game
 * - LEFT/RIGHT: Điều chỉnh volume
 * 
 * DESIGN PATTERNS:
 * - Delegation: Menu → Helper classes
 * - Factory: MenuButtonFactory tạo styled buttons
 * - Singleton: GameStateManager cho save/load
 * - Observer: Action listeners cho buttons
 * 
 * @author Arkanoid Game Team
 * @version 1.0
 */
public class Menu extends JFrame {
    
    // ============================================================
    // THUỘC TÍNH - UI Components
    // ============================================================
    
    /** Background image (menuBackground.png) */
    private BufferedImage backgroundImage;
    
    /** Mảng 4 buttons: [PLAY, HIGH SCORE, CUSTOM, QUIT] */
    private JButton[] buttons;
    
    /** Main panel với GridBagLayout + background painting */
    private JPanel mainPanel;
    
    // ============================================================
    // THUỘC TÍNH - Helper Objects
    // ============================================================
    
    /** Audio manager để phát menuMusic.wav */
    private MenuAudioManager audioManager;
    
    /** Input handler để xử lý keyboard navigation */
    private MenuInputHandler inputHandler;
    
    // ============================================================
    // GLOBAL VOLUME - Được dùng bởi cả Menu và GamePanel
    // ============================================================
    
    /**
     * Global volume cho toàn bộ game (0.0 → 1.0)
     * 
     * RANGE:
     * - 0.0 = Mute
     * - 0.5 = 50% volume
     * - 1.0 = Max volume
     * 
     * DEFAULT: 0.8 (80%)
     * 
     * ĐƯỢC DÙNG BỞI:
     * - MenuAudioManager (menu music)
     * - AudioManager (game music)
     * - GameRenderer (volume slider display)
     */
    public static float globalVolume = 0.8f;
    
    // ============================================================
    // CONSTRUCTOR
    // ============================================================
    
    /**
     * Constructor - Khởi tạo menu với tất cả components
     * 
     * FLOW:
     * 1. Cấu hình JFrame (800x600, centered)
     * 2. Khởi tạo MenuAudioManager → play menuMusic.wav
     * 3. Load background image (menu.png)
     * 4. Tạo 4 buttons với MenuButtonFactory
     * 5. Check GameStateManager → PLAY vs RESUME
     * 6. Setup action listeners cho buttons
     * 7. Setup keyboard navigation với MenuInputHandler
     * 8. Layout buttons với GridBagLayout
     * 9. Thêm volume slider ở cuối
     * 
     * BUTTONS:
     * - PLAY/RESUME (Green): Start new game hoặc resume saved game
     * - HIGH SCORE (Gold): Mở HighScorePanel
     * - CUSTOM (Blue): Mở LevelSelectionDialog
     * - QUIT (Red): Exit game
     */
    public Menu() {
        // ---- BƯỚC 1: Cấu hình JFrame ----
        setTitle("ARKANOID");
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center screen
        setFocusable(true);
        setFocusableWindowState(true);
        
        // ---- BƯỚC 2: Khởi tạo audio manager ----
        audioManager = new MenuAudioManager();
        
        // ---- BƯỚC 3: Load resources ----
        loadBackgroundImage();
        audioManager.loadAndPlayMenuMusic();
        
        // ---- BƯỚC 4: Tạo main panel với custom paintComponent ----
        // Anonymous class để vẽ background image
        mainPanel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // Vẽ background full screen
                if (backgroundImage != null) {
                    g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
                }
            }
        };
        mainPanel.setOpaque(false); // Transparent để thấy background
        mainPanel.setFocusable(true);
        setContentPane(mainPanel);

        // ---- BƯỚC 5: Load custom font ----
        Font btnFont = FontManager.getFont(Font.BOLD, 24);
        
        // ---- BƯỚC 6: Check saved game state ----
        GameStateManager stateManager = GameStateManager.getInstance();
        boolean hasSavedGame = stateManager.hasSavedGame();
        
        // ---- BƯỚC 7: Tạo 4 buttons với styled colors ----
        // Button 1: PLAY (new game) hoặc RESUME (continue saved game)
        String playButtonText = hasSavedGame ? "RESUME" : "PLAY";
        JButton playButton = MenuButtonFactory.createStyledButton(playButtonText, btnFont, 
            new Color(34, 139, 34), new Color(0, 100, 0)); // Green gradient
        
        // Button 2: HIGH SCORE (xem top 10)
        JButton highScoreButton = MenuButtonFactory.createStyledButton("HIGH SCORE", btnFont,
            new Color(255, 185, 15), new Color(218, 165, 32)); // Gold gradient
        
        // Button 3: CUSTOM (chọn level 1-5)
        JButton customButton = MenuButtonFactory.createStyledButton("CUSTOM", btnFont,
            new Color(30, 144, 255), new Color(0, 100, 200)); // Blue gradient
        
        // Button 4: QUIT (thoát game)
        JButton quitButton = MenuButtonFactory.createStyledButton("QUIT", btnFont,
            new Color(220, 20, 60), new Color(139, 0, 0)); // Red gradient

        // ---- BƯỚC 8: Setup action listeners ----
        
        // PLAY/RESUME Button Action
        playButton.addActionListener(e -> {
            // Stop menu music trước khi chuyển sang game
            audioManager.stopMenuMusic();
            
            // Tạo game window
            JFrame frame = new JFrame("Arkanoid");
            GamePanel gamePanel;
            
            // Load saved game hoặc tạo game mới
            if (hasSavedGame) {
                gamePanel = new GamePanel(stateManager.loadGameState());
                stateManager.clearSavedGame(); // Xóa save sau khi load
            } else {
                gamePanel = new GamePanel(); // Default: Level 1, 3 lives, score 0
            }
            
            // Setup game window
            frame.add(gamePanel);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setResizable(false);
            frame.pack(); // Auto size theo GamePanel preferred size
            frame.setLocationRelativeTo(null); // Center
            frame.setVisible(true);
            gamePanel.requestFocusInWindow(); // Focus cho input
            
            // Đóng menu window
            dispose();
        });
        
        // HIGH SCORE Button Action
        highScoreButton.addActionListener(e -> {
            new HighScorePanel().setVisible(true);
        });
        
        // CUSTOM Button Action
        customButton.addActionListener(e -> {
            LevelSelectionDialog dialog = new LevelSelectionDialog(this, this);
            dialog.show();
        });
        
        // QUIT Button Action
        quitButton.addActionListener(e -> {
            audioManager.stopMenuMusic();
            System.exit(0);
        });
        
        // ---- BƯỚC 9: Lưu buttons vào mảng (cho keyboard navigation) ----
        buttons = new JButton[]{playButton, highScoreButton, customButton, quitButton};
        
        // ---- BƯỚC 10: Setup keyboard navigation ----
        inputHandler = new MenuInputHandler(buttons);
        inputHandler.initializeFocus(); // Focus vào button đầu tiên
        
        // Add KeyListener vào cả JFrame và mainPanel
        addKeyListener(inputHandler.createKeyAdapter());
        mainPanel.addKeyListener(inputHandler.createKeyAdapter());

        // ---- BƯỚC 11: Layout với GridBagLayout ----
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        
        // Spacer phía trên (để buttons không quá cao)
        gbc.gridy = 0;
        gbc.weighty = 0.4;
        gbc.insets = new Insets(160, 0, 0, 0);
        add(Box.createVerticalGlue(), gbc);
        
        // Play button (row 1)
        gbc.gridy = 1;
        gbc.weighty = 0;
        gbc.insets = new Insets(8, 20, 10, 20);
        add(playButton, gbc);
        
        // High Score button (row 2)
        gbc.gridy = 2;
        gbc.insets = new Insets(8, 20, 10, 20);
        add(highScoreButton, gbc);
        
        // Custom button (row 3)
        gbc.gridy = 3;
        gbc.insets = new Insets(8, 20, 10, 20);
        add(customButton, gbc);
        
        // Quit button (row 4)
        gbc.gridy = 4;
        gbc.insets = new Insets(8, 20, 10, 20);
        add(quitButton, gbc);
        
        // Volume control slider (row 5)
        gbc.gridy = 5;
        gbc.insets = new Insets(30, 20, 10, 20);
        add(createVolumeControl(), gbc);
        
        // Spacer phía dưới (push buttons lên)
        gbc.gridy = 6;
        gbc.weighty = 0.6;
        add(Box.createVerticalGlue(), gbc);
        
        // ---- BƯỚC 12: Request focus khi window mở ----
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowOpened(java.awt.event.WindowEvent e) {
                mainPanel.requestFocusInWindow();
            }
        });
    }
    
    // ============================================================
    // VOLUME CONTROL - Tạo panel điều chỉnh âm lượng
    // ============================================================
    
    /**
     * Tạo panel điều chỉnh âm lượng (Volume Slider)
     * 
     * COMPONENTS:
     * - Label "VOLUME:" (Yellow, BOLD 18)
     * - JSlider (0-100, default 80)
     * - Value Label "80%" (White, BOLD 16)
     * 
     * SLIDER SETTINGS:
     * - Range: 0-100 (0% → 100%)
     * - Major Ticks: 25 (0, 25, 50, 75, 100)
     * - Minor Ticks: 5 (every 5%)
     * - Paint Ticks: Yes
     * - Focusable: No (để không ảnh hưởng keyboard navigation)
     * 
     * EVENT:
     * - ChangeListener: Update globalVolume + MenuAudioManager volume
     * 
     * @return JPanel chứa volume controls
     */
    private JPanel createVolumeControl() {
        JPanel volumePanel = new JPanel();
        volumePanel.setOpaque(false); // Transparent để thấy background
        volumePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 5));
        volumePanel.setPreferredSize(new Dimension(400, 50));
        
        // ---- LABEL "VOLUME:" ----
        JLabel volumeLabel = new JLabel("VOLUME:");
        volumeLabel.setFont(FontManager.getFont(Font.BOLD, 18));
        volumeLabel.setForeground(Color.YELLOW);
        
        // ---- SLIDER (0-100) ----
        JSlider volumeSlider = new JSlider(0, 100, (int)(globalVolume * 100));
        volumeSlider.setPreferredSize(new Dimension(200, 35));
        volumeSlider.setOpaque(false);
        volumeSlider.setForeground(Color.WHITE);
        volumeSlider.setMajorTickSpacing(25); // 0, 25, 50, 75, 100
        volumeSlider.setMinorTickSpacing(5);  // Every 5%
        volumeSlider.setPaintTicks(true);     // Show tick marks
        volumeSlider.setFocusable(false);     // Không chiếm focus (keyboard nav không ảnh hưởng)
        
        // ---- VALUE LABEL "80%" ----
        JLabel valueLabel = new JLabel((int)(globalVolume * 100) + "%");
        valueLabel.setFont(FontManager.getFont(Font.BOLD, 16));
        valueLabel.setForeground(Color.WHITE);
        valueLabel.setPreferredSize(new Dimension(50, 30));
        
        // ---- CHANGE LISTENER: Update volume khi kéo slider ----
        volumeSlider.addChangeListener(e -> {
            int volume = volumeSlider.getValue(); // 0-100
            valueLabel.setText(volume + "%");     // Update label
            globalVolume = volume / 100.0f;       // Convert to 0.0-1.0
            audioManager.setVolume(globalVolume); // Update menu music volume
        });
        
        // ---- Set âm lượng ban đầu cho audio manager ----
        audioManager.setVolume(globalVolume);
        
        // ---- Add components vào panel ----
        volumePanel.add(volumeLabel);
        volumePanel.add(volumeSlider);
        volumePanel.add(valueLabel);
        
        return volumePanel;
    }
    
    // ============================================================
    // LOAD BACKGROUND - Load menu background image
    // ============================================================
    
    /**
     * Load ảnh nền menu từ resources
     * 
     * FILE PATH: /Image/menu.png
     * 
     * ERROR HANDLING:
     * - Nếu không tìm thấy file → print error, backgroundImage = null
     * - Nếu lỗi khi đọc file → print error, backgroundImage = null
     * 
     * NULL BACKGROUND: Menu vẫn hoạt động, chỉ không có ảnh nền
     */
    private void loadBackgroundImage() {
        try {
            // Tìm file /Image/menu.png trong resources
            java.net.URL imgURL = getClass().getResource("/Image/menu.png");
            
            if (imgURL != null) {
                // Load image từ URL
                backgroundImage = ImageIO.read(imgURL);
                System.out.println("Menu background loaded successfully!");
            } else {
                // File không tồn tại
                System.out.println("Menu background not found: /Image/menu.png");
            }
        } catch (Exception e) {
            // Lỗi khi đọc file (IOException, etc.)
            System.out.println("Error loading menu background: " + e.getMessage());
            backgroundImage = null;
        }
    }
    
    // ============================================================
    // PUBLIC METHODS - Được gọi từ bên ngoài
    // ============================================================
    
    /**
     * Dừng nhạc menu
     * 
     * ĐƯỢC GỌI TỪ:
     * - LevelSelectionDialog: Khi chuyển sang custom level
     * - Play button action listener: Khi start game
     * 
     * WHY: Tránh menu music + game music chạy cùng lúc
     */
    public void stopMenuMusic() {
        audioManager.stopMenuMusic();
    }

    // ============================================================
    // MAIN - Entry point của application
    // ============================================================
    
    /**
     * Main method - Khởi động menu
     * 
     * FLOW:
     * 1. SwingUtilities.invokeLater() → Run trên EDT (Event Dispatch Thread)
     * 2. Tạo Menu instance
     * 3. setVisible(true) → Show menu window
     * 
     * WHY EDT: Swing không thread-safe, phải run UI code trên EDT
     * 
     * @param args Command line arguments (không dùng)
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Menu().setVisible(true));
    }
}

