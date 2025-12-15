import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class SetupPanel extends JPanel {
    private JComboBox<Integer> playerCountCombo;
    private JTextField[] nameFields;
    private JLabel[] labels;

    // --- UPDATE: Constructor Menerima SoundManager ---
    public SetupPanel(MainApp app, SoundManager soundManager) {
        setLayout(new GridBagLayout());

        // Hapus background color solid, karena kita akan gambar sendiri di paintComponent
        setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;

        // --- TITLE ---
        JLabel lblTitle = new JLabel("PENGATURAN PEMAIN");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 32));
        lblTitle.setForeground(new Color(100, 200, 255)); // Biru Neon Terang
        lblTitle.setHorizontalAlignment(SwingConstants.CENTER);

        // Efek Shadow pada Title agar lebih pop-up
        lblTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        gbc.gridy = 0; gbc.gridwidth = 2;
        add(lblTitle, gbc);

        // --- PILIHAN JUMLAH PEMAIN ---
        gbc.gridy = 1; gbc.gridwidth = 1;
        add(createLabel("Jumlah Pemain:"), gbc);

        Integer[] options = {2, 3, 4};
        playerCountCombo = new JComboBox<>(options);
        playerCountCombo.setFont(new Font("Segoe UI", Font.BOLD, 14));
        playerCountCombo.setBackground(new Color(236, 240, 241));
        playerCountCombo.setFocusable(false);
        gbc.gridx = 1;
        add(playerCountCombo, gbc);

        // --- INPUT NAMA ---
        nameFields = new JTextField[4];
        labels = new JLabel[4];
        String[] defaultNames = {"Player 1", "Player 2", "Player 3", "Player 4"};

        // Warna text label nama pemain (Disesuaikan agar terang)
        Color[] colors = {
                new Color(100, 180, 255), // Biru Muda
                new Color(255, 100, 100), // Merah Muda
                new Color(100, 255, 150), // Hijau Muda
                new Color(255, 230, 100)  // Kuning Muda
        };
        String[] colorNames = {"(Biru)", "(Merah)", "(Hijau)", "(Kuning)"};

        int startRow = 2;
        for (int i = 0; i < 4; i++) {
            gbc.gridy = startRow + i;
            gbc.gridx = 0;

            JLabel lbl = createLabel("Nama " + defaultNames[i] + " " + colorNames[i] + ":");
            lbl.setForeground(colors[i]); // Set warna spesifik
            labels[i] = lbl;
            add(lbl, gbc);

            gbc.gridx = 1;
            nameFields[i] = createTextField(defaultNames[i]);
            add(nameFields[i], gbc);
        }

        // Logic Combo Box
        playerCountCombo.addActionListener(e -> {
            soundManager.playSFX("click.wav");
            updateFields();
        });
        updateFields();

        // --- TOMBOL MULAI ---
        JButton btnStart = new JButton("LUNCURKAN MISI");
        btnStart.setFont(new Font("Segoe UI", Font.BOLD, 18));
        btnStart.setBackground(new Color(46, 204, 113));
        btnStart.setForeground(Color.WHITE);
        btnStart.setPreferredSize(new Dimension(200, 50));
        btnStart.setFocusPainted(false);
        btnStart.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));
        btnStart.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btnStart.addActionListener(e -> {
            soundManager.playSFX("click.wav");
            int count = (int) playerCountCombo.getSelectedItem();
            List<String> names = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                String txt = nameFields[i].getText().trim();
                names.add(txt.isEmpty() ? "Player " + (i+1) : txt);
            }
            app.startGame(names);
        });

        gbc.gridy = 7; gbc.gridx = 0; gbc.gridwidth = 2;
        gbc.insets = new Insets(30, 10, 10, 10);
        add(btnStart, gbc);

        // --- TOMBOL KEMBALI ---
        JButton btnBack = new JButton("<< Kembali ke Main Menu");
        btnBack.setContentAreaFilled(false);
        btnBack.setBorderPainted(false);
        btnBack.setForeground(new Color(200, 200, 255)); // Warna agak biru pucat
        btnBack.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        btnBack.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btnBack.addActionListener(e -> {
            soundManager.playSFX("click.wav");
            app.showCard("MENU");
        });
        gbc.gridy = 8;
        add(btnBack, gbc);
    }

    // --- 1. FITUR VISUAL: Background Galaxy ---
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // Gradasi Gelap (Deep Space)
        GradientPaint gp = new GradientPaint(
                0, 0, new Color(15, 15, 40),
                getWidth(), getHeight(), new Color(30, 20, 60)
        );
        g2d.setPaint(gp);
        g2d.fillRect(0, 0, getWidth(), getHeight());

        // Gambar Bintang-bintang
        g2d.setColor(Color.WHITE);
        for (int i = 0; i < 150; i++) {
            int x = (int)(Math.random() * getWidth());
            int y = (int)(Math.random() * getHeight());
            int size = (int)(Math.random() * 2) + 1;
            g2d.fillOval(x, y, size, size);
        }
    }

    private void updateFields() {
        int count = (int) playerCountCombo.getSelectedItem();
        for (int i = 0; i < 4; i++) {
            boolean visible = i < count;
            nameFields[i].setVisible(visible);
            labels[i].setVisible(visible);
        }
        revalidate(); repaint();
    }

    // --- 2. FITUR VISUAL: Label Terang ---
    private JLabel createLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lbl.setForeground(new Color(220, 220, 255)); // Putih kebiruan agar terbaca di background gelap
        return lbl;
    }

    // --- 3. FITUR VISUAL: TextField Tema Sci-Fi ---
    private JTextField createTextField(String defaultText) {
        JTextField tf = new JTextField(defaultText);
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tf.setPreferredSize(new Dimension(200, 35));

        // Background TextField Gelap
        tf.setBackground(new Color(40, 40, 70));
        // Teks Putih
        tf.setForeground(Color.WHITE);
        // Kursor Putih
        tf.setCaretColor(Color.WHITE);

        // Border Biru Neon
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(100, 150, 255), 1),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));

        return tf;
    }
}