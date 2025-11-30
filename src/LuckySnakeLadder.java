import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import javax.swing.Timer;

public class LuckySnakeLadder extends JPanel {

    private BoardPanel boardPanel;
    private ControlPanel controlPanel;
    private MainApp mainApp;

    private Map<Integer, Integer> ladders = new HashMap<>();
    private Map<Integer, Integer> bonusNodes = new HashMap<>();

    private Stack<Integer> p1History = new Stack<>();
    private Stack<Integer> p2History = new Stack<>();
    private Queue<Integer> p1AnimQueue = new LinkedList<>();
    private Queue<Integer> p2AnimQueue = new LinkedList<>();

    private int p1VisualPos = 1;
    private int p2VisualPos = 1;
    private int p1Score = 0;
    private int p2Score = 0;
    private JLabel scoreLabel;
    private int currentPlayer = 1;
    private boolean isAnimating = false;
    private Timer animTimer;

    private String p1Name;
    private String p2Name;

    private int lastDiceRoll = 0;
    private int posBeforeTurn = 1;
    private boolean ladderWasUsedInTurn = false;

    private final int MAX_TILE = 64;

    public LuckySnakeLadder(String p1Name, String p2Name, MainApp mainApp) {
        this.p1Name = p1Name;
        this.p2Name = p2Name;
        this.mainApp = mainApp;

        setLayout(new BorderLayout());
        setBackground(new Color(44, 62, 80));

        generateSpiralLadders();
        generateRandomBonusNodes();

        p1History.push(1);
        p2History.push(1);

        boardPanel = new BoardPanel(ladders, bonusNodes);

        add(boardPanel, BorderLayout.CENTER);

        controlPanel = new ControlPanel();
        add(controlPanel, BorderLayout.SOUTH);

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setBackground(new Color(44, 62, 80));
        JButton btnBack = new JButton("<< Menu");
        btnBack.setBackground(new Color(52, 73, 94));
        btnBack.setForeground(Color.WHITE);
        btnBack.addActionListener(e -> mainApp.showCard("MENU"));
        topPanel.add(btnBack);

        scoreLabel = new JLabel("  |  " + p1Name + ": 0 pts  |  " + p2Name + ": 0 pts");
        scoreLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        scoreLabel.setForeground(new Color(241, 196, 15));
        topPanel.add(scoreLabel);

        add(topPanel, BorderLayout.NORTH);

        boardPanel.updatePositions(1, 1);
        updateTurnLabel();

        controlPanel.setGameReference(this);

        animTimer = new Timer(200, e -> processAnimation());
    }

    // ... (SISA CODE DI BAWAHNYA SAMA PERSIS SEPERTI SEBELUMNYA) ...
    // Copy paste method generateSpiralLadders() sampai class PlayerScore
    // dari kode sebelumnya ke sini. Tidak ada perubahan logika game, hanya UI.

    private void generateSpiralLadders() {
        Random rand = new Random();
        ladders.clear();
        while (ladders.size() < 5) {
            int start = rand.nextInt(MAX_TILE - 15) + 1;
            int jump = rand.nextInt(15) + 10;
            int end = start + jump;
            if (end > MAX_TILE) end = MAX_TILE;
            if (start == 1 || start >= MAX_TILE) continue;
            if (ladders.containsKey(start) || ladders.containsValue(start)) continue;
            if (ladders.containsKey(end) || ladders.containsValue(end)) continue;
            ladders.put(start, end);
        }
    }

    private void generateRandomBonusNodes() {
        Random rand = new Random();
        bonusNodes.clear();
        int bonusCount = rand.nextInt(6) + 5;
        Set<Integer> usedNodes = new HashSet<>();
        usedNodes.add(1);
        usedNodes.add(MAX_TILE);
        usedNodes.addAll(ladders.keySet());
        usedNodes.addAll(ladders.values());

        while (bonusNodes.size() < bonusCount) {
            int node = rand.nextInt(MAX_TILE - 2) + 2;
            if (!usedNodes.contains(node)) {
                bonusNodes.put(node, rand.nextInt(5) + 1);
                usedNodes.add(node);
            }
        }
    }

