package com.arkanoid.game.game;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import com.arkanoid.game.entity.*;
import com.arkanoid.game.util.GameStateManager.GameState;
import com.arkanoid.game.util.HighScoreManager;

// ============================================================
// CLASS: GamePanel - Panel chính chứa game logic Arkanoid
// ============================================================

/**
 * Class GamePanel - Panel chính điều phối toàn bộ game Arkanoid
 * 
 * KIẾN TRÚC: Delegation Pattern (Ủy quyền)
 * GamePanel đóng vai trò là "Coordinator" (Điều phối viên), ủy quyền
 * các công việc cụ thể cho các helper class chuyên biệt:
 * 
 * ┌──────────────────────────────────────────────────────┐
 * │              GAMEPANEL (Coordinator)                  │
 * │  - Chứa game state (paddle, balls, bricks, score...) │
 * │  - Điều phối game loop (120 FPS)                      │
 * │  - Ủy quyền xử lý cho helper classes                 │
 * └────────────┬─────────────────────────────────────────┘
 *              │
 *      ┌───────┴───────────────────────┬──────────────┬──────────────┬─────────────┐
 *      ▼                               ▼              ▼              ▼             ▼
 * ┌──────────┐  ┌────────────┐  ┌──────────┐  ┌──────────────┐  ┌──────────┐
 * │ Renderer │  │  Updater   │  │  Input   │  │StateHandler  │  │  Audio   │
 * │          │  │            │  │ Handler  │  │              │  │ Manager  │
 * │ Vẽ đồ họa│  │Update logic│  │Xử lý phím│  │Save/Load game│  │Nhạc nền  │
 * └──────────┘  └────────────┘  └──────────┘  └──────────────┘  └──────────┘
 * 
 * HELPER CLASSES (Lớp Trợ Giúp):
 * 1. GameRenderer: Vẽ tất cả đồ họa (background, entities, UI, HUD)
 * 2. GameUpdater: Cập nhật logic (collision, power-up, boss, win/lose)
 * 3. InputHandler: Xử lý keyboard input (move, space, pause, menu)
 * 4. GameStateHandler: Save/load game, high score submission
 * 5. AudioManager: Quản lý nhạc nền (load, play, pause, volume)
 * 
 * GAME LOOP (Vòng lặp game):
 * - FPS: 120 khung hình/giây
 * - Thread: Game chạy trên thread riêng (implements Runnable)
 * - Logic: Fixed timestep với delta accumulation
 *   + Mỗi frame: update() logic → repaint() graphics
 *   + Khi pause: chỉ skip update(), vẫn repaint để hiện pause menu
 * 
 * GAME ENTITIES (Các thực thể game):
 * - Paddle: 1 đối tượng (player control)
 * - Balls: ArrayList (có thể có nhiều bóng qua power-up TWIN)
 * - Bricks: ArrayList (gạch của level, có thể regenerate)
 * - PowerUps: ArrayList (rơi từ gạch bị phá)
 * - Lasers: ArrayList (bắn từ paddle khi có power-up LASER)
 * - Boss: 1 đối tượng (chỉ ở level 5)
 * - Projectiles: ArrayList (đạn của boss)
 * - ShieldBricks: ArrayList (khiên của boss)
 * 
 * GAME STATE (Trạng thái game):
 * - levelNumber: Level hiện tại (1-5)
 * - lives: Số mạng còn lại (3 ban đầu)
 * - score: Điểm số hiện tại
 * - gameOver: Game kết thúc (thua)
 * - gameWon: Game chiến thắng (đánh bại boss)
 * - paused: Game tạm dừng
 * - showPauseMenu: Hiện menu pause (Resume/Quit)
 * - gameStarted: Đã bắt đầu chơi (đã nhấn Space lần đầu)
 * 
 * DESIGN PATTERN:
 * - Delegation: Ủy quyền xử lý cho helper classes
 * - Observer: KeyListener để nhận input
 * - Thread: Runnable để chạy game loop độc lập
 * - Package-private fields: Cho phép helper classes truy cập trực tiếp
 * 
 * @author Arkanoid Game Team
 * @version 1.0
 */
