import javax.swing.*;
import java.awt.*;

public class MainApp extends JFrame {

    private CardLayout cardLayout;
    private JPanel mainContainer;
    private MainMenuPanel menuPanel;
    private SetupPanel setupPanel;
    private LuckySnakeLadder gamePanel;

    public MainApp() {
        setTitle("Lucky Snake Ladder - Spiral Edition");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 900); // Larger size for spiral board
        setLocationRelativeTo(null);


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
        // Set UI Look and Feel menjadi lebih modern (Cross Platform)
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            // Global UI Tweaks
            UIManager.put("Button.arc", 15);
            UIManager.put("Component.arc", 15);
            UIManager.put("ProgressBar.arc", 15);
            UIManager.put("TextComponent.arc", 15);
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> new MainApp().setVisible(true));
    }
}