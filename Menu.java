import java.awt.*;
import javax.swing.*;

public class Menu extends JFrame {
    public Menu() {
        setTitle("Arkanoid Menu");
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridBagLayout());

        Font btnFont = new Font("Arial", Font.BOLD, 32);
        JButton playButton = new JButton("Chơi");
        JButton exitButton = new JButton("Thoát");
        playButton.setFont(btnFont);
        exitButton.setFont(btnFont);
        playButton.setPreferredSize(new Dimension(250, 80));
        exitButton.setPreferredSize(new Dimension(250, 80));

        playButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                new Main();
                dispose();
            }
        });
        exitButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                System.exit(0);
            }
        });

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(30, 10, 30, 10);
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(playButton, gbc);
        gbc.gridy = 1;
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
