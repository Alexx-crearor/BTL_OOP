package com.arkanoid.game.game;

import javax.swing.JFrame;

import com.arkanoid.game.ui.Menu;

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