    private void updateScoreDisplay() {
        scoreLabel.setText("  |  " + p1Name + ": " + p1Score + " pts  |  " + p2Name + ": " + p2Score + " pts");
    }

    private void updateTurnLabel() {
        String statusAdd = (lastDiceRoll == 5) ? " (BONUS TURN!)" : "";
        if (currentPlayer == 1) {
            controlPanel.setTurnLabel("Giliran: " + p1Name + statusAdd);
        } else {
            controlPanel.setTurnLabel("Giliran: " + p2Name + statusAdd);
        }
    }

    private boolean isPrime(int n) {
        if (n <= 1) return false;
        for (int i = 2; i <= Math.sqrt(n); i++) {
            if (n % i == 0) return false;
        }
        return true;
    }

    private void triggerAutoDijkstra(int startNode) {
        List<Integer> path = calculateDijkstraPath(startNode);
        boardPanel.setShortestPath(path);
    }

    private List<Integer> calculateDijkstraPath(int startNode) {
        PriorityQueue<int[]> pq = new PriorityQueue<>(Comparator.comparingInt(a -> a[1]));
        Map<Integer, Integer> dist = new HashMap<>();
        Map<Integer, Integer> prev = new HashMap<>();

        for (int i = 1; i <= MAX_TILE; i++) dist.put(i, Integer.MAX_VALUE);

        dist.put(startNode, 0);
        pq.offer(new int[]{startNode, 0});

        while (!pq.isEmpty()) {
            int[] current = pq.poll();
            int u = current[0];
            int d = current[1];

            if (d > dist.get(u)) continue;
            if (u == MAX_TILE) break;

            int walkDest = u + 1;
            if (walkDest <= MAX_TILE) {
                int newDist = d + 1;
                if (newDist < dist.get(walkDest)) {
                    dist.put(walkDest, newDist);
                    prev.put(walkDest, u);
                    pq.offer(new int[]{walkDest, newDist});
                }
            }

            if (ladders.containsKey(u)) {
                int ladderTarget = ladders.get(u);
                int newDist = d;
                if (newDist < dist.get(ladderTarget)) {
                    dist.put(ladderTarget, newDist);
                    prev.put(ladderTarget, u);
                    pq.offer(new int[]{ladderTarget, newDist});
                }
            }
        }

        List<Integer> path = new ArrayList<>();
        Integer curr = MAX_TILE;
        if (dist.get(MAX_TILE) == Integer.MAX_VALUE) return new ArrayList<>();

        while (curr != null) {
            path.add(0, curr);
            curr = prev.get(curr);
        }
        return path;
    }

    public void playTurn() {
        if (isAnimating) return;

        boardPanel.setShortestPath(null);
        posBeforeTurn = (currentPlayer == 1) ? p1VisualPos : p2VisualPos;
        ladderWasUsedInTurn = false;

        Random rand = new Random();
        int steps = rand.nextInt(6) + 1;
        lastDiceRoll = steps;

        boolean isGreen = rand.nextDouble() < 0.7;

        isAnimating = true;
        controlPanel.disableButtons();

        Stack<Integer> currentHistory = (currentPlayer == 1) ? p1History : p2History;
        Queue<Integer> currentQueue = (currentPlayer == 1) ? p1AnimQueue : p2AnimQueue;

        int currentSimulationPos = currentHistory.peek();

        if (isGreen) {
            for (int i = 0; i < steps; i++) {
                int nextStep = currentSimulationPos + 1;
                if (nextStep > MAX_TILE) nextStep = MAX_TILE;

                currentHistory.push(nextStep);
                currentQueue.add(nextStep);
                currentSimulationPos = nextStep;

                if (ladders.containsKey(currentSimulationPos)) {
                    if (isPrime(posBeforeTurn)) {
                        int targetTop = ladders.get(currentSimulationPos);
                        currentHistory.push(targetTop);
                        currentQueue.add(targetTop);
                        currentSimulationPos = targetTop;
                        ladderWasUsedInTurn = true;
                    }
                }

                if (currentSimulationPos == MAX_TILE) break;
            }
        } else {
            for (int i = 0; i < steps; i++) {
                int nextStep = currentSimulationPos - 1;
                if (nextStep < 1) nextStep = 1;
                currentHistory.push(nextStep);
                currentQueue.add(nextStep);
                currentSimulationPos = nextStep;
            }
        }

        String pName = (currentPlayer == 1) ? p1Name : p2Name;
        String act = isGreen ? "MAJU" : "MUNDUR";
        Color col = isGreen ? new Color(39, 174, 96) : new Color(231, 76, 60);

        String msg = pName + " " + act + " " + steps;
        if (steps == 5) msg += " (BONUS!)";

        controlPanel.updateStatus(msg, col, steps, isGreen);

        animTimer.start();
    }

