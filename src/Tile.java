import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;

public class Tile extends JPanel {
    private int id;
    private boolean p1Present = false;
    private boolean p2Present = false;
    private boolean hasBonus = false;
    private int bonusPoint = 0;

    public Tile(int id, boolean hasBonus, int bonusPoint) {
        this.id = id;
        this.hasBonus = hasBonus;
        this.bonusPoint = bonusPoint;

        setOpaque(false);
        setLayout(null);
    }

    public void setPlayers(boolean p1, boolean p2) {
        this.p1Present = p1;
        this.p2Present = p2;
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
        int size = Math.min(w, h) - 4; // Margin biar tidak terpotong

        int cx = w / 2;
        int cy = h / 2;

        // --- 1. Background Node (Lingkaran) ---
        if (id == 64) {
            g2d.setColor(new Color(255, 215, 0, 220)); // Emas Solid (Finish)
        } else if (id == 1) {
            g2d.setColor(new Color(46, 204, 113, 220)); // Hijau Solid (Start)
        } else if (hasBonus) {
            g2d.setColor(new Color(100, 50, 200, 200)); // Ungu terang jika ada Bonus
        } else {
            g2d.setColor(new Color(30, 30, 60, 200)); // Biru Gelap (Biasa)
        }
        g2d.fillOval(cx - size / 2, cy - size / 2, size, size);

        // --- 2. Frame Putih ---
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(2f));
        g2d.drawOval(cx - size / 2, cy - size / 2, size, size);

        // --- 3. Teks di Tengah (LOGIKA BARU) ---
        String text;
        Font font;
        Color textColor;

        if (id == 64) {
            text = "FINISH";
            font = new Font("Segoe UI", Font.BOLD, 10);
            textColor = Color.BLACK;
        } else if (id == 1) {
            text = "START";
            font = new Font("Segoe UI", Font.BOLD, 10);
            textColor = Color.WHITE;
        } else if (hasBonus) {
            // === INI YANG DIMINTA ===
            // Tampilkan Bonus Point, bukan ID Node
            text = "+" + bonusPoint;
            font = new Font("Segoe UI", Font.BOLD, 18); // Font Besar
            textColor = new Color(255, 215, 0); // Warna Emas
        } else {
            // Jika tidak ada bonus, tampilkan ID kecil saja sebagai penanda
            text = String.valueOf(id);
            font = new Font("Segoe UI", Font.PLAIN, 12); // Font Biasa
            textColor = new Color(200, 200, 200); // Abu-abu terang
        }

        g2d.setFont(font);
        g2d.setColor(textColor);
        FontMetrics fm = g2d.getFontMetrics();
        int tx = cx - fm.stringWidth(text) / 2;
        int ty = cy + fm.getAscent() / 2 - 2;

        // Geser teks sedikit jika ada pemain agar tidak tertumpuk
        if (p1Present || p2Present) {
            ty -= 6;
        }

        g2d.drawString(text, tx, ty);

        // --- 4. Tanda Pemain (Kecil di bawah teks) ---
        if (p1Present) {
            drawPlayer(g2d, cx - 8, cy + 10, new Color(52, 152, 219)); // Biru
        }
        if (p2Present) {
            drawPlayer(g2d, cx + 8, cy + 10, new Color(231, 76, 60)); // Merah
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