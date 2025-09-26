import java.awt.*;
import java.awt.event.*;
import javax.swing.*;


public class ControlWindow extends JPanel implements Runnable, KeyListener {

    // Hằng số kích thước cửa sổ
    public static final int WIDTH = 800, HEIGHT = 600;

    // Paddle và bóng
    private final Item player = new Item();
    private final Item ball = new Item();
    private Thread gameThread;
    private final java.util.Random random = new java.util.Random();
    private boolean gameOver = false;



    private void setup() {

        // Paddle
        player.width = 120;
        player.height = 20;
        player.x = (WIDTH - player.width) / 2;
        player.y = HEIGHT - player.height - 50;
        player.dx = 15;

        // Ball
        ball.width = 20;
        ball.height = 20;
        ball.x = (WIDTH - ball.width) / 2;
        ball.y = (HEIGHT - ball.height) / 2;
        ball.dx = -2;
        ball.dy = 2;

        // Start game thread
        gameThread = new Thread(this);
        gameThread.start();
    }



    @Override
    public void run() {
        final double FPS = 60.0;
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

        // Vẽ paddle
        g.setColor(Color.BLUE);
        g.fillRect(player.x, player.y, player.width, player.height);

        // Vẽ bóng
        g.setColor(Color.RED);
        g.fillOval(ball.x, ball.y, ball.width, ball.height);

        // Vẽ chữ Game Over nếu thua
        if (gameOver) {
            drawCenteredString(g, "Game Over", WIDTH, HEIGHT);
        }
        g.dispose();
    }

    // Hàm vẽ chữ căn giữa
    private void drawCenteredString(Graphics g, String text, int width, int height) {
        Font font = new Font("Arial", Font.PLAIN, 30);
        g.setFont(font);
        FontMetrics fm = g.getFontMetrics();
        int x = (width - fm.stringWidth(text)) / 2;
        int y = (height - fm.getHeight()) / 2 + fm.getAscent();
        g.setColor(Color.WHITE);
        g.drawString(text, x, y);
    }

    public ControlWindow() {
        setDoubleBuffered(true);
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);
        setup();
    }
    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {
        if (gameOver) return;
        if (e.getKeyCode() == KeyEvent.VK_LEFT)
            player.x = Math.max(0, player.x - player.dx);
        else if (e.getKeyCode() == KeyEvent.VK_RIGHT)
            player.x = Math.min(WIDTH - player.width, player.x + player.dx);
    }

    @Override
    public void keyReleased(KeyEvent e) {}

    // Cập nhật trạng thái game
    private void updateGame() {
        if (gameOver) return;

        // Di chuyển bóng
        ball.x += ball.dx;
        ball.y += ball.dy;

        // Va chạm tường
        if (ball.x <= 0 || ball.x + ball.width >= WIDTH) ball.dx *= -1;
        if (ball.y <= 0) ball.dy *= -1;

        // Game over nếu bóng rơi xuống dưới
        if (ball.y + ball.height >= HEIGHT) gameOver = true;
        
        // Va chạm paddle
        Rectangle ballRect = new Rectangle(ball.x, ball.y, ball.width, ball.height);
        Rectangle playerRect = new Rectangle(player.x, player.y, player.width, player.height);
        if (ballRect.intersects(playerRect)) {
            ball.dy = -Math.abs(ball.dy);
            ball.dx = random.nextInt(3) - 1; // -1, 0, 1
        }
    }
}
