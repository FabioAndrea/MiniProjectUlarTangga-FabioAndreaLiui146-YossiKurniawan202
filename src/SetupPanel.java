import javax.swing.*;
import java.awt.*;
import javax.swing.*;
import java.awt.*;

public class SetupPanel extends JPanel {
    private JTextField p1NameField, p2NameField;

    public SetupPanel(MainApp app) {
        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;

        JLabel lblTitle = new JLabel("🚀 PERSIAPAN MISI");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 32));
        lblTitle.setForeground(new Color(100, 200, 255));
        lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 0; gbc.gridwidth = 2;
        add(lblTitle, gbc);

        gbc.gridy = 2; gbc.gridwidth = 1;
        add(createLabel("Pilot 1 (Biru):"), gbc);

        p1NameField = createTextField("Astronaut 1");
        gbc.gridx = 1;
        add(p1NameField, gbc);

        gbc.gridy = 3; gbc.gridx = 0;
        add(createLabel("Pilot 2 (Merah):"), gbc);

        p2NameField = createTextField("Astronaut 2");
        gbc.gridx = 1;
        add(p2NameField, gbc);

        gbc.gridy = 4; gbc.gridx = 0; gbc.gridwidth = 2;
        gbc.insets = new Insets(40, 10, 10, 10);

        JButton btnStart = new JButton("🌌 LUNCURKAN MISI");
        btnStart.setFont(new Font("Segoe UI", Font.BOLD, 18));
        btnStart.setBackground(new Color(52, 152, 219));
        btnStart.setForeground(Color.WHITE);
        btnStart.setPreferredSize(new Dimension(250, 50));
        btnStart.setFocusPainted(false);
        btnStart.addActionListener(e -> {
            String p1 = p1NameField.getText().trim().isEmpty() ? "Astronaut 1" : p1NameField.getText().trim();
            String p2 = p2NameField.getText().trim().isEmpty() ? "Astronaut 2" : p2NameField.getText().trim();
            app.startGame(p1, p2);
        });
        add(btnStart, gbc);

        gbc.gridy = 5;
        JButton btnBack = new JButton("◀ Kembali");
        btnBack.setForeground(new Color(150, 180, 220));
        btnBack.setContentAreaFilled(false);
        btnBack.setBorderPainted(false);
        btnBack.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnBack.addActionListener(e -> app.showCard("MENU"));
        add(btnBack, gbc);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // Space gradient background
        GradientPaint gp = new GradientPaint(
                0, 0, new Color(15, 15, 40),
                getWidth(), getHeight(), new Color(30, 20, 60)
        );
        g2d.setPaint(gp);
        g2d.fillRect(0, 0, getWidth(), getHeight());

        // Draw stars
        g2d.setColor(Color.WHITE);
        for (int i = 0; i < 100; i++) {
            int x = (int)(Math.random() * getWidth());
            int y = (int)(Math.random() * getHeight());
            int size = (int)(Math.random() * 2) + 1;
            g2d.fillOval(x, y, size, size);
        }
    }

    private JLabel createLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lbl.setForeground(new Color(200, 220, 255));
        return lbl;
    }

    private JTextField createTextField(String defaultText) {
        JTextField tf = new JTextField(defaultText);
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tf.setPreferredSize(new Dimension(200, 35));
        tf.setBackground(new Color(30, 30, 60));
        tf.setForeground(new Color(200, 220, 255));
        tf.setCaretColor(new Color(200, 220, 255));
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(100, 150, 255), 2),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        return tf;
    }
}