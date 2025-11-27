import javax.swing.*;
import java.awt.*;

public class SetupPanel extends JPanel {
    private JTextField p1NameField, p2NameField;

    public SetupPanel(MainApp app) {
        setLayout(new GridBagLayout());
        setBackground(new Color(236, 240, 241));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;

        JLabel lblTitle = new JLabel("PENGATURAN PEMAIN");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblTitle.setForeground(new Color(44, 62, 80));
        lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 0; gbc.gridwidth = 2;
        add(lblTitle, gbc);

        gbc.gridy = 2; gbc.gridwidth = 1;
        add(createLabel("Nama Player 1 (Biru):"), gbc);

        p1NameField = createTextField("Player 1");
        gbc.gridx = 1;
        add(p1NameField, gbc);

        gbc.gridy = 3; gbc.gridx = 0;
        add(createLabel("Nama Player 2 (Merah):"), gbc);

        p2NameField = createTextField("Player 2");
        gbc.gridx = 1;
        add(p2NameField, gbc);

        gbc.gridy = 4; gbc.gridx = 0; gbc.gridwidth = 2;
        gbc.insets = new Insets(40, 10, 10, 10);

        JButton btnStart = new JButton("MULAI PERMAINAN");
        btnStart.setFont(new Font("Segoe UI", Font.BOLD, 18));
        btnStart.setBackground(new Color(52, 152, 219));
        btnStart.setForeground(Color.WHITE);
        btnStart.setPreferredSize(new Dimension(200, 50));
        btnStart.addActionListener(e -> {
            String p1 = p1NameField.getText().trim().isEmpty() ? "Player 1" : p1NameField.getText().trim();
            String p2 = p2NameField.getText().trim().isEmpty() ? "Player 2" : p2NameField.getText().trim();
            app.startGame(p1, p2);
        });
        add(btnStart, gbc);

        gbc.gridy = 5;
        JButton btnBack = new JButton("Kembali");
        btnBack.setForeground(Color.GRAY);
        btnBack.setContentAreaFilled(false);
        btnBack.setBorderPainted(false);
        btnBack.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnBack.addActionListener(e -> app.showCard("MENU"));
        add(btnBack, gbc);
    }

    private JLabel createLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lbl.setForeground(new Color(44, 62, 80));
        return lbl;
    }

    private JTextField createTextField(String defaultText) {
        JTextField tf = new JTextField(defaultText);
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tf.setPreferredSize(new Dimension(200, 35));
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(189, 195, 199)),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        return tf;
    }
}