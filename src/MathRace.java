import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import javax.sound.sampled.*;

/**
 * Klasa reprezentująca główny panel gry Matematyczny Wyścig. Zarządza logiką gry, grafiką
 * oraz interakcją użytkownika.
 */
class MathRacePanel extends JPanel implements ActionListener {

    // Stałe i zmienne odpowiadające za logikę i grafikę gry

    private int carX = 472;
    private final int carY = 680;
    private final int carWidth = 55;
    private final int carHeight = 110;
    private final Timer timer;
    private final ArrayList<Rectangle> obstacles;
    private final ArrayList<Rectangle> coins;
    private final ArrayList<Rectangle> flags;
    private final Random random;
    private int score = 0;
    private int nextMathChallengeScore = 50;
    private final int maxScore = 1000;
    private int hearts = 3;
    private boolean isGameStarted = false;
    private boolean isGamePaused = false;
    private boolean isMuted = false;
    private int currentLevel = 1;
    private final int level1Score = 200;
    private final int level2Score = 300;
    private final int level3Score = 400;
    private int elementSpeed = 10;
    private Image carImage;
    private Image coinImage;
    private Image obstacleImage;
    private Image flagImage;
    private Image heartImage;
    private JButton menuButton;

    private Clip backgroundMusic;

    /**
     * Metoda obsługująca wczytywanie zasobów dźwiękowych i rozpoczęcie odtwarzania
     * zapętlonej muzyki w tle.
     */
    private void playBackgroundMusic() {
        try {
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(getClass().getResource("/assets/muzyka.wav"));
            backgroundMusic = AudioSystem.getClip();
            backgroundMusic.open(audioStream);
            backgroundMusic.loop(Clip.LOOP_CONTINUOUSLY); // Zapętlenie muzyki
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Zatrzymuje muzykę w tle.
     */
    private void stopBackgroundMusic() {
        if (backgroundMusic != null && backgroundMusic.isRunning()) {
            backgroundMusic.stop();
        }
    }

    /**
     * Wznawia odtwarzanie muzyki w tle, jeśli była wcześniej zatrzymana.
     */
    private void resumeBackgroundMusic() {
        if (backgroundMusic != null && !backgroundMusic.isRunning()) {
            backgroundMusic.loop(Clip.LOOP_CONTINUOUSLY);
        }
    }


    /**
     * Konstruktor klasy. Inicjalizuje komponenty graficzne, załadowane zasoby
     * i definiuje logikę działania gry.
     */
    public MathRacePanel() {

        playBackgroundMusic();

        this.setFocusable(true);
        this.setPreferredSize(new Dimension(1050, 800));
        this.setBackground(Color.GRAY);
        this.setLayout(null);

        carImage = new ImageIcon(getClass().getResource("/assets/car_f1.png")).getImage()
                .getScaledInstance(carWidth, carHeight, Image.SCALE_SMOOTH);
        coinImage = new ImageIcon(getClass().getResource("/assets/coins.png")).getImage()
                .getScaledInstance(55, 55, Image.SCALE_SMOOTH);
        obstacleImage = new ImageIcon(getClass().getResource("/assets/crates_study_x2.png")).getImage()
                .getScaledInstance(55, 55, Image.SCALE_SMOOTH);
        flagImage = new ImageIcon(getClass().getResource("/assets/flaga_f1.png")).getImage()
                .getScaledInstance(55, 55, Image.SCALE_SMOOTH);
        heartImage = new ImageIcon(getClass().getResource("/assets/heart.png")).getImage()
                .getScaledInstance(30, 30, Image.SCALE_SMOOTH); // Skala serca

        menuButton = new JButton("MENU");
        menuButton.setBounds(950, 10, 80, 30);
        menuButton.setFocusable(false);
        menuButton.setBackground(Color.GRAY);
        menuButton.setForeground(Color.WHITE);
        menuButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showMenuOptions();
            }
        });
        this.add(menuButton);

