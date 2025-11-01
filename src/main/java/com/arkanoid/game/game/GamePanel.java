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

import com.arkanoid.game.entity.Ball;
import com.arkanoid.game.entity.Brick;
import com.arkanoid.game.entity.Laser;
import com.arkanoid.game.entity.Paddle;
import com.arkanoid.game.entity.PowerUp;
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
    
    public GamePanel() {
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
        currentLevel = new Level(level);
        bricks = currentLevel.getBricks();
        levelNumber = level;
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
}

