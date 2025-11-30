import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class SetupPanel extends JPanel {
    private JComboBox<Integer> playerCountCombo;
    private JTextField[] nameFields;
    private JLabel[] labels;

    public SetupPanel(MainApp app) {
        setLayout(new GridBagLayout());
        setBackground(new Color(236, 240, 241));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;

        JLabel lblTitle = new JLabel("PENGATURAN PEMAIN");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblTitle.setForeground(new Color(44, 62, 80));
        lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 0; gbc.gridwidth = 2;
        add(lblTitle, gbc);

        gbc.gridy = 1; gbc.gridwidth = 1;
        add(createLabel("Jumlah Pemain:"), gbc);

        Integer[] options = {2, 3, 4};
        playerCountCombo = new JComboBox<>(options);
        playerCountCombo.setFont(new Font("Segoe UI", Font.BOLD, 14));
        gbc.gridx = 1;
        add(playerCountCombo, gbc);

        nameFields = new JTextField[4];
        labels = new JLabel[4];
        String[] defaultNames = {"Player 1", "Player 2", "Player 3", "Player 4"};
        Color[] colors = {new Color(52, 152, 219), new Color(231, 76, 60), new Color(46, 204, 113), new Color(241, 196, 15)};
        String[] colorNames = {"(Biru)", "(Merah)", "(Hijau)", "(Kuning)"};

        int startRow = 2;
        for (int i = 0; i < 4; i++) {
            gbc.gridy = startRow + i;
            gbc.gridx = 0;
            JLabel lbl = createLabel("Nama " + defaultNames[i] + " " + colorNames[i] + ":");
            lbl.setForeground(colors[i]);
            labels[i] = lbl;
            add(lbl, gbc);

            gbc.gridx = 1;
            nameFields[i] = createTextField(defaultNames[i]);
            add(nameFields[i], gbc);
        }

        playerCountCombo.addActionListener(e -> updateFields());
        updateFields();

        JButton btnStart = new JButton("MULAI PERMAINAN");
        btnStart.setFont(new Font("Segoe UI", Font.BOLD, 18));
        btnStart.setBackground(new Color(52, 73, 94));
        btnStart.setForeground(Color.WHITE);
        btnStart.setPreferredSize(new Dimension(200, 50));
        btnStart.addActionListener(e -> {
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

        JButton btnBack = new JButton("Kembali");
        btnBack.setContentAreaFilled(false);
        btnBack.setBorderPainted(false);
        btnBack.setForeground(Color.GRAY);
        btnBack.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnBack.addActionListener(e -> app.showCard("MENU"));
        gbc.gridy = 8;
        add(btnBack, gbc);
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

    private JLabel createLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        return lbl;
    }

    private JTextField createTextField(String defaultText) {
        JTextField tf = new JTextField(defaultText);
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tf.setPreferredSize(new Dimension(200, 35));
        return tf;
    }
}