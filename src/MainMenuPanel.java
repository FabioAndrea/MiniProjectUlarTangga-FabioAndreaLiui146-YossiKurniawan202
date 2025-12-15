import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class MainMenuPanel extends JPanel {

    public MainMenuPanel(MainApp app, SoundManager soundManager) {
        setLayout(new GridBagLayout());
        // Background ditangani oleh paintComponent, jadi setOpaque false
        setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        // --- JUDUL UTAMA ---
        JLabel titleLabel = new JLabel("ULAR TANGGA");
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 52)); // Font lebih besar
        titleLabel.setForeground(new Color(255, 255, 255)); // Putih Bersih

        // Efek Shadow pada Teks (Sedikit manual dengan Border kosong)
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        gbc.gridy = 0; gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(titleLabel, gbc);

        // --- SUB JUDUL ---
        JLabel subtitleLabel = new JLabel("By Fabio and Yossi");
        subtitleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        subtitleLabel.setForeground(new Color(100, 200, 255)); // Biru Neon
        subtitleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 10, 60, 10); // Jarak ke tombol
        add(subtitleLabel, gbc);

        // --- TOMBOL MAIN ---
        JButton btnPlay = createStyledButton("MULAI PETUALANGAN", new Color(46, 204, 113));
        btnPlay.addActionListener(e -> {
            soundManager.playSFX("click.wav");
            app.showCard("SETUP");
        });
        gbc.gridy = 2;
        gbc.insets = new Insets(10, 50, 15, 50);
        add(btnPlay, gbc);

        // --- TOMBOL KELUAR ---
        JButton btnExit = createStyledButton("KELUAR", new Color(231, 76, 60));
        btnExit.addActionListener(e -> {
            soundManager.playSFX("click.wav");
            System.exit(0);
        });
        gbc.gridy = 3;
        gbc.insets = new Insets(0, 50, 10, 50); // Jarak lebih rapat
        add(btnExit, gbc);

        // --- COPYRIGHT KECIL DI BAWAH ---
        JLabel creditLabel = new JLabel("Created by Fabio & Yossi");
        creditLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        creditLabel.setForeground(new Color(150, 150, 180));
        gbc.gridy = 4;
        gbc.insets = new Insets(50, 10, 10, 10);
        add(creditLabel, gbc);
    }

    // --- 1. VISUAL: Background Galaxy (Sama dengan SetupPanel) ---
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // Anti-aliasing untuk gambar yang lebih halus
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Gradasi Gelap (Deep Space)
        GradientPaint gp = new GradientPaint(
                0, 0, new Color(15, 15, 40),
                getWidth(), getHeight(), new Color(30, 20, 60)
        );
        g2d.setPaint(gp);
        g2d.fillRect(0, 0, getWidth(), getHeight());

        // Gambar Bintang-bintang (Lebih banyak dari SetupPanel agar meriah)
        g2d.setColor(Color.WHITE);
        for (int i = 0; i < 200; i++) {
            int x = (int)(Math.random() * getWidth());
            int y = (int)(Math.random() * getHeight());

            // Variasi ukuran bintang
            int size = (Math.random() > 0.9) ? 2 : 1;

            // Efek kelap-kelip sederhana (transparansi acak)
            int alpha = (int)(Math.random() * 155) + 100; // 100-255
            g2d.setColor(new Color(255, 255, 255, alpha));

            g2d.fillOval(x, y, size, size);
        }
    }

    // --- 2. VISUAL: Tombol Gaya Sci-Fi ---
    private JButton createStyledButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);

        // Border Putih/Neon agar kontras dengan background gelap
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 255, 255, 200), 2),
                BorderFactory.createEmptyBorder(12, 30, 12, 30)
        ));

        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Efek Hover Sederhana (Terang saat mouse masuk)
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                btn.setBackground(bg.brighter());
            }
            public void mouseExited(MouseEvent evt) {
                btn.setBackground(bg);
            }
        });

        return btn;
    }
}