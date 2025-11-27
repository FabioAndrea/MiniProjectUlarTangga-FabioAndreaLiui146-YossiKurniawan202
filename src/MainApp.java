import javax.swing.*;
import java.awt.*;

public class MainApp extends JFrame {

    private CardLayout cardLayout;
    private JPanel mainContainer;
    private MainMenuPanel menuPanel;
    private SetupPanel setupPanel;
    private LuckySnakeLadder gamePanel;

    // Tambahkan variabel SoundManager
    private SoundManager soundManager;

    public MainApp() {
        setTitle("Lucky Snake Ladder - Ultimate Edition");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 800);
        setLocationRelativeTo(null);

        // --- MULAI MUSIK DI SINI ---
        soundManager = new SoundManager();
        // Pastikan file "bgm.wav" ada di folder proyek utama Anda
        soundManager.playMusic("bgm.wav");
        // ---------------------------

        // Setup CardLayout
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

    public void startGame(String p1Name, String p2Name) {
        if (gamePanel != null) {
            mainContainer.remove(gamePanel);
        }
        gamePanel = new LuckySnakeLadder(p1Name, p2Name, this);
        mainContainer.add(gamePanel, "GAME");
        cardLayout.show(mainContainer, "GAME");
        gamePanel.requestFocusInWindow();
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            UIManager.put("Button.arc", 15);
            UIManager.put("Component.arc", 15);
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> new MainApp().setVisible(true));
    }
}