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

/**
 * GamePanel - Panel chính chứa logic game Arkanoid
 * Được chia nhỏ với các helper class:
 * - GameRenderer: Xử lý vẽ đồ họa
 * - GameUpdater: Xử lý update logic
 * - InputHandler: Xử lý keyboard input
 * - GameStateHandler: Xử lý save/load game
 * - AudioManager: Quản lý âm thanh
 */
public class GamePanel extends JPanel implements Runnable, KeyListener {
    // Hằng số
    public static final int WIDTH = 800;
    public static final int HEIGHT = 600;
    private static final int FPS = 120;
    
    // Background
    private BufferedImage backgroundImage;
    
    // Managers & Helpers
    private AudioManager audioManager;
    private GameRenderer renderer;
    private GameUpdater updater;
    private InputHandler inputHandler;
    private GameStateHandler stateHandler;
    
    // Game objects - package-private để các class helper có thể truy cập
    Paddle paddle;
    ArrayList<Ball> balls;
    ArrayList<Brick> bricks;
    ArrayList<PowerUp> powerUps;
    ArrayList<Laser> lasers;
    
    // Game state - package-private
    Level currentLevel;
    int levelNumber = 1;
    int lives = 3;
    int score = 0;
    boolean gameOver = false;
    boolean gameWon = false;
    boolean paused = false;
    boolean showPauseMenu = false;
    int pauseMenuSelection = 0; // 0 = Resume, 1 = Quit to Menu
    boolean ballCaught = false;
    Ball caughtBall = null;
    boolean highScoreSubmitted = false; // Đánh dấu đã submit high score
    boolean gameStarted = false; // Trạng thái game đã bắt đầu chưa
    
    // High score manager
    private HighScoreManager highScoreManager;
    
    // Controls
    boolean movingLeft = false;
    boolean movingRight = false;
    boolean spacePressed = false;
    
    // Thread
    private Thread gameThread;

    /** Đối tượng trùm cuối của game. Sẽ được tạo ở màn 5. */
    Boss boss;

    /** Danh sách các viên đạn mà trùm bắn ra. */
    ArrayList<Projectile> projectiles;

    /** Danh sách riêng cho các viên gạch khiên của trùm. */
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

