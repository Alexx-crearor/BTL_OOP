package com.arkanoid.game.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Quản lý high scores - đọc/ghi từ file
 */
public class HighScoreManager {
    private static final String HIGH_SCORE_FILE = "highscores.txt";
    private static final int MAX_HIGH_SCORES = 10;
    
    private List<HighScoreEntry> highScores;
    
    public HighScoreManager() {
        highScores = new ArrayList<>();
        loadHighScores();
    }
    
    /**
     * Đọc high scores từ file
     */
    private void loadHighScores() {
        Path filePath = Paths.get(HIGH_SCORE_FILE);
        
        if (!Files.exists(filePath)) {
            // File chưa tồn tại, bắt đầu với danh sách rỗng
            // Chỉ lưu khi có người chơi game over và nhập tên
            System.out.println("High score file not found. Starting with empty list.");
            return;
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(HIGH_SCORE_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                HighScoreEntry entry = HighScoreEntry.fromString(line);
                if (entry != null) {
                    highScores.add(entry);
                }
            }
            
            // Sắp xếp theo điểm giảm dần
            Collections.sort(highScores);
            
            System.out.println("Loaded " + highScores.size() + " high scores from file.");
            
        } catch (IOException e) {
            System.err.println("Error loading high scores: " + e.getMessage());
            // Lỗi đọc file → bắt đầu với danh sách rỗng
            highScores.clear();
        }
    }
    

    
    /**
     * Lưu high scores vào file
     */
    public void saveHighScores() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(HIGH_SCORE_FILE))) {
            for (HighScoreEntry entry : highScores) {
                writer.write(entry.toString());
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error saving high scores: " + e.getMessage());
        }
    }
    
    /**
     * Kiểm tra xem điểm có đủ vào top 10 không
     */
    public boolean isHighScore(int score) {
        if (highScores.size() < MAX_HIGH_SCORES) {
            return true; // Còn chỗ trống
        }
        
        // Kiểm tra xem điểm có cao hơn điểm thấp nhất không
        return score > highScores.get(highScores.size() - 1).getScore();
    }
    
    /**
     * Thêm high score mới
     */
    public void addHighScore(String playerName, int score) {
        // Giới hạn độ dài tên
        if (playerName == null || playerName.trim().isEmpty()) {
            playerName = "PLAYER";
        }
        playerName = playerName.trim().substring(0, Math.min(playerName.length(), 15));
        
        HighScoreEntry newEntry = new HighScoreEntry(playerName, score);
        highScores.add(newEntry);
        
        // Sắp xếp lại
        Collections.sort(highScores);
        
        // Giữ chỉ top 10
        if (highScores.size() > MAX_HIGH_SCORES) {
            highScores = highScores.subList(0, MAX_HIGH_SCORES);
        }
        
        // Lưu vào file
        saveHighScores();
    }
    
    /**
     * Lấy danh sách high scores
     */
    public List<HighScoreEntry> getHighScores() {
        return new ArrayList<>(highScores);
    }
    
    /**
     * Lấy vị trí của điểm trong bảng xếp hạng (1-based)
     * Trả về -1 nếu không vào top 10
     */
    public int getRank(int score) {
        for (int i = 0; i < highScores.size(); i++) {
            if (score > highScores.get(i).getScore()) {
                return i + 1;
            }
        }
        
        if (highScores.size() < MAX_HIGH_SCORES) {
            return highScores.size() + 1;
        }
        
        return -1; // Không vào top 10
    }
}
