import javax.swing.*;
import java.awt.*;

class Tile extends JPanel {
    private int id;
    private boolean p1Present = false;
    private boolean p2Present = false;
    private boolean hasBonus = false;
    private int bonusPoint = 0;

    public Tile(int id, boolean hasBonus, int bonusPoint) {
        this.id = id;
        this.hasBonus = hasBonus;
        this.bonusPoint = bonusPoint;

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
    // #bonus node
    public void setBonusStatus(boolean hasBonus, int bonusPoint) {
        this.hasBonus = hasBonus;
        this.bonusPoint = bonusPoint;
        repaint();
    } // #bonus node

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        // Draw bonus indicator #bonus node
        if (hasBonus) {
            // Star background
            g2d.setColor(new Color(255, 215, 0, 100)); // Gold with transparency
            g2d.fillRect(0, 0, w, h);

            // Draw star icon
            drawStar(g2d, w - 18, 18, 8, new Color(241, 196, 15));

            // Draw bonus point value
            g2d.setColor(new Color(241, 196, 15));
            g2d.setFont(new Font("Segoe UI", Font.BOLD, 10));
            String bonusText = "+" + bonusPoint;
            FontMetrics fm = g2d.getFontMetrics();
            g2d.drawString(bonusText, w - 18 - fm.stringWidth(bonusText)/2, 28);
        }

        // Shadow for pawns #bonus node

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
    // #bonus node
    private void drawStar(Graphics2D g2, int cx, int cy, int radius, Color color) {
        int[] xPoints = new int[10];
        int[] yPoints = new int[10];

        double angle = Math.PI / 2; // Start from top
        double angleStep = Math.PI / 5;

        for (int i = 0; i < 10; i++) {
            int r = (i % 2 == 0) ? radius : radius / 2;
            xPoints[i] = cx + (int)(r * Math.cos(angle));
            yPoints[i] = cy - (int)(r * Math.sin(angle));
            angle -= angleStep;
        }

        g2.setColor(color);
        g2.fillPolygon(xPoints, yPoints, 10);

        // Border
        g2.setColor(new Color(243, 156, 18));
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawPolygon(xPoints, yPoints, 10);
    } //#bonus node

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