package src;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import javax.swing.*;


public class GameClient {
    // Configurazione del server
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 2006;
    
    // Componenti di comunicazione con il server
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    
    // Componenti principali dell'interfaccia
    private JFrame mainFrame;
    private CardLayout cardLayout;
    private JPanel mainPanel;
    
    // Componenti del pannello di login
    private JPanel loginPanel;
    private JTextField usernameField;
    private JButton loginButton;
    private JButton registerButton;
    private JLabel loginErrorLabel;
    
    // Componenti del pannello di gioco
    private JPanel gamePanel;
    private JLabel welcomeLabel;
    private JTextField guessField;
    private JButton guessButton;
    private JLabel resultLabel;
    private JLabel attemptsLabel;
    private int attemptCount;
    
    // Componenti del pannello dei risultati
    private JPanel resultPanel;
    private JLabel finalResultLabel;
    private JButton playAgainButton;
    private JButton quitButton;
    
    // Dati di gioco
    private String username;
    private boolean isCorrect = false;
    
    // Componenti del pannello classifica
    private JPanel leaderboardPanel;
    private JTextArea leaderboardTextArea;
    private JButton backButton;
    
    // Costruttore che avvia la connessione e l'interfaccia
    public GameClient() {
        connectToServer();
        GUI();
    }
    
