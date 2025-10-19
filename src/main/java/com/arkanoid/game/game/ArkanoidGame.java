package com.arkanoid.game.game;

import javax.swing.SwingUtilities;

import com.arkanoid.game.ui.Menu;

/**
 * Game Arkanoid - Main Entry Point
 * 1 Game mode duy nhất: Chơi theo level (5 levels)
 * 
 * Features:
 * - 5 levels với patterns khác nhau
 * - Nhiều loại gạch với độ bền khác nhau
 * - 9 Power-ups đa dạng
 * - Laser shooting
 * - Multiple balls
 * - High score system
 */
public class ArkanoidGame {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Khởi động menu chính
            new Menu().setVisible(true);
        });
    }
}

