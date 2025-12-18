import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainApp extends JFrame {

    private CardLayout cardLayout;
    private JPanel mainContainer;
    private MainMenuPanel menuPanel;
    private SetupPanel setupPanel;
    private LuckySnakeLadder gamePanel;
    private SoundManager soundManager;

    // [DATA PERSISTENCE]
    // Map ini menyimpan data kemenangan dan total skor pemain.
    // Ditaruh di sini agar data TIDAK HILANG saat game di-restart (LuckySnakeLadder di-reset).
    private Map<String, Integer> raceWins = new HashMap<>();
    private Map<String, Integer> totalCumulativeScore = new HashMap<>();

    public MainApp() {
        setTitle("Ular Tangga Galaxy Edition");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 900);
        setLocationRelativeTo(null);

        // Inisialisasi SoundManager (termasuk proses warm-up audio)
        soundManager = new SoundManager();

        cardLayout = new CardLayout();
        mainContainer = new JPanel(cardLayout);

        // Panel Menu dan Setup dibuat sekali saja di awal
        menuPanel = new MainMenuPanel(this, soundManager);
        setupPanel = new SetupPanel(this, soundManager);

        mainContainer.add(menuPanel, "MENU");
        mainContainer.add(setupPanel, "SETUP");

        add(mainContainer);
        cardLayout.show(mainContainer, "MENU");
    }

    public void showCard(String cardName) {
        // [LOGIC AUDIO CONTROL]
        // Mengatur status musik berdasarkan halaman yang aktif.
        if (cardName.equals("MENU")) {
            // Saat di menu utama, matikan semua musik (hening)
            soundManager.stopBGM();
            soundManager.stopWinMusic();
        }
        else if (cardName.equals("SETUP")) {
            // Saat masuk pengaturan, nyalakan BGM dengan Volume Penuh (Scale 1.0)
            soundManager.setBGMScale(1.0f);
            soundManager.playBGM("game_bgm.wav");
        }
        cardLayout.show(mainContainer, cardName);
    }

    // [LOGIC START / RESTART GAME]
    // Method ini berfungsi untuk MEMULAI atau MERESET permainan.
    public void startGame(List<String> playerNames) {
        // 1. Bersihkan Memori: Hapus panel game lama jika ada
        if (gamePanel != null) {
            mainContainer.remove(gamePanel);
        }

        // 2. Inisialisasi Data: Pastikan pemain punya slot di database skor
        for(String name : playerNames) {
            raceWins.putIfAbsent(name, 0);
            totalCumulativeScore.putIfAbsent(name, 0);
        }

        // 3. Buat Instance Baru: Membuat objek game baru (Posisi & Dadu reset otomatis)
        gamePanel = new LuckySnakeLadder(playerNames, this, soundManager);
        mainContainer.add(gamePanel, "GAME");

        // [AUDIO DUCKING]
        // Kecilkan volume BGM (0.6f) agar tidak menutupi suara dadu/langkah
        soundManager.playBGM("game_bgm.wav");
        soundManager.setBGMScale(0.6f);

        cardLayout.show(mainContainer, "GAME");
        gamePanel.requestFocusInWindow();
    }

    // Method Helper untuk menambah dan mengambil data skor
    public void addRaceWin(String playerName) {
        raceWins.put(playerName, raceWins.getOrDefault(playerName, 0) + 1);
    }

    public void addScore(String playerName, int score) {
        totalCumulativeScore.put(playerName, totalCumulativeScore.getOrDefault(playerName, 0) + score);
    }

    public int getRaceWins(String playerName) { return raceWins.getOrDefault(playerName, 0); }
    public int getTotalScore(String playerName) { return totalCumulativeScore.getOrDefault(playerName, 0); }

    public static void main(String[] args) {
        try {
            // Mengatur tampilan UI agar tombol terlihat lebih modern (Rounded)
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            UIManager.put("Button.arc", 15);
            UIManager.put("Component.arc", 15);
        } catch (Exception ignored) {}
        SwingUtilities.invokeLater(() -> new MainApp().setVisible(true));
    }
}