package com.arkanoid.game.util;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

// ============================================================
// CLASS: HighScoreEntry - Entry cho high score
// ============================================================

/**
 * Class HighScoreEntry - Đại diện cho 1 entry trong bảng xếp hạng
 * 
 * THUỘC TÍNH:
 * - playerName: Tên người chơi (max 15 ký tự)
 * - score: Điểm số đạt được
 * - date: Thời gian đạt được (format: yyyy-MM-dd HH:mm)
 * 
 * INTERFACE IMPLEMENTATIONS:
 * 1. Serializable: Có thể serialize để lưu vào file
 *    - Cần cho save/load game state
 *    - serialVersionUID = 1L để đảm bảo tương thích
 * 
 * 2. Comparable<HighScoreEntry>: Có thể sắp xếp
 *    - compareTo(): Sắp xếp giảm dần theo score
 *    - Dùng cho Collections.sort(highScores)
 * 
 * FILE FORMAT (highscores.txt):
 * ```
 * PLAYER1|5000|2024-01-15 14:30
 * PLAYER2|4500|2024-01-15 14:25
 * PLAYER3|4000|2024-01-15 14:20
 * ```
 * - Delimiter: | (pipe character)
 * - 3 fields: name|score|date
 * 
 * CÁCH SỬ DỤNG:
 * ```java
 * // Tạo entry mới (auto timestamp)
 * HighScoreEntry entry = new HighScoreEntry("PLAYER", 5000);
 * 
 * // Parse từ file
 * HighScoreEntry entry = HighScoreEntry.fromString("PLAYER|5000|2024-01-15 14:30");
 * 
 * // Save to file
 * String line = entry.toString(); // "PLAYER|5000|2024-01-15 14:30"
 * ```
 * 
 * DESIGN PATTERN: Value Object (Immutable data holder)
 * 
 * @author Arkanoid Game Team
 * @version 1.0
 */
public class HighScoreEntry implements Serializable, Comparable<HighScoreEntry> {
    
    // ============================================================
    // HẰNG SỐ - Serialization
    // ============================================================
    
    /**
     * Serial version UID cho Serializable interface
     * 
     * MỤC ĐÍCH:
     * - Đảm bảo tương thích khi deserialize từ file cũ
     * - Nếu thay đổi class structure, cần tăng version
     * - JVM dùng để verify object khi deserialize
     */
    private static final long serialVersionUID = 1L;
    
    // ============================================================
    // THUỘC TÍNH - Player Data
    // ============================================================
    
    /**
     * Tên người chơi
     * - Max length: 15 ký tự (validated bởi HighScoreManager)
     * - Default: "PLAYER" nếu không nhập
     * - Không được null hoặc empty
     */
    private String playerName;
    
    /**
     * Điểm số đạt được
     * - Tích lũy từ phá gạch, đánh boss
     * - Dùng để sắp xếp high scores (giảm dần)
     * - Phải > 0 để được lưu
     */
    private int score;
    
    /**
     * Thời gian đạt được điểm (timestamp)
     * - Format: "yyyy-MM-dd HH:mm" (ví dụ: "2024-01-15 14:30")
     * - Auto generate khi tạo entry mới
     * - Có thể empty string nếu load từ file cũ
     */
    private String date;
    
    // ============================================================
    // CONSTRUCTOR - Tạo Entry Mới (Auto Timestamp)
    // ============================================================
    
    /**
     * Constructor tạo entry mới với timestamp hiện tại
     * 
     * CÁCH HOẠT ĐỘNG:
     * - Lưu playerName và score
     * - Tự động tạo timestamp hiện tại với format "yyyy-MM-dd HH:mm"
     * 
     * SỬ DỤNG:
     * - Khi người chơi game over và submit high score
     * - Timestamp được tạo tự động
     * 
     * @param playerName Tên người chơi (đã validated)
     * @param score Điểm số đạt được
     */
    public HighScoreEntry(String playerName, int score) {
        this.playerName = playerName;
        this.score = score;
        
        // Tạo timestamp hiện tại với format "yyyy-MM-dd HH:mm"
        this.date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }
    
    // ============================================================
    // CONSTRUCTOR - Load từ File (With Timestamp)
    // ============================================================
    
    /**
     * Constructor để load entry từ file (với timestamp có sẵn)
     * 
     * CÁCH HOẠT ĐỘNG:
     * - Lưu playerName, score, và date từ parameters
     * - Không tạo timestamp mới
     * 
     * SỬ DỤNG:
     * - Khi parse entry từ highscores.txt
     * - Dùng bởi fromString() method
     * 
     * @param playerName Tên người chơi
     * @param score Điểm số
     * @param date Timestamp (format: yyyy-MM-dd HH:mm)
     */
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
