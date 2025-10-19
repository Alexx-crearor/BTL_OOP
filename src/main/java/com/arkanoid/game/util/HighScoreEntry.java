package com.arkanoid.game.util;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Entry cho high score - lưu tên người chơi và điểm số
 */
public class HighScoreEntry implements Serializable, Comparable<HighScoreEntry> {
    private static final long serialVersionUID = 1L;
    
    private String playerName;
    private int score;
    private String date;
    
    public HighScoreEntry(String playerName, int score) {
        this.playerName = playerName;
        this.score = score;
        this.date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }
    
    // Constructor cho việc đọc từ file
    public HighScoreEntry(String playerName, int score, String date) {
        this.playerName = playerName;
        this.score = score;
        this.date = date;
    }
    
    public String getPlayerName() {
        return playerName;
    }
    
    public int getScore() {
        return score;
    }
    
    public String getDate() {
        return date;
    }
    
    @Override
    public int compareTo(HighScoreEntry other) {
        // Sắp xếp giảm dần theo điểm
        return Integer.compare(other.score, this.score);
    }
    
    @Override
    public String toString() {
        return playerName + "|" + score + "|" + date;
    }
    
    public static HighScoreEntry fromString(String line) {
        String[] parts = line.split("\\|");
        if (parts.length >= 2) {
            String name = parts[0];
            int score = Integer.parseInt(parts[1]);
            String date = parts.length >= 3 ? parts[2] : "";
            return new HighScoreEntry(name, score, date);
        }
        return null;
    }
}
