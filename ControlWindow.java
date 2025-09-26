import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Random;
import javax.swing.*;


class ControlWindow extends JPanel implements Runnable, KeyListener {
    private Item player;
    private Item ball;
    private Thread gameThread;
    private java.util.Random random = new Random();
    private boolean gameOver = false;



    private void setup(){
        gameThread = new Thread(this);
        gameThread.start();

    player = new Item();
        player.width = 120;
        player.height = 20;
        player.x = (WIDTH - player.width) / 2;
        player.y = HEIGHT - player.height - 50;
        player.dx = 15;

    ball = new Item();
        ball.width = 20;
        ball.height = 20;
        ball.x = (WIDTH - ball.width) / 2;
        ball.y = (HEIGHT - ball.height) / 2;
        ball.dx = -2;
        ball.dy = 2;
    }



    @Override
    public void run() {
        System.out.println("Game thread is running");
        long time1 = System.nanoTime();
        long time2;
        double delta = 0.0;
        double ticks = 60.0;
        double secs = 1_000_000_000.0 / ticks;
        while(gameThread != null) {
            time2 = System.nanoTime();
            delta += (time2 - time1) / secs;
            time1 = time2;
            if(delta >= 0) {
                repaint();
                delta--;
                logic();
            }
        }
    }

    public static final int WIDTH = 800;
    public static final int HEIGHT = 600;

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.BLUE);
        g.fillRect(player.x, player.y, player.width, player.height);

        g.setColor(Color.RED);
        g.fillOval(ball.x, ball.y, ball.width, ball.height);

        if(gameOver){
            g.setColor(Color.WHITE);
            Font font = new Font("Arial", Font.PLAIN, 30);
            g.setFont(font);
            String msg = "Game Over";
            FontMetrics fm = g.getFontMetrics(font);
            int textWidth = fm.stringWidth(msg);
            int textHeight = fm.getHeight();
            int x = (WIDTH - textWidth) / 2;
            int y = (HEIGHT - textHeight) / 2 + fm.getAscent();
            g.drawString(msg, x, y);
        }
        g.dispose();
    }

    public ControlWindow() {
        this.setDoubleBuffered(true);
        this.setBackground(Color.BLACK);
        this.setFocusable(true);
        this.addKeyListener(this);
        setup();
    }
    @Override
    public void keyTyped(KeyEvent e) {}


    @Override
    public void keyPressed(KeyEvent e) {
        if(!gameOver){
            if(e.getKeyCode() == KeyEvent.VK_LEFT) {
                player.x -= player.dx - 5;
            } else if(e.getKeyCode() == KeyEvent.VK_RIGHT) {
                player.x += player.dx + 5;
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        
    }

    private void logic(){
        if(!gameOver){
            ball.x += ball.dx;
            ball.y += ball.dy;

            if(ball.x <= 0 || ball.x + ball.width >= WIDTH) {
                ball.dx *=-1;
            }
            if(ball.y <= 0) {
                ball.dy *=-1;
            }

            if(ball.y + ball.height >= HEIGHT) {
                gameOver = true;
            }

            if(new Rectangle(ball.x, ball.y, ball.width, ball.height).intersects(new Rectangle(player.x, player.y, player.width, player.height))) {
                ball.dy = -Math.abs(ball.dy);
                ball.dx= random.nextInt(5-2) - 2;
            }
        }
    }
}