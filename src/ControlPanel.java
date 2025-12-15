import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;

class ControlPanel extends JPanel {
    private JButton rollButton;
    private JLabel diceLabel, colorLabel, statusLabel, turnLabel;
    private LuckySnakeLadder game;
    private SoundManager soundManager; // Tambah referensi

    // --- UPDATE: Constructor Menerima SoundManager ---
    public ControlPanel(SoundManager sm) {
        this.soundManager = sm;
        setPreferredSize(new Dimension(getWidth(), 180));
        setBackground(new Color(52, 73, 94));
        setLayout(new GridBagLayout());

        // ... (Kode Layout SAMA) ...
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 15, 5, 15);

        turnLabel = new JLabel("Menunggu Permainan...", SwingConstants.CENTER);
        turnLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        turnLabel.setForeground(Color.WHITE);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 4;
        add(turnLabel, gbc);

        JPanel buttonPanel = new JPanel(new GridLayout(1, 1, 5, 10));
        buttonPanel.setOpaque(false);

        rollButton = createStyledButton("KOCOK DADU", new Color(39, 174, 96));
        // --- UPDATE: Play Click Sound di sini (Visual Feedback) ---
        // Logika "Kocok Dadu" (dice.wav) ada di game.playTurn()
        rollButton.addActionListener(e -> {
            if (game != null) {
                // soundManager.playSFX("click.wav"); // Opsional, jika ingin bunyi klik sebelum dadu
                game.playTurn();
            }
        });

        buttonPanel.add(rollButton);

        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1; gbc.gridheight = 2;
        add(buttonPanel, gbc);

        // ... (Sisa komponen dadu/status SAMA) ...
        gbc.gridx = 1; gbc.gridy = 1; gbc.gridheight = 1; gbc.gridwidth = 1;
        add(createLabel("Angka", Color.LIGHT_GRAY), gbc);

        diceLabel = createBoxLabel("?");
        gbc.gridy = 2;
        add(diceLabel, gbc);

        gbc.gridx = 2; gbc.gridy = 1;
        add(createLabel("Arah", Color.LIGHT_GRAY), gbc);

        colorLabel = createBoxLabel("");
        colorLabel.setBackground(Color.DARK_GRAY);
        gbc.gridy = 2;
        add(colorLabel, gbc);

        statusLabel = new JLabel("Siap bermain!");
        statusLabel.setFont(new Font("Segoe UI", Font.ITALIC, 16));
        statusLabel.setForeground(new Color(236, 240, 241));
        gbc.gridx = 3; gbc.gridy = 1; gbc.gridheight = 2;
        add(statusLabel, gbc);
    }

    // ... (Sisa method createButton, label, updateStatus SAMA) ...
    private JButton createStyledButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }
    private JLabel createLabel(String text, Color color) {
        JLabel lbl = new JLabel(text);
        lbl.setForeground(color);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        return lbl;
    }
    private JLabel createBoxLabel(String text) {
        JLabel lbl = new JLabel(text, SwingConstants.CENTER);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lbl.setOpaque(true);
        lbl.setBackground(Color.WHITE);
        lbl.setPreferredSize(new Dimension(60, 60));
        lbl.setBorder(new LineBorder(new Color(44, 62, 80), 2, true));
        return lbl;
    }
    public void setGameReference(LuckySnakeLadder game) { this.game = game; }
    public void setTurnLabel(String text, Color playerColor) {
        turnLabel.setText(text);
        turnLabel.setForeground(playerColor);
    }
    public void updateStatus(String text, Color color, int diceVal, boolean isGreen) {
        statusLabel.setText(text);
        statusLabel.setForeground(color);
        if(diceVal > 0) diceLabel.setText(String.valueOf(diceVal));

        if (isGreen) {
            colorLabel.setBackground(new Color(39, 174, 96));
            colorLabel.setText(">>");
        } else {
            colorLabel.setBackground(new Color(231, 76, 60));
            colorLabel.setText("<<");
        }
        colorLabel.setForeground(Color.WHITE);
    }
    public void disableButtons() { rollButton.setEnabled(false); }
    public void enableButtons() { rollButton.setEnabled(true); }
    public void setGameOver(String msg) {
        disableButtons();
        turnLabel.setText(msg);
        turnLabel.setForeground(new Color(46, 204, 113));
    }
}