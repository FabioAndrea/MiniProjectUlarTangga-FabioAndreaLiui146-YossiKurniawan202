import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import javax.swing.Timer;

public class LuckySnakeLadder extends JPanel {

    // Class Player Internal
    public static class Player {
        public String name;
        public int id;
        public int position = 1;
        public int currentScore = 0; // Skor Sesi Ini
        public Color color;
        public Stack<Integer> history = new Stack<>();

        public Player(String name, int id, Color color) {
            this.name = name;
            this.id = id;
            this.color = color;
            this.history.push(1);
        }
    }

    private BoardPanel boardPanel;
    private ControlPanel controlPanel;
    private MainApp mainApp;
    private SoundManager soundManager;

    private Map<Integer, Integer> ladders = new HashMap<>();
    private Map<Integer, Integer> bonusNodes = new HashMap<>();

    private List<Player> players = new ArrayList<>();
    private int currentPlayerIndex = 0;

    private Queue<Integer> animQueue = new LinkedList<>();
    private boolean isAnimating = false;
    private Timer animTimer;

    private int lastDiceRoll = 0;
    private int posBeforeTurn = 1;
    private boolean ladderWasUsedInTurn = false;

    private final int MAX_TILE = 64;
    private final int GRID_SIZE = 8;

    public LuckySnakeLadder(List<String> playerNames, MainApp mainApp, SoundManager soundManager) {
        this.mainApp = mainApp;
        this.soundManager = soundManager;

        setLayout(new BorderLayout());
        setBackground(new Color(15, 15, 40));

        Color[] pColors = {
                new Color(52, 152, 219), // Biru
                new Color(231, 76, 60),  // Merah
                new Color(46, 204, 113), // Hijau
                new Color(241, 196, 15)  // Kuning
        };

        for (int i = 0; i < playerNames.size(); i++) {
            players.add(new Player(playerNames.get(i), i, pColors[i]));
        }

        generateSpiralLadders();
        generateRandomBonusNodes();

        boardPanel = new BoardPanel(ladders, bonusNodes, players);
        add(boardPanel, BorderLayout.CENTER);

        controlPanel = new ControlPanel();
        add(controlPanel, BorderLayout.SOUTH);

        setupTopPanel();

        boardPanel.updatePositions();
        updateTurnLabel();
        controlPanel.setGameReference(this);

        animTimer = new Timer(200, e -> processAnimation());
    }

    private void setupTopPanel() {
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(15, 15, 40));
        topPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        leftPanel.setOpaque(false);

        JButton btnBack = createNavButton("<< Menu");
        btnBack.addActionListener(e -> mainApp.showCard("MENU"));

        JButton btnRestart = createNavButton("🔄 Restart");
        btnRestart.setBackground(new Color(231, 76, 60));
        btnRestart.addActionListener(e -> {
            if(JOptionPane.showConfirmDialog(this, "Restart Game?", "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                restartGame();
            }
        });

        leftPanel.add(btnBack);
        leftPanel.add(btnRestart);
        topPanel.add(leftPanel, BorderLayout.WEST);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightPanel.setOpaque(false);
        JLabel lblVol = new JLabel("Vol: "); lblVol.setForeground(Color.WHITE);
        JSlider volSlider = new JSlider(0, 100, 70);
        volSlider.setOpaque(false);
        volSlider.setPreferredSize(new Dimension(80, 20));
        volSlider.addChangeListener(e -> {
            if (soundManager != null) soundManager.setVolume(volSlider.getValue() / 100f);
        });
        rightPanel.add(lblVol);
        rightPanel.add(volSlider);
        topPanel.add(rightPanel, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);
    }

