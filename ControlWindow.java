import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

public class ControlWindow extends JPanel implements Runnable, KeyListener {
    // Game state
    private boolean movingLeft = false, movingRight = false;
    private Item player;
    private List<Item> balls;
    private Item[] bricks;
    private Thread gameThread;
    private boolean gameOver = false;
    private boolean gameWon = false;
    private int score = 0;
    private int amount = 0;
    private BufferedImage backgroundImage;

    // Game dimensions from config
    private static final int BRICK_X = GameConfig.BRICK_COLS;
    private static final int BRICK_Y = GameConfig.BRICK_ROWS;
    private static final int BRICK_WIDTH = GameConfig.BRICK_WIDTH;
    private static final int BRICK_HEIGHT = GameConfig.BRICK_HEIGHT;
    private static final int PADDLE_WIDTH = GameConfig.PADDLE_WIDTH;
    private static final int PADDLE_HEIGHT = GameConfig.PADDLE_HEIGHT;
    private static final int BALL_SIZE = GameConfig.BALL_SIZE;
    private static final int PADDLE_SPEED = 5;
    private static final int BALL_SPEED = 3;


    // Khởi tạo game
    public ControlWindow() {
        setBackground(Color.BLACK);
        setFocusable(true);
        ImageLoader.loadImages(); // Load images
        initGame();
    }

    private void initGame() {
        addKeyListener(this);
        amount = BRICK_X * BRICK_Y;
        
        // Initialize paddle
        player = new Item();
        player.width = PADDLE_WIDTH;
        player.height = PADDLE_HEIGHT;
        player.x = (GameConfig.WIDTH - PADDLE_WIDTH) / 2;
        player.y = GameConfig.HEIGHT - PADDLE_HEIGHT - 50;
        player.dx = PADDLE_SPEED;

        // Initialize ball
        balls = new ArrayList<>();
        Item ball = new Item();
        ball.width = ball.height = BALL_SIZE;
        ball.x = (GameConfig.WIDTH - BALL_SIZE) / 2;
        ball.y = (GameConfig.HEIGHT - BALL_SIZE) / 2;
        ball.dx = -BALL_SPEED;
        ball.dy = BALL_SPEED;
        balls.add(ball);

        // Initialize bricks
        bricks = new Item[amount];
        for (int i = 0; i < BRICK_X; i++) {
            for (int j = 0; j < BRICK_Y; j++) {
                int index = i * BRICK_Y + j;
                bricks[index] = new Item();
                bricks[index].x = i * BRICK_WIDTH + GameConfig.BRICK_OFFSET_X;
                bricks[index].y = j * BRICK_HEIGHT + GameConfig.BRICK_OFFSET_Y;
            }
        }

        // Reset game state
        score = 0;
        gameOver = false;
        gameWon = false;

        // Start game thread
        gameThread = new Thread(this);
        gameThread.start();
    }