        JButton muteButton = new JButton("WYCISZ");
        muteButton.setBounds(950, 50, 80, 30);
        muteButton.setBackground(Color.GRAY);
        muteButton.setForeground(Color.WHITE);
        muteButton.addActionListener(e -> {
            if (isMuted) {
                resumeBackgroundMusic();
                muteButton.setText("WYCISZ");
            } else {
                stopBackgroundMusic();
                muteButton.setText("WŁĄCZ MUZYKĘ");
            }
            isMuted = !isMuted;
        });
        this.add(muteButton);

        JButton startButton = new JButton("START");
        startButton.setBounds(460, 370, 130, 50);
        startButton.setFocusable(false);
        startButton.setBackground(Color.RED);
        startButton.setForeground(Color.WHITE);
        startButton.setFont(new Font("Arial", Font.BOLD, 18));
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                isGameStarted = true;
                startButton.setVisible(false);
                timer.start();
                requestFocus();
            }
        });
        this.add(startButton);

        this.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_LEFT && carX > 0) {
                    carX -= 30;
                } else if (e.getKeyCode() == KeyEvent.VK_RIGHT && carX < getWidth() - carWidth) {
                    carX += 30;
                }
            }
        });

        obstacles = new ArrayList<>();
        coins = new ArrayList<>();
        flags = new ArrayList<>();
        random = new Random();

        timer = new Timer(30, this);
    }

    /**
     * Wyświetla okno z opcjami gry, umożliwiając wstrzymanie, reset lub zakończenie gry.
     */
    private void showMenuOptions() {

        // Logika wyświetlania menu gry

        if (isGameStarted && !isGamePaused) {
            isGamePaused = true;
            timer.stop();
        }

        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS)); // Układ pionowy

        Dimension buttonSize = new Dimension(200, 50); // Wszystkie przyciski tej samej wielkości

        JButton resumeButton = new JButton("Wznów grę");
        JButton restartButton = new JButton("Zacznij od nowa");
        JButton exitButton = new JButton("Zakończ grę");

        setButtonProperties(resumeButton, buttonSize);
        setButtonProperties(restartButton, buttonSize);
        setButtonProperties(exitButton, buttonSize);

        resumeButton.addActionListener(e -> {
            if (isGamePaused) {
                isGamePaused = false;
                timer.start();
            }
            SwingUtilities.getWindowAncestor(menuPanel).dispose(); // Zamknięcie okna dialogowego
        });

        restartButton.addActionListener(e -> {
            resetGame();
            SwingUtilities.getWindowAncestor(menuPanel).dispose(); // Zamknięcie okna dialogowego
        });

        exitButton.addActionListener(e -> {
            System.exit(0);
        });

        menuPanel.add(resumeButton);
        menuPanel.add(Box.createRigidArea(new Dimension(0, 10))); // Odstęp między przyciskami
        menuPanel.add(restartButton);
        menuPanel.add(Box.createRigidArea(new Dimension(0, 10))); // Odstęp między przyciskami
        menuPanel.add(exitButton);

        JOptionPane.showOptionDialog(
                this,
                menuPanel,
                "Menu",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                new Object[]{},
                null
        );
    }

    /**
     * Metoda konfigurująca wizualne danego JButton
     */
    private void setButtonProperties(JButton button, Dimension size) {
        button.setPreferredSize(size);
        button.setMaximumSize(size);
        button.setFocusable(false);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setBackground(Color.LIGHT_GRAY);
        button.setForeground(Color.BLACK);
        button.setFont(new Font("Arial", Font.BOLD, 16));
    }

    /**
     * Metoda odpowiedzialna za rozpoczęcie gry po naciśnięciu przycisku START.
     */
    private void resetGame() {

        // Logika resetu gry

        isGameStarted = false;
        isGamePaused = false;
        score = 0;
        hearts = 3;
        currentLevel = 1;
        carX = 472;
        obstacles.clear();
        coins.clear();
        flags.clear();
        repaint();

        Component startButton = getComponentAt(460, 370);
        if (startButton instanceof JButton) {
            startButton.setVisible(true);
        }
    }

    /**
     * Rysuje elementy gry, w tym samochód gracza, przeszkody, monety i flagi.
     *
     * @param g obiekt Graphics używany do rysowania na panelu
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Logika rysowania

        if (!isGameStarted || isGamePaused) {
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 28));
            g.drawString("Aby rozpocząć grę, wciśnij przycisk START", 200, 300);
            return; // Nie rysujemy nic więcej przed startem gry
        }

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString("Wynik: " + score, 10, 20);

        int heartsWidth = hearts * 35;
        int heartsX = (getWidth() - heartsWidth) / 2;

        for (int i = 0; i < hearts; i++) {
            g.drawImage(heartImage, heartsX + i * 35, 10, this);
        }

        g.drawImage(carImage, carX, carY, carWidth, carHeight, this);

        for (Rectangle obstacle : obstacles) {
            g.drawImage(obstacleImage, obstacle.x, obstacle.y, obstacle.width, obstacle.height, this);
        }

        for (Rectangle coin : coins) {
            g.drawImage(coinImage, coin.x, coin.y, coin.width, coin.height, this);
        }

        for (Rectangle flag : flags) {
            g.drawImage(flagImage, flag.x, flag.y, flag.width, flag.height, this);
        }
    }

    /**
     * Obsługuje zdarzenia generowane przez Timer, takie jak ruch przeszkód,
     * zbieranie monet i logika poziomów.
     *
     * @param e zdarzenie ActionEvent generowane przez Timer
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if (!isGameStarted || isGamePaused) return;

        // Logika obsługi gry

        if (score >= maxScore) {
            timer.stop();
            JOptionPane.showMessageDialog(this, "Brawo! Osiągnąłeś maksymalny wynik!");
            System.exit(0);
        }

        if (score >= nextMathChallengeScore) {
            showMathChallenge();
            nextMathChallengeScore += 50;
        }

        if (random.nextInt(100) < 10) {
            int obstacleX = random.nextInt(getWidth() - 55);
            obstacles.add(new Rectangle(obstacleX, 0, 55, 55));
        }

        if (random.nextInt(100) < 10) {
            int coinX = random.nextInt(getWidth() - 55);
            coins.add(new Rectangle(coinX, 0, 55, 55));
        }

        if (random.nextInt(1000) < 5) {
            int flagX = random.nextInt(getWidth() - 55);
            flags.add(new Rectangle(flagX, 0, 55, 55));
        }

        if (score >= level3Score) {
            timer.stop();
            JOptionPane.showMessageDialog(this, "Gratulacje! Ukończyłeś wszystkie poziomy!");
            System.exit(0);
        } else if (score >= level2Score && currentLevel == 2) {
            timer.stop();
            currentLevel++;
            elementSpeed += 2;
            resetLevel();
            JOptionPane.showMessageDialog(this, "Poziom 2 ukończony! Czas na Poziom 3.");
            timer.start();
        } else if (score >= level1Score && currentLevel == 1) {
            timer.stop();
            currentLevel++;
            elementSpeed += 3;
            resetLevel();
            JOptionPane.showMessageDialog(this, "Poziom 1 ukończony! Czas na Poziom 2.");
            timer.start();
        }

        Iterator<Rectangle> obstacleIterator = obstacles.iterator();
        while (obstacleIterator.hasNext()) {
            Rectangle obstacle = obstacleIterator.next();
            obstacle.y += elementSpeed;
            if (obstacle.y > getHeight()) {
                obstacleIterator.remove();
            } else if (obstacle.intersects(new Rectangle(carX, carY, carWidth, carHeight))) {
                hearts--;
                if (hearts == 0) {
                    timer.stop();
                    JOptionPane.showMessageDialog(this, "Koniec gry! Twój wynik to: " + score);
                    System.exit(0);
                }
                obstacleIterator.remove();
            }
        }

        Iterator<Rectangle> coinIterator = coins.iterator();
        while (coinIterator.hasNext()) {
            Rectangle coin = coinIterator.next();
            coin.y += elementSpeed;
            if (coin.y > getHeight()) {
                coinIterator.remove();
            } else if (coin.intersects(new Rectangle(carX, carY, carWidth, carHeight))) {
                coinIterator.remove();
                score += 10;
            }
        }

        Iterator<Rectangle> flagIterator = flags.iterator();
        while (flagIterator.hasNext()) {
            Rectangle flag = flagIterator.next();
            flag.y += elementSpeed;
            if (flag.y > getHeight()) {
                flagIterator.remove();
            } else if (flag.intersects(new Rectangle(carX, carY, carWidth, carHeight))) {
                flagIterator.remove();
                score += 20;
            }
        }

        repaint();
    }

    /**
     * Metoda odpowiedzialna za resetowanie poziomów.
     */
    private void resetLevel() {

        // Logika resetowania poziomów

        score = 0;
        nextMathChallengeScore = 50;
        obstacles.clear();
        coins.clear();
        flags.clear();
        hearts = 3;
        repaint();
    }

    /**
     * Wyświetla losowe zadanie matematyczne w zależności od poziomu gry.
     * Zadanie wywoływane jest w oknie dialogowym, a odpowiedzi gracza są weryfikowane.
     */
    private void showMathChallenge() {

        // Logika wyświetlania zadania matematycznego

        int a = random.nextInt(10) + 1;
        int b = random.nextInt(10) + 1;
        String question = "";
        int correctAnswer = 0;

        if (currentLevel == 1) {
            if (random.nextBoolean()) {
                correctAnswer = a + b;
                question = a + " + " + b + " = ?";
            } else {
                correctAnswer = a - b;
                question = a + " - " + b + " = ?";
            }
        } else if (currentLevel == 2) {
            if (random.nextBoolean()) {
                correctAnswer = a * b;
                question = a + " * " + b + " = ?";
            } else {

                while (a % b != 0) {
                    a = random.nextInt(10) + 1;
                    b = random.nextInt(9) + 1;
                }
                correctAnswer = a / b;
                question = a + " / " + b + " = ?";
            }
        } else if (currentLevel == 3) {
            if (random.nextBoolean()) {
                correctAnswer = (int) Math.pow(a, 2);
                question = a + "² = ?";
            } else {
                correctAnswer = (int) Math.sqrt(a * a);
                question = "√" + (a * a) + " = ?";
            }
        }

        String answer = JOptionPane.showInputDialog(this, question, "Zadanie matematyczne", JOptionPane.QUESTION_MESSAGE);

        try {
            if (Integer.parseInt(answer) == correctAnswer) {
                JOptionPane.showMessageDialog(this, "Brawo! Poprawna odpowiedź!");
            } else {
                hearts--;
                JOptionPane.showMessageDialog(this, "Błędna odpowiedź! Tracisz jedno życie.");
            }
        } catch (NumberFormatException ex) {
            hearts--;
            JOptionPane.showMessageDialog(this, "Nieprawidłowy format odpowiedzi! Tracisz jedno życie.");
        }

        if (hearts <= 0) {
            timer.stop();
            JOptionPane.showMessageDialog(this, " Utraciłeś wszystkie życia, koniec gry! Twój wynik to: " + score);
            System.exit(0);
        }
    }


}

/**
 * Główna klasa MathRace odpowiedzialna za uruchomienie gry.
 */
public class MathRace extends JFrame {

    /**
     * Konstruktor inicjalizujący główne okno gry.
     */
    public MathRace() {
        setTitle("Matematyczny wyścig");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        add(new MathRacePanel());
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    /**
     * Punkt wejścia do aplikacji. Tworzy i uruchamia grę w odrębnym wątku.
     *
     * @param args argumenty wiersza poleceń
     */

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MathRace::new);
    }
}
