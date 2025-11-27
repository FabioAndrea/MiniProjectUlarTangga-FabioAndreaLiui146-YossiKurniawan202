import javax.swing.*;
import java.awt.*;

class Tile extends JPanel {
    private int id;
    private boolean p1Present = false;
    private boolean p2Present = false;

    public Tile(int id) {
        this.id = id;

        // Desain Tile Checkerboard Modern
        if (id % 2 == 0) setBackground(new Color(236, 240, 241)); // Putih Tulang
        else setBackground(new Color(189, 195, 199)); // Abu-abu Silver

        setLayout(new BorderLayout());

        JLabel numLabel = new JLabel(String.valueOf(id), SwingConstants.LEFT);
        numLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        numLabel.setForeground(new Color(127, 140, 141));
        numLabel.setBorder(BorderFactory.createEmptyBorder(2, 4, 0, 0));
        add(numLabel, BorderLayout.NORTH);
    }

    public void setPlayers(boolean p1, boolean p2) {
        this.p1Present = p1;
        this.p2Present = p2;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        // Shadow Pion
        if (p1Present || p2Present) {
            g2d.setColor(new Color(0,0,0,30));
            g2d.fillOval(w/2 - 10, h/2 + 10, 24, 8);
        }

        if (p1Present) {
            int offsetX = p2Present ? -8 : 0;
            drawPawn(g2d, w/2 + offsetX, h/2 + 5, new Color(52, 152, 219), "1"); // Biru Modern
        }

        if (p2Present) {
            int offsetX = p1Present ? 8 : 0;
            drawPawn(g2d, w/2 + offsetX, h/2 + 5, new Color(231, 76, 60), "2"); // Merah Modern
        }
    }

    private void drawPawn(Graphics2D g2, int cx, int cy, Color color, String label) {
        int size = 22;
        int[] xPoints = {cx - size/2, cx + size/2, cx};
        int[] yPoints = {cy + size/2, cy + size/2, cy - size/4};

        g2.setColor(color);
        g2.fillPolygon(xPoints, yPoints, 3);

        int headR = size / 2 + 6;
        g2.fillOval(cx - headR/2, cy - size/2 - headR/2, headR, headR);

        // Border Pion
        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawPolygon(xPoints, yPoints, 3);
        g2.drawOval(cx - headR/2, cy - size/2 - headR/2, headR, headR);

        // Nomor
        g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(label, cx - fm.stringWidth(label)/2, cy - size/2 + 4);
    }
}