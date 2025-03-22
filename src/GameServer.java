package src;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class GameServer {
    // Configurazione del server
    private static final int PORT = 2006;
    private static final String USERS_CSV_FILE = "data/users.csv";
    // Memorizza gli utenti registrati con username come chiave
    private static Map<String, User> users = new HashMap<>();
    // Generatore di numeri casuali per il gioco
    private static Random random = new Random();

    // Classe interna per gestire i dati degli utenti
    private static class User {
        String username;
        List<Integer> punteggiPartite;
        int partiteGiocate;
        
        // Costruttore che inizializza un nuovo utente
        public User(String username) {
            this.username = username;
            this.punteggiPartite = new ArrayList<>();
            this.partiteGiocate = 0;
        }
        
        // Calcola il punteggio totale dell'utente
        public int getScoreTotale() {
            int total = 0;
            for (int score : punteggiPartite) {
                total += score;
            }
            return total;
        }
        
        // Converte l'utente in stringa per salvarlo su CSV
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder(username);
            for (int score : punteggiPartite) {
                sb.append(",").append(score);
            }
            return sb.toString();
        }
    }

    // Creazione del database
    private static void createData() {
        // Crea un oggetto File per la cartella, estraendo il percorso della cartella dal file
        File directory = new File(USERS_CSV_FILE).getParentFile(); 
    
        // Verifica se la cartella esiste
        if (!directory.exists()) {
            // Crea la cartella
            if (directory.mkdirs()) { 
                System.out.println("Creazione di data/users.csv");
            } else {
                System.out.println("Errore nella creazione della cartella.");
            }
        } else {
            System.out.println("Caricamento di data/users.csv");
        }
    }

    // Metodo principale che avvia il server
    public static void main(String[] args) {

        //crea il file csv nella directory
        createData();

        // Carica gli utenti esistenti dal file CSV
        caricaUtenti();

        // Crea un thread pool per gestire le connessioni in parallelo
        ExecutorService pool = Executors.newCachedThreadPool();
        
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server avviato sulla porta " + PORT);

            // Loop infinito per accettare nuove connessioni
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Nuovo client connesso: " + clientSocket.getInetAddress().getHostAddress());
                // Passa il client a un thread separato dal pool
                pool.execute(new ClientHandler(clientSocket));
            }
        } catch (IOException e) {
            System.err.println("Errore del server: " + e.getMessage());
        } finally {
            // Chiude il pool quando il server termina
            pool.shutdown();
        }
    }

    // Carica i dati degli utenti dal file CSV
    private static void caricaUtenti() {
        File file = new File(USERS_CSV_FILE);
        if (!file.exists()) {
            try {
                // Crea un nuovo file se non esiste
                file.createNewFile();
                System.out.println("Creato nuovo file users.csv");
            } catch (IOException e) {
                System.err.println("Impossibile creare il file " + USERS_CSV_FILE);
            }
            return;
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(USERS_CSV_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 1) {
                    String username = parts[0];
                    User user = new User(username);
                    
                    // Carica i punteggi delle partite precedenti
                    for (int i = 1; i < parts.length; i++) {
                        try {
                            int score = Integer.parseInt(parts[i]);
                            user.punteggiPartite.add(score);
                            user.partiteGiocate++;
                        } catch (NumberFormatException e) {
                            System.err.println("Errore nel parsing del punteggio: " + parts[i]);
                        }
                    }
                    
                    // Aggiunge l'utente alla mappa
                    users.put(username, user);
                }
            }
            System.out.println("Utenti registrati: " + users.size());
        } catch (IOException e) {
            System.err.println("Errore nella lettura del file utenti: " + e.getMessage());
        }
    }

    // Salva tutti gli utenti nel file CSV
    private static void salvaUtenti() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(USERS_CSV_FILE))) {
            for (User user : users.values()) {
                writer.println(user.toString());
            }
        } catch (IOException e) {
            System.err.println("Errore durante la scrittura sul file utenti: " + e.getMessage());
        }
    }

    // Aggiunge un punteggio a un utente esistente o ne crea uno nuovo
    private static void aggiungiPunteggioPartita(String username, int punteggio) {
        User user = users.getOrDefault(username, new User(username));
        user.punteggiPartite.add(punteggio);
        user.partiteGiocate++;
        users.put(username, user);
        // Salva immediatamente dopo ogni aggiornamento di punteggio
        salvaUtenti();
    }

    // Classe interna per gestire ogni client in un thread separato
    private static class ClientHandler implements Runnable {
        private Socket clientSocket;
        private PrintWriter out;
        private BufferedReader in;
        private String username;
        private int numeroSegreto;
        private int tentativi;
        private int puntiPerVicinanza;
        private static final int MAX_TENTATIVI = 3;

        // Array di messaggi casuali per le diverse situazioni di gioco
        private static final String[] VINCITA_MESSAGGI = {
            "Grande! Hai indovinato!",
            "Esatto! Sei un maestro dei numeri!",
            "Numero esatto! Sei un fenomeno!"
        };

        private static final String[] PERDITA_CON_PUNTI_MESSAGGI = {
            "Sei andato vicino!",
            "Mancava poco!",
            "Sei sulla strada giusta, un altro tentativo!"
        };

        private static final String[] PERDITA_MESSAGGI = {
            "Ritenta, sarai più fortunato!",
            "Sfida mancata, ma la prossima sarà meglio!",
            "Non ti arrendere, prova ancora!"
        };

        // Costruttore che inizializza il handler con il socket client
        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        // Logica principale di gestione della connessione client
        @Override
        public void run() {
            try {
                // Inizializzazione dei canali di comunicazione
                out = new PrintWriter(clientSocket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                // Gestione del login/registrazione
                boolean authenticated = false;
                while (!authenticated) {
                    String request = in.readLine();
                    if (request == null) return;

                    if (request.startsWith("LOGIN:")) {
                        // Tentativo di login con username esistente
                        String username = request.substring(6).trim();
                        if (users.containsKey(username)) {
                            this.username = username;
                            out.println("SUCCESS:" + username);
                            authenticated = true;
                        } else {
                            out.println("ERROR:Username non trovato. Registrati prima.");
                        }
                    } else if (request.startsWith("REGISTER:")) {
                        // Registrazione nuovo utente
                        String username = request.substring(9).trim();
                        if (users.containsKey(username)) {
                            out.println("ERROR:Username già esistente");
                        } else {
                            users.put(username, new User(username));
                            salvaUtenti();
                            out.println("SUCCESS:Registrazione completata");
                        }
                    }
                }

                // Ciclo di gioco - permette di giocare più partite consecutive
                boolean isRigioca = true;
                while (isRigioca) {
                    startNewGame();

                    boolean isGameOver = false;

                    // Ciclo di gestione dei tentativi in una singola partita
                    while (!isGameOver && tentativi < MAX_TENTATIVI) {
                        String input = in.readLine();
                        if (input == null) return;

                        if (input.equals("LEADERBOARD")) {
                            // Invia la classifica senza interrompere la partita
                            inviaClassifica();
                            continue;
                        }

                        if (input.startsWith("GUESS:")) {
                            try {
                                // Analisi del tentativo dell'utente
                                int number = Integer.parseInt(input.substring(6).trim());
                                tentativi++;
                                
                                // Determina se il tentativo è vicino al numero corretto
                                boolean isCloseGuess = Math.abs(number - numeroSegreto) * 100 / numeroSegreto <= 10 && number != numeroSegreto;
                                
                                // Memorizza i tentativi vicini per il calcolo dei punti
                                if (isCloseGuess) {
                                    puntiPerVicinanza++;
                                }

                                if (number == numeroSegreto) {
                                    // Numero indovinato correttamente
                                    isGameOver = true;
                                    // Calcolo punteggio per aver indovinato
                                    int puntiTotali = 3; // Punti base
                                    switch (tentativi) {
                                        case 1: puntiTotali += 3; break; // Bonus per il primo tentativo
                                        case 2: puntiTotali += 2; break; // Bonus per il secondo tentativo
                                        case 3: puntiTotali += 1; break; // Bonus per il terzo tentativo
                                    }
                                    // Aggiorna i dati dell'utente
                                    aggiungiPunteggioPartita(username, puntiTotali);
                                    String messaggioVincita = getMessaggioCasuale(VINCITA_MESSAGGI);
                                    String puntiTesto = (puntiTotali == 1) ? "+" + puntiTotali + " punto!" : "+" + puntiTotali + " punti!";
                                    out.println("CORRECT:" + messaggioVincita + " " + puntiTesto);
                                } else if (number < numeroSegreto) {
                                    // Numero troppo basso
                                    out.println("LOW:Numero troppo basso");
                                } else {
                                    // Numero troppo alto
                                    out.println("HIGH:Numero troppo alto");
                                }

                                // Verifica se ha esaurito i tentativi senza indovinare
                                if (tentativi >= MAX_TENTATIVI && !isGameOver) {
                                    String messaggioPerdita;
                                    String puntiTesto;
                                    
                                    // Assegna punti parziali per tentativi vicini
                                    if (puntiPerVicinanza > 0) {
                                        messaggioPerdita = getMessaggioCasuale(PERDITA_CON_PUNTI_MESSAGGI);
                                        puntiTesto = (puntiPerVicinanza == 1) ? "+" + puntiPerVicinanza + " punto" : "+" + puntiPerVicinanza + " punti";
                                        // Registra i punti di vicinanza
                                        aggiungiPunteggioPartita(username, puntiPerVicinanza);
                                    } else {
                                        messaggioPerdita = getMessaggioCasuale(PERDITA_MESSAGGI);
                                        puntiTesto = "0 punti";
                                        // Registra 0 punti
                                        aggiungiPunteggioPartita(username, 0);
                                    }
                                    
                                    // Invia messaggio di fine partita
                                    out.println("GAMEOVER:" + messaggioPerdita + " " + puntiTesto + ". Il numero era " + numeroSegreto);
                                    isGameOver = true;
                                }
                            } catch (NumberFormatException e) {
                                out.println("ERROR:Inserisci un numero valido");
                            }
                        } else if (input.equals("QUIT")) {
                            // Gestione uscita dal gioco
                            isRigioca = false;
                            isGameOver = true;
                        } else if (input.equals("LOGOUT")) {
                            // Gestione logout
                            isRigioca = false;
                            isGameOver = true;
                        }
                    }

                    // Attende la richiesta di giocare ancora
                    if (isRigioca) {
                        String playAgainRequest = in.readLine();
                        if (playAgainRequest == null || !playAgainRequest.equals("PLAYAGAIN")) {
                            isRigioca = false;
                        }
                    }
                }
            } catch (IOException e) {
                System.err.println("Errore nella gestione del client: " + e.getMessage());
            } finally {
                // Chiusura del socket e delle risorse
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    System.err.println("Errore nella chiusura del socket: " + e.getMessage());
                }
            }
        }

        // Inizializza una nuova partita generando un numero casuale
        private void startNewGame() {
            numeroSegreto = random.nextInt(100) + 1;
            tentativi = 0;
            puntiPerVicinanza = 0;
            System.out.println("Nuova partita per " + username + ". Numero segreto: " + numeroSegreto);
            out.println("NEWGAME:Benvenuto " + username + "!");
        }

        // Invia la classifica ordinata per punteggio al client
        private void inviaClassifica() {
            List<User> classifica = new ArrayList<>(users.values());
            // Ordina per punteggio totale in ordine decrescente
            classifica.sort((u1, u2) -> Integer.compare(u2.getScoreTotale(), u1.getScoreTotale()));
            
            // Invio intestazione con formattazione tabellare
            out.println(String.format("%-10s | %-20s | %-15s | %-15s", "Posizione", "Username", "Punteggio", "Partite"));
            out.println("-".repeat(65)); // Linea di separazione
            
            // Invia ogni riga della classifica con posizione, username, punteggio e partite
            for (int i = 0; i < classifica.size(); i++) {
                User user = classifica.get(i);
                out.println(String.format("%-10s | %-20s | %-15d | %-15d", 
                            i + 1, // Posizione (indice + 1)
                            user.username, 
                            user.getScoreTotale(), 
                            user.partiteGiocate));
            }
            
            out.println("END_LEADERBOARD");
        }

        // Metodo per selezionare un messaggio casuale da un array
        private String getMessaggioCasuale(String[] messaggi) {
            return messaggi[random.nextInt(messaggi.length)];
        }
    }
}
