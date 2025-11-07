package com.arkanoid.game.ui;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;

// ============================================================
// CLASS: MenuAudioManager - Quản lý menu music
// ============================================================

/**
 * Class MenuAudioManager - Quản lý nhạc nền cho Menu
 * 
 * TRÁCH NHIỆM:
 * 1. Load menutheme.wav từ resources
 * 2. Play music với loop continuous
 * 3. Điều chỉnh volume theo Menu.globalVolume
 * 4. Stop music khi chuyển sang game
 * 
 * FILE: /Sound/menutheme.wav
 * 
 * VOLUME CONTROL:
 * - Linear to decibel conversion: dB = 20 * log10(volume)
 * - Range: 0.0 (mute) → 1.0 (max volume)
 * - Special case: volume=0 → set to minimum dB
 * 
 * DESIGN: Separation of Concerns
 * - Menu chỉ quản lý UI
 * - MenuAudioManager chỉ quản lý audio
 * 
 * @author Arkanoid Game Team
 * @version 1.0
 */
public class MenuAudioManager {
    /** Clip để play menu music (loop continuously) */
    private Clip menuMusicClip;
    
    /**
     * Load và play menu music với loop
     * 
     * FLOW:
     * 1. Stop music cũ (nếu có)
     * 2. Load /Sound/menutheme.wav
     * 3. Open clip
     * 4. Set volume theo Menu.globalVolume
     * 5. Loop continuously
     * 
     * ERROR HANDLING:
     * - File not found → print error, menuMusicClip = null
     * - Load error → print error, menuMusicClip = null
     */
    public void loadAndPlayMenuMusic() {
        try {
            stopMenuMusic(); // Stop music cũ nếu đang chạy
            
            // Load menutheme.wav
            java.net.URL musicURL = getClass().getResource("/Sound/menutheme.wav");
            if (musicURL != null) {
                AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(musicURL);
                menuMusicClip = AudioSystem.getClip();
                menuMusicClip.open(audioInputStream);
                
                // Set volume theo globalVolume
                setVolume(Menu.globalVolume);
                
                // Play loop continuously
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
     * Điều chỉnh volume của menu music
     * 
     * CONVERSION: Linear (0.0-1.0) → Decibel
     * - Formula: dB = 20 * log10(volume)
     * - Special: volume=0 → minimum dB (mute)
     * - Clamp: dB phải trong range [min, max] của FloatControl
     * 
     * @param volume Volume từ 0.0 (mute) đến 1.0 (max)
     */
    public void setVolume(float volume) {
        if (menuMusicClip != null && menuMusicClip.isOpen()) {
            try {
                FloatControl volumeControl = (FloatControl) menuMusicClip.getControl(FloatControl.Type.MASTER_GAIN);
                
                if (volume <= 0.0f) {
                    // Mute: Set to minimum
                    volumeControl.setValue(volumeControl.getMinimum());
                } else {
                    // Convert linear to decibel
                    volume = Math.max(0.0001f, Math.min(1.0f, volume));
                    float dB = (float) (Math.log(volume) / Math.log(10.0) * 20.0);
                    
                    // Clamp to valid range
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
     * Stop menu music
     * 
     * ĐƯỢC GỌI TỪ:
     * - Menu PLAY button (trước khi start game)
     * - Menu QUIT button (trước khi exit)
     * - LevelSelectionDialog (trước khi start custom game)
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