    @Override
    public void run() {
        final double TIME_PER_FRAME = 1_000_000_000.0 / GameConfig.FPS;
        long lastTime = System.nanoTime();
        double delta = 0;

        while (!gameOver && !gameWon) {
            long now = System.nanoTime();
            delta += (now - lastTime) / TIME_PER_FRAME;
            lastTime = now;

            if (delta >= 1) {
                updateGame();
                repaint();
                delta--;
            }
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        // Draw background
        BufferedImage background = ImageLoader.getImage("background");
        if (background != null) {
            g.drawImage(background, 0, 0, GameConfig.WIDTH, GameConfig.HEIGHT, null);
        }
        
        // Draw paddle with image
        BufferedImage paddleImg = ImageLoader.getImage("paddle");
        if (paddleImg != null) {
            g.drawImage(paddleImg, player.x, player.y, player.width, player.height, null);
        } else {
            g.setColor(Color.BLUE);
            g.fillRect(player.x, player.y, player.width, player.height);
        }
        
        // Draw balls (as circles since no ball image available)
        g.setColor(Color.WHITE);
        for (Item ball : balls) {
            g.fillOval(ball.x, ball.y, ball.width, ball.height);
            g.setColor(Color.RED);
            g.drawOval(ball.x, ball.y, ball.width, ball.height);
            g.setColor(Color.WHITE);
        }
        
        // Draw bricks with different colored images
        for (int i = 0; i < BRICK_X; i++) {
            for (int j = 0; j < BRICK_Y; j++) {
                int index = i * BRICK_Y + j;
                if (bricks[index].x >= 0) {  // Only draw active bricks
                    // Select brick image based on row
                    BufferedImage brickImg = switch (j % 8) {
                        case 0 -> ImageLoader.getImage("brick_red");
                        case 1 -> ImageLoader.getImage("brick_orange");
                        case 2 -> ImageLoader.getImage("brick_gold");
                        case 3 -> ImageLoader.getImage("brick_green");
                        case 4 -> ImageLoader.getImage("brick_cyan");
                        case 5 -> ImageLoader.getImage("brick_blue");
                        case 6 -> ImageLoader.getImage("brick_lightblue");
                        default -> ImageLoader.getImage("brick_silver");
                    };
                    
                    if (brickImg != null) {
                        g.drawImage(brickImg, bricks[index].x, bricks[index].y, BRICK_WIDTH, BRICK_HEIGHT, null);
                    } else {
                        // Fallback to colored rectangles if image is not available
                        g.setColor(switch (j % 6) {
                            case 0 -> Color.RED;
                            case 1 -> Color.ORANGE;
                            case 2 -> Color.YELLOW;
                            case 3 -> Color.GREEN;
                            case 4 -> Color.BLUE;
                            default -> Color.MAGENTA;
                        });
                        g.fillRect(bricks[index].x, bricks[index].y, BRICK_WIDTH, BRICK_HEIGHT);
                        g.setColor(Color.WHITE);
                        g.drawRect(bricks[index].x, bricks[index].y, BRICK_WIDTH, BRICK_HEIGHT);
                    }
                }
            }
        }
        
        // Draw score
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString("Score: " + score, 10, 30);
        
        // Draw game state messages
        if (gameWon) {
            drawCenteredString(g, "You Win!", GameConfig.WIDTH, GameConfig.HEIGHT);
        } else if (gameOver) {
            drawCenteredString(g, "Game Over", GameConfig.WIDTH, GameConfig.HEIGHT);
        }
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
        if (gameOver || gameWon) {
            if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                initGame();
            }
            return;
        }

        if (e.getKeyCode() == KeyEvent.VK_LEFT) {
            movingLeft = true;
        } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
            movingRight = true;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_LEFT) {
            movingLeft = false;
        } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
            movingRight = false;
        }
    }

    private void updateGame() {
        if (gameOver || gameWon) return;

            // Update paddle position
        if (movingLeft) {
            player.x = Math.max(0, player.x - player.dx);
        }
        if (movingRight) {
            player.x = Math.min(GameConfig.WIDTH - player.width, player.x + player.dx);
        }

        // Update balls
        List<Item> newBalls = new ArrayList<>();
        for (Item ball : balls) {
            // Move ball
            ball.x += ball.dx;
            ball.y += ball.dy;

            Rectangle ballRect = new Rectangle(ball.x, ball.y, ball.width, ball.height);

            // Check brick collisions
            for (int i = 0; i < amount; i++) {
                if (bricks[i].x >= 0) {
                    Rectangle brickRect = new Rectangle(bricks[i].x, bricks[i].y, BRICK_WIDTH, BRICK_HEIGHT);
                    if (ballRect.intersects(brickRect)) {
                        bricks[i].x = -100; // Deactivate brick
                        score += 100;
                        ball.dy *= -1;
                        break;
                    }
                }
            }

            // Check wall collisions
            if (ball.x <= 0 || ball.x + ball.width >= GameConfig.WIDTH) {
                ball.dx *= -1;
            }
            if (ball.y <= 0) {
                ball.dy *= -1;
            }

            // Check paddle collision
            Rectangle paddleRect = new Rectangle(player.x, player.y, player.width, player.height);
            if (ballRect.intersects(paddleRect)) {
                ball.dy = -Math.abs(ball.dy); // Always bounce up
                // Adjust ball direction based on where it hits the paddle
                double relativeIntersectX = (player.x + (player.width / 2.0)) - (ball.x + (ball.width / 2.0));
                double normalizedRelativeIntersectX = relativeIntersectX / (player.width / 2.0);
                ball.dx = (int)(-normalizedRelativeIntersectX * BALL_SPEED); // Adjust horizontal speed
            }

            // Keep ball only if it's still in play
            if (ball.y + ball.height < GameConfig.HEIGHT) {
                newBalls.add(ball);
            }
        }
        balls = newBalls;
        
        // Check game over condition - if all balls are lost
        if (balls.isEmpty()) {
            gameOver = true;
        }

        // Check win condition
        boolean allBricksGone = true;
        for (int i = 0; i < amount; i++) {
            if (bricks[i].x >= 0) {
                allBricksGone = false;
                break;
            }
        }
        if (allBricksGone) {
            gameWon = true;
        }
    }
}
