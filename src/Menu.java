import java.awt.*;
import javax.swing.*;

public class Menu extends JFrame {
    public Menu() {
        setTitle("Arkanoid - Chọn chế độ");
        setSize(900, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridBagLayout());
        getContentPane().setBackground(new Color(20, 20, 40));

        Font btnFont = new Font("Arial", Font.BOLD, 28);
        Font titleFont = new Font("Arial", Font.BOLD, 48);
        
        // Tiêu đề
        JLabel title = new JLabel("ARKANOID");
        title.setFont(titleFont);
        title.setForeground(new Color(255, 215, 0));
        
        // Nút chơi game đầy đủ (với levels, power-ups, laser)
        JButton playAdvancedButton = new JButton("🎮 Chơi Game Đầy Đủ");
        playAdvancedButton.setFont(btnFont);
        playAdvancedButton.setPreferredSize(new Dimension(350, 80));
        playAdvancedButton.setBackground(new Color(0, 150, 0));
        playAdvancedButton.setForeground(Color.WHITE);
        playAdvancedButton.setFocusPainted(false);
        
        // Nút chơi game cơ bản
        JButton playBasicButton = new JButton("🎯 Chơi Game Cơ Bản");
        playBasicButton.setFont(new Font("Arial", Font.PLAIN, 24));
        playBasicButton.setPreferredSize(new Dimension(350, 60));
        playBasicButton.setBackground(new Color(70, 130, 180));
        playBasicButton.setForeground(Color.WHITE);
        playBasicButton.setFocusPainted(false);
        
        // Nút thoát
        JButton exitButton = new JButton("❌ Thoát");
        exitButton.setFont(new Font("Arial", Font.PLAIN, 24));
        exitButton.setPreferredSize(new Dimension(350, 60));
        exitButton.setBackground(new Color(200, 50, 50));
        exitButton.setForeground(Color.WHITE);
        exitButton.setFocusPainted(false);

        // Mô tả game đầy đủ
        JLabel advancedDesc = new JLabel("<html><center>✓ 5 Levels<br>✓ 9 Power-ups<br>✓ Laser Shooting<br>✓ Multiple Balls</center></html>");
        advancedDesc.setFont(new Font("Arial", Font.PLAIN, 14));
        advancedDesc.setForeground(new Color(200, 200, 200));

        // Mô tả game cơ bản
        JLabel basicDesc = new JLabel("<html><center>Phiên bản đơn giản<br>Chỉ paddle và gạch</center></html>");
        basicDesc.setFont(new Font("Arial", Font.PLAIN, 14));
        basicDesc.setForeground(new Color(180, 180, 180));

        playAdvancedButton.addActionListener(e -> {
            // Chạy game đầy đủ với tất cả tính năng
            JFrame frame = new JFrame("Arkanoid - Advanced Edition");
            GamePanel gamePanel = new GamePanel();
            frame.add(gamePanel);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setResizable(false);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
            gamePanel.requestFocusInWindow();
            dispose();
        });
        
        playBasicButton.addActionListener(e -> {
            // Chạy game cơ bản
            new Main();
            dispose();
        });
        
        exitButton.addActionListener(e -> System.exit(0));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.insets = new Insets(10, 10, 10, 10);
        
        // Thêm tiêu đề
        gbc.gridy = 0;
        gbc.insets = new Insets(20, 10, 30, 10);
        add(title, gbc);
        
        // Game đầy đủ
        gbc.gridy = 1;
        gbc.insets = new Insets(10, 10, 5, 10);
        add(playAdvancedButton, gbc);
        
        gbc.gridy = 2;
        gbc.insets = new Insets(0, 10, 20, 10);
        add(advancedDesc, gbc);
        
        // Game cơ bản
        gbc.gridy = 3;
        gbc.insets = new Insets(10, 10, 5, 10);
        add(playBasicButton, gbc);
        
        gbc.gridy = 4;
        gbc.insets = new Insets(0, 10, 20, 10);
        add(basicDesc, gbc);
        
        // Nút thoát
        gbc.gridy = 5;
        gbc.insets = new Insets(20, 10, 10, 10);
        add(exitButton, gbc);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new Menu().setVisible(true);
            }
        });
    }
}
