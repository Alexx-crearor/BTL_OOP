package com.arkanoid.game.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

import com.arkanoid.game.util.FontManager;
import com.arkanoid.game.util.HighScoreEntry;
import com.arkanoid.game.util.HighScoreManager;

/**
 * Panel hiển thị bảng xếp hạng high scores
 */
public class HighScorePanel extends JFrame {
    private HighScoreManager highScoreManager;
    
    public HighScorePanel() {
        highScoreManager = new HighScoreManager();
        
        setTitle("HIGH SCORES");
        setSize(700, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        
        initComponents();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(new Color(20, 20, 40));
        
        // Header
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(20, 20, 40));
        
        JLabel titleLabel = new JLabel("TOP 10 HIGH SCORES");
        titleLabel.setFont(FontManager.getFont(Font.BOLD, 36));
        titleLabel.setForeground(new Color(255, 215, 0));
        headerPanel.add(titleLabel);
        
        // Main panel với danh sách scores
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(new Color(20, 20, 40));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
        
        List<HighScoreEntry> scores = highScoreManager.getHighScores();
        
        // Header của bảng
        JPanel tableHeader = createTableHeader();
        mainPanel.add(tableHeader);
        mainPanel.add(Box.createVerticalStrut(10));
        
        // Các dòng scores
        for (int i = 0; i < scores.size(); i++) {
            HighScoreEntry entry = scores.get(i);
            JPanel row = createScoreRow(i + 1, entry);
            mainPanel.add(row);
            mainPanel.add(Box.createVerticalStrut(5));
        }
        
        // Scroll pane
        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        // Button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(new Color(20, 20, 40));
        
        JButton closeButton = new JButton("Back to Menu");
        closeButton.setFont(FontManager.getFont(Font.BOLD, 20));
        closeButton.setPreferredSize(new Dimension(250, 50));
        closeButton.setBackground(new Color(70, 130, 180));
        closeButton.setForeground(Color.WHITE);
        closeButton.setFocusPainted(false);
        closeButton.addActionListener(e -> dispose());
        
        buttonPanel.add(closeButton);
        
        add(headerPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createTableHeader() {
        JPanel header = new JPanel();
        header.setLayout(new GridLayout(1, 4, 10, 0));
        header.setBackground(new Color(30, 30, 50));
        header.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        
        String[] columns = {"#", "NAME", "SCORE", "DATE"};
        int[] widths = {50, 200, 150, 200};
        
        for (int i = 0; i < columns.length; i++) {
            JLabel label = new JLabel(columns[i], SwingConstants.CENTER);
            label.setFont(FontManager.getFont(Font.BOLD, 16));
            label.setForeground(new Color(255, 215, 0));
            label.setPreferredSize(new Dimension(widths[i], 30));
            header.add(label);
        }
        
        return header;
    }
    
    private JPanel createScoreRow(int rank, HighScoreEntry entry) {
        JPanel row = new JPanel();
        row.setLayout(new GridLayout(1, 4, 10, 0));
        
        // Màu sắc theo hạng
        Color bgColor;
        Color fgColor;
        if (rank == 1) {
            bgColor = new Color(255, 215, 0, 50); // Vàng
            fgColor = new Color(255, 215, 0);
        } else if (rank == 2) {
            bgColor = new Color(192, 192, 192, 50); // Bạc
            fgColor = new Color(220, 220, 220);
        } else if (rank == 3) {
            bgColor = new Color(205, 127, 50, 50); // Đồng
            fgColor = new Color(205, 127, 50);
        } else {
            bgColor = new Color(40, 40, 60);
            fgColor = Color.WHITE;
        }
        
        row.setBackground(bgColor);
        row.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        
        // Rank
        String rankText = rank <= 3 ? getMedal(rank) + " " + rank : String.valueOf(rank);
        JLabel rankLabel = new JLabel(rankText, SwingConstants.CENTER);
        rankLabel.setFont(FontManager.getFont(Font.BOLD, 18));
        rankLabel.setForeground(fgColor);
        
        // Name
        JLabel nameLabel = new JLabel(entry.getPlayerName(), SwingConstants.LEFT);
        nameLabel.setFont(FontManager.getFont(Font.BOLD, 18));
        nameLabel.setForeground(fgColor);
        
        // Score
        JLabel scoreLabel = new JLabel(String.format("%,d", entry.getScore()), SwingConstants.CENTER);
        scoreLabel.setFont(new Font("Courier New", Font.BOLD, 18));
        scoreLabel.setForeground(fgColor);
        
        // Date
        JLabel dateLabel = new JLabel(entry.getDate(), SwingConstants.CENTER);
        dateLabel.setFont(FontManager.getFont(Font.PLAIN, 14));
        dateLabel.setForeground(new Color(180, 180, 180));
        
        row.add(rankLabel);
        row.add(nameLabel);
        row.add(scoreLabel);
        row.add(dateLabel);
        
        return row;
    }
    
    private String getMedal(int rank) {
        switch (rank) {
            case 1: return "#1";
            case 2: return "#2";
            case 3: return "#3";
            default: return "";
        }
    }
}
