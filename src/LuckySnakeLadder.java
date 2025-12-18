import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import javax.swing.Timer;

public class LuckySnakeLadder extends JPanel {

    // Class Helper untuk menyimpan antrean notifikasi (agar tidak muncul bertumpuk)
    private static class NotificationRequest {
        String title; String message; Color color;
        public NotificationRequest(String title, String message, Color color) {
            this.title = title; this.message = message; this.color = color;
        }
    }

    public static class Player {
        public String name; public int id; public int position = 1;
        // Menyimpan skor sesi saat ini (sebelum diakumulasi ke MainApp)
        public int currentScore = 0;
        public Color color;
        public Stack<Integer> history = new Stack<>(); // Menyimpan riwayat langkah

        public Player(String name, int id, Color color) {
            this.name = name; this.id = id; this.color = color;
            this.history.push(1);
        }
    }

    private BoardPanel boardPanel;
    private ControlPanel controlPanel;
    private MainApp mainApp;
    private SoundManager soundManager;

    private Map<Integer, Integer> ladders = new HashMap<>(); // Peta Tangga (Start -> End)
    private Map<Integer, Integer> bonusNodes = new HashMap<>(); // Peta Bonus (Tile -> Point)
    private List<Player> players = new ArrayList<>();

    // List label untuk Scoreboard Live di panel atas
    private List<JLabel> scoreLabels = new ArrayList<>();

    private int currentPlayerIndex = 0;
    private Queue<Integer> animQueue = new LinkedList<>(); // Antrean animasi langkah
    private boolean isAnimating = false;
    private Timer animTimer;

    private int lastDiceRoll = 0;
    private int posBeforeTurn = 1;
    private boolean ladderWasUsedInTurn = false;

    // Variabel untuk mereset urutan suara langkah (step_1, step_2...)
    private int currentStepSequence = 1;

    private Queue<NotificationRequest> notificationQueue = new LinkedList<>();
    private JDialog currentResultDialog;
    private final int MAX_TILE = 64;

    public LuckySnakeLadder(List<String> playerNames, MainApp mainApp, SoundManager soundManager) {
        this.mainApp = mainApp;
        this.soundManager = soundManager;

        setLayout(new BorderLayout());
        setBackground(new Color(15, 15, 40)); // Tema Gelap Galaxy

        // Warna-warna pion pemain
        Color[] pColors = {
                new Color(52, 152, 219), // Biru
                new Color(231, 76, 60),  // Merah
                new Color(46, 204, 113), // Hijau
                new Color(241, 196, 15)  // Kuning
        };

        for (int i = 0; i < playerNames.size(); i++) {
            players.add(new Player(playerNames.get(i), i, pColors[i]));
        }

        // Generate Papan Permainan
        generateSpiralLadders();
        generateRandomBonusNodes();

        boardPanel = new BoardPanel(ladders, bonusNodes, players);
        add(boardPanel, BorderLayout.CENTER);

        controlPanel = new ControlPanel(soundManager);
        add(controlPanel, BorderLayout.SOUTH);

        // Setup UI bagian atas (Tombol & Scoreboard)
        setupTopPanel();

        boardPanel.updatePositions();
        updateTurnLabel();
        controlPanel.setGameReference(this);

        // Timer animasi langkah (275ms per langkah)
        animTimer = new Timer(275, e -> processAnimation());
    }

    private void setupTopPanel() {
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(15, 15, 40));
        topPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        // --- PANEL KIRI (NAVIGASI) ---
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        leftPanel.setOpaque(false);

        JButton btnBack = createNavButton("<< Menu");
        btnBack.addActionListener(e -> {
            soundManager.playSFX("click.wav");
            closeResultDialog();
            if(animTimer.isRunning()) animTimer.stop();
            mainApp.showCard("MENU");
        });

