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

    private Stack<Integer> p1History = new Stack<>();
    private Stack<Integer> p2History = new Stack<>();
    private Queue<Integer> p1AnimQueue = new LinkedList<>();
    private Queue<Integer> p2AnimQueue = new LinkedList<>();

    private int p1VisualPos = 1;
    private int p2VisualPos = 1;
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

        p1History.push(1);
        p2History.push(1);

        boardPanel = new BoardPanel(ladders);
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
        add(topPanel, BorderLayout.NORTH);

        boardPanel.updatePositions(1, 1);
        updateTurnLabel();

        controlPanel.setGameReference(this);

        animTimer = new Timer(200, e -> processAnimation());
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

        // --- Feedback Tangga ---
        // Karena logika naik tangga sudah "baked in" di playTurn,
        // di sini kita hanya memberi notifikasi visual/Dijkstra.
        if (ladderWasUsedInTurn) {
            controlPanel.updateStatus("TANGGA DIPAKAI!", Color.ORANGE, 0, true);
            triggerAutoDijkstra(currentPos); // Tampilkan jalur ke finish dari posisi baru
        }

        // Cek Menang
        if (currentPos == MAX_TILE) {
            String winner = (currentPlayer == 1) ? p1Name : p2Name;
            JOptionPane.showMessageDialog(this, "SELAMAT! " + winner + " MENANG!");
            controlPanel.setGameOver(winner + " MENANG!");
            animTimer.stop();
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
}