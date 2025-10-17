package com.arkanoid.game.game;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/**
 * Game Arkanoid - Phiên bản nâng cao
 * Bao gồm:
 * - Nhiều loại gạch với độ bền khác nhau
 * - Power-ups đa dạng
 * - 5 levels với patterns khác nhau
 * - Laser shooting
 * - Multiple balls
 * - Điều khiển mượt mà
 */
public class ArkanoidGame {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Arkanoid - Advanced Edition");
            GamePanel gamePanel = new GamePanel();
            
            frame.add(gamePanel);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setResizable(false);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
            
            gamePanel.requestFocusInWindow();
        });
    }
}

