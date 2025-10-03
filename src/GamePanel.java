import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.*;

public class GamePanel extends JPanel implements Runnable, KeyListener {
    // Hằng số
    public static final int WIDTH = 800;
    public static final int HEIGHT = 600;
    private static final int FPS = 120;
    
    // Game objects
    private Paddle paddle;
    private ArrayList<Ball> balls;
    private ArrayList<Brick> bricks;
    private ArrayList<PowerUp> powerUps;
    private ArrayList<Laser> lasers;
    
    // Game state
    private Level currentLevel;
    private int levelNumber = 1;
    private int lives = 3;
    private int score = 0;
    private boolean gameOver = false;
    private boolean gameWon = false;
    private boolean paused = false;
    private boolean ballCaught = false;
    private Ball caughtBall = null;
    
    // Controls
    private boolean movingLeft = false;
    private boolean movingRight = false;
    private boolean spacePressed = false;
    
    // Thread
    private Thread gameThread;
    private Random random = new Random();
    
    public GamePanel() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);
        
        initGame();
    }
    
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
        
        // Start game thread
        gameThread = new Thread(this);
        gameThread.start();
    }
    
    private void loadLevel(int level) {
        currentLevel = new Level(level);
        bricks = currentLevel.getBricks();
        levelNumber = level;
    }
    
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
    
    private void update() {
        updatePaddle();
        updateBalls();
        updatePowerUps();
        updateLasers();
        checkLevelComplete();
    }
    
    private void updatePaddle() {
        paddle.update(WIDTH);
        boolean paddleMoved = false;
        
        if (movingLeft) {
            paddle.moveLeft(WIDTH);
            paddleMoved = true;
        }
        if (movingRight) {
            paddle.moveRight(WIDTH);
            paddleMoved = true;
        }
        
        // Nếu paddle di chuyển, bắt đầu di chuyển bóng
        if (paddleMoved) {
            for (Ball ball : balls) {
                if (!ball.isMoving()) {
                    ball.startMoving();
                }
            }
        }
    }
    
    private void updateBalls() {
        for (int i = balls.size() - 1; i >= 0; i--) {
            Ball ball = balls.get(i);
            
            if (ballCaught && ball == caughtBall) {
                ball.setPosition(paddle.getCenterX() - ball.width / 2, paddle.y - ball.height);
                if (spacePressed) {
                    releaseBall(ball);
                }
                continue;
            }
            
            // Nếu bóng chưa di chuyển, giữ nó ở trên paddle
            if (!ball.isMoving()) {
                ball.setPosition(paddle.getCenterX() - ball.width / 2, paddle.y - ball.height);
            } else {
                ball.update();
                checkBallCollisions(ball);
            }
            
            if (ball.y > HEIGHT) balls.remove(i);
        }
        
        if (balls.isEmpty()) {
            lives--;
            if (lives <= 0) {
                gameOver = true;
            } else {
                resetBall();
            }
        }
    }
    
    private void releaseBall(Ball ball) {
        ballCaught = false;
        caughtBall = null;
        ball.setVelocity(random.nextInt(3) - 1, -2); // Giảm tốc độ từ -3 xuống -2
        ball.startMoving();
        spacePressed = false;
    }
    
    private void updatePowerUps() {
        powerUps.removeIf(p -> {
            p.update();
            if (p.getBounds().intersects(paddle.getBounds())) {
                applyPowerUp(p);
                return true;
            }
            return p.isOffScreen(HEIGHT);
        });
    }
    
    private void updateLasers() {
        lasers.removeIf(laser -> {
            laser.update();
            for (Brick brick : bricks) {
                if (!brick.isDestroyed && laser.getBounds().intersects(brick.getBounds())) {
                    if (brick.hit()) {
                        score += getScoreForBrick(brick);
                        checkPowerUpDrop(brick);
                    }
                    laser.setActive(false);
                    break;
                }
            }
            return !laser.isActive() || laser.isOffScreen();
        });
    }
    
    private void checkLevelComplete() {
        if (currentLevel.isCompleted()) {
            levelNumber++;
            if (levelNumber > 5) {
                gameWon = gameOver = true;
            } else {
                loadLevel(levelNumber);
                resetBall();
            }
        }
    }
    
    private void checkBallCollisions(Ball ball) {
        Rectangle ballRect = ball.getBounds();
        
        // Va chạm với tường
        if (ball.x <= 0 || ball.x + ball.width >= WIDTH) {
            ball.reverseX();
        }
        if (ball.y <= 0) {
            ball.reverseY();
        }
        
        // Va chạm với paddle
        if (ballRect.intersects(paddle.getBounds())) {
            ball.reverseY();
            // Thêm hiệu ứng góc dựa vào vị trí chạm
            int paddleCenter = paddle.x + paddle.width / 2;
            int ballCenter = ball.x + ball.width / 2;
            int diff = ballCenter - paddleCenter;
            ball.dx = diff / 15; // Điều chỉnh góc
        }
        
        // Va chạm với gạch - CHỈ PHÁ 1 GẠCH
        boolean hitBrick = false;
        for (Brick brick : bricks) {
            if (!brick.isDestroyed && ballRect.intersects(brick.getBounds())) {
                if (!ball.isIncandescent()) {
                    // Xác định hướng va chạm
                    Rectangle intersection = ballRect.intersection(brick.getBounds());
                    if (intersection.width < intersection.height) {
                        ball.reverseX();
                    } else {
                        ball.reverseY();
                    }
                }
                
                if (brick.hit()) {
                    score += getScoreForBrick(brick);
                    checkPowerUpDrop(brick);
                }
                
                // CHỈ PHÁ 1 GẠCH rồi thoát (trừ khi có Incandescent)
                if (!ball.isIncandescent()) {
                    break;
                }
                
                // Với Incandescent, vẫn chỉ cho phá tối đa 3 gạch mỗi lần update
                hitBrick = true;
                if (hitBrick) {
                    break; // Chỉ xử lý 1 gạch mỗi frame
                }
            }
        }
    }
    
    private void checkPowerUpDrop(Brick brick) {
        if (brick.canDropPowerUp() && random.nextInt(100) < 30) { // 30% chance
            PowerUp.PowerUpType[] types = PowerUp.PowerUpType.values();
            PowerUp.PowerUpType type = types[random.nextInt(types.length)];
            PowerUp powerUp = new PowerUp(brick.x + brick.width / 2 - 20, brick.y, type);
            powerUps.add(powerUp);
            brick.setDroppedPowerUp();
        }
    }
    
    private void applyPowerUp(PowerUp powerUp) {
        switch (powerUp.getType()) {
            case ENLARGE: paddle.activateEnlarge(600); break;
            case REDUCE: paddle.activateReduce(600); break;
            case LASER: paddle.activateLaser(600); break;
            case SLOW: balls.forEach(b -> b.setSpeed(2)); break;
            case CATCH: 
                ballCaught = true;
                if (!balls.isEmpty()) caughtBall = balls.get(0);
                break;
            case TWIN: addTwinBall(); break;
            case DISRUPT: addMultipleBalls(2); break;
            case MEGABALL: balls.forEach(b -> b.activateMegaBall(600)); break;
            case INCANDESCENCE: balls.forEach(b -> b.activateIncandescent(600)); break;
        }
    }
    
    private void addTwinBall() {
        if (!balls.isEmpty()) {
            Ball orig = balls.get(0);
            Ball twin = new Ball(orig.x, orig.y, orig.width);
            twin.setVelocity(-orig.dx, orig.dy);
            twin.startMoving(); // Bắt đầu di chuyển ngay
            balls.add(twin);
        }
    }
    
    private void addMultipleBalls(int count) {
        if (!balls.isEmpty()) {
            Ball orig = balls.get(0);
            for (int i = 0; i < count; i++) {
                Ball newBall = new Ball(orig.x, orig.y, orig.width);
                int angle = random.nextInt(360);
                newBall.setVelocity((int)(2 * Math.cos(Math.toRadians(angle))),  // Giảm từ 3 xuống 2
                                   (int)(2 * Math.sin(Math.toRadians(angle))));
                newBall.startMoving(); // Bắt đầu di chuyển ngay
                balls.add(newBall);
            }
        }
    }
    
    private int getScoreForBrick(Brick brick) {
        int[] scores = {10, 20, 30, 40, 50, 60, 100, 150, 50};
        int idx = brick.getType().ordinal();
        return idx < scores.length ? scores[idx] : 50;
    }
    
    private void resetBall() {
        balls.clear();
        Ball newBall = new Ball(WIDTH / 2 - 10, paddle.y - 25, 20);
        // Bóng mới sẽ không di chuyển cho đến khi paddle di chuyển
        balls.add(newBall);
        ballCaught = false;
        caughtBall = null;
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Vẽ bricks
        for (Brick brick : bricks) {
            brick.draw(g2d);
        }
        
        // Vẽ paddle
        paddle.draw(g2d);
        
        // Vẽ balls
        for (Ball ball : balls) {
            ball.draw(g2d);
        }
        
        // Vẽ power-ups
        for (PowerUp powerUp : powerUps) {
            powerUp.draw(g2d);
        }
        
        // Vẽ lasers
        for (Laser laser : lasers) {
            laser.draw(g2d);
        }
        
        // Vẽ UI
        drawUI(g2d);
        
        // Vẽ game over/won
        if (gameOver) {
            drawGameOver(g2d);
        }
        
        g2d.dispose();
    }
    
    private void drawUI(Graphics g) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 18));
        String[] info = {"Score: " + score, "Lives: " + lives, "Level: " + levelNumber};
        for (int i = 0; i < info.length; i++) {
            g.drawString(info[i], 10, 25 + i * 25);
        }
        g.drawString("Bricks: " + currentLevel.getRemainingBricks(), WIDTH - 120, 25);
        
        if (paused) drawCenteredText(g, "PAUSED", 40);
    }
    
    private void drawGameOver(Graphics g) {
        g.setColor(new Color(0, 0, 0, 180));
        g.fillRect(0, 0, WIDTH, HEIGHT);
        
        g.setColor(Color.WHITE);
        drawCenteredText(g, gameWon ? "YOU WIN!" : "GAME OVER", 50, -50);
        drawCenteredText(g, "Final Score: " + score, 30, 20);
        drawCenteredText(g, "Press ENTER to restart or ESC to exit", 20, 70);
    }
    
    private void drawCenteredText(Graphics g, String text, int fontSize) {
        drawCenteredText(g, text, fontSize, 0);
    }
    
    private void drawCenteredText(Graphics g, String text, int fontSize, int yOffset) {
        g.setFont(new Font("Arial", fontSize > 35 ? Font.BOLD : Font.PLAIN, fontSize));
        FontMetrics fm = g.getFontMetrics();
        int x = (WIDTH - fm.stringWidth(text)) / 2;
        int y = HEIGHT / 2 + yOffset;
        g.drawString(text, x, y);
    }
    
    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        
        if (key == KeyEvent.VK_LEFT) movingLeft = true;
        if (key == KeyEvent.VK_RIGHT) movingRight = true;
        if (key == KeyEvent.VK_SPACE) {
            spacePressed = true;
            if (paddle.hasLaser()) {
                fireLaser();
            }
        }
        if (key == KeyEvent.VK_P) paused = !paused;
        
        if (gameOver) {
            if (key == KeyEvent.VK_ENTER) {
                restartGame();
            } else if (key == KeyEvent.VK_ESCAPE) {
                System.exit(0);
            }
        }
    }
    
    @Override
    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_LEFT) movingLeft = false;
        if (key == KeyEvent.VK_RIGHT) movingRight = false;
        if (key == KeyEvent.VK_SPACE) spacePressed = false;
    }
    
    @Override
    public void keyTyped(KeyEvent e) {}
    
    private void fireLaser() {
        lasers.add(new Laser(paddle.x + 10, paddle.y));
        lasers.add(new Laser(paddle.x + paddle.width - 14, paddle.y));
    }
    
    private void restartGame() {
        gameOver = false;
        gameWon = false;
        score = 0;
        lives = 3;
        levelNumber = 1;
        balls.clear();
        powerUps.clear();
        lasers.clear();
        initGame();
    }
}
