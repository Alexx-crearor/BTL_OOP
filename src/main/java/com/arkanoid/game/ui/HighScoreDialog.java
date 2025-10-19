package com.arkanoid.game.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.arkanoid.game.util.FontManager;

/**
 * Dialog nhập tên khi đạt high score
 */
public class HighScoreDialog extends JDialog {
    private String playerName = null;
    private JTextField nameField;
    private int score;
    private int rank;
    
    public HighScoreDialog(JFrame parent, int score, int rank) {
        super(parent, "CONGRATULATIONS!", true);
        this.score = score;
        this.rank = rank;
        
        initComponents();
        setSize(500, 300);
        setLocationRelativeTo(parent);
        setResizable(false);
        
        // Đảm bảo playerName = null khi đóng dialog bằng nút X
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                playerName = null; // Không lưu khi đóng bằng X
            }
        });
    }
    
    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(new Color(20, 20, 40));
        
        // Panel tiêu đề
        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(new Color(20, 20, 40));
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        
        JLabel congratsLabel = new JLabel("CONGRATULATIONS!");
        congratsLabel.setFont(FontManager.getFont(Font.BOLD, 32));
        congratsLabel.setForeground(new Color(255, 215, 0));
        congratsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel rankLabel = new JLabel("You ranked #" + rank + " on the leaderboard!");
        rankLabel.setFont(FontManager.getFont(Font.BOLD, 20));
        rankLabel.setForeground(new Color(255, 255, 100));
        rankLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel scoreLabel = new JLabel("Score: " + score);
        scoreLabel.setFont(FontManager.getFont(Font.PLAIN, 18));
        scoreLabel.setForeground(Color.WHITE);
        scoreLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        titlePanel.add(Box.createVerticalStrut(10));
        titlePanel.add(congratsLabel);
        titlePanel.add(Box.createVerticalStrut(10));
        titlePanel.add(rankLabel);
        titlePanel.add(Box.createVerticalStrut(5));
        titlePanel.add(scoreLabel);
        
        // Panel nhập tên
        JPanel inputPanel = new JPanel();
        inputPanel.setBackground(new Color(20, 20, 40));
        inputPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 20));
        
        JLabel nameLabel = new JLabel("Enter your name:");
        nameLabel.setFont(FontManager.getFont(Font.BOLD, 16));
        nameLabel.setForeground(Color.WHITE);
        
        nameField = new JTextField(15);
        nameField.setFont(FontManager.getFont(Font.PLAIN, 18));
        nameField.setHorizontalAlignment(JTextField.CENTER);
        nameField.addActionListener(e -> submitName());
        
        inputPanel.add(nameLabel);
        inputPanel.add(nameField);
        
        // Panel buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(new Color(20, 20, 40));
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 10));
        
        JButton okButton = new JButton("Save");
        okButton.setFont(FontManager.getFont(Font.BOLD, 18));
        okButton.setPreferredSize(new Dimension(120, 40));
        okButton.setBackground(new Color(0, 150, 0));
        okButton.setForeground(Color.WHITE);
        okButton.setFocusPainted(false);
        okButton.addActionListener(e -> submitName());
        
        JButton cancelButton = new JButton("Skip");
        cancelButton.setFont(FontManager.getFont(Font.PLAIN, 16));
        cancelButton.setPreferredSize(new Dimension(120, 40));
        cancelButton.setBackground(new Color(150, 150, 150));
        cancelButton.setForeground(Color.WHITE);
        cancelButton.setFocusPainted(false);
        cancelButton.addActionListener(e -> {
            playerName = null;
            dispose();
        });
        
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        
        add(titlePanel, BorderLayout.NORTH);
        add(inputPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void submitName() {
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Please enter your name!", 
                "Error", 
                JOptionPane.WARNING_MESSAGE);
            nameField.requestFocus();
            return;
        }
        
        playerName = name;
        dispose();
    }
    
    /**
     * Hiển thị dialog và trả về tên người chơi
     * @return tên người chơi hoặc null nếu cancel
     */
    public String getPlayerName() {
        setVisible(true); // Block cho đến khi dispose
        return playerName;
    }
}
