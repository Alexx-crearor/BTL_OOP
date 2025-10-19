package com.arkanoid.game.ui;

import com.arkanoid.game.game.GamePanel;
import com.arkanoid.game.util.FontManager;

import java.awt.*;
import javax.swing.*;

/**
 * Menu chính - Chỉ có 1 game mode: Chơi theo level
 */
public class Menu extends JFrame {
    public Menu() {
        setTitle("ARKANOID");
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridBagLayout());
        getContentPane().setBackground(new Color(20, 20, 40));

        Font btnFont = FontManager.getFont(Font.BOLD, 24);
        Font titleFont = FontManager.getFont(Font.BOLD, 52);
        
        // Tiêu đề
        JLabel title = new JLabel("ARKANOID");
        title.setFont(titleFont);
        title.setForeground(new Color(255, 215, 0));
        
        // Nút Play
        JButton playButton = new JButton("PLAY");
        playButton.setFont(btnFont);
        playButton.setPreferredSize(new Dimension(280, 70));
        playButton.setBackground(new Color(0, 150, 0));
        playButton.setForeground(Color.WHITE);
        playButton.setFocusPainted(false);
        playButton.setBorderPainted(false);
        
        // Nút High Score
        JButton highScoreButton = new JButton("HIGH SCORE");
        highScoreButton.setFont(btnFont);
        highScoreButton.setPreferredSize(new Dimension(280, 70));
        highScoreButton.setBackground(new Color(218, 165, 32));
        highScoreButton.setForeground(Color.WHITE);
        highScoreButton.setFocusPainted(false);
        highScoreButton.setBorderPainted(false);
        
        // Nút Quit
        JButton quitButton = new JButton("QUIT");
        quitButton.setFont(btnFont);
        quitButton.setPreferredSize(new Dimension(280, 70));
        quitButton.setBackground(new Color(200, 50, 50));
        quitButton.setForeground(Color.WHITE);
        quitButton.setFocusPainted(false);
        quitButton.setBorderPainted(false);

        // Action listeners
        playButton.addActionListener(e -> {
            JFrame frame = new JFrame("Arkanoid");
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
        
        highScoreButton.addActionListener(e -> {
            new HighScorePanel().setVisible(true);
        });
        
        quitButton.addActionListener(e -> System.exit(0));

        // Layout
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.insets = new Insets(15, 20, 15, 20);
        
        gbc.gridy = 0;
        gbc.insets = new Insets(30, 20, 40, 20);
        add(title, gbc);
        
        gbc.gridy = 1;
        gbc.insets = new Insets(15, 20, 15, 20);
        add(playButton, gbc);
        
        gbc.gridy = 2;
        add(highScoreButton, gbc);
        
        gbc.gridy = 3;
        add(quitButton, gbc);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new Menu().setVisible(true);
            }
        });
    }
}