public class GamePanel extends JPanel implements Runnable, KeyListener {
    
    // ============================================================
    // HẰNG SỐ - Cấu hình cửa sổ và FPS
    // ============================================================
    
    /**
     * Chiều rộng cửa sổ game (pixels)
     * Được sử dụng để:
     * - Xác định kích thước panel
     * - Tính toán vị trí center (WIDTH/2)
     * - Giới hạn di chuyển paddle/ball/boss
     */
    public static final int WIDTH = 800;
    
    /**
     * Chiều cao cửa sổ game (pixels)
     * Được sử dụng để:
     * - Xác định kích thước panel
     * - Tính toán vị trí đặt paddle (HEIGHT - 70)
     * - Kiểm tra ball rơi ra ngoài màn hình
     */
    public static final int HEIGHT = 600;
    
    /**
     * Số khung hình trên giây (Frames Per Second)
     * - 120 FPS: Game chạy mượt, collision detection chính xác
     * - Higher FPS: Cải thiện response time và smooth animation
     */
    private static final int FPS = 120;
    
    // ============================================================
    // THUỘC TÍNH - Background Image
    // ============================================================
    
    /**
     * Ảnh nền của game (backgroundLevel1.png)
     * - Load từ resources/Image/ khi khởi tạo
     * - Được vẽ đầu tiên trước tất cả entities
     * - null nếu load thất bại (fallback về màu đen)
     */
    private BufferedImage backgroundImage;
    
    // ============================================================
    // THUỘC TÍNH - Manager Classes (Các lớp quản lý)
    // ============================================================
    
    /**
     * AudioManager - Quản lý nhạc nền của game
     * 
     * CHỨC NĂNG:
     * - Load và phát nhạc cho từng level (level1Music.wav -> level5Music.wav)
     * - Pause/resume nhạc khi game pause/resume
     * - Điều chỉnh volume theo Menu.globalVolume
     * - Stop nhạc khi quit to menu
     */
    private AudioManager audioManager;
    
    /**
     * GameRenderer - Lớp phụ trách vẽ đồ họa
     * 
     * CHỨC NĂNG:
     * - Vẽ background, entities (paddle, balls, bricks, powerups...)
     * - Vẽ HUD (score, lives, level number)
     * - Vẽ pause menu với volume slider
     * - Vẽ game over / game won screen
     * - Vẽ boss health bar (level 5)
     */
    private GameRenderer renderer;
    
    /**
     * GameUpdater - Lớp phụ trách update logic
     * 
     * CHỨC NĂNG:
     * - Cập nhật vị trí entities (move paddle, update ball, powerups...)
     * - Kiểm tra va chạm (ball-brick, ball-paddle, ball-wall, laser-brick...)
     * - Xử lý power-up effects (apply to paddle/ball)
     * - Xử lý boss logic (attack, shield, take damage)
     * - Kiểm tra win/lose condition
     */
    private GameUpdater updater;
    
    /**
     * InputHandler - Lớp phụ trách xử lý input
     * 
     * CHỨC NĂNG:
     * - Xử lý phím mũi tên (di chuyển paddle)
     * - Xử lý Space (start ball, shoot laser, release caught ball)
     * - Xử lý Esc (pause/unpause, navigate pause menu)
     * - Xử lý phím số (cheat codes nếu có)
     * - Xử lý Enter (restart game sau khi game over)
     */
    private InputHandler inputHandler;
    
    /**
     * GameStateHandler - Lớp phụ trách save/load
     * 
     * CHỨC NĂNG:
     * - Save game state vào file (level, score, lives, entities...)
     * - Load game state từ file
     * - Kiểm tra và submit high score khi game over
     * - Quit to menu (cleanup và return về menu chính)
     */
    private GameStateHandler stateHandler;
    
