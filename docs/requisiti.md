# Requisiti del Sistema GuessMaster

## Requisiti Funzionali

### Gestione Utenti
1. Registrazione di nuovi utenti con username unico
2. Login di utenti esistenti
3. Logout che permette il cambio di utente

### Meccanica di Gioco
1. Generazione di un numero casuale tra 1 e 100 per ogni partita
2. Limiti di 3 tentativi per partita
3. Feedback sul tentativo (troppo alto, troppo basso, corretto)
4. Sistema di punteggio basato su:
   - Indovinare il numero (+3 punti base)
   - Bonus in base al numero di tentativi (1° tentativo: +3, 2° tentativo: +2, 3° tentativo: +1)
   - Punti di vicinanza quando i tentativi sono entro il 10% del numero segreto

### Interfaccia e Navigazione
1. Interfaccia grafica con più pannelli (login, gioco, risultati, classifica)
2. Visualizzazione della classifica ordinata per punteggio
3. Possibilità di giocare più partite consecutive
4. Visualizzazione del numero di tentativi rimasti

### Persistenza dei Dati
1. Salvataggio dei dati utente (username, punteggi, partite giocate)
2. Caricamento dei dati al riavvio del server

## Requisiti Non Funzionali

### Architettura
1. Architettura client-server con comunicazione socket
2. Pattern Model-View-Controller per la separazione della logica di gioco dall'interfaccia utente

### Prestazioni
1. Gestione simultanea di più client attraverso un thread pool
2. Risposta in tempo reale ai tentativi dell'utente

### Usabilità
1. Interfaccia grafica intuitiva con messaggi di feedback chiari
2. Messaggi di errore descrittivi per input non validi
3. Varietà di messaggi casuali per vincite e perdite per migliorare l'esperienza utente

### Affidabilità
1. Gestione degli errori di connessione e comunicazione
2. Salvataggio immediato dei dati dopo ogni aggiornamento di punteggio

### Sicurezza
1. Validazione degli input dell'utente (controllo lunghezza username, validazione input numerici)

### Manutenibilità
1. Codice organizzato in classi e metodi con responsabilità ben definite
2. Commenti esplicativi per le funzionalità principali

### Portabilità
1. Implementazione in Java per garantire compatibilità multipiattaforma
2. Utilizzo di Swing per l'interfaccia grafica (compatibile con diversi sistemi operativi)

### Interoperabilità
1. Protocollo di comunicazione testuale basato su comandi per facilitare l'interazione client-server
2. Formato CSV per la persistenza dei dati
