package com.arkanoid.game.game;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;

import com.arkanoid.game.ui.Menu;

// ============================================================
// CLASS: AudioManager - Quản lý âm thanh và nhạc nền trong game
// ============================================================

/**
 * Class AudioManager - Quản lý toàn bộ âm thanh và nhạc nền trong game
 * 
 * TRÁCH NHIỆM CHÍNH:
 * - Load và phát nhạc nền cho từng level (level1Music.wav -> level5Music.wav)
 * - Điều khiển play/pause/stop/resume nhạc nền
 * - Đồng bộ âm lượng với Menu.globalVolume (0.0 -> 1.0)
 * - Chuyển đổi âm lượng từ linear sang decibel cho Java Sound API
 * 
 * CÔNG NGHỆ SỬ DỤNG:
 * - javax.sound.sampled.Clip: Để load và phát file WAV
 * - javax.sound.sampled.AudioInputStream: Để đọc audio data
 * - javax.sound.sampled.FloatControl: Để điều chỉnh âm lượng (MASTER_GAIN)
 * 
 * CÁCH HOẠT ĐỘNG:
 * 1. GamePanel tạo AudioManager khi khởi tạo
 * 2. Khi load level mới: gọi loadAndPlayLevelMusic(levelNumber)
 * 3. Khi pause game: gọi pauseMusic()
 * 4. Khi resume game: gọi resumeMusic()
 * 5. Khi thay đổi volume trong menu: gọi updateVolume()
 * 6. Khi thoát level: gọi stopLevelMusic()
 * 
 * DESIGN PATTERN: Service/Manager Pattern
 * - Tách biệt logic xử lý audio khỏi GamePanel
 * - Cung cấp interface đơn giản cho việc quản lý nhạc nền
 * 
 * @author Arkanoid Game Team
 * @version 1.0
 */
public class AudioManager {
    
    // ============================================================
    // THUỘC TÍNH - Audio Clip
    // ============================================================
    
    /**
     * Clip chứa nhạc nền của level hiện tại
     * 
     * CHI TIẾT:
     * - null: Chưa load nhạc hoặc đã stop
     * - Khi load level mới: clip cũ sẽ bị stop và close, tạo clip mới
     * - Được sử dụng để:
     *   + Phát nhạc lặp lại vô hạn (LOOP_CONTINUOUSLY)
     *   + Pause/resume nhạc
     *   + Điều chỉnh âm lượng qua FloatControl
     *   + Stop và đóng clip khi không dùng
     */
    private Clip levelMusicClip;
    
    // ============================================================
    // PHƯƠNG THỨC PUBLIC - Load và phát nhạc nền
    // ============================================================
    
