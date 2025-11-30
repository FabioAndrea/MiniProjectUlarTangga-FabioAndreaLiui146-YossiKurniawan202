import javax.sound.sampled.*;
import java.io.File;

public class SoundManager {
    private float currentVolume = 0.7f; // Default volume 70%

    // Method untuk memutar efek suara (SFX)
    public void playSFX(String filename) {
        try {
            File soundFile = new File(filename);
            if (soundFile.exists()) {
                AudioInputStream audioInput = AudioSystem.getAudioInputStream(soundFile);
                Clip clip = AudioSystem.getClip();
                clip.open(audioInput);

                // Set volume sebelum dimainkan
                setClipVolume(clip, currentVolume);

                clip.start();
            }
        } catch (Exception e) {
            // Silent error agar game tidak crash jika file audio hilang
        }
    }

    // --- BAGIAN INI YANG SEBELUMNYA HILANG ---
    // Method ini dipanggil oleh Slider di LuckySnakeLadder
    public void setVolume(float volume) {
        this.currentVolume = volume;
    }

    // Helper untuk mengubah gain (Desibel)
    private void setClipVolume(Clip clip, float volume) {
        try {
            if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);

                // Rumus konversi 0.0-1.0 ke Desibel
                float min = gainControl.getMinimum();
                float max = gainControl.getMaximum();
                float range = max - min;
                float gain = (range * volume) + min;

                gainControl.setValue(gain);
            }
        } catch (Exception ignored) {}
    }
}