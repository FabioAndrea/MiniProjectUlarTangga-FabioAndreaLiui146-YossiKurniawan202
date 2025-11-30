import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Tile extends JPanel {
    private int id;
    private boolean hasBonus = false;
    private int bonusPoint = 0;

    // List player untuk render banyak pion
    private List<LuckySnakeLadder.Player> playersOnTile = new ArrayList<>();

    public Tile(int id, boolean hasBonus, int bonusPoint) {
        this.id = id;
        this.hasBonus = hasBonus;
        this.bonusPoint = bonusPoint;
        setOpaque(false);
        setLayout(null);
    }

    public void clearPlayers() {
        playersOnTile.clear();
        repaint();
    }

    public void addPlayer(LuckySnakeLadder.Player p) {
        playersOnTile.add(p);
        repaint();
    }

    public void setBonusStatus(boolean hasBonus, int bonusPoint) {
        this.hasBonus = hasBonus;
        this.bonusPoint = bonusPoint;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();
        int size = Math.min(w, h) - 4;
        int cx = w / 2;
        int cy = h / 2;

        if (id == 64) g2d.setColor(new Color(255, 215, 0, 220));
        else if (id == 1) g2d.setColor(new Color(46, 204, 113, 220));
        else if (hasBonus) g2d.setColor(new Color(100, 50, 200, 200));
        else g2d.setColor(new Color(30, 30, 60, 200));

        g2d.fillOval(cx - size / 2, cy - size / 2, size, size);
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(2f));
        g2d.drawOval(cx - size / 2, cy - size / 2, size, size);

        String text;
        Font font;
        Color textColor = Color.WHITE;

        if (id == 64) { text = "FINISH"; font = new Font("Segoe UI", Font.BOLD, 10); textColor = Color.BLACK; }
        else if (id == 1) { text = "START"; font = new Font("Segoe UI", Font.BOLD, 10); }
        else if (hasBonus) { text = "+" + bonusPoint; font = new Font("Segoe UI", Font.BOLD, 18); textColor = Color.YELLOW; }
        else { text = String.valueOf(id); font = new Font("Segoe UI", Font.PLAIN, 12); textColor = Color.LIGHT_GRAY; }

        g2d.setFont(font);
        g2d.setColor(textColor);
        FontMetrics fm = g2d.getFontMetrics();
        int tx = cx - fm.stringWidth(text) / 2;
        int ty = cy + fm.getAscent() / 2 - 2;

        if(!playersOnTile.isEmpty()) ty -= 8;
        g2d.drawString(text, tx, ty);

        // Render hingga 4 pemain
        int offset = 10;
        for (LuckySnakeLadder.Player p : playersOnTile) {
            int px = cx, py = cy + 5;
            switch(p.id) {
                case 0: px -= offset; py -= offset/2; break; // P1
                case 1: px += offset; py -= offset/2; break; // P2
                case 2: px -= offset; py += offset; break;   // P3
                case 3: px += offset; py += offset; break;   // P4
            }
            drawPlayer(g2d, px, py, p.color);
        }
    }

    private void drawPlayer(Graphics2D g2d, int x, int y, Color c) {
        g2d.setColor(c);
        g2d.fillOval(x - 5, y - 5, 10, 10);
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(1f));
        g2d.drawOval(x - 5, y - 5, 10, 10);
    }
}