    private void processAnimation() {
        Queue<Integer> currentQueue = (currentPlayer == 1) ? p1AnimQueue : p2AnimQueue;
        if (!currentQueue.isEmpty()) {
            int nextPos = currentQueue.poll();
            if (currentPlayer == 1) p1VisualPos = nextPos;
            else p2VisualPos = nextPos;
            boardPanel.updatePositions(p1VisualPos, p2VisualPos);
        } else {
            finishTurnLogic();
        }
    }

    private void finishTurnLogic() {
        int currentPos = (currentPlayer == 1) ? p1VisualPos : p2VisualPos;

        if (bonusNodes.containsKey(currentPos)) {
            int bonusPoint = bonusNodes.get(currentPos);
            if (currentPlayer == 1) p1Score += bonusPoint;
            else p2Score += bonusPoint;

            updateScoreDisplay();
            String playerName = (currentPlayer == 1) ? p1Name : p2Name;
            JOptionPane.showMessageDialog(this,
                    "⭐ " + playerName + " mendapat BONUS POINT!\n" +
                            "Point: +" + bonusPoint + "\nTotal: " + ((currentPlayer == 1) ? p1Score : p2Score),
                    "Bonus Point!", JOptionPane.INFORMATION_MESSAGE);

            bonusNodes.remove(currentPos);
            boardPanel.updateBonusNodes(bonusNodes);
        }

        if (ladderWasUsedInTurn) {
            controlPanel.updateStatus("TANGGA DIPAKAI!", Color.ORANGE, 0, true);
            triggerAutoDijkstra(currentPos);
        }

        if (currentPos == MAX_TILE) {
            String raceWinner = (currentPlayer == 1) ? p1Name : p2Name;
            animTimer.stop();
            showFinalLeaderboard(raceWinner);
        } else {
            if (lastDiceRoll == 5) {
                JOptionPane.showMessageDialog(this, "Dapat Angka 5! Main lagi!");
            } else {
                currentPlayer = (currentPlayer == 1) ? 2 : 1;
            }
            updateTurnLabel();
            isAnimating = false;
            controlPanel.enableButtons();
            animTimer.stop();
        }
    }

    private void showFinalLeaderboard(String raceWinner) {
        JDialog leaderboardDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "🏆 GAME OVER", true);
        leaderboardDialog.setSize(500, 400);
        leaderboardDialog.setLocationRelativeTo(this);
        leaderboardDialog.setLayout(new BorderLayout());

        // Simpel saja untuk leaderboard
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(new JLabel("Pemenang Balapan: " + raceWinner));
        panel.add(new JLabel("Skor P1: " + p1Score));
        panel.add(new JLabel("Skor P2: " + p2Score));

        JButton btnClose = new JButton("Tutup");
        btnClose.addActionListener(e -> {
            leaderboardDialog.dispose();
            mainApp.showCard("MENU");
        });
        panel.add(btnClose);

        leaderboardDialog.add(panel);
        leaderboardDialog.setVisible(true);
    }

    private static class PlayerScore {
        String name;
        int score;
        PlayerScore(String name, int score) {
            this.name = name;
            this.score = score;
        }
    }
}