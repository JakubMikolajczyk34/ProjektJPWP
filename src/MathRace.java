import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

class MathRacePanel extends JPanel implements ActionListener {
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
    private final int maxScore = 500;
    private int hearts = 3;
    private boolean isGameStarted = false; // Flaga dla stanu gry
    private boolean isGamePaused = false; // Flaga dla zatrzymanej gry

    // Zmienne dla obrazów
    private Image carImage;
    private Image coinImage;
    private Image obstacleImage;
    private Image flagImage;
    private Image heartImage;

    // Zmienna dla przycisku "MENU"
    private JButton menuButton;

    public MathRacePanel() {
        this.setFocusable(true);
        this.setPreferredSize(new Dimension(1050, 800));
        this.setBackground(Color.GRAY);
        this.setLayout(null); // Używamy layoutu null, aby ręcznie ustawiać komponenty

        // Wczytywanie obrazków i skalowanie ich do odpowiedniego rozmiaru
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

        // Przyciski
        menuButton = new JButton("MENU");
        menuButton.setBounds(950, 10, 80, 30);
        menuButton.setFocusable(false);
        menuButton.setBackground(Color.GRAY);
        menuButton.setForeground(Color.WHITE);
        menuButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Akcja wywołująca menu opcji po kliknięciu
                showMenuOptions();
            }
        });
        this.add(menuButton);

        JButton startButton = new JButton("START");
        startButton.setBounds(460, 370, 130, 50);
        startButton.setFocusable(false);
        startButton.setBackground(Color.RED);
        startButton.setForeground(Color.WHITE);
        startButton.setFont(new Font("Arial", Font.BOLD, 18));
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                isGameStarted = true; // Ustawienie flagi, że gra się rozpoczęła
                startButton.setVisible(false); // Ukrycie przycisku START
                timer.start(); // Uruchomienie timera
                requestFocus(); // Przekierowanie focusu na panel gry
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

        // Timer nie startuje automatycznie - czeka na kliknięcie START
        timer = new Timer(30, this);
    }

    private void showMenuOptions() {
        // Zatrzymanie gry po kliknięciu przycisku MENU
        if (isGameStarted && !isGamePaused) {
            isGamePaused = true; // Ustawiamy flagę, że gra jest zatrzymana
            timer.stop(); // Zatrzymanie timera
        }

        // Tworzenie panelu z przyciskami
        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS)); // Układ pionowy

        // Tworzenie przycisków z ustalonym rozmiarem
        Dimension buttonSize = new Dimension(200, 50); // Wszystkie przyciski tej samej wielkości

        JButton resumeButton = new JButton("Wznów grę");
        JButton restartButton = new JButton("Zacznij od nowa");
        JButton exitButton = new JButton("Zakończ grę");

        // Ustawienie rozmiarów przycisków
        setButtonProperties(resumeButton, buttonSize);
        setButtonProperties(restartButton, buttonSize);
        setButtonProperties(exitButton, buttonSize);

        // Dodawanie akcji do przycisków
        resumeButton.addActionListener(e -> {
            if (isGamePaused) {
                isGamePaused = false;
                timer.start(); // Wznowienie gry po przerwie
            }
            SwingUtilities.getWindowAncestor(menuPanel).dispose(); // Zamknięcie okna dialogowego
        });

        restartButton.addActionListener(e -> {
            resetGame(); // Resetowanie gry
            SwingUtilities.getWindowAncestor(menuPanel).dispose(); // Zamknięcie okna dialogowego
        });

        exitButton.addActionListener(e -> {
            System.exit(0); // Zakończenie gry
        });

        // Dodawanie przycisków do panelu
        menuPanel.add(resumeButton);
        menuPanel.add(Box.createRigidArea(new Dimension(0, 10))); // Odstęp między przyciskami
        menuPanel.add(restartButton);
        menuPanel.add(Box.createRigidArea(new Dimension(0, 10))); // Odstęp między przyciskami
        menuPanel.add(exitButton);

        // Wyświetlenie opcji w większym oknie dialogowym
        JOptionPane.showOptionDialog(
                this,
                menuPanel,
                "Menu",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                new Object[]{}, // Usunięcie domyślnych przycisków
                null
        );
    }

    // Metoda konfigurująca przyciski
    private void setButtonProperties(JButton button, Dimension size) {
        button.setPreferredSize(size); // Ustawienie preferowanego rozmiaru
        button.setMaximumSize(size);   // Ustawienie maksymalnego rozmiaru
        button.setFocusable(false);    // Wyłączenie domyślnego obramowania focus
        button.setAlignmentX(Component.CENTER_ALIGNMENT); // Wyśrodkowanie w poziomie
        button.setBackground(Color.LIGHT_GRAY); // Tło przycisku
        button.setForeground(Color.BLACK);     // Kolor tekstu
        button.setFont(new Font("Arial", Font.BOLD, 16)); // Większa czcionka
    }



    // Metoda resetująca grę
    private void resetGame() {
        isGameStarted = false;
        isGamePaused = false;
        score = 0;
        hearts = 3;
        obstacles.clear();
        coins.clear();
        flags.clear();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (!isGameStarted || isGamePaused) {
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 28));
            g.drawString("Aby rozpocząć grę, wciśnij przycisk START", 200, 300);
            return; // Nie rysujemy nic więcej przed startem gry
        }

        // Rysowanie górnej sekcji z wynikiem i sercami
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString("Wynik: " + score, 10, 20);

        // Obliczenie pozycji serc na środku ekranu
        int heartsWidth = hearts * 35; // Oblicz szerokość wszystkich serc razem
        int heartsX = (getWidth() - heartsWidth) / 2; // Wyśrodkowanie serc

        // Rysowanie serc na środku ekranu
        for (int i = 0; i < hearts; i++) {
            g.drawImage(heartImage, heartsX + i * 35, 10, this);
        }

        // Rysowanie samochodu
        g.drawImage(carImage, carX, carY, carWidth, carHeight, this);

        // Rysowanie przeszkód
        for (Rectangle obstacle : obstacles) {
            g.drawImage(obstacleImage, obstacle.x, obstacle.y, obstacle.width, obstacle.height, this);
        }

        // Rysowanie monet
        for (Rectangle coin : coins) {
            g.drawImage(coinImage, coin.x, coin.y, coin.width, coin.height, this);
        }

        // Rysowanie flagi
        for (Rectangle flag : flags) {
            g.drawImage(flagImage, flag.x, flag.y, flag.width, flag.height, this);
        }
    }



    private void updateElements(ArrayList<Rectangle> elements, int speed) {
        Iterator<Rectangle> iterator = elements.iterator();
        while (iterator.hasNext()) {
            Rectangle element = iterator.next();
            element.y += speed;
            if (element.y > getHeight()) {
                iterator.remove();
            }
        }
    }



    @Override
    public void actionPerformed(ActionEvent e) {
        if (!isGameStarted || isGamePaused) return; // Gra działa tylko po kliknięciu START

        // Sprawdzanie, czy osiągnięto maksymalny wynik
        if (score >= maxScore) {
            timer.stop();
            JOptionPane.showMessageDialog(this, "Brawo! Ukończyłeś poziom!");
            System.exit(0); // Kończenie gry
        }

        // Wyświetlanie zadania matematycznego przy określonych progach punktowych
        if (score >= nextMathChallengeScore) {
            showMathChallenge(); // Wywołanie metody, bez sprawdzania zwracanej wartości
            nextMathChallengeScore += 50; // Ustawienie nowego progu punktowego
        }

        // Dodawanie nowych przeszkód, monet i flag
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

        // Przesuwanie i obsługa kolizji dla przeszkód
        Iterator<Rectangle> obstacleIterator = obstacles.iterator();
        while (obstacleIterator.hasNext()) {
            Rectangle obstacle = obstacleIterator.next();
            obstacle.y += 10;
            if (obstacle.y > getHeight()) {
                obstacleIterator.remove();
            } else if (obstacle.intersects(new Rectangle(carX, carY, carWidth, carHeight))) {
                hearts--; // Utrata życia przy kolizji
                if (hearts == 0) {
                    timer.stop();
                    JOptionPane.showMessageDialog(this, "Koniec gry! Twój wynik to: " + score);
                    System.exit(0); // Kończenie gry po utracie wszystkich serc
                }
                obstacleIterator.remove();
            }
        }

        // Przesuwanie i obsługa kolizji dla monet
        Iterator<Rectangle> coinIterator = coins.iterator();
        while (coinIterator.hasNext()) {
            Rectangle coin = coinIterator.next();
            coin.y += 10;
            if (coin.y > getHeight()) {
                coinIterator.remove();
            } else if (coin.intersects(new Rectangle(carX, carY, carWidth, carHeight))) {
                coinIterator.remove();
                score += 10; // Dodanie punktów za zebranie monety
            }
        }

        // Przesuwanie i obsługa kolizji dla flag
        Iterator<Rectangle> flagIterator = flags.iterator();
        while (flagIterator.hasNext()) {
            Rectangle flag = flagIterator.next();
            flag.y += 10;
            if (flag.y > getHeight()) {
                flagIterator.remove();
            } else if (flag.intersects(new Rectangle(carX, carY, carWidth, carHeight))) {
                flagIterator.remove();
                score += 20; // Dodanie punktów za zebranie flagi
            }
        }

        repaint(); // Odświeżenie ekranu po każdej klatce
    }

    // Funkcja wyświetlająca zadanie matematyczne
    private void showMathChallenge() {
        int a = random.nextInt(10) + 1; // Losowa liczba od 1 do 10
        int b = random.nextInt(10) + 1; // Losowa liczba od 1 do 10
        String[] operators = {"+", "-", "*"};
        String operator = operators[random.nextInt(operators.length)];

        int correctAnswer;
        String question;

        switch (operator) {
            case "+" -> {
                correctAnswer = a + b;
                question = a + " + " + b + " = ?";
            }
            case "-" -> {
                correctAnswer = a - b;
                question = a + " - " + b + " = ?";
            }
            case "*" -> {
                correctAnswer = a * b;
                question = a + " * " + b + " = ?";
            }
            default -> throw new IllegalStateException("Unexpected value: " + operator);
        }

        String answer = JOptionPane.showInputDialog(this, question, "Zadanie matematyczne", JOptionPane.QUESTION_MESSAGE);

        try {
            if (Integer.parseInt(answer) == correctAnswer) {
                JOptionPane.showMessageDialog(this, "Brawo! Poprawna odpowiedź!");
            } else {
                hearts--; // Odejmij życie za błędną odpowiedź
                JOptionPane.showMessageDialog(this, "Błędna odpowiedź! Tracisz jedno życie.");
            }
        } catch (NumberFormatException ex) {
            hearts--; // Odejmij życie, jeśli wpisano niepoprawną wartość
            JOptionPane.showMessageDialog(this, "Nieprawidłowy format odpowiedzi! Tracisz jedno życie.");
        }

        // Sprawdź, czy liczba żyć spadła do zera
        if (hearts <= 0) {
            timer.stop();
            JOptionPane.showMessageDialog(this, "Koniec gry! Wykorzystałeś wszystkie życia. Twój wynik to: " + score);
            System.exit(0); // Kończenie gry
        }
    }


}


public class MathRace extends JFrame {
    public MathRace() {
        setTitle("Matematyczny wyścig");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        add(new MathRacePanel());
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MathRace::new);
    }
}

