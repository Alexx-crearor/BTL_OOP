import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class ControlWindow extends JPanel implements Runnable, KeyListener {
    // Trạng thái phím
    public boolean movingLeft = false, movingRight = false;

    // Hằng số kích thước cửa sổ
    public static final int WIDTH = 800, HEIGHT = 600;

    // Paddle, bóng và gạch
    public final Item player = new Item();
    public final Item ball = new Item();
    public Item[] bricks;
    public int amount = 0;
    private Thread gameThread;
    public final java.util.Random random = new java.util.Random();
    public boolean gameOver = false;
    public boolean gameWon = false;
    public int score = 0;
    private boolean ballStarted = false; // Bóng chỉ di chuyển khi paddle di chuyển

    // Bricks
    public static final int BRICK_X = 10;
    public static final int BRICK_Y = 6;
    public static final int BRICK_WIDTH = 48;
    public static final int BRICK_HEIGHT = 20;


    // Khởi tạo game
    public ControlWindow() {
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);
        setup();
    }

    public void setup() {
        amount = 0;
        // Paddle
        player.width = 120;
        player.height = 20;
        player.x = (WIDTH - player.width) / 2;
        player.y = HEIGHT - player.height - 50;
    player.dx = 3;

        // Ball - bắt đầu trên paddle
        ball.width = 20;
        ball.height = 20;
        ball.x = (WIDTH - ball.width) / 2;
        ball.y = HEIGHT - player.height - 50 - ball.height; // Đặt trên paddle
        ball.dx = 0; // Chưa di chuyển
        ball.dy = 0;
        ballStarted = false;

        // Bricks
        bricks = new Item[BRICK_X * BRICK_Y];
        for (int i = 0; i < BRICK_X; i++) {
            for (int j = 0; j < BRICK_Y; j++) {
                bricks[amount] = new Item();
                bricks[amount].x = i * BRICK_WIDTH + 150;
                bricks[amount].y = j * BRICK_HEIGHT + 30;
                amount++;
            }
        }

        // Start game thread
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public void run() {
        final double FPS = 120.0;
        final double TIME_PER_FRAME = 1_000_000_000.0 / FPS;
        long lastTime = System.nanoTime();
        double delta = 0;
        while (true) {
            long now = System.nanoTime();
            delta += (now - lastTime) / TIME_PER_FRAME;
            lastTime = now;
            if (delta >= 1) {
                updateGame();
                repaint();
                delta--;
            }
            if (gameOver) break;
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (gameWon) {
            drawCenteredString(g, "You Win!", WIDTH, HEIGHT);
        }

        // Vẽ paddle
        g.setColor(Color.BLUE);
        g.fillRect(player.x, player.y, player.width, player.height);

        // Vẽ bóng
        g.setColor(Color.RED);
        g.fillOval(ball.x, ball.y, ball.width, ball.height);

        // Vẽ gạch
        for (int i = 0; i < amount; i++) {
            g.setColor(Color.GREEN);
            g.fillRect(bricks[i].x, bricks[i].y, BRICK_WIDTH, BRICK_HEIGHT);
            g.setColor(Color.BLACK);
            g.drawRect(bricks[i].x, bricks[i].y, BRICK_WIDTH + 1, BRICK_HEIGHT + 1);
        }
        // Vẽ điểm với font to
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString("Score: " + score, 10, 30);

        // Vẽ chữ Game Over nếu thua
        if (gameOver) {
            drawCenteredString(g, "Game Over", WIDTH, HEIGHT);
        }
        g.dispose();
    }

    private void drawCenteredString(Graphics g, String text, int width, int height) {
        Font font = new Font("Arial", Font.PLAIN, 30);
        g.setFont(font);
        FontMetrics fm = g.getFontMetrics();
        int x = (width - fm.stringWidth(text)) / 2;
        int y = (height - fm.getHeight()) / 2 + fm.getAscent();
        g.setColor(Color.WHITE);
        g.drawString(text, x, y);
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {
        if (gameOver) return;
        if (e.getKeyCode() == KeyEvent.VK_LEFT) movingLeft = true;
        if (e.getKeyCode() == KeyEvent.VK_RIGHT) movingRight = true;
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_LEFT) movingLeft = false;
        if (e.getKeyCode() == KeyEvent.VK_RIGHT) movingRight = false;
    }

    private void updateGame() {
        if (gameOver || gameWon) return;

        // Di chuyển paddle
        boolean paddleMoved = false;
        if (movingLeft) {
            player.x = Math.max(0, player.x - player.dx);
            paddleMoved = true;
        }
        if (movingRight) {
            player.x = Math.min(WIDTH - player.width, player.x + player.dx);
            paddleMoved = true;
        }
        
        // Bắt đầu di chuyển bóng khi paddle di chuyển
        if (paddleMoved && !ballStarted) {
            ballStarted = true;
            ball.dx = random.nextBoolean() ? -2 : 2; 
            ball.dy = -2;
        }
        
        // Nếu bóng chưa bắt đầu, giữ nó trên paddle
        if (!ballStarted) {
            ball.x = player.x + player.width / 2 - ball.width / 2;
            ball.y = player.y - ball.height;
            return; // Không xử lý va chạm nếu bóng chưa di chuyển
        }

        // Di chuyển bóng và kiểm tra va chạm
        updateBallPosition();
        
        // Va chạm với tường
        if (ball.x <= 0 || ball.x + ball.width >= WIDTH) ball.dx *= -1;
        if (ball.y <= 0) {
            ball.dy *= -1;
        }
        if (ball.y + ball.height >= HEIGHT) gameOver = true;
        
        // Va chạm paddle
        Rectangle ballRect = new Rectangle(ball.x, ball.y, ball.width, ball.height);
        Rectangle playerRect = new Rectangle(player.x, player.y, player.width, player.height);
        if (ballRect.intersects(playerRect)) {
            ball.dy = -Math.abs(ball.dy);
            ball.dx = random.nextInt(2) == 1 ? 2 : -2; // Tốc độ cố định ±2
        }
        
        // Kiểm tra thắng
        gameWon = java.util.Arrays.stream(bricks).limit(amount).noneMatch(b -> b.x >= 0);
    }
    
    private void updateBallPosition() {
        ball.x += ball.dx;
        ball.y += ball.dy;
        checkBrickCollision();
    }
    
    private void checkBrickCollision() {
        Rectangle ballRect = new Rectangle(ball.x, ball.y, ball.width, ball.height);
        for (int i = 0; i < amount; i++) {
            if (bricks[i].x >= 0 && ballRect.intersects(new Rectangle(bricks[i].x, bricks[i].y, BRICK_WIDTH, BRICK_HEIGHT))) {
                // Tính toán hướng va chạm
                int ballCenterX = ball.x + ball.width / 2;
                int ballCenterY = ball.y + ball.height / 2;
                int brickCenterX = bricks[i].x + BRICK_WIDTH / 2;
                int brickCenterY = bricks[i].y + BRICK_HEIGHT / 2;
                
                int dx = Math.abs(ballCenterX - brickCenterX);
                int dy = Math.abs(ballCenterY - brickCenterY);
                
                // Va chạm ngang hoặc dọc
                if (dx > dy) {
                    ball.dx *= -1;
                } else {
                    ball.dy *= -1;
                }
                
                bricks[i].x = -100;
                score++;
                break;
            }
        }
    }
}
