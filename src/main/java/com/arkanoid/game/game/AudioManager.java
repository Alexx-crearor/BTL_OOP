package com.arkanoid.game.game;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;

import com.arkanoid.game.ui.Menu;

/**
 * Quản lý âm thanh và nhạc nền trong game
 * Chịu trách nhiệm: load, play, pause, stop nhạc nền
 * Sử dụng globalVolume từ Menu để đồng bộ âm lượng
 */
public class AudioManager {
    private Clip levelMusicClip; // Clip chứa nhạc nền level
    
    /**
     * Load và phát nhạc nền cho level
     * Nhạc sẽ loop liên tục với âm lượng từ Menu.globalVolume
     */
    public void loadAndPlayLevelMusic(int levelNumber) {
        try {
            stopLevelMusic(); // Dừng nhạc cũ nếu có
            
            // Load file WAV từ resources
            String path = "/Sound/level" + levelNumber + "Music.wav";
            java.net.URL musicURL = getClass().getResource(path);
            if (musicURL != null) {
                AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(musicURL);
                levelMusicClip = AudioSystem.getClip();
                levelMusicClip.open(audioInputStream);
                
                // Điều chỉnh âm lượng theo globalVolume
                updateVolume();
                
                // Phát nhạc lặp lại vô hạn
                levelMusicClip.loop(Clip.LOOP_CONTINUOUSLY);
                System.out.println("Level 1 music loaded and playing at volume: " + (int)(Menu.globalVolume * 100) + "%");
            } else {
                System.out.println("Music file not found: /Sound/level1Music.wav");
            }
        } catch (Exception e) {
            System.out.println("Error loading level music: " + e.getMessage());
            levelMusicClip = null;
        }
    }
    
    /**
     * Cập nhật âm lượng từ Menu.globalVolume
     * Sử dụng công thức chuyển đổi linear (0.0-1.0) sang decibel
     */
    public void updateVolume() {
        if (levelMusicClip != null && levelMusicClip.isOpen()) {
            try {
                FloatControl volumeControl = (FloatControl) levelMusicClip.getControl(FloatControl.Type.MASTER_GAIN);
                float volume = Menu.globalVolume;
                
                // Xử lý volume = 0 (mute)
                if (volume <= 0.0f) {
                    volumeControl.setValue(volumeControl.getMinimum());
                } else {
                    // Convert linear volume (0.0-1.0) to decibel
                    volume = Math.max(0.0001f, Math.min(1.0f, volume));
                    float dB = (float) (Math.log(volume) / Math.log(10.0) * 20.0);
                    
                    // Clamp dB to valid range
                    float min = volumeControl.getMinimum();
                    float max = volumeControl.getMaximum();
                    dB = Math.max(min, Math.min(max, dB));
                    
                    volumeControl.setValue(dB);
                }
            } catch (Exception e) {
                System.out.println("Could not update volume: " + e.getMessage());
            }
        }
    }
    
    /**
     * Dừng và đóng nhạc nền hoàn toàn
     */
    public void stopLevelMusic() {
        if (levelMusicClip != null && levelMusicClip.isOpen()) {
            levelMusicClip.stop();
            levelMusicClip.close();
            levelMusicClip = null;
        }
    }
    
    /**
     * Tạm dừng nhạc (không đóng clip)
     */
    public void pauseMusic() {
        if (levelMusicClip != null && levelMusicClip.isRunning()) {
            levelMusicClip.stop();
        }
    }
    
    /**
     * Tiếp tục phát nhạc đã tạm dừng
     */
    public void resumeMusic() {
        if (levelMusicClip != null && !levelMusicClip.isRunning()) {
            levelMusicClip.start();
        }
    }
}
