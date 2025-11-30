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

    // --- DATA PERSISTEN (DATABASE SEMENTARA) ---
    // Menyimpan jumlah kemenangan balapan (Finish duluan)
    private Map<String, Integer> raceWins = new HashMap<>();

    // Menyimpan total poin bonus akumulatif
    private Map<String, Integer> totalCumulativeScore = new HashMap<>();

    public MainApp() {
        setTitle("Lucky Snake Ladder - 4 Player Edition");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 900);
        setLocationRelativeTo(null);

        soundManager = new SoundManager();

        cardLayout = new CardLayout();
        mainContainer = new JPanel(cardLayout);

        menuPanel = new MainMenuPanel(this);
        setupPanel = new SetupPanel(this);

        mainContainer.add(menuPanel, "MENU");
        mainContainer.add(setupPanel, "SETUP");

        add(mainContainer);
        cardLayout.show(mainContainer, "MENU");
    }

    public void showCard(String cardName) {
        cardLayout.show(mainContainer, cardName);
    }

    // Method ini dipanggil saat tombol START atau RESTART ditekan
    public void startGame(List<String> playerNames) {
        if (gamePanel != null) {
            mainContainer.remove(gamePanel);
        }

        // Pastikan pemain ada di database, tapi JANGAN RESET nilainya jika sudah ada
        for(String name : playerNames) {
            raceWins.putIfAbsent(name, 0); // Jika belum ada, set 0. Jika ada, biarkan.
            totalCumulativeScore.putIfAbsent(name, 0);
        }

        // Buat game baru
        gamePanel = new LuckySnakeLadder(playerNames, this, soundManager);
        mainContainer.add(gamePanel, "GAME");

        cardLayout.show(mainContainer, "GAME");
        gamePanel.requestFocusInWindow();
    }

    // Menambah 1 poin kemenangan balapan
    public void addRaceWin(String playerName) {
        raceWins.put(playerName, raceWins.getOrDefault(playerName, 0) + 1);
    }

    // Menambah skor poin bonus
    public void addScore(String playerName, int score) {
        totalCumulativeScore.put(playerName, totalCumulativeScore.getOrDefault(playerName, 0) + score);
    }

    // Getters untuk Leaderboard
    public int getRaceWins(String playerName) { return raceWins.getOrDefault(playerName, 0); }
    public int getTotalScore(String playerName) { return totalCumulativeScore.getOrDefault(playerName, 0); }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            UIManager.put("Button.arc", 15);
            UIManager.put("Component.arc", 15);
        } catch (Exception ignored) {}
        SwingUtilities.invokeLater(() -> new MainApp().setVisible(true));
    }
}