        // Tombol Restart (Memanggil fungsi reset total)
        JButton btnRestart = createNavButton("Restart");
        btnRestart.setBackground(new Color(231, 76, 60));
        btnRestart.addActionListener(e -> {
            soundManager.playSFX("click.wav");
            if(JOptionPane.showConfirmDialog(this, "Restart Game?", "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                closeResultDialog();
                soundManager.stopWinMusic();
                restartGame();
            }
        });

        leftPanel.add(btnBack);
        leftPanel.add(btnRestart);
        topPanel.add(leftPanel, BorderLayout.WEST);

        // --- PANEL TENGAH (SCOREBOARD LIVE) ---
        JPanel scorePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 5));
        scorePanel.setOpaque(false);
        scoreLabels.clear();

        for (Player p : players) {
            JLabel lbl = new JLabel(p.name + ": " + p.currentScore);
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 16));
            lbl.setForeground(p.color); // Warna teks mengikuti warna pion

            // Memberi border tipis agar tulisan terbaca jelas di background gelap
            lbl.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(p.color, 1, true),
                    BorderFactory.createEmptyBorder(2, 8, 2, 8)
            ));

            scoreLabels.add(lbl);
            scorePanel.add(lbl);
        }
        topPanel.add(scorePanel, BorderLayout.CENTER);

        // --- PANEL KANAN (VOLUME CONTROL) ---
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightPanel.setOpaque(false);
        JLabel lblVol = new JLabel("Vol: "); lblVol.setForeground(Color.WHITE);
        JSlider volSlider = new JSlider(0, 100, soundManager.getVolumeInt());
        volSlider.setOpaque(false);
        volSlider.setPreferredSize(new Dimension(80, 20));
        volSlider.addChangeListener(e -> {
            soundManager.setVolume(volSlider.getValue() / 100f);
        });
        rightPanel.add(lblVol);
        rightPanel.add(volSlider);
        topPanel.add(rightPanel, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);
    }

    // Method untuk update teks skor di panel atas secara real-time
    private void updateScoreDisplay() {
        for (int i = 0; i < players.size(); i++) {
            Player p = players.get(i);
            scoreLabels.get(i).setText(p.name + ": " + p.currentScore);
        }
        revalidate();
        repaint();
    }

    private void closeResultDialog() {
        if (currentResultDialog != null && currentResultDialog.isVisible()) {
            currentResultDialog.dispose();
        }
    }

    private JButton createNavButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(new Color(50, 50, 80));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        return btn;
    }

    // Logic Reset: Meminta MainApp membuat instance baru dari game ini
    private void restartGame() {
        if(animTimer.isRunning()) animTimer.stop();
        List<String> names = new ArrayList<>();
        for(Player p : players) names.add(p.name);
        mainApp.startGame(names);
    }

    private void updateTurnLabel() {
        Player p = players.get(currentPlayerIndex);
        String statusAdd = (lastDiceRoll == 5) ? " (BONUS TURN!)" : "";
        controlPanel.setTurnLabel("Giliran: " + p.name + " (" + p.currentScore + " pts)" + statusAdd, p.color);
    }

    // Cek Bilangan Prima (Syarat naik tangga)
    private boolean isPrime(int n) {
        if (n <= 1) return false;
        for (int i = 2; i <= Math.sqrt(n); i++) {
            if (n % i == 0) return false;
        }
        return true;
    }

    // Algoritma membuat tangga spiral acak
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

    // Algoritma menyebar bonus poin (menghindari tile bertangga)
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

    // Algoritma Dijkstra untuk mencari jalur terpendek visual
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

    // Logic kecepatan dadu (Makin kecil angka, makin lambat jalannya)
    private int getAnimSpeed(int diceRoll) {
        switch (diceRoll) {
            case 1: return 500; case 2: return 450; case 3: return 400;
            case 4: return 300; case 5: return 250; case 6: return 200;
            default: return 300;
        }
    }

    // --- LOGIC UTAMA GILIRAN ---
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

        // Probabilitas: 70% Hijau (Maju), 30% Merah (Mundur)
        boolean isGreen = rand.nextDouble() < 0.7;

        isAnimating = true;
        controlPanel.disableButtons();

        Stack<Integer> history = currentP.history;
        int simPos = history.peek();
        animQueue.clear();

        if (isGreen) {
            // Logic MAJU
            for (int i = 0; i < steps; i++) {
                int next = simPos + 1;
                if (next > MAX_TILE) next = MAX_TILE;
                history.push(next);
                animQueue.add(next);
                simPos = next;

                // Cek Tangga (Hanya aktif jika Start Prima)
                if (ladders.containsKey(simPos)) {
                    if (isPrime(posBeforeTurn)) {
                        int top = ladders.get(simPos);
                        history.push(top);
                        animQueue.add(top);
                        simPos = top;
                        ladderWasUsedInTurn = true;
                    }
                }
                if (simPos == MAX_TILE) break;
            }
        } else {
            // Logic MUNDUR (Manual Calculation agar tidak turun tangga)
            for (int i = 0; i < steps; i++) {
                int next = simPos - 1;
                if (next < 1) next = 1;
                history.push(next);
                animQueue.add(next);
                simPos = next;
            }
        }

        controlPanel.updateStatus(currentP.name + (isGreen ? " MAJU " : " MUNDUR ") + steps,
                isGreen ? new Color(39, 174, 96) : new Color(231, 76, 60),
                steps, isGreen);

        // Reset urutan suara langkah kembali ke 1 setiap kocokan baru
        currentStepSequence = 1;

        animTimer.setDelay(getAnimSpeed(lastDiceRoll));
        animTimer.start();
    }

    private void processAnimation() {
        if (!animQueue.isEmpty()) {
            // Logic Sequence Suara (1 sampai 6 berulang/mentok)
            int soundIndex = Math.min(currentStepSequence, 6);
            String soundFile = "step_" + soundIndex + ".wav";
            soundManager.playSFX(soundFile);
            currentStepSequence++; // Naikkan counter

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

        notificationQueue.clear();

        // 1. Cek Bonus Point
        if (bonusNodes.containsKey(pos)) {
            int pts = bonusNodes.get(pos);
            currentP.currentScore += pts;
            bonusNodes.remove(pos); // Hapus bonus setelah diambil
            boardPanel.updateBonusNodes(bonusNodes);

            // Update Scoreboard atas
            updateScoreDisplay();

            notificationQueue.add(new NotificationRequest(
                    "BONUS POINTS!",
                    currentP.name + " dapat +" + pts + " pts!",
                    new Color(241, 196, 15) // Kuning
            ));
        }

        // 2. Cek Tangga
        if (ladderWasUsedInTurn) {
            controlPanel.updateStatus("TANGGA!", Color.ORANGE, 0, true);
            triggerAutoDijkstra(pos);

            notificationQueue.add(new NotificationRequest(
                    "WARP ZONE!",
                    "Start Prima! Naik Tangga!",
                    new Color(0, 255, 255) // Cyan
            ));
        }

        // 3. Cek Menang
        if (pos == MAX_TILE) {
            animTimer.stop();
            soundManager.playWinMusic("win.wav");

            mainApp.addRaceWin(currentP.name);
            for(Player p : players) {
                mainApp.addScore(p.name, p.currentScore);
            }
            updateScoreDisplay(); // Update skor terakhir

            showFinalRankingDialog(currentP);
            return;
        }

        // 4. Cek Dadu 5 (Extra Turn)
        if (lastDiceRoll == 5) {
            notificationQueue.add(new NotificationRequest(
                    "LUCKY ROLL!",
                    "Dapat angka 5! Main lagi!",
                    new Color(46, 204, 113) // Hijau
            ));
        }

        animTimer.stop();
        processNotificationQueue();
    }

    private void processNotificationQueue() {
        if (notificationQueue.isEmpty()) {
            finalizeTurn();
            return;
        }

        NotificationRequest req = notificationQueue.poll();

        // Play Sound Effect Sesuai Tipe Notifikasi SEBELUM dialog muncul
        if (req.title.contains("BONUS")) soundManager.playSFX("extra.wav");
        else if (req.title.contains("WARP")) soundManager.playSFX("ladder.wav");
        else if (req.title.contains("LUCKY")) soundManager.playSFX("extra.wav");

        showSequentialNotification(req);
    }

    private void showSequentialNotification(NotificationRequest req) {
        JDialog d = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), false);
        d.setUndecorated(true);
        d.setFocusableWindowState(true);
        d.setLayout(new BorderLayout());

        JPanel p = new JPanel(new GridLayout(2, 1));
        p.setBackground(new Color(20, 20, 50, 240));
        // Border tebal sesuai warna tipe notifikasi
        p.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(req.color, 4),
                BorderFactory.createEmptyBorder(20, 40, 20, 40)
        ));

        JLabel lblTitle = new JLabel(req.title, SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle.setForeground(req.color);

        JLabel lblMsg = new JLabel(req.message, SwingConstants.CENTER);
        lblMsg.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        lblMsg.setForeground(Color.WHITE);

        JLabel lblHint = new JLabel("(Klik untuk lanjut)", SwingConstants.CENTER);
        lblHint.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblHint.setForeground(Color.GRAY);

        JPanel msgPanel = new JPanel(new BorderLayout());
        msgPanel.setOpaque(false);
        msgPanel.add(lblMsg, BorderLayout.CENTER);
        msgPanel.add(lblHint, BorderLayout.SOUTH);

        p.add(lblTitle);
        p.add(msgPanel);
        d.add(p);
        d.pack();
        d.setLocationRelativeTo(this);

        Timer autoCloseTimer = new Timer(3000, e -> d.dispose());
        autoCloseTimer.setRepeats(false);
        autoCloseTimer.start();

        MouseAdapter closeAction = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                autoCloseTimer.stop();
                d.dispose();
            }
        };
        p.addMouseListener(closeAction);
        d.addMouseListener(closeAction);

        d.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                processNotificationQueue();
            }
        });

        d.setVisible(true);
    }

    private void finalizeTurn() {
        if (lastDiceRoll != 5) {
            currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
        }
        updateTurnLabel();
        isAnimating = false;
        controlPanel.enableButtons();
    }

    private void showFinalRankingDialog(Player raceWinner) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "STATISTIK GLOBAL", false);
        currentResultDialog = dialog;

        dialog.setSize(800, 500);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(new Color(15, 15, 40));

        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                soundManager.stopWinMusic();
                soundManager.stopBGM();
                soundManager.playSFX("click.wav");
                currentResultDialog = null;
            }
        });

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(15, 15, 40));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JLabel title = new JLabel("HASIL AKHIR & AKUMULASI", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(Color.WHITE);
        headerPanel.add(title, BorderLayout.CENTER);

        dialog.add(headerPanel, BorderLayout.NORTH);

        JPanel contentPanel = new JPanel(new GridLayout(1, 2, 15, 0));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        contentPanel.setBackground(new Color(15, 15, 40));

        PriorityQueue<Player> raceRankQueue = new PriorityQueue<>((p1, p2) ->
                mainApp.getRaceWins(p2.name) - mainApp.getRaceWins(p1.name)
        );
        raceRankQueue.addAll(players);
        JPanel racePanel = createRankListPanel("🏆 KLASEMEN RACE WINS", raceRankQueue, true);

        PriorityQueue<Player> scoreRankQueue = new PriorityQueue<>((p1, p2) ->
                mainApp.getTotalScore(p2.name) - mainApp.getTotalScore(p1.name)
        );
        scoreRankQueue.addAll(players);
        JPanel scorePanel = createRankListPanel("💰 KLASEMEN TOTAL POIN", scoreRankQueue, false);

        contentPanel.add(racePanel);
        contentPanel.add(scorePanel);
        dialog.add(contentPanel, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        btnPanel.setBackground(new Color(15, 15, 40));

        JButton btnRestart = new JButton("Main Lagi (Restart)");
        btnRestart.setBackground(new Color(46, 204, 113));
        btnRestart.setForeground(Color.WHITE);
        btnRestart.addActionListener(e -> {
            soundManager.stopWinMusic();
            soundManager.stopBGM();
            soundManager.playSFX("click.wav");
            dialog.dispose();
            currentResultDialog = null;
            restartGame();
        });

        JButton btnMenu = new JButton("Menu Utama");
        btnMenu.addActionListener(e -> {
            soundManager.stopWinMusic();
            soundManager.stopBGM();
            soundManager.playSFX("click.wav");
            dialog.dispose();
            currentResultDialog = null;
            mainApp.showCard("MENU");
        });

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
        p.add(Box.createVerticalGlue());
        return p;
    }
}