import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainApp extends JFrame {

    private CardLayout cardLayout;
    private JPanel mainContainer;
    // Asumsi class MainMenuPanel & SetupPanel kamu sudah ada dan benar
    private MainMenuPanel menuPanel;
    private SetupPanel setupPanel;
    private LuckySnakeLadder gamePanel;
    private SoundManager soundManager;

    private Map<String, Integer> raceWins = new HashMap<>();
    private Map<String, Integer> totalCumulativeScore = new HashMap<>();

    public MainApp() {
        setTitle("Ular Tangga");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 900);
        setLocationRelativeTo(null);

        soundManager = new SoundManager();

        cardLayout = new CardLayout();
        mainContainer = new JPanel(cardLayout);

        menuPanel = new MainMenuPanel(this, soundManager);
        setupPanel = new SetupPanel(this, soundManager);

        mainContainer.add(menuPanel, "MENU");
        mainContainer.add(setupPanel, "SETUP");

        add(mainContainer);
        cardLayout.show(mainContainer, "MENU");
    }

    public void showCard(String cardName) {
        // --- LOGIC AUDIO SAAT PINDAH MENU ---
        if (cardName.equals("MENU")) {
            // Balik Menu -> Matiin semua musik
            soundManager.stopBGM();
            soundManager.stopWinMusic();
        }
        else if (cardName.equals("SETUP")) {
            // Masuk Setup -> Nyalain BGM Full Volume (100%)
            soundManager.playBGM("game_bgm.wav");
        }

        cardLayout.show(mainContainer, cardName);
    }

    public void startGame(List<String> playerNames) {
        if (gamePanel != null) {
            mainContainer.remove(gamePanel);
        }

        for(String name : playerNames) {
            raceWins.putIfAbsent(name, 0);
            totalCumulativeScore.putIfAbsent(name, 0);
        }

        gamePanel = new LuckySnakeLadder(playerNames, this, soundManager);
        mainContainer.add(gamePanel, "GAME");

        soundManager.playBGM("game_bgm.wav");

        cardLayout.show(mainContainer, "GAME");
        gamePanel.requestFocusInWindow();
    }

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
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            UIManager.put("Button.arc", 15);
            UIManager.put("Component.arc", 15);
        } catch (Exception ignored) {}
        SwingUtilities.invokeLater(() -> new MainApp().setVisible(true));
    }
}