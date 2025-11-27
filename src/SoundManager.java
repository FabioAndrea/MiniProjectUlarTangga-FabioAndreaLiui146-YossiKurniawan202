import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class SoundManager {
    private Clip clip;

    public void playMusic(String location) {
        try {
            File musicPath = new File(location);

            if (musicPath.exists()) {
                AudioInputStream audioInput = AudioSystem.getAudioInputStream(musicPath);
                clip = AudioSystem.getClip();
                clip.open(audioInput);

                // Atur agar musik looping (berulang terus)
                clip.loop(Clip.LOOP_CONTINUOUSLY);

                // Kecilkan volume sedikit (Opsional)
                FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                gainControl.setValue(-10.0f); // Kurangi 10 desibel agar tidak terlalu berisik

                clip.start();
            } else {
                System.out.println("File audio tidak ditemukan di: " + location);
            }
        } catch (Exception e) {
            System.out.println("Error memutar musik:");
            e.printStackTrace();
        }
    }

    public void stopMusic() {
        if (clip != null && clip.isRunning()) {
            clip.stop();
        }
    }
}