    /**
     * Load và phát nhạc nền cho level được chỉ định
     * 
     * LOGIC HOẠT ĐỘNG:
     * 1. Dừng và đóng nhạc cũ nếu đang phát (stopLevelMusic)
     * 2. Load file WAV từ resources: /Sound/level{levelNumber}Music.wav
     *    - Ví dụ: level 1 -> /Sound/level1Music.wav
     *    - Ví dụ: level 5 -> /Sound/level5Music.wav
     * 3. Tạo AudioInputStream từ URL của file
     * 4. Tạo Clip mới và open với audio stream
     * 5. Cập nhật volume theo Menu.globalVolume (0.0 -> 1.0)
     * 6. Bật loop vô hạn để nhạc phát liên tục
     * 
     * XỬ LÝ LỖI:
     * - Nếu file không tồn tại: in warning, không crash game
     * - Nếu lỗi decode/load audio: in error, set clip = null
     * 
     * ĐẶC ĐIỂM:
     * - Sử dụng Clip.LOOP_CONTINUOUSLY để phát liên tục
     * - Volume được convert từ linear (0.0-1.0) sang decibel
     * - File WAV phải nằm trong resources/Sound/
     * 
     * @param levelNumber Số thứ tự level (1-5)
     */
    public void loadAndPlayLevelMusic(int levelNumber) {
        try {
            // BƯỚC 1: Dừng nhạc cũ nếu có
            stopLevelMusic(); // Giải phóng resources của clip cũ
            
            // BƯỚC 2: Tạo đường dẫn đến file nhạc
            // Format: /Sound/level1Music.wav, /Sound/level2Music.wav, ...
            String path = "/Sound/level" + levelNumber + "Music.wav";
            
            // BƯỚC 3: Load file từ classpath resources
            java.net.URL musicURL = getClass().getResource(path);
            
            if (musicURL != null) {
                // File tồn tại - tiến hành load
                
                // BƯỚC 4: Tạo AudioInputStream từ file
                AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(musicURL);
                
                // BƯỚC 5: Tạo Clip mới và mở stream
                levelMusicClip = AudioSystem.getClip();
                levelMusicClip.open(audioInputStream);
                
                // BƯỚC 6: Điều chỉnh âm lượng theo Menu.globalVolume
                // Convert từ linear (0.0-1.0) sang decibel
                updateVolume();
                
                // BƯỚC 7: Bật loop vô hạn
                // Nhạc sẽ tự động lặp lại khi kết thúc
                levelMusicClip.loop(Clip.LOOP_CONTINUOUSLY);
                
                // Debug: In thông báo thành công
                System.out.println("Level 1 music loaded and playing at volume: " + (int)(Menu.globalVolume * 100) + "%");
            } else {
                // File không tồn tại - in warning nhưng không crash game
                System.out.println("Music file not found: /Sound/level1Music.wav");
            }
        } catch (Exception e) {
            // Xử lý mọi lỗi audio (UnsupportedAudioFileException, IOException, LineUnavailableException)
            System.out.println("Error loading level music: " + e.getMessage());
            levelMusicClip = null; // Đảm bảo clip = null nếu load thất bại
        }
    }
    
    // ============================================================
    // PHƯƠNG THỨC PUBLIC - Điều chỉnh âm lượng
    // ============================================================
    
    /**
     * Cập nhật âm lượng nhạc nền từ Menu.globalVolume
     * 
     * CÔNG THỨC CHUYỂN ĐỔI:
     * - Input: Menu.globalVolume (0.0 -> 1.0) - Linear scale
     * - Output: MASTER_GAIN (decibel) - Logarithmic scale
     * - Formula: dB = 20 * log10(volume)
     * 
     * XỬ LÝ ĐẶC BIỆT:
     * - volume = 0.0: Set về minimum gain (mute hoàn toàn)
     * - volume < 0.0001: Clamp về 0.0001 để tránh log(0) = -∞
     * - dB result: Clamp vào range [min, max] của FloatControl
     * 
     * CÁCH HOẠT ĐỘNG:
     * 1. Kiểm tra clip có đang open không
     * 2. Lấy FloatControl.Type.MASTER_GAIN từ clip
     * 3. Nếu volume = 0: set về minimum (mute)
     * 4. Nếu volume > 0: convert sang decibel với công thức logarit
     * 5. Clamp dB vào range hợp lệ của control
     * 6. Set giá trị mới cho volume control
     * 
     * SỬ DỤNG:
     * - Được gọi tự động khi load nhạc mới
     * - Được gọi từ GamePanel khi user thay đổi volume trong menu
     * 
     * @throws IllegalArgumentException Nếu clip không hỗ trợ MASTER_GAIN control
     */
    public void updateVolume() {
        // Kiểm tra clip có tồn tại và đã open chưa
        if (levelMusicClip != null && levelMusicClip.isOpen()) {
            try {
                // BƯỚC 1: Lấy volume control từ clip
                // MASTER_GAIN: Điều chỉnh âm lượng tổng thể (đơn vị: decibel)
                FloatControl volumeControl = (FloatControl) levelMusicClip.getControl(FloatControl.Type.MASTER_GAIN);
                
                // BƯỚC 2: Lấy giá trị volume từ Menu (0.0 -> 1.0)
                float volume = Menu.globalVolume;
                
                // BƯỚC 3: Xử lý trường hợp volume = 0 (mute)
                if (volume <= 0.0f) {
                    // Set về minimum gain để mute hoàn toàn
                    volumeControl.setValue(volumeControl.getMinimum());
                } else {
                    // BƯỚC 4: Clamp volume vào range hợp lệ [0.0001, 1.0]
                    // - Min = 0.0001: Tránh log(0) = -∞
                    // - Max = 1.0: Volume tối đa
                    volume = Math.max(0.0001f, Math.min(1.0f, volume));
                    
                    // BƯỚC 5: Convert linear volume sang decibel
                    // Công thức: dB = 20 * log10(volume)
                    // Ví dụ:
                    // - volume = 1.0 -> dB = 0.0 (full volume)
                    // - volume = 0.5 -> dB ≈ -6.0 (nửa volume)
                    // - volume = 0.1 -> dB ≈ -20.0 (1/10 volume)
                    float dB = (float) (Math.log(volume) / Math.log(10.0) * 20.0);
                    
                    // BƯỚC 6: Clamp dB vào range hợp lệ của control
                    // Mỗi audio device có range khác nhau (thường là [-80.0, 6.0])
                    float min = volumeControl.getMinimum();
                    float max = volumeControl.getMaximum();
                    dB = Math.max(min, Math.min(max, dB));
                    
                    // BƯỚC 7: Set giá trị decibel mới
                    volumeControl.setValue(dB);
                }
            } catch (Exception e) {
                // Lỗi khi điều chỉnh volume (device không hỗ trợ, control không khả dụng)
                System.out.println("Could not update volume: " + e.getMessage());
            }
        }
    }
    
