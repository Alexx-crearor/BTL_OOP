import javax.swing.*;

/**
 * Main class cho game Arkanoid phiên bản CƠ BẢN
 * Chỉ có paddle, ball và bricks - không có power-ups, laser, hay levels
 * 
 * Để chơi game ĐẦY ĐỦ với tất cả tính năng, chạy ArkanoidGame.java
 */
public class Main extends JFrame {
    private ControlWindow cw = new ControlWindow();
    
    public Main(){
        this.setTitle("Arkanoid - Basic Edition");
        this.setSize(ControlWindow.WIDTH, ControlWindow.HEIGHT);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);
        this.setResizable(false);
        this.setVisible(true);
        this.add(cw);
    }

    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            new Menu().setVisible(true);
        });
    }
}
