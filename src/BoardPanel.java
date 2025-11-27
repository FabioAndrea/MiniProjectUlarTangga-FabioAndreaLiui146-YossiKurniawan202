import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;

class BoardPanel extends JPanel {
    private Tile[] tiles = new Tile[65]; // Array size 64 + 1 (index 0 unused)
    private Map<Integer, Integer> ladders;
    private Map<Integer, Integer> bonusNodes; // Node -> Bonus Point #bonus node
    private List<Integer> shortestPath = null;

    public BoardPanel(Map<Integer, Integer> ladders, Map<Integer, Integer> bonusNodes) {
        this.ladders = ladders;
        this.bonusNodes = bonusNodes;
        setLayout(new GridLayout(8, 8, 2, 2)); // UBAH KE 8x8
        setBackground(new Color(44, 62, 80));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Generate Tiles 8x8
        // Row 7 (Atas) sampai Row 0 (Bawah)
        for (int row = 7; row >= 0; row--) {
            if (row % 2 == 0) {
                // Baris Genap: Kiri ke Kanan
                for (int col = 0; col < 8; col++) {
                    int id = row * 8 + col + 1;
                    addTile(id);
                }
            } else {
                // Baris Ganjil: Kanan ke Kiri
                for (int col = 7; col >= 0; col--) {
                    int id = row * 8 + col + 1;
                    addTile(id);
                }
            }
        }
    }

    private void addTile(int id) {
        boolean hasBonus = bonusNodes.containsKey(id); //#bonus node
        int bonusPoint = hasBonus ? bonusNodes.get(id) : 0; //#bonus node
        Tile tile = new Tile(id, hasBonus, bonusPoint);
        tiles[id] = tile;
        add(tile);
    }

    public void updatePositions(int p1, int p2) {
        for (int i = 1; i <= 64; i++) { // Update loop limit ke 64
            tiles[i].setPlayers(i == p1, i == p2);
        }
        repaint();
    }

    //#bonus node
    public void updateBonusNodes(Map<Integer, Integer> newBonusNodes) {
        this.bonusNodes = newBonusNodes;
        for (int i = 1; i <= 64; i++) {
            boolean hasBonus = bonusNodes.containsKey(i);
            int bonusPoint = hasBonus ? bonusNodes.get(i) : 0;
            tiles[i].setBonusStatus(hasBonus, bonusPoint);
        }
        repaint();
    }//#bonus node

    public void setShortestPath(List<Integer> path) {
        this.shortestPath = path;
        repaint();
    }

    @Override
    protected void paintChildren(Graphics g) {
        super.paintChildren(g);

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Gambar Tangga
        if (ladders != null) {
            g2d.setStroke(new BasicStroke(6));
            for (Map.Entry<Integer, Integer> entry : ladders.entrySet()) {
                int startId = entry.getKey();
                int endId = entry.getValue();
                Point p1 = getTileCenter(startId);
                Point p2 = getTileCenter(endId);

                if (p1 != null && p2 != null) {
                    g2d.setColor(new Color(0,0,0, 50));
                    g2d.drawLine(p1.x+3, p1.y+3, p2.x+3, p2.y+3);

                    g2d.setColor(new Color(230, 126, 34));
                    g2d.drawLine(p1.x, p1.y, p2.x, p2.y);

                    // Indikator Naik
                    g2d.setColor(new Color(46, 204, 113));
                    g2d.fillOval(p1.x - 12, p1.y - 12, 24, 24);

                    // Panah arah di tengah
                    int mx = (p1.x + p2.x)/2;
                    int my = (p1.y + p2.y)/2;
                    g2d.setColor(Color.WHITE);
                    g2d.fillOval(mx-4, my-4, 8, 8);
                }
            }
        }
    }

    private Point getTileCenter(int id) {
        if (id < 1 || id > 64) return null; // Limit 64
        Component c = tiles[id];
        if (c != null) {
            Rectangle bounds = c.getBounds();
            return new Point(bounds.x + bounds.width / 2, bounds.y + bounds.height / 2);
        }
        return null;
    }
}