    // ============================================================
    // THUỘC TÍNH - Game Entities (Package-private cho helper classes)
    // ============================================================
    
    /**
     * Paddle (Thanh đỡ) - Do người chơi điều khiển
     * 
     * CHI TIẾT:
     * - Vị trí: Gần đáy màn hình (HEIGHT - 70)
     * - Di chuyển: Left/Right arrows
     * - Power-ups: Enlarge (to hơn), Reduce (nhỏ hơn), Laser (bắn laser)
     * - Chức năng: Đỡ bóng, bắt bóng (CATCH), bắn laser
     */
    Paddle paddle;
    
    /**
     * Balls - Danh sách các bóng đang hoạt động
     * 
     * CHI TIẾT:
     * - Ban đầu: 1 bóng đặt trên paddle
     * - Power-up TWIN: Tạo thêm 2 bóng mới
     * - Power-up DISRUPT: Tách 1 bóng thành 3 bóng
     * - Mất mạng: Khi tất cả bóng rơi ra ngoài màn hình
     * - Power-ups: Mega Ball (xuyên gạch), Incandescent (xuyên gạch không phá)
     */
    ArrayList<Ball> balls;
    
    /**
     * Bricks - Danh sách các gạch của level hiện tại
     * 
     * CHI TIẾT:
     * - Load từ Level class (generateLevel1-5)
     * - Các loại: RED, ORANGE, GREEN, CYAN, BLUE, LIGHT_BLUE, GOLD, SILVER, REGENERATING
     * - Regenerating brick: Biến mất 10 giây rồi xuất hiện lại
     * - Win condition: Phá hết tất cả gạch (trừ REGENERATING và level 5 boss)
     */
    ArrayList<Brick> bricks;
    
    /**
     * PowerUps - Danh sách power-ups đang rơi xuống
     * 
     * CHI TIẾT:
     * - Rơi từ gạch bị phá (random 20% chance)
     * - 9 loại: ENLARGE, REDUCE, LASER, SLOW, CATCH, TWIN, DISRUPT, MEGABALL, INCANDESCENCE
     * - Thu thập: Va chạm với paddle
     * - Tự động xóa: Khi rơi ra ngoài màn hình
     */
    ArrayList<PowerUp> powerUps;
    
    /**
     * Lasers - Danh sách laser bắn từ paddle
     * 
     * CHI TIẾT:
     * - Được tạo khi: Paddle có power-up LASER và nhấn Space
     * - Mỗi lần bắn: 2 laser (từ 2 đầu paddle)
     * - Di chuyển: Thẳng lên trên (dy = -10)
     * - Va chạm: Phá gạch khi chạm
     * - Tự động xóa: Khi ra ngoài màn hình
     */
    ArrayList<Laser> lasers;
    
    // ============================================================
    // THUỘC TÍNH - Game State Variables (Biến trạng thái game)
    // ============================================================
    
    /**
     * currentLevel - Level object hiện tại
     * Chứa pattern gạch và logic tạo level
     */
    Level currentLevel;
    
    /** levelNumber - Số level hiện tại (1-5) */
    int levelNumber = 1;
    
    /** lives - Số mạng còn lại (3 ban đầu, mất mạng khi ball rơi ra ngoài) */
    int lives = 3;
    
    /** score - Điểm số tích lũy (phá gạch, đánh boss) */
    int score = 0;
    
    /** gameOver - true khi hết mạng hoặc user quit */
    boolean gameOver = false;
    
    /** gameWon - true khi đánh bại boss (level 5) */
    boolean gameWon = false;
    
    /** paused - true khi nhấn Esc (tạm dừng game) */
    boolean paused = false;
    
    /** showPauseMenu - true khi hiển thị pause menu với 2 option (Resume/Quit) */
    boolean showPauseMenu = false;
    
    /** pauseMenuSelection - Lựa chọn trong pause menu (0 = Resume, 1 = Quit to Menu) */
    int pauseMenuSelection = 0;
    