    // Inizializza la connessione con il server
    private void connectToServer() {
        try {
            socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Impossibile connettersi al server: " + e.getMessage(), 
                                      "Errore di connessione", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }
    
    // Costruisce l'interfaccia grafica completa
    private void GUI() {
        // Configurazione finestra principale
        mainFrame = new JFrame("Indovina il Numero");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setMinimumSize(new Dimension(600, 400));
        mainFrame.setResizable(false);
        mainFrame.setLocationRelativeTo(null);
        
        // Configurazione layout a schede
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        
        // Inizializzazione pannelli
        PannelloLogin();
        PannelloGioco();
        risultati();
        PannelloClassifica();
        
        // Aggiunta pannelli al container principale
        mainPanel.add(loginPanel, "login");
        mainPanel.add(gamePanel, "game");
        mainPanel.add(resultPanel, "result");
        mainPanel.add(leaderboardPanel, "leaderboard");
        
        mainFrame.add(mainPanel);
        cardLayout.show(mainPanel, "login");
        mainFrame.setVisible(true);
        
        // Gestione chiusura finestra
        mainFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                quit();
            }
        });
    }
    
    // Crea il pannello di login/registrazione
    private void PannelloLogin() {
        loginPanel = new JPanel();
        loginPanel.setLayout(new BoxLayout(loginPanel, BoxLayout.Y_AXIS));
        loginPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Titolo del gioco
        JLabel titleLabel = new JLabel("GuessMaster");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 30));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Etichetta istruzioni
        JLabel instructionLabel = new JLabel("Inserisci il tuo username:");
        instructionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Campo username
        usernameField = new JTextField(20);
        usernameField.setMaximumSize(new Dimension(200, 30));
        usernameField.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Pannello pulsanti
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        loginButton = new JButton("Accedi");
        registerButton = new JButton("Registrati");
        
        buttonPanel.add(loginButton);
        buttonPanel.add(registerButton);
        
        // Etichetta per messaggi di errore
        loginErrorLabel = new JLabel(" ");
        loginErrorLabel.setForeground(Color.RED);
        loginErrorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Assemblaggio componenti con spaziatura
        loginPanel.add(Box.createVerticalGlue());
        loginPanel.add(titleLabel);
        loginPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        loginPanel.add(instructionLabel);
        loginPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        loginPanel.add(usernameField);
        loginPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        loginPanel.add(buttonPanel);
        loginPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        loginPanel.add(loginErrorLabel);
        loginPanel.add(Box.createVerticalGlue());
        
        // Gestori eventi
        loginButton.addActionListener(e -> Login());
        registerButton.addActionListener(e -> Registrati());
        usernameField.addActionListener(e -> Login());
    }
    
    // Crea il pannello di gioco
    private void PannelloGioco() {
        gamePanel = new JPanel();
        gamePanel.setLayout(new BoxLayout(gamePanel, BoxLayout.Y_AXIS));
        gamePanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Etichetta di benvenuto personalizzata
        welcomeLabel = new JLabel("Benvenuto!");
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 18));
        welcomeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Istruzioni gioco
        JLabel instructionLabel = new JLabel("Indovina un numero tra 1 e 100:");
        instructionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Campo input tentativo
        guessField = new JTextField(10);
        guessField.setMaximumSize(new Dimension(100, 30));
        guessField.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Pulsante invio
        guessButton = new JButton("Invia");
        guessButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Etichetta risultato tentativo
        resultLabel = new JLabel(" ");
        resultLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Contatore tentativi
        attemptsLabel = new JLabel("Tentativi: 0/3");
        attemptsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Pulsanti aggiuntivi
        JButton leaderboardButton = new JButton("Visualizza Classifica");
        JButton logoutButton = new JButton("Logout");
        
        // Pannello per pulsanti in basso
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BorderLayout());
        bottomPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        bottomPanel.add(logoutButton, BorderLayout.WEST);  // Logout a sinistra
        bottomPanel.add(leaderboardButton, BorderLayout.EAST);  // Classifica a destra
        
        // Assemblaggio componenti con spaziatura
        gamePanel.add(Box.createVerticalGlue());
        gamePanel.add(welcomeLabel);
        gamePanel.add(Box.createRigidArea(new Dimension(0, 20)));
        gamePanel.add(instructionLabel);
        gamePanel.add(Box.createRigidArea(new Dimension(0, 10)));
        gamePanel.add(guessField);
        gamePanel.add(Box.createRigidArea(new Dimension(0, 10)));
        gamePanel.add(guessButton);
        gamePanel.add(Box.createRigidArea(new Dimension(0, 10)));
        gamePanel.add(resultLabel);
        gamePanel.add(Box.createRigidArea(new Dimension(0, 10)));
        gamePanel.add(attemptsLabel);
        gamePanel.add(Box.createVerticalGlue());
        gamePanel.add(bottomPanel);
        
        // Gestori eventi
        guessButton.addActionListener(e -> controllaTentativi());
        guessField.addActionListener(e -> controllaTentativi());
        leaderboardButton.addActionListener(e -> visualizzaClassifica());
        logoutButton.addActionListener(e -> logout());
    }
    
    // Crea il pannello dei risultati finali
    private void risultati() {
        resultPanel = new JPanel();
        resultPanel.setLayout(new BoxLayout(resultPanel, BoxLayout.Y_AXIS));
        resultPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Etichetta risultato finale
        finalResultLabel = new JLabel("Game Over");
        finalResultLabel.setFont(new Font("Arial", Font.BOLD, 18));
        finalResultLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Pannello pulsanti
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Pulsanti opzione fine partita
        playAgainButton = new JButton("Gioca ancora");
        quitButton = new JButton("Esci");
        
        buttonPanel.add(playAgainButton);
        buttonPanel.add(quitButton);
        
        // Assemblaggio componenti con spaziatura
        resultPanel.add(Box.createVerticalGlue());
        resultPanel.add(finalResultLabel);
        resultPanel.add(Box.createRigidArea(new Dimension(0, 30)));
        resultPanel.add(buttonPanel);
        resultPanel.add(Box.createVerticalGlue());
        
        // Gestori eventi
        playAgainButton.addActionListener(e -> Rigioca());
        quitButton.addActionListener(e -> quit());
    }
    
    // Crea il pannello della classifica
    private void PannelloClassifica() {
        leaderboardPanel = new JPanel();
        leaderboardPanel.setLayout(new BorderLayout(10, 10));
        leaderboardPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Titolo classifica
        JLabel titleLabel = new JLabel("Classifica");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setHorizontalAlignment(JLabel.CENTER);
        
        // Area testo per visualizzare la classifica
        leaderboardTextArea = new JTextArea(15, 30);
        leaderboardTextArea.setEditable(false);
        // Font monospace per allineamento tabellare
        leaderboardTextArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(leaderboardTextArea);
        
        // Pannello inferiore con pulsanti
        JPanel bottomPanel = new JPanel(new BorderLayout());
        
        backButton = new JButton("Torna al Gioco");
        JButton logoutButton = new JButton("Logout");
        
        // Disposizione pulsanti allineati ai bordi
        JPanel leftButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        leftButtonPanel.add(logoutButton);
        
        JPanel rightButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightButtonPanel.add(backButton);
        
        bottomPanel.add(leftButtonPanel, BorderLayout.WEST);
        bottomPanel.add(rightButtonPanel, BorderLayout.EAST);
        
        // Gestori eventi
        backButton.addActionListener(e -> cardLayout.show(mainPanel, "game"));
        logoutButton.addActionListener(e -> logout());
        
        // Assemblaggio pannello principale
        leaderboardPanel.add(titleLabel, BorderLayout.NORTH);
        leaderboardPanel.add(scrollPane, BorderLayout.CENTER);
        leaderboardPanel.add(bottomPanel, BorderLayout.SOUTH);
    }
    
    // Gestisce il processo di login
    private void Login() {
        username = usernameField.getText().trim();
        
        // Validazione input
        if (username.isEmpty()) {
            loginErrorLabel.setText("Inserisci un username valido");
            return;
        }
        
        // Controllo lunghezza massima username
        if (username.length() > 18) {
            loginErrorLabel.setText("Username troppo lungo (max 18 caratteri)");
            return;
        }
        
        // Invio richiesta al server
        out.println("LOGIN:" + username);
        
        try {
            // Attesa e gestione risposta
            String response = in.readLine();
            
            if (response.startsWith("ERROR:")) {
                loginErrorLabel.setText(response.substring(6));
            } else if (response.startsWith("SUCCESS:")) {
                Inizio();
            }
        } catch (IOException e) {
            loginErrorLabel.setText("Errore di comunicazione: " + e.getMessage());
        }
    }
    
    // Gestisce il processo di registrazione
    private void Registrati() {
        username = usernameField.getText().trim();
        
        // Validazione input
        if (username.isEmpty()) {
            loginErrorLabel.setText("Inserisci un username valido");
            return;
        }
        
        // Controllo lunghezza massima username
        if (username.length() > 18) {
            loginErrorLabel.setText("Username troppo lungo (max 18 caratteri)");
            return;
        }
        
        // Invio richiesta al server
        out.println("REGISTER:" + username);
        
        try {
            // Attesa e gestione risposta
            String response = in.readLine();
            
            if (response.startsWith("ERROR:")) {
                loginErrorLabel.setText(response.substring(6));
            } else if (response.startsWith("SUCCESS:")) {
                loginErrorLabel.setText("Registrazione completata. Ora puoi accedere.");
            }
        } catch (IOException e) {
            loginErrorLabel.setText("Errore di comunicazione: " + e.getMessage());
        }
    }
    
    // Inizia una nuova partita dopo il login
    private void Inizio() {
        try {
            String gameStart = in.readLine();
            if (gameStart.startsWith("NEWGAME:")) {
                String welcomeMessage = gameStart.substring(8);
                welcomeLabel.setText(welcomeMessage);
                
                // Reset dei campi di gioco
                guessField.setText("");
                resultLabel.setText(" ");
                attemptCount = 0;
                attemptsLabel.setText("Tentativi: " + attemptCount + "/3");
                isCorrect = false;
                
                // Mostra pannello di gioco
                cardLayout.show(mainPanel, "game");
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(mainFrame, "Errore di comunicazione: " + e.getMessage(), 
                "Errore", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    // Gestisce il tentativo dell'utente di indovinare il numero
    private void controllaTentativi() {
        // Verifica limite tentativi
        if (attemptCount >= 3 && !isCorrect) {
            resultLabel.setText("Hai esaurito i tentativi");
            return;
        }

        String guessText = guessField.getText().trim();
        
        // Validazione input
        if (guessText.isEmpty()) {
            resultLabel.setText("Inserisci un numero valido");
            return;
        }
        
        try {
            // Conversione e invio tentativo
            int guess = Integer.parseInt(guessText);
            out.println("GUESS:" + guess);
            
            // Gestione risposte dal server
            String response = in.readLine();
            
            if (response.startsWith("LOW:")) {
                // Numero troppo basso
                resultLabel.setText(response.substring(4));
                resultLabel.setForeground(Color.BLUE);
                attemptCount++;
                attemptsLabel.setText("Tentativi: " + attemptCount + "/3");
                
                // Verifica fine tentativi
                if (attemptCount >= 3) {
                    try {
                        // Legge messaggio game over dal server
                        String gameOverMessage = in.readLine();
                        if (gameOverMessage.startsWith("GAMEOVER:")) {
                            showResult(gameOverMessage.substring(9));
                        }
                    } catch (IOException ex) {
                        System.err.println("Errore nella lettura del messaggio di fine partita: " + ex.getMessage());
                    }
                }
            } else if (response.startsWith("HIGH:")) {
                // Numero troppo alto
                resultLabel.setText(response.substring(5));
                resultLabel.setForeground(Color.BLUE);
                attemptCount++;
                attemptsLabel.setText("Tentativi: " + attemptCount + "/3"); //numero di tentativi
                
                // Verifica fine tentativi
                if (attemptCount >= 3) {
                    try {
                        // Legge messaggio game over dal server
                        String gameOverMessage = in.readLine();
                        if (gameOverMessage.startsWith("GAMEOVER:")) {
                            showResult(gameOverMessage.substring(9));
                        }
                    } catch (IOException ex) {
                        System.err.println("Errore nella lettura del messaggio di fine partita: " + ex.getMessage());
                    }
                }
            } else if (response.startsWith("CORRECT:")) {
                // Numero indovinato
                resultLabel.setText(response.substring(8));
                resultLabel.setForeground(Color.GREEN);
                attemptCount++;
                attemptsLabel.setText("Tentativi: " + attemptCount + "/3");
                isCorrect = true;
                
                // Mostra risultato finale
                showResult(response.substring(8));
            } else if (response.startsWith("GAMEOVER:")) {
                // Partita terminata
                showResult(response.substring(9));
            } else if (response.startsWith("ERROR:")) {
                // Errore dal server
                resultLabel.setText(response.substring(6));
                resultLabel.setForeground(Color.RED);
            }
        } catch (NumberFormatException e) {
            // Input non numerico
            resultLabel.setText("Inserisci un numero valido");
            resultLabel.setForeground(Color.RED);
        } catch (IOException e) {
            // Errore di comunicazione
            JOptionPane.showMessageDialog(mainFrame, "Errore di comunicazione: " + e.getMessage(), 
                "Errore", JOptionPane.ERROR_MESSAGE);
        }
        
        // Reset campo input
        guessField.setText("");
    }
    
    // Visualizza il risultato finale con formattazione avanzata
    private void showResult(String message) {
        // Pannello per contenere messaggi multilinea
        JPanel resultMessagePanel = new JPanel();
        resultMessagePanel.setLayout(new BoxLayout(resultMessagePanel, BoxLayout.Y_AXIS));
        resultMessagePanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Divisione messaggio in testo principale e punteggio
        String mainMessage = message;
        String pointsMessage = "";
        
        if (message.contains("+")) {
            // Estrae il messaggio di punteggio positivo
            int lastIndexOfPlus = message.lastIndexOf("+");
            int pointPosition = message.indexOf("punt", lastIndexOfPlus);
            
            if (pointPosition > 0) {
                mainMessage = message.substring(0, lastIndexOfPlus).trim();
                pointsMessage = message.substring(lastIndexOfPlus).trim();
            }
        } else if (message.contains("0 punti")) {
            // Estrae il messaggio di zero punti ma mantiene l'informazione sul numero
            int zeroPosition = message.lastIndexOf("0 punti");
            if (zeroPosition > 0) {
                mainMessage = message.substring(0, zeroPosition).trim();
                
                // Estrai la parte con i punti e il numero segreto
                int numeroPosition = message.indexOf("Il numero era", zeroPosition);
                if (numeroPosition > 0) {
                    pointsMessage = "0 punti. " + message.substring(numeroPosition);
                } else {
                    pointsMessage = "0 punti.";
                }
            }
        }
        
        // Crea etichetta per messaggio principale
        JLabel mainLabel = new JLabel(mainMessage);
        mainLabel.setFont(new Font("Arial", Font.BOLD, 18));
        mainLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Crea etichetta per il punteggio se presente
        JLabel pointsLabel = null;
        if (!pointsMessage.isEmpty()) {
            pointsLabel = new JLabel(pointsMessage);
            pointsLabel.setFont(new Font("Arial", Font.ITALIC, 16));
            pointsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            
            // Imposta colore in base al punteggio
            if (pointsMessage.startsWith("+")) {
                pointsLabel.setForeground(new Color(0, 128, 0)); // Verde scuro
            } else {
                pointsLabel.setForeground(Color.RED);
            }
        }
        
        // Assemblaggio componenti con spaziatura
        resultMessagePanel.add(mainLabel);
        if (pointsLabel != null) {
            resultMessagePanel.add(Box.createRigidArea(new Dimension(0, 10)));
            resultMessagePanel.add(pointsLabel);
        }
        
        // Reset del pannello risultati
        resultPanel.removeAll();
        
        // Ricrea il pannello pulsanti
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        buttonPanel.add(playAgainButton);
        buttonPanel.add(quitButton);
        
        // Ricostruzione pannello risultati
        resultPanel.setLayout(new BoxLayout(resultPanel, BoxLayout.Y_AXIS));
        resultPanel.add(Box.createVerticalGlue());
        resultPanel.add(resultMessagePanel);
        resultPanel.add(Box.createRigidArea(new Dimension(0, 30)));
        resultPanel.add(buttonPanel);
        resultPanel.add(Box.createVerticalGlue());
        resultPanel.revalidate();
        resultPanel.repaint();
        
        // Visualizza pannello risultati
        cardLayout.show(mainPanel, "result");
    }
    
    // Inizia una nuova partita
    private void Rigioca() {
        try {
            out.println("PLAYAGAIN");
            
            // Reset dei campi di gioco
            guessField.setText("");
            resultLabel.setText(" ");
            attemptCount = 0;
            attemptsLabel.setText("Tentativi: " + attemptCount + "/3");
            isCorrect = false;
            
            // Avvia nuova partita
            Inizio();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(mainFrame, "Errore durante il riavvio: " + e.getMessage(), 
                "Errore", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    // Termina l'applicazione
    private void quit() {
        try {
            out.println("QUIT");
            socket.close();
        } catch (IOException e) {
            System.err.println("Errore nella chiusura del socket: " + e.getMessage());
        }
        System.exit(0);
    }
    
    // Effettua il logout e torna alla schermata iniziale
    private void logout() {
        try {
            out.println("LOGOUT");
            
            // Chiusura connessione esistente
            try {
                socket.close();
            } catch (IOException e) {
                System.err.println("Errore nella chiusura del socket: " + e.getMessage());
            }
            
            // Nuova connessione
            connectToServer();
            
            
            usernameField.setText("");
            loginErrorLabel.setText("");
            cardLayout.show(mainPanel, "login");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(mainFrame, "Errore durante il logout: " + e.getMessage(), 
                "Errore", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    // Visualizza la classifica dei giocatori
    private void visualizzaClassifica() {
        out.println("LEADERBOARD");
        
        try {
            leaderboardTextArea.setText("");
            String line;
            
            // Ricezione e visualizzazione dati fino al marcatore di fine
            while (!(line = in.readLine()).equals("END_LEADERBOARD")) {
                leaderboardTextArea.append(line + "\n");
            }
            
            // Mostra pannello classifica
            cardLayout.show(mainPanel, "leaderboard");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(mainFrame, "Errore nella ricezione della classifica: " + e.getMessage(), 
                "Errore", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    // Metodo principale
    public static void main(String[] args) {
        // Avvia client nell'EDT di Swing
        SwingUtilities.invokeLater(() -> new GameClient());
    }
}
