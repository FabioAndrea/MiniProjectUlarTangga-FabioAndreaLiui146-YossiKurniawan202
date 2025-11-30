import javax.swing.*;
import java.awt.*;
import java.awt.geom.Point2D;
import java.util.List;
import java.util.Map;

public class BoardPanel extends JPanel {
    private Tile[] tiles = new Tile[70];
    private Map<Integer, Integer> ladders;
    private Map<Integer, Integer> bonusNodes;
    private List<LuckySnakeLadder.Player> players;
    private List<Integer> shortestPath = null;

    private Point2D.Double[] tilePositions = new Point2D.Double[65];
    private final int CENTER_X = 550;
    private final int CENTER_Y = 300;
    private final int TILE_SIZE = 50;
    private final int TOTAL_TILES = 64;

    public BoardPanel(Map<Integer, Integer> ladders, Map<Integer, Integer> bonusNodes, List<LuckySnakeLadder.Player> players) {
        this.ladders = ladders;
        this.bonusNodes = bonusNodes;
        this.players = players;

        setLayout(null);
        setBackground(new Color(10, 10, 30));

        calculateGalaxySpiralPositions();
        createTiles();
    }

    private void calculateGalaxySpiralPositions() {
        tilePositions[TOTAL_TILES] = new Point2D.Double(CENTER_X, CENTER_Y);
        double currentRadius = 70.0;
        double currentAngle = Math.PI / 2;
        double spacing = 65.0;
        double b = 80.0 / (2 * Math.PI);

        for (int id = TOTAL_TILES - 1; id >= 1; id--) {
            double x = CENTER_X + currentRadius * Math.cos(currentAngle);
            double y = CENTER_Y + currentRadius * Math.sin(currentAngle);
            tilePositions[id] = new Point2D.Double(x, y);

            double dTheta = spacing / currentRadius;
            currentAngle -= dTheta;
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
            int x = (int)(pos.x - TILE_SIZE / 2);
            int y = (int)(pos.y - TILE_SIZE / 2);

            if (id == TOTAL_TILES) {
                int bigSize = TILE_SIZE + 20;
                tile.setBounds((int)(pos.x - bigSize/2), (int)(pos.y - bigSize/2), bigSize, bigSize);
            } else {
                tile.setBounds(x, y, TILE_SIZE, TILE_SIZE);
            }
            add(tile);
        }
    }

    public void updatePositions() {
        for(int i=1; i<=TOTAL_TILES; i++) {
            if(tiles[i] != null) tiles[i].clearPlayers();
        }
        for(LuckySnakeLadder.Player p : players) {
            if(p.position <= TOTAL_TILES) {
                tiles[p.position].addPlayer(p);
            }
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

        // Bintang
        g2d.setColor(Color.WHITE);
        for (int i = 0; i < 250; i++) {
            int x = (int)(Math.random() * getWidth());
            int y = (int)(Math.random() * getHeight());
            g2d.fillOval(x, y, (int)(Math.random()*2)+1, (int)(Math.random()*2)+1);
        }

        // Jalur
        g2d.setStroke(new BasicStroke(2f));
        g2d.setColor(new Color(255, 255, 255, 40));
        for (int i = 1; i < TOTAL_TILES; i++) {
            Point2D.Double p1 = tilePositions[i];
            Point2D.Double p2 = tilePositions[i+1];
            if (p1 != null && p2 != null) g2d.drawLine((int)p1.x, (int)p1.y, (int)p2.x, (int)p2.y);
        }

        // Tangga
        if (ladders != null) {
            for (Map.Entry<Integer, Integer> entry : ladders.entrySet()) {
                Point2D.Double p1 = tilePositions[entry.getKey()];
                Point2D.Double p2 = tilePositions[entry.getValue()];
                g2d.setColor(new Color(0, 255, 255, 120));
                float[] dash = {10f, 10f};
                g2d.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10f, dash, 0f));
                g2d.drawLine((int)p1.x, (int)p1.y, (int)p2.x, (int)p2.y);
            }
        }
    }
}