    /** ballCaught - true khi ball bị paddle bắt (power-up CATCH) */
    boolean ballCaught = false;
    
    /** caughtBall - Tham chiếu đến ball đang bị bắt (để release khi nhấn Space) */
    Ball caughtBall = null;
    
    /** highScoreSubmitted - Đánh dấu đã submit high score (tránh submit nhiều lần) */
    boolean highScoreSubmitted = false;
    
    /** gameStarted - true khi đã nhấn Space lần đầu (ẩn "PRESS SPACE TO START") */
    boolean gameStarted = false;
    
    // ============================================================
    // THUỘC TÍNH - High Score Manager
    // ============================================================
    
    /**
     * highScoreManager - Quản lý bảng xếp hạng
     * 
     * CHỨC NĂNG:
     * - Load high scores từ highscores.txt
     * - Save high scores sau khi submit
     * - Check xem score hiện tại có vào top 10 không
     */
    private HighScoreManager highScoreManager;
    
    // ============================================================
    // THUỘC TÍNH - Control Flags (Biến điều khiển)
    // ============================================================
    
    /** movingLeft - true khi đang giữ phím Left Arrow */
    boolean movingLeft = false;
    
    /** movingRight - true khi đang giữ phím Right Arrow */
    boolean movingRight = false;
    
    /** spacePressed - true khi đang giữ phím Space */
    boolean spacePressed = false;
    
    // ============================================================
    // THUỘC TÍNH - Thread
    // ============================================================
    
    /**
     * gameThread - Thread chạy game loop
     * Game loop chạy độc lập với UI thread để đảm bảo FPS ổn định
     */
    private Thread gameThread;
    
    // ============================================================
    // THUỘC TÍNH - Boss Fight (Level 5)
    // ============================================================
    
    /**
     * boss - Đối tượng trùm cuối game
     * 
     * CHI TIẾT:
     * - Chỉ tồn tại ở level 5 (khi hết gạch)
     * - HP: 25, attack interval: 3s, shield interval: 10s
     * - Win condition: Giảm HP về 0
     */
    Boss boss;
    
    /**
     * projectiles - Danh sách đạn do boss bắn ra
     * 
     * CHI TIẾT:
     * - Boss bắn mỗi 3 giây
     * - Di chuyển thẳng xuống
     * - Va chạm với paddle: Mất 1 mạng
     */
    ArrayList<Projectile> projectiles;
    
    /**
     * shieldBricks - Danh sách gạch khiên bảo vệ boss
     * 
     * CHI TIẾT:
     * - Boss tạo khiên mỗi 10 giây
     * - Bao quanh boss
     * - Phải phá khiên trước mới đánh boss
     */
    ArrayList<Brick> shieldBricks;
    
