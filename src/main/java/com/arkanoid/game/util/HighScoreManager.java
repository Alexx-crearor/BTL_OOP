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

// ============================================================
// CLASS: HighScoreManager - Quản lý bảng xếp hạng
// ============================================================

/**
 * Class HighScoreManager - Quản lý bảng xếp hạng điểm cao
 * 
 * CHỨC NĂNG CHÍNH:
 * - Load/Save high scores từ/vào file "highscores.txt"
 * - Duy trì top 10 điểm cao nhất
 * - Sắp xếp tự động theo điểm giảm dần
 * - Validate tên người chơi và điểm số
 * - Kiểm tra xem điểm có vào top 10 không
 * 
 * FILE FORMAT (highscores.txt):
 * ```
 * PLAYER1|5000|2024-01-15 14:30
 * PLAYER2|4500|2024-01-15 14:25
 * PLAYER3|4000|2024-01-15 14:20
 * ...
 * ```
 * 
 * VALIDATION RULES:
 * - Tên người chơi: Max 15 ký tự, trim whitespace
 * - Tên rỗng/null: Thay bằng "PLAYER"
 * - Điểm số: Phải >= 0
 * - Top 10: Chỉ giữ 10 điểm cao nhất
 * 
 * CÁCH SỬ DỤNG:
 * ```java
 * HighScoreManager manager = new HighScoreManager();
 * 
 * // Kiểm tra điểm có đủ vào top 10 không
 * if (manager.isHighScore(5000)) {
 *     // Thêm vào bảng xếp hạng
 *     manager.addHighScore("PLAYER", 5000);
 * }
 * 
 * // Lấy danh sách top 10
 * List<HighScoreEntry> topScores = manager.getHighScores();
 * ```
 * 
 * THREAD SAFETY:
 * - Không thread-safe (nên dùng trong single thread)
 * - File I/O có thể throw IOException
 * 
 * DESIGN PATTERN: Manager/Service Pattern
 * 
 * @author Arkanoid Game Team
 * @version 1.0
 */
public class HighScoreManager {
    
    // ============================================================
    // CONSTANTS - Các hằng số
    // ============================================================
    
    /**
     * Tên file lưu high scores
     * File nằm ở thư mục root của project
     */
    private static final String HIGH_SCORE_FILE = "highscores.txt";
    
    /**
     * Số lượng high scores tối đa được lưu (top 10)
     * Entries vượt quá sẽ bị cắt bớt
     */
    private static final int MAX_HIGH_SCORES = 10;
    
    /**
     * Độ dài tối đa của tên người chơi (15 ký tự)
     * Tên dài hơn sẽ bị truncate
     */
    private static final int MAX_NAME_LENGTH = 15;
    
    /**
     * Tên mặc định nếu người chơi không nhập
     * Dùng khi playerName = null hoặc empty
     */
    private static final String DEFAULT_NAME = "PLAYER";
    
    // ============================================================
    // FIELDS - Thuộc tính
    // ============================================================
    
    /**
     * Danh sách các high score entries
     * 
     * ĐẶC ĐIỂM:
     * - Luôn được sắp xếp giảm dần (điểm cao nhất ở đầu)
     * - Tối đa 10 entries
     * - Được load từ file khi khởi tạo
     * - Tự động save khi thêm entry mới
     */
    private List<HighScoreEntry> highScores;
    
    // ============================================================
    // CONSTRUCTOR - Khởi tạo
    // ============================================================
    
    /**
     * Constructor - Khởi tạo HighScoreManager và load dữ liệu từ file
     * 
     * FLOW:
     * 1. Khởi tạo ArrayList rỗng
     * 2. Gọi loadHighScores() để load từ file
     * 3. Nếu file không tồn tại: Danh sách rỗng
     * 4. Nếu file tồn tại: Parse và sort entries
     */
    public HighScoreManager() {
        highScores = new ArrayList<>();
        loadHighScores();
    }
    
    // ============================================================
    // PRIVATE METHODS - Load/Save file
    // ============================================================
    
