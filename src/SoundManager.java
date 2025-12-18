import javax.sound.sampled.*;
import java.io.File;
import java.net.URL;

public class SoundManager {
    private float currentVolume = 0.75f;
    private float bgmScaleFactor = 1.0f; // Scale 1.0 = Full, 0.6 = Ducking

    private Clip winMusicClip;
    private Clip bgmClip;

    // Thread warm-up agar audio tidak delay
    public SoundManager() {
        new Thread(() -> {
            try {
                AudioInputStream audioInput = loadAudio("click.wav");
                if (audioInput != null) {
                    Clip clip = AudioSystem.getClip();
                    clip.open(audioInput);
                    if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                        FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                        gainControl.setValue(gainControl.getMinimum());
                    }
                    clip.start();
                    Thread.sleep(50);
                    clip.stop();
                    clip.close();
                }
            } catch (Exception ignored) {}
        }).start();
    }

    public void setBGMScale(float scale) {
        this.bgmScaleFactor = scale;
        if (bgmClip != null && bgmClip.isOpen()) {
            setClipVolume(bgmClip, currentVolume * bgmScaleFactor);
        }
    }

    public void playBGM(String filename) {
        if (bgmClip != null && bgmClip.isRunning()) return;

        stopWinMusic();
        stopBGM();

        try {
            AudioInputStream audioInput = loadAudio(filename);
            if (audioInput == null) return;

            bgmClip = AudioSystem.getClip();
            bgmClip.open(audioInput);
            setClipVolume(bgmClip, currentVolume * bgmScaleFactor);
            bgmClip.loop(Clip.LOOP_CONTINUOUSLY);
            bgmClip.start();
        } catch (Exception e) {
            System.err.println("Error playBGM: " + e.getMessage());
        }
    }

    public void stopBGM() {
        if (bgmClip != null && bgmClip.isRunning()) {
            bgmClip.stop();
            bgmClip.close();
            bgmClip = null;
        }
    }

    public void playWinMusic(String filename) {
        stopBGM();
        stopWinMusic();

        try {
            AudioInputStream audioInput = loadAudio(filename);
            if (audioInput == null) return;

            winMusicClip = AudioSystem.getClip();
            winMusicClip.open(audioInput);
            setClipVolume(winMusicClip, currentVolume);
            winMusicClip.loop(Clip.LOOP_CONTINUOUSLY);
            winMusicClip.start();
        } catch (Exception e) {
            System.err.println("Error playWinMusic: " + e.getMessage());
        }
    }

    public void stopWinMusic() {
        if (winMusicClip != null && winMusicClip.isRunning()) {
            winMusicClip.stop();
            winMusicClip.close();
        }
    }

    // Play SFX di thread terpisah (No-Lag)
    public void playSFX(String filename) {
        playSound(filename, -1);
    }

    public void playFixedDurationSFX(String filename, int durationMs) {
        playSound(filename, durationMs);
    }

    public void setVolume(float volume) {
        this.currentVolume = volume;
        if (winMusicClip != null && winMusicClip.isOpen()) {
            setClipVolume(winMusicClip, volume);
        }
        if (bgmClip != null && bgmClip.isOpen()) {
            setClipVolume(bgmClip, volume * bgmScaleFactor);
        }
    }

    public int getVolumeInt() {
        return (int) (currentVolume * 100);
    }

    private void playSound(String filename, int durationMs) {
        new Thread(() -> {
            try {
                AudioInputStream audioInput = loadAudio(filename);
                if (audioInput == null) return;

                Clip clip = AudioSystem.getClip();
                clip.open(audioInput);
                setClipVolume(clip, currentVolume);
                clip.start();

                if (durationMs > 0) {
                    try {
                        Thread.sleep(durationMs);
                        if (clip.isRunning()) clip.stop();
                        clip.close();
                    } catch (Exception ignored) {}
                } else {
                    clip.addLineListener(event -> {
                        if (event.getType() == LineEvent.Type.STOP) clip.close();
                    });
                }
            } catch (Exception e) {}
        }).start();
    }

    private AudioInputStream loadAudio(String filename) {
        try {
            File f = new File(filename);
            if (f.exists()) return AudioSystem.getAudioInputStream(f);
            URL url = getClass().getResource("/" + filename);
            if (url != null) return AudioSystem.getAudioInputStream(url);
            url = getClass().getResource(filename);
            if (url != null) return AudioSystem.getAudioInputStream(url);
            return null;
        } catch (Exception e) { return null; }
    }

    private void setClipVolume(Clip clip, float volume) {
        try {
            if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                float min = gainControl.getMinimum();
                float max = gainControl.getMaximum();
                float range = max - min;
                float gain = (range * volume) + min;
                gainControl.setValue(gain);
            }
        } catch (Exception ignored) {}
    }
}