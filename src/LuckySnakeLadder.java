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
    private Map<Integer, Integer> bonusNodes = new HashMap<>(); // Node -> Bonus Point #bonus node

    private Stack<Integer> p1History = new Stack<>();
    private Stack<Integer> p2History = new Stack<>();
    private Queue<Integer> p1AnimQueue = new LinkedList<>();
    private Queue<Integer> p2AnimQueue = new LinkedList<>();

    private int p1VisualPos = 1;
    private int p2VisualPos = 1;
    private int p1Score = 0; // Total bonus point Player 1 #bonus node
    private int p2Score = 0; // Total bonus point Player 2 #bonus node
    private JLabel scoreLabel;
    private int currentPlayer = 1;
    private boolean isAnimating = false;
    private Timer animTimer;

    private String p1Name;
    private String p2Name;

    private int lastDiceRoll = 0;
    private int posBeforeTurn = 1;
    // Flag untuk menandakan apakah turn ini menggunakan tangga (untuk trigger Dijkstra/Efek)
    private boolean ladderWasUsedInTurn = false;

    private final int MAX_TILE = 64;
    private final int GRID_SIZE = 8;

    public LuckySnakeLadder(String p1Name, String p2Name, MainApp mainApp) {
        this.p1Name = p1Name;
        this.p2Name = p2Name;
        this.mainApp = mainApp;

        setLayout(new BorderLayout());
        setBackground(new Color(44, 62, 80));

        generateStrictLadders();
        generateRandomBonusNodes(); // Generate bonus nodes #bonus node

        p1History.push(1);
        p2History.push(1);

        boardPanel = new BoardPanel(ladders, bonusNodes); //#tambahin bonus node
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

        // Score labels #bonus node
        scoreLabel = new JLabel("  |  " + p1Name + ": " + p2Score + "pts  |  " + p2Name + ": " + p1Score + " pts");
        scoreLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        scoreLabel.setForeground(new Color(241, 196, 15));
        topPanel.add(scoreLabel);// #bonus node

        add(topPanel, BorderLayout.NORTH);

        boardPanel.updatePositions(1, 1);
        updateTurnLabel();

        controlPanel.setGameReference(this);

        animTimer = new Timer(200, e -> processAnimation());
    }
    //#bonus node
    private void generateRandomBonusNodes() {
        Random rand = new Random();

        // Jumlah node bonus acak antara 5-10
        int bonusCount = rand.nextInt(6) + 5; // 5 sampai 10 node

        Set<Integer> usedNodes = new HashSet<>();

        // Hindari node 1 (start), node 64 (finish), dan node yang sudah ada tangga
        usedNodes.add(1);
        usedNodes.add(MAX_TILE);
        usedNodes.addAll(ladders.keySet());
        usedNodes.addAll(ladders.values());

        while (bonusNodes.size() < bonusCount) {
            int node = rand.nextInt(MAX_TILE - 2) + 2; // Node 2-63
            if (!usedNodes.contains(node)) {
                int bonusPoint = rand.nextInt(5) + 1; // 1-5 point
                bonusNodes.put(node, bonusPoint);
                usedNodes.add(node);
            }
        }
    }//#bonus node

    private void updateScoreDisplay() {
        scoreLabel.setText("  |  " + p1Name + ": " + p1Score + "pts  |  " + p2Name + ": " + p2Score + " pts");
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

    private void generateStrictLadders() {
        Random rand = new Random();
        ladders.clear();
        while (ladders.size() < 5) {
            int startRow = rand.nextInt(GRID_SIZE - 2);
            int startCol = rand.nextInt(GRID_SIZE);
            int minEndRow = startRow + 1;
            int endRow = rand.nextInt((GRID_SIZE - 1) - minEndRow + 1) + minEndRow;
            int endCol = rand.nextInt(GRID_SIZE);

            int startId = getTileIdFromCoords(startRow, startCol);
            int endId = getTileIdFromCoords(endRow, endCol);

            if (startId > endId) continue;
            if (startId != 1 && startId < MAX_TILE &&
                    !ladders.containsKey(startId) &&
                    !ladders.containsValue(startId) &&
                    startId != endId) {
                ladders.put(startId, endId);
            }
        }
    }

    private int getTileIdFromCoords(int row, int col) {
        if (row % 2 == 0) return (row * GRID_SIZE) + col + 1;
        else return (row * GRID_SIZE) + (GRID_SIZE - 1 - col) + 1;
    }

    // --- LOGIKA UTAMA PERGERAKAN ---
    // --- LOGIKA UTAMA PERGERAKAN ---
    public void playTurn() {
        if (isAnimating) return;

        boardPanel.setShortestPath(null);
        posBeforeTurn = (currentPlayer == 1) ? p1VisualPos : p2VisualPos;
        ladderWasUsedInTurn = false;

        Random rand = new Random();
        int steps = rand.nextInt(6) + 1;
        lastDiceRoll = steps;

        boolean isGreen = rand.nextDouble() < 0.7; // 70% Maju (Hijau)

        isAnimating = true;
        controlPanel.disableButtons();

        Stack<Integer> currentHistory = (currentPlayer == 1) ? p1History : p2History;
        Queue<Integer> currentQueue = (currentPlayer == 1) ? p1AnimQueue : p2AnimQueue;

        // Kita hitung pergerakan langkah demi langkah dari posisi terakhir
        int currentSimulationPos = currentHistory.peek();

        if (isGreen) {
            // --- MAJU (HIJAU) ---
            // Loop sebanyak jumlah dadu
            for (int i = 0; i < steps; i++) {

                // 1. Maju 1 langkah normal
                int nextStep = currentSimulationPos + 1;

                // --- PERBAIKAN DI SINI (Ganti nextPos menjadi nextStep) ---
                if (nextStep > MAX_TILE) nextStep = MAX_TILE;

                // Tambahkan langkah normal ke antrean
                currentHistory.push(nextStep);
                currentQueue.add(nextStep);
                currentSimulationPos = nextStep;

                // 2. CEK INTERUPSI TANGGA (Pass-Through Check)
                if (ladders.containsKey(currentSimulationPos)) {
                    if (isPrime(posBeforeTurn)) {

                        // NAIK TANGGA
                        int targetTop = ladders.get(currentSimulationPos);

                        // Tambahkan langkah "teleport" ke antrean
                        currentHistory.push(targetTop);
                        currentQueue.add(targetTop);

                        // Update posisi simulasi ke atas tangga
                        currentSimulationPos = targetTop;

                        // Tandai tangga dipakai
                        ladderWasUsedInTurn = true;
                    }
                }

                // Jika sudah sampai finish, stop loop sisa langkah
                if (currentSimulationPos == MAX_TILE) break;
            }

        } else {
            // --- MUNDUR (MERAH) ---
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

        // Check bonus node #bonus node
        if (bonusNodes.containsKey(currentPos)) {
            int bonusPoint = bonusNodes.get(currentPos);
            if (currentPlayer == 1) {
                p1Score += bonusPoint;
            } else {
                p2Score += bonusPoint;
            }

            updateScoreDisplay();

            String playerName = (currentPlayer == 1) ? p1Name : p2Name;

            // Show notification
            JOptionPane.showMessageDialog(this,
                    playerName + " mendapat BONUS POINT!\n" +
                            "Point yang diperoleh: +" + bonusPoint + " pts\n" +
                            "Total Score: " + ((currentPlayer == 1) ? p1Score : p2Score) + " pts",
                    "Bonus Point!",
                    JOptionPane.INFORMATION_MESSAGE);


            // Remove bonus node after collected
            bonusNodes.remove(currentPos);
            boardPanel.updateBonusNodes(bonusNodes);
        }//#bonus node

        // --- Feedback Tangga ---
        // Karena logika naik tangga sudah "baked in" di playTurn,
        // di sini kita hanya memberi notifikasi visual/Dijkstra.
        if (ladderWasUsedInTurn) {
            controlPanel.updateStatus("TANGGA DIPAKAI!", Color.ORANGE, 0, true);
            triggerAutoDijkstra(currentPos); // Tampilkan jalur ke finish dari posisi baru
        }

        // Cek Menang
        if (currentPos == MAX_TILE) {
            String raceWinner = (currentPlayer == 1) ? p1Name : p2Name;
            animTimer.stop();

            // Show final leaderboard
            showFinalLeaderboard(raceWinner);
        } else {
            // Logika Bonus Turn 5
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
        // Create leaderboard dialog
        JDialog leaderboardDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                "🏆 GAME OVER - LEADERBOARD", true);
        leaderboardDialog.setSize(500, 400);
        leaderboardDialog.setLocationRelativeTo(this);
        leaderboardDialog.setLayout(new BorderLayout(10, 10));

        // Header Panel
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(44, 62, 80));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("🏁 PERMAINAN SELESAI!");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        headerPanel.add(titleLabel);

        JLabel raceWinnerLabel = new JLabel("Pemain Tercepat: " + raceWinner);
        raceWinnerLabel.setFont(new Font("Segoe UI", Font.ITALIC, 16));
        raceWinnerLabel.setForeground(new Color(241, 196, 15));
        raceWinnerLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel headerContainer = new JPanel(new GridLayout(2, 1, 5, 5));
        headerContainer.setBackground(new Color(44, 62, 80));
        headerContainer.add(titleLabel);
        headerContainer.add(raceWinnerLabel);
        leaderboardDialog.add(headerContainer, BorderLayout.NORTH);

        // Leaderboard Panel
        JPanel leaderboardPanel = new JPanel();
        leaderboardPanel.setLayout(new BoxLayout(leaderboardPanel, BoxLayout.Y_AXIS));
        leaderboardPanel.setBackground(new Color(236, 240, 241));
        leaderboardPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        // Title for leaderboard
        JLabel leaderboardTitle = new JLabel("PERINGKAT BERDASARKAN BONUS POINT");
        leaderboardTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        leaderboardTitle.setForeground(new Color(52, 73, 94));
        leaderboardTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        leaderboardPanel.add(leaderboardTitle);
        leaderboardPanel.add(Box.createVerticalStrut(20));

        // Create player list with scores
        java.util.List<PlayerScore> players = new java.util.ArrayList<>();
        players.add(new PlayerScore(p1Name, p1Score));
        players.add(new PlayerScore(p2Name, p2Score));

        // Sort by score (highest first)
        players.sort((a, b) -> Integer.compare(b.score, a.score));

        // Display rankings
        String[] medals = {"🥇", "🥈"};
        Color[] rankColors = {
                new Color(255, 215, 0),    // Gold
                new Color(192, 192, 192)   // Silver
        };

        for (int i = 0; i < players.size(); i++) {
            PlayerScore player = players.get(i);
            JPanel rankPanel = createRankPanel(
                    medals[i],
                    i + 1,
                    player.name,
                    player.score,
                    rankColors[i]
            );
            leaderboardPanel.add(rankPanel);
            leaderboardPanel.add(Box.createVerticalStrut(15));
        }

        // Winner announcement
        PlayerScore pointWinner = players.get(0);
        String winnerText;
        if (pointWinner.score == players.get(1).score) {
            winnerText = "SERI! Kedua pemain memiliki point yang sama!";
        } else {
            winnerText = "JUARA BONUS POINT: " + pointWinner.name + "!";
        }

        JLabel winnerLabel = new JLabel(winnerText);
        winnerLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        winnerLabel.setForeground(new Color(46, 204, 113));
        winnerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        leaderboardPanel.add(Box.createVerticalStrut(10));
        leaderboardPanel.add(winnerLabel);

        JScrollPane scrollPane = new JScrollPane(leaderboardPanel);
        scrollPane.setBorder(null);
        leaderboardDialog.add(scrollPane, BorderLayout.CENTER);

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        buttonPanel.setBackground(new Color(236, 240, 241));

        JButton btnNewGame = createDialogButton("🎮 Main Lagi", new Color(46, 204, 113));
        btnNewGame.addActionListener(e -> {
            leaderboardDialog.dispose();
            mainApp.showCard("SETUP");
        });

        JButton btnMenu = createDialogButton("Menu Utama", new Color(52, 152, 219));
        btnMenu.addActionListener(e -> {
            leaderboardDialog.dispose();
            mainApp.showCard("MENU");
        });

        JButton btnExit = createDialogButton("Keluar", new Color(231, 76, 60));
        btnExit.addActionListener(e -> System.exit(0));

        buttonPanel.add(btnNewGame);
        buttonPanel.add(btnMenu);
        buttonPanel.add(btnExit);

        leaderboardDialog.add(buttonPanel, BorderLayout.SOUTH);

        // Update control panel
        controlPanel.setGameOver("GAME OVER - " + pointWinner.name + " Juara Point!");

        // Show dialog
        leaderboardDialog.setVisible(true);
    }

    private JPanel createRankPanel(String medal, int rank, String name, int score, Color accentColor) {
        JPanel panel = new JPanel(new BorderLayout(15, 0));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(accentColor, 3, true),
                BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));
        panel.setMaximumSize(new Dimension(400, 80));

        // Medal and Rank
        JLabel medalLabel = new JLabel(medal + " #" + rank);
        medalLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        medalLabel.setForeground(accentColor);

        // Player Name
        JLabel nameLabel = new JLabel(name);
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        nameLabel.setForeground(new Color(44, 62, 80));

        // Score
        JLabel scoreLabel = new JLabel(score + " pts");
        scoreLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        scoreLabel.setForeground(new Color(241, 196, 15));

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftPanel.setBackground(Color.WHITE);
        leftPanel.add(medalLabel);
        leftPanel.add(nameLabel);

        panel.add(leftPanel, BorderLayout.WEST);
        panel.add(scoreLabel, BorderLayout.EAST);

        return panel;
    }

    private JButton createDialogButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(12, 25, 12, 25));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }
    // Inner class for player score
    private static class PlayerScore {
        String name;
        int score;

        PlayerScore(String name, int score) {
            this.name = name;
            this.score = score;
        }
    }
}