    public GamePanel() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);
        
        // Thêm mouse listener để xử lý volume slider
        addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                handleVolumeSliderClick(e.getX(), e.getY());
            }
        });
        
        addMouseMotionListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseDragged(java.awt.event.MouseEvent e) {
                handleVolumeSliderClick(e.getX(), e.getY());
            }
        });
        
        // Khởi tạo managers
        audioManager = new AudioManager();
        highScoreManager = new HighScoreManager();
        loadBackground();
        
        // Khởi tạo helpers sau khi load background
        renderer = new GameRenderer(WIDTH, HEIGHT, backgroundImage);
        updater = new GameUpdater(this);
        inputHandler = new InputHandler(this);
        stateHandler = new GameStateHandler(this, highScoreManager);
        
        initGame();
    }
    
    /**
     * Constructor để load game đã lưu
     */
    public GamePanel(GameState savedState) {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);
        
        // Khởi tạo managers
        audioManager = new AudioManager();
        highScoreManager = new HighScoreManager();
        loadBackground();
        
        // Khởi tạo helpers sau khi load background
        renderer = new GameRenderer(WIDTH, HEIGHT, backgroundImage);
        updater = new GameUpdater(this);
        inputHandler = new InputHandler(this);
        stateHandler = new GameStateHandler(this, highScoreManager);
        
        loadGameFromState(savedState);
    }
    
    /**
     * Load background image từ resources
     */
    private void loadBackground() {
        try {
            // Load từ classpath resources
            java.net.URL imgURL = getClass().getResource("/Image/backgroundLevel1.png");
            if (imgURL != null) {
                backgroundImage = ImageIO.read(imgURL);
                System.out.println("Background loaded successfully!");
            } else {
                System.out.println("Resource not found: /Image/backgroundLevel1.png");
            }
        } catch (Exception e) {
            System.out.println("Error loading background: " + e.getMessage());
            backgroundImage = null;
        }
    }
    
    /**
     * Khởi tạo game mới: tạo paddle, ball, load level, phát nhạc
     */
    private void initGame() {
        // Khởi tạo paddle
        paddle = new Paddle(WIDTH / 2 - 60, HEIGHT - 70, WIDTH, HEIGHT);
        
        // Khởi tạo balls
        balls = new ArrayList<>();
        Ball ball = new Ball(WIDTH / 2 - 10, paddle.y - 25, 20); // Đặt bóng lên trên paddle
        balls.add(ball);
        
        // Khởi tạo collections
        powerUps = new ArrayList<>();
        lasers = new ArrayList<>();
        projectiles = new ArrayList<>();
        shieldBricks = new ArrayList<>();
        
        // Load level
        loadLevel(levelNumber);
        
        // Load và phát nhạc nền
        audioManager.loadAndPlayLevelMusic(levelNumber);
        
        // Start game thread
        gameThread = new Thread(this);
        gameThread.start();
    }
    
    /**
     * Load level mới với các bricks tương ứng
     * @param level Số level (1-5)
     */
    public void loadLevel(int level) {
        // Bước 1: Luôn tải level mới và cập nhật các thông tin cơ bản.
        this.currentLevel = new Level(level);
        this.levelNumber = level;
        this.bricks = currentLevel.getBricks(); // Lấy danh sách gạch của level mới.

        // Bước 2: Dọn dẹp trạng thái của màn trùm cũ (nếu có).
        this.boss = null; // QUAN TRỌNG: Xóa trùm cũ để đảm bảo không có lỗi.
        if (this.shieldBricks != null) this.shieldBricks.clear();
        if (this.projectiles != null) this.projectiles.clear();

        // Bước 3: Dựa trên level MỚI, quyết định có tạo trùm hay không.
        // Nếu là màn 5 VÀ level mới không có gạch, thì tạo trùm.
        if (this.levelNumber == 5 && this.bricks.isEmpty()) {
            System.out.println("Màn 5 không có gạch -> Đã đến lúc tạo trùm!");
            this.boss = new Boss(WIDTH, HEIGHT);
        }
    }
    
    /**
     * Game loop chính - chạy ở 120 FPS
     */
    @Override
    public void run() {
        long lastTime = System.nanoTime();
        double nsPerTick = 1_000_000_000.0 / FPS;
        double delta = 0;
        
        while (!gameOver) {
            long now = System.nanoTime();
            delta += (now - lastTime) / nsPerTick;
            lastTime = now;
            
            if (delta >= 1) {
                if (!paused) {
                    update();
                }
                repaint();
                delta--;
            }
        }
    }
    
    /**
     * Update game state - delegate sang GameUpdater
     */
    private void update() {
        updater.update();
    }
    
    /**
     * Reset ball về vị trí ban đầu khi mất mạng
     */
    public void resetBall() {
        balls.clear();
        Ball newBall = new Ball(WIDTH / 2 - 10, paddle.y - 25, 20);
        // Bóng mới sẽ không di chuyển cho đến khi bấm space
        balls.add(newBall);
        ballCaught = false;
        caughtBall = null;
        gameStarted = false; // Reset trạng thái để hiện lại thông báo "PRESS SPACE TO START"
    }
    
    /**
     * Vẽ game - delegate sang GameRenderer
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Sử dụng GameRenderer để vẽ tất cả graphics
        renderer.render(g, this);
    }
    
    /**
     * Xử lý phím nhấn - delegate sang InputHandler
     */
    @Override
    public void keyPressed(KeyEvent e) {
        inputHandler.handleKeyPressed(e);
    }
    
    /**
     * Xử lý phím thả - delegate sang InputHandler
     */
    @Override
    public void keyReleased(KeyEvent e) {
        inputHandler.handleKeyReleased(e);
    }
    
    @Override
    public void keyTyped(KeyEvent e) {}
    
    /**
     * Restart game - reset tất cả về trạng thái ban đầu
     */
    void restartGame() {
        gameOver = false;
        gameWon = false;
        score = 0;
        lives = 3;
        levelNumber = 1;
        highScoreSubmitted = false;
        balls.clear();
        powerUps.clear();
        lasers.clear();
        initGame();
    }
    
    /**
     * Quit to menu - lưu game và quay về menu chính
     */
    void quitToMenu() {
        stateHandler.quitToMenu(audioManager);
    }
    
    /**
     * Xử lý click chuột lên thanh volume slider trong pause menu
     */
    private void handleVolumeSliderClick(int mouseX, int mouseY) {
        // Chỉ xử lý khi đang ở pause menu
        if (!showPauseMenu) {
            return;
        }
        
        // Vị trí thanh slider (phải giống với drawVolumeSlider trong GameRenderer)
        int sliderWidth = 300;
        int sliderHeight = 20;
        int sliderX = (WIDTH - sliderWidth) / 2;
        int sliderY = HEIGHT - 100;
        
        // Kiểm tra xem chuột có click vào vùng slider không (mở rộng vùng click)
        if (mouseX >= sliderX && mouseX <= sliderX + sliderWidth &&
            mouseY >= sliderY - 5 && mouseY <= sliderY + sliderHeight + 15) {
            
            // Tính toán volume mới dựa vào vị trí click
            float newVolume = (float)(mouseX - sliderX) / sliderWidth;
            newVolume = Math.max(0.0f, Math.min(1.0f, newVolume)); // Giới hạn 0-1
            
            // Cập nhật volume
            com.arkanoid.game.ui.Menu.globalVolume = newVolume;
            audioManager.updateVolume();
        }
    }
    
    /**
     * Load game từ saved state - delegate sang GameStateHandler
     */
    private void loadGameFromState(GameState state) {
        stateHandler.loadGameFromState(state);
        
        // Load và phát nhạc nền
        audioManager.loadAndPlayLevelMusic(levelNumber);
        
        // Start game thread
        gameThread = new Thread(this);
        gameThread.start();
    }
    
    /**
     * Kiểm tra và submit high score nếu đủ điểm
     */
    void checkAndSubmitHighScore() {
        stateHandler.checkAndSubmitHighScore();
    }
    
    /**
     * Cập nhật volume cho nhạc game theo Menu.globalVolume
     */
    public void updateVolume() {
        if (audioManager != null) {
            audioManager.updateVolume();
        }
    }
    
    /**
     * Lấy AudioManager để truy cập từ bên ngoài
     */
    public AudioManager getAudioManager() {
        return audioManager;
    }

    /**
     * Cung cấp quyền truy cập công khai vào đối tượng trùm.
     * @return Đối tượng Boss hoặc null nếu chưa được tạo.
     */
    public Boss getBoss() {
        return this.boss;
    }

    /**
     * Cung cấp quyền truy cập công khai vào danh sách đạn của trùm.
     * @return ArrayList chứa các Projectile.
     */
    public ArrayList<Projectile> getProjectiles() {
        return this.projectiles;
    }

    /**
     * Cung cấp quyền truy cập công khai vào danh sách khiên của trùm.
     * @return ArrayList chứa các Brick khiên.
     */
    public ArrayList<Brick> getShieldBricks() {
        return this.shieldBricks;
    }
}