    private JButton createNavButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(new Color(50, 50, 80));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        return btn;
    }

    private void restartGame() {
        // Ambil nama pemain saat ini untuk dipakai di game selanjutnya
        List<String> names = new ArrayList<>();
        for(Player p : players) names.add(p.name);
        // Data lama di MainApp TIDAK dihapus, jadi terakumulasi
        mainApp.startGame(names);
    }

    private void updateTurnLabel() {
        Player p = players.get(currentPlayerIndex);
        String statusAdd = (lastDiceRoll == 5) ? " (BONUS TURN!)" : "";
        controlPanel.setTurnLabel("Giliran: " + p.name + " (" + p.currentScore + " pts)" + statusAdd, p.color);
    }

    private boolean isPrime(int n) {
        if (n <= 1) return false;
        for (int i = 2; i <= Math.sqrt(n); i++) if (n % i == 0) return false;
        return true;
    }

    private void generateSpiralLadders() {
        Random rand = new Random();
        ladders.clear();
        while (ladders.size() < 5) {
            int start = rand.nextInt(MAX_TILE - 15) + 1;
            int jump = rand.nextInt(15) + 10;
            int end = start + jump;
            if (end > MAX_TILE) end = MAX_TILE;
            if (start == 1 || start >= MAX_TILE || ladders.containsKey(start) || ladders.containsValue(start)) continue;
            ladders.put(start, end);
        }
    }

    private void generateRandomBonusNodes() {
        Random rand = new Random();
        bonusNodes.clear();
        int bonusCount = rand.nextInt(6) + 5;
        while (bonusNodes.size() < bonusCount) {
            int node = rand.nextInt(MAX_TILE - 2) + 2;
            if (!ladders.containsKey(node) && !ladders.containsValue(node)) {
                bonusNodes.put(node, rand.nextInt(5) + 1);
            }
        }
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
                int target = ladders.get(u);
                if (d < dist.get(target)) {
                    dist.put(target, d);
                    prev.put(target, u);
                    pq.offer(new int[]{target, d});
                }
            }
        }
        List<Integer> path = new ArrayList<>();
        Integer curr = MAX_TILE;
        if (dist.get(MAX_TILE) == Integer.MAX_VALUE) return new ArrayList<>();
        while (curr != null) { path.add(0, curr); curr = prev.get(curr); }
        return path;
    }

    public void playTurn() {
        if (isAnimating) return;
        boardPanel.setShortestPath(null);

        Player currentP = players.get(currentPlayerIndex);
        posBeforeTurn = currentP.position;
        ladderWasUsedInTurn = false;

        soundManager.playSFX("dice.wav");
        Random rand = new Random();
        int steps = rand.nextInt(6) + 1;
        lastDiceRoll = steps;
        boolean isGreen = rand.nextDouble() < 1;

        isAnimating = true;
        controlPanel.disableButtons();

        Stack<Integer> history = currentP.history;
        int simPos = history.peek();
        animQueue.clear();

        if (isGreen) {
            for (int i = 0; i < steps; i++) {
                int next = simPos + 1;
                if (next > MAX_TILE) next = MAX_TILE;
                history.push(next);
                animQueue.add(next);
                simPos = next;

                if (ladders.containsKey(simPos) && isPrime(posBeforeTurn)) {
                    int top = ladders.get(simPos);
                    history.push(top);
                    animQueue.add(top);
                    simPos = top;
                    ladderWasUsedInTurn = true;
                }
                if (simPos == MAX_TILE) break;
            }
        } else {
            for (int i = 0; i < steps; i++) {
                if (history.size() > 1) {
                    history.pop();
                    int prev = history.peek();
                    animQueue.add(prev);
                    simPos = prev;
                }
            }
        }

        controlPanel.updateStatus(currentP.name + (isGreen ? " MAJU " : " MUNDUR ") + steps,
                isGreen ? new Color(39, 174, 96) : new Color(231, 76, 60),
                steps, isGreen);
        animTimer.start();
    }

    private void processAnimation() {
        if (!animQueue.isEmpty()) {
            soundManager.playSFX("step.wav");
            int nextPos = animQueue.poll();
            players.get(currentPlayerIndex).position = nextPos;
            boardPanel.updatePositions();
        } else {
            finishTurnLogic();
        }
    }

    private void finishTurnLogic() {
        Player currentP = players.get(currentPlayerIndex);
        int pos = currentP.position;

        if (bonusNodes.containsKey(pos)) {
            int pts = bonusNodes.get(pos);
            currentP.currentScore += pts;
            JOptionPane.showMessageDialog(this, currentP.name + " dapat +" + pts + " pts!");
            bonusNodes.remove(pos);
            boardPanel.updateBonusNodes(bonusNodes);
        }

        if (ladderWasUsedInTurn) {
            soundManager.playSFX("ladder.wav");
            controlPanel.updateStatus("TANGGA!", Color.ORANGE, 0, true);
            triggerAutoDijkstra(pos);
        }

        if (pos == MAX_TILE) {
            soundManager.playSFX("win.wav");
            animTimer.stop();

            // --- UPDATE DATA STATISTIK KE MAINAPP ---
            // 1. Tambah kemenangan balapan buat yang finish
            mainApp.addRaceWin(currentP.name);

            // 2. Tambah poin untuk SEMUA pemain (akumulasi)
            for(Player p : players) {
                mainApp.addScore(p.name, p.currentScore);
            }

            // Tampilkan Klasemen Global
            showFinalRankingDialog(currentP);
        } else {
            if (lastDiceRoll == 5) {
                JOptionPane.showMessageDialog(this, "Dapat 5! Main lagi!");
            } else {
                currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
            }
            updateTurnLabel();
            isAnimating = false;
            controlPanel.enableButtons();
            animTimer.stop();
        }
    }

    // --- LOGIKA LEADERBOARD DENGAN 2 KATEGORI TERPISAH ---
    private void showFinalRankingDialog(Player raceWinner) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "STATISTIK GLOBAL", true);
        dialog.setSize(800, 500); // Lebar ditambah untuk 2 kolom
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(new Color(15, 15, 40));

        // Header
        JLabel title = new JLabel("HASIL AKHIR & AKUMULASI", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(Color.WHITE);
        title.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));
        dialog.add(title, BorderLayout.NORTH);

        // Content Panel (Grid 2 Kolom)
        JPanel contentPanel = new JPanel(new GridLayout(1, 2, 15, 0));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        contentPanel.setBackground(new Color(15, 15, 40));

        // --- KATEGORI 1: TOTAL KEMENANGAN BALAPAN (RACE WINS) ---
        // Kita urutkan player berdasarkan data di MainApp (raceWins)
        PriorityQueue<Player> raceRankQueue = new PriorityQueue<>((p1, p2) ->
                mainApp.getRaceWins(p2.name) - mainApp.getRaceWins(p1.name) // Descending
        );
        raceRankQueue.addAll(players);

        JPanel racePanel = createRankListPanel("🏆 KLASEMEN RACE WINS (Finish 1st)", raceRankQueue, true);

        // --- KATEGORI 2: TOTAL POIN TERTINGGI (SCORE WINS) ---
        // Kita urutkan player berdasarkan data di MainApp (totalScore)
        PriorityQueue<Player> scoreRankQueue = new PriorityQueue<>((p1, p2) ->
                mainApp.getTotalScore(p2.name) - mainApp.getTotalScore(p1.name) // Descending
        );
        scoreRankQueue.addAll(players);

        JPanel scorePanel = createRankListPanel("💰 KLASEMEN TOTAL POIN", scoreRankQueue, false);

        contentPanel.add(racePanel);
        contentPanel.add(scorePanel);
        dialog.add(contentPanel, BorderLayout.CENTER);

        // Footer Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        btnPanel.setBackground(new Color(15, 15, 40));

        JButton btnRestart = new JButton("Main Lagi (Restart)");
        btnRestart.setBackground(new Color(46, 204, 113));
        btnRestart.setForeground(Color.WHITE);
        btnRestart.addActionListener(e -> {
            dialog.dispose();
            restartGame(); // Panggil method restart
        });

        JButton btnMenu = new JButton("Menu Utama");
        btnMenu.addActionListener(e -> { dialog.dispose(); mainApp.showCard("MENU"); });

        btnPanel.add(btnRestart);
        btnPanel.add(btnMenu);
        dialog.add(btnPanel, BorderLayout.SOUTH);

        controlPanel.setGameOver("GAME OVER - " + raceWinner.name + " Finish!");
        dialog.setVisible(true);
    }

    private JPanel createRankListPanel(String titleStr, PriorityQueue<Player> queue, boolean isRace) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(new Color(30, 30, 60));

        Color borderColor = isRace ? Color.CYAN : Color.ORANGE;
        p.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(borderColor, 2),
                titleStr, 0,0, new Font("Segoe UI", Font.BOLD, 14), borderColor));

        int rank = 1;
        while(!queue.isEmpty()) {
            Player pl = queue.poll();

            // Ambil data dari MainApp (Persistent Data)
            int val = isRace ? mainApp.getRaceWins(pl.name) : mainApp.getTotalScore(pl.name);
            String suffix = isRace ? " x Menang" : " Pts";

            String labelStr = "#" + rank + " " + pl.name + " : " + val + suffix;

            JLabel lbl = new JLabel(labelStr);
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 16));
            lbl.setForeground(Color.WHITE);
            lbl.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

            p.add(lbl);
            rank++;
        }
        // Isi kekosongan agar layout rapi
        p.add(Box.createVerticalGlue());
        return p;
    }
}