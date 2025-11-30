import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.util.List;
import java.util.Map;

public class BoardPanel extends JPanel {
    private Tile[] tiles = new Tile[65];
    private Map<Integer, Integer> ladders;
    private Map<Integer, Integer> bonusNodes;
    private List<Integer> shortestPath = null;

    private Point2D.Double[] tilePositions = new Point2D.Double[65];

    // --- SETUP UKURAN FIT SCREEN ---
    private final int CENTER_X = 550;

    // --- PERBAIKAN DISINI: CENTER_Y DINAIKKAN ---
    // Sebelumnya 360, diubah menjadi 300 agar naik dan tidak terpotong di bawah.
    private final int CENTER_Y = 300;

    private final int TILE_SIZE = 50;
    private final int TOTAL_TILES = 64;

    public BoardPanel(Map<Integer, Integer> ladders, Map<Integer, Integer> bonusNodes) {
        this.ladders = ladders;
        this.bonusNodes = bonusNodes;
        setLayout(null);
        setBackground(new Color(10, 10, 30));
        setBorder(null);

        // Ukuran preferensi disesuaikan dengan area yang tersedia
        setPreferredSize(new Dimension(1100, 700));

        calculateGalaxySpiralPositions();
        createTiles();
    }

    private void calculateGalaxySpiralPositions() {
        // --- LOGIKA: CONSTANT GAP SPIRAL (INSIDE-OUT) ---

        // 1. Letakkan Node 64 (Finish) TEPAT di tengah
        tilePositions[TOTAL_TILES] = new Point2D.Double(CENTER_X, CENTER_Y);

        // Parameter Spiral
        // Radius awal sedikit lebih besar agar node 63 tidak terlalu dekat dengan 64
        double currentRadius = 70.0;
        double currentAngle = Math.PI / 2; // Mulai dari atas (agar ekor spiral berakhir di bawah)

        // Jarak konstan antar titik tengah node (Tile 50px + Spasi 15px)
        double spacing = 65.0;

        // Konstanta lebar jalur antar putaran.
        double laneWidth = 80.0;
        double b = laneWidth / (2 * Math.PI);

        // 2. Loop MUNDUR dari 63 ke 1 (Bergerak dari dalam ke luar)
        for (int id = TOTAL_TILES - 1; id >= 1; id--) {

            // Hitung posisi X dan Y
            double x = CENTER_X + currentRadius * Math.cos(currentAngle);
            double y = CENTER_Y + currentRadius * Math.sin(currentAngle);
            tilePositions[id] = new Point2D.Double(x, y);

            // --- HITUNG POSISI NODE BERIKUTNYA (KE LUAR) ---

            // Hitung perubahan sudut agar jarak busur tetap sama dengan 'spacing'
            double dTheta = spacing / currentRadius;

            // Update sudut (putar spiral berlawanan arah jarum jam)
            currentAngle -= dTheta;

            // Update radius (linear growth)
            // Kita gunakan sudut absolut untuk perhitungan radius agar radius selalu positif bertambah
            // Kita tambahkan offset pada sudut agar radius awal pas di 70.0
            double angleForRadius = (Math.PI / 2) - currentAngle;
            currentRadius = 70.0 + (b * angleForRadius);
        }
    }

    private void createTiles() {
        for (int id = 1; id <= TOTAL_TILES; id++) {
            boolean hasBonus = bonusNodes.containsKey(id);
            int bonusPoint = hasBonus ? bonusNodes.get(id) : 0;
            Tile tile = new Tile(id, hasBonus, bonusPoint);
            tiles[id] = tile;

            Point2D.Double pos = tilePositions[id];

            // Tengahkan Tile
            int x = (int)(pos.x - TILE_SIZE / 2);
            int y = (int)(pos.y - TILE_SIZE / 2);

            // Khusus Tile FINISH (64), buat sedikit lebih besar & efek khusus
            if (id == TOTAL_TILES) {
                int bigSize = TILE_SIZE + 20; // Lebih besar
                tile.setBounds((int)(pos.x - bigSize/2), (int)(pos.y - bigSize/2), bigSize, bigSize);
            } else {
                tile.setBounds(x, y, TILE_SIZE, TILE_SIZE);
            }

            add(tile);
        }
    }

    public void updatePositions(int p1, int p2) {
        for (int i = 1; i <= TOTAL_TILES; i++) {
            if (tiles[i] != null) tiles[i].setPlayers(i == p1, i == p2);
        }
        repaint();
    }

    public void updateBonusNodes(Map<Integer, Integer> newBonusNodes) {
        this.bonusNodes = newBonusNodes;
        for (int i = 1; i <= TOTAL_TILES; i++) {
            if (tiles[i] != null) {
                boolean hasBonus = bonusNodes.containsKey(i);
                int bonusPoint = hasBonus ? bonusNodes.get(i) : 0;
                tiles[i].setBonusStatus(hasBonus, bonusPoint);
            }
        }
        repaint();
    }

    public void setShortestPath(List<Integer> path) {
        this.shortestPath = path;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Background Bintang
        g2d.setColor(Color.WHITE);
        for (int i = 0; i < 250; i++) {
            int x = (int)(Math.random() * getWidth());
            int y = (int)(Math.random() * getHeight());
            int size = (int)(Math.random() * 2) + 1;
            g2d.fillOval(x, y, size, size);
        }

        drawGalaxyPath(g2d);
    }

    private void drawGalaxyPath(Graphics2D g2d) {
        // Garis Jalur
        g2d.setStroke(new BasicStroke(2f));
        g2d.setColor(new Color(255, 255, 255, 40));

        for (int i = 1; i < TOTAL_TILES; i++) {
            Point2D.Double p1 = tilePositions[i];
            Point2D.Double p2 = tilePositions[i+1];
            if (p1 != null && p2 != null) {
                g2d.drawLine((int)p1.x, (int)p1.y, (int)p2.x, (int)p2.y);
            }
        }

        // Tangga
        if (ladders != null) {
            for (Map.Entry<Integer, Integer> entry : ladders.entrySet()) {
                Point2D.Double p1 = tilePositions[entry.getKey()];
                Point2D.Double p2 = tilePositions[entry.getValue()];

                // Efek Neon Tangga
                g2d.setColor(new Color(0, 255, 255, 120));
                float[] dash = {10f, 10f};
                g2d.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10f, dash, 0f));
                g2d.drawLine((int)p1.x, (int)p1.y, (int)p2.x, (int)p2.y);
            }
        }

        // Jalur Dijkstra
        if (shortestPath != null && shortestPath.size() > 1) {
            g2d.setColor(Color.YELLOW);
            g2d.setStroke(new BasicStroke(4f));
            for (int i = 0; i < shortestPath.size() - 1; i++) {
                Point2D.Double p1 = tilePositions[shortestPath.get(i)];
                Point2D.Double p2 = tilePositions[shortestPath.get(i+1)];
                if (p1 != null && p2 != null) {
                    g2d.drawLine((int)p1.x, (int)p1.y, (int)p2.x, (int)p2.y);
                }
            }
        }
    }
}