package com.arkanoid.game.ui;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;

/**
 * Quản lý âm thanh menu
 * Chức năng:
 * - Load và phát nhạc nền menu
 * - Điều chỉnh âm lượng
 * - Dừng nhạc khi thoát menu
 */
public class MenuAudioManager {
    private Clip menuMusicClip;
    
    /**
     * Load và phát nhạc nền menu
     * Nhạc sẽ loop liên tục với âm lượng từ Menu.globalVolume
     */
    public void loadAndPlayMenuMusic() {
        try {
            stopMenuMusic(); // Dừng nhạc cũ nếu có
            
            java.net.URL musicURL = getClass().getResource("/Sound/menutheme.wav");
            if (musicURL != null) {
                AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(musicURL);
                menuMusicClip = AudioSystem.getClip();
                menuMusicClip.open(audioInputStream);
                
                // Điều chỉnh âm lượng theo globalVolume
                setVolume(Menu.globalVolume);
                
                menuMusicClip.loop(Clip.LOOP_CONTINUOUSLY);
                System.out.println("Menu music loaded and playing!");
            } else {
                System.out.println("Menu music not found: /Sound/menutheme.wav");
            }
        } catch (Exception e) {
            System.out.println("Error loading menu music: " + e.getMessage());
            menuMusicClip = null;
        }
    }
    
    /**
     * Điều chỉnh âm lượng của nhạc nền menu
     * @param volume Âm lượng từ 0.0 (mute) đến 1.0 (max)
     */
    public void setVolume(float volume) {
        if (menuMusicClip != null && menuMusicClip.isOpen()) {
            try {
                FloatControl volumeControl = (FloatControl) menuMusicClip.getControl(FloatControl.Type.MASTER_GAIN);
                
                // Xử lý volume = 0 (mute)
                if (volume <= 0.0f) {
                    volumeControl.setValue(volumeControl.getMinimum());
                } else {
                    // Convert linear volume (0.0-1.0) to decibel
                    // Clamp volume to avoid log(0)
                    volume = Math.max(0.0001f, Math.min(1.0f, volume));
                    float dB = (float) (Math.log(volume) / Math.log(10.0) * 20.0);
                    
                    // Clamp dB to valid range
                    float min = volumeControl.getMinimum();
                    float max = volumeControl.getMaximum();
                    dB = Math.max(min, Math.min(max, dB));
                    
                    volumeControl.setValue(dB);
                }
            } catch (Exception e) {
                System.out.println("Could not set volume: " + e.getMessage());
            }
        }
    }
    
    /**
     * Dừng nhạc nền menu
     */
    public void stopMenuMusic() {
        if (menuMusicClip != null && menuMusicClip.isOpen()) {
            menuMusicClip.stop();
            menuMusicClip.close();
            menuMusicClip = null;
        }
    }
    
    /**
     * Kiểm tra xem nhạc có đang phát không
     */
    public boolean isPlaying() {
        return menuMusicClip != null && menuMusicClip.isRunning();
    }
}