    // ============================================================
    // PHƯƠNG THỨC PUBLIC - Stop/Pause/Resume nhạc nền
    // ============================================================
    
    /**
     * Dừng và đóng nhạc nền hoàn toàn
     * 
     * CÁCH HOẠT ĐỘNG:
     * 1. Stop(): Dừng việc phát nhạc
     * 2. Close(): Giải phóng audio resources (buffers, line, ...)
     * 3. Set clip = null để garbage collector có thể thu hồi bộ nhớ
     * 
     * SỬ DỤNG:
     * - Khi chuyển level mới (load nhạc khác)
     * - Khi thoát về menu chính
     * - Khi đóng game
     * 
     * ĐẶC ĐIỂM:
     * - Sau khi stop, không thể resume được (phải load lại)
     * - Giải phóng hoàn toàn audio resources
     */
    public void stopLevelMusic() {
        if (levelMusicClip != null && levelMusicClip.isOpen()) {
            levelMusicClip.stop();    // Dừng phát nhạc
            levelMusicClip.close();   // Giải phóng audio resources
            levelMusicClip = null;    // Cho phép garbage collection
        }
    }
    
    /**
     * Tạm dừng nhạc nền (không đóng clip)
     * 
     * CÁCH HOẠT ĐỘNG:
     * - Dừng việc phát nhạc nhưng GIỮ NGUYÊN clip
     * - Vị trí phát hiện tại được lưu lại
     * - Có thể resume để tiếp tục từ vị trí cũ
     * 
     * SỬ DỤNG:
     * - Khi pause game (Esc hoặc lost focus)
     * - Khi hiển thị dialog/menu tạm thời
     * 
     * ĐẶC ĐIỂM:
     * - Không giải phóng resources (nhanh hơn stop)
     * - Có thể resume ngay lập tức
     * - Frame position được giữ nguyên
     */
    public void pauseMusic() {
        if (levelMusicClip != null && levelMusicClip.isRunning()) {
            levelMusicClip.stop(); // stop() chỉ dừng phát, không đóng clip
        }
    }
    
    /**
     * Tiếp tục phát nhạc đã tạm dừng
     * 
     * CÁCH HOẠT ĐỘNG:
     * - Kiểm tra clip tồn tại và KHÔNG đang phát
     * - Gọi start() để tiếp tục từ vị trí cũ
     * - Loop mode được giữ nguyên (LOOP_CONTINUOUSLY)
     * 
     * SỬ DỤNG:
     * - Khi resume game sau khi pause
     * - Khi đóng dialog/menu và quay lại game
     * 
     * ĐẶC ĐIỂM:
     * - Tiếp tục từ frame position trước khi pause
     * - Không cần load lại file audio
     * - Volume giữ nguyên theo Menu.globalVolume
     */
    public void resumeMusic() {
        if (levelMusicClip != null && !levelMusicClip.isRunning()) {
            levelMusicClip.start(); // Tiếp tục từ vị trí đã dừng
        }
    }
}