    /**
     * Đọc high scores từ file
     * 
     * LOGIC:
     * 1. Kiểm tra file có tồn tại không
     *    - Không tồn tại: Log message, return (list rỗng)
     * 2. Đọc từng dòng với BufferedReader
     * 3. Parse mỗi dòng thành HighScoreEntry
     * 4. Sắp xếp entries theo điểm giảm dần
     * 5. Log số lượng entries đã load
     * 
     * ERROR HANDLING:
     * - IOException: Log error, clear list
     * - Parse error: Skip dòng đó, tiếp tục đọc
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
     * 
     * Behavior:
     * - Ghi đè toàn bộ file với danh sách hiện tại
     * - Format: "name,score" (một entry/dòng)
     * - Tự động tạo file nếu chưa tồn tại
     * - BufferedWriter để tối ưu performance
     * 
     * @throws IOException nếu không thể ghi file (được catch và log)
     */
    public void saveHighScores() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(HIGH_SCORE_FILE))) {
            for (HighScoreEntry entry : highScores) {
                writer.write(entry.toString());
                writer.newLine();
            }
            
            System.out.println("[HighScoreManager] Saved " + highScores.size() + " high scores to file.");
            
        } catch (IOException e) {
            System.err.println("[HighScoreManager] ERROR: Failed to save high scores to '" + HIGH_SCORE_FILE + "'");
            System.err.println("Reason: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // ==================== PUBLIC METHODS ====================
    
    /**
     * Kiểm tra xem điểm số có đủ tiêu chuẩn vào bảng xếp hạng không
     * 
     * @param score Điểm số cần kiểm tra (phải >= 0)
     * @return true nếu đạt tiêu chuẩn vào top 10, false nếu không
     * 
     * Logic:
     * - Nếu chưa đủ 10 entries → chấp nhận (còn chỗ trống)
     * - Nếu đã đủ 10 → so sánh với điểm thấp nhất (entry cuối)
     */
    public boolean isHighScore(int score) {
        // Validation: điểm âm không hợp lệ
        if (score < 0) {
            System.err.println("[HighScoreManager] WARNING: Invalid score " + score + " (must be >= 0)");
            return false;
        }
        
        // Còn chỗ trống trong top 10?
        if (highScores.size() < MAX_HIGH_SCORES) {
            return true;
        }
        
        // Đã đủ 10 → so sánh với điểm thấp nhất (entry cuối cùng)
        HighScoreEntry lowestEntry = highScores.get(highScores.size() - 1);
        return score > lowestEntry.getScore();
    }
    
    /**
     * Thêm một high score mới vào bảng xếp hạng
     * 
     * @param playerName Tên người chơi (null/empty → dùng "PLAYER")
     * @param score Điểm số (phải >= 0)
     * 
     * Behavior:
     * - Validate và chuẩn hóa tên (trim, max 15 ký tự)
     * - Thêm entry mới vào danh sách
     * - Tự động sắp xếp lại giảm dần
     * - Cắt bớt nếu vượt quá 10 entries
     * - Lưu xuống file ngay lập tức
     */
    public void addHighScore(String playerName, int score) {
        // === VALIDATION ===
        
        // 1. Validate score (phải >= 0)
        if (score < 0) {
            System.err.println("[HighScoreManager] ERROR: Cannot add negative score: " + score);
            return;
        }
        
        // 2. Validate và chuẩn hóa tên người chơi
        if (playerName == null || playerName.trim().isEmpty()) {
            playerName = DEFAULT_NAME;
            System.out.println("[HighScoreManager] No player name provided, using default: '" + DEFAULT_NAME + "'");
        }
        
        // Trim và giới hạn độ dài
        playerName = playerName.trim();
        if (playerName.length() > MAX_NAME_LENGTH) {
            System.out.println("[HighScoreManager] Player name too long, truncating to " + MAX_NAME_LENGTH + " chars");
            playerName = playerName.substring(0, MAX_NAME_LENGTH);
        }
        
        // === ADD ENTRY ===
        
        HighScoreEntry newEntry = new HighScoreEntry(playerName, score);
        highScores.add(newEntry);
        
        System.out.println("[HighScoreManager] Added new high score: " + playerName + " - " + score);
        
        // === SORT & TRIM ===
        
        // Sắp xếp lại giảm dần (điểm cao nhất lên đầu)
        Collections.sort(highScores);
        
        // Giữ chỉ top 10
        if (highScores.size() > MAX_HIGH_SCORES) {
            highScores = highScores.subList(0, MAX_HIGH_SCORES);
            System.out.println("[HighScoreManager] Trimmed to top " + MAX_HIGH_SCORES);
        }
        
        // === SAVE ===
        
        saveHighScores();
    }
    
    /**
     * Lấy danh sách high scores (copy để tránh modify từ bên ngoài)
     * 
     * @return ArrayList chứa tất cả high score entries (sắp xếp giảm dần)
     */
    public List<HighScoreEntry> getHighScores() {
        return new ArrayList<>(highScores);
    }
    
    /**
     * Lấy vị trí (rank) của một điểm số trong bảng xếp hạng
     * 
     * @param score Điểm số cần kiểm tra
     * @return Vị trí (1-based): 1 = cao nhất, 10 = thấp nhất trong top 10, -1 = không vào top
     * 
     * Examples:
     * - score=1000, top1=900 → return 1 (sẽ lên vị trí số 1)
     * - score=500, top5=600, top6=400 → return 6
     * - score=100, top10=200 → return -1 (không đủ)
     */
    public int getRank(int score) {
        // Validation
        if (score < 0) {
            System.err.println("[HighScoreManager] WARNING: Cannot rank negative score: " + score);
            return -1;
        }
        
        // Tìm vị trí của điểm trong danh sách hiện tại
        for (int i = 0; i < highScores.size(); i++) {
            if (score > highScores.get(i).getScore()) {
                return i + 1; // 1-based index
            }
        }
        
        // Nếu không cao hơn ai cả nhưng còn chỗ trống → vị trí cuối
        if (highScores.size() < MAX_HIGH_SCORES) {
            return highScores.size() + 1;
        }
        
        // Không vào top 10
        return -1;
    }
}
