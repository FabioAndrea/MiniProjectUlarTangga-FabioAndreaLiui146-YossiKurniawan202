import javax.swing.*;
import java.awt.*;

public class MainMenuPanel extends JPanel {

    // --- UPDATE: Constructor Menerima SoundManager ---
    public MainMenuPanel(MainApp app, SoundManager soundManager) {
        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        JLabel titleLabel = new JLabel("ULAR TANGGA");
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 48));
        titleLabel.setForeground(Color.WHITE);

        gbc.gridy = 0; gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(titleLabel, gbc);

        JLabel subtitleLabel = new JLabel("By Fabio And Yossi");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        subtitleLabel.setForeground(new Color(236, 240, 241));
        subtitleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 10, 60, 10);
        add(subtitleLabel, gbc);

        JButton btnPlay = createStyledButton("MAIN SEKARANG", new Color(46, 204, 113));
        // --- UPDATE: Play Click Sound ---
        btnPlay.addActionListener(e -> {
            soundManager.playSFX("click.wav");
            app.showCard("SETUP");
        });
        gbc.gridy = 2;
        gbc.insets = new Insets(10, 50, 10, 50);
        add(btnPlay, gbc);

        JButton btnExit = createStyledButton("KELUAR", new Color(231, 76, 60));
        // --- UPDATE: Play Click Sound ---
        btnExit.addActionListener(e -> {
            soundManager.playSFX("click.wav");
            System.exit(0);
        });
        gbc.gridy = 3;
        add(btnExit, gbc);
    }

    // ... (paintComponent & createStyledButton sama seperti sebelumnya) ...
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        GradientPaint gp = new GradientPaint(0, 0, new Color(44, 62, 80), getWidth(), getHeight(), new Color(22, 160, 133));
        g2d.setPaint(gp);
        g2d.fillRect(0, 0, getWidth(), getHeight());
    }

    private JButton createStyledButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 18));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(15, 40, 15, 40));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }
}