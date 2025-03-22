# GuessMaster - Indovina il Numero



## Descrizione
Il progetto consiste in un gioco client-server "Indovina il Numero", dove i giocatori devono tentare di indovinare un numero segreto generato entro un numero limitato di tentativi.

[Requisiti del Sistema GuessMaster](docs/requisiti.md)


## Struttura del Progetto
```
/GameProject
│-- /src
│   │-- GameClient.java
│   │-- GameServer.java
│-- /data
│   │-- users.csv
|-- /docs
|   |-- requisiti.md
│-- /bin (output compilato)
|-- run.bat
│-- README.md
```

## API di Comunicazione Client-Server
| Comando | Descrizione |
|---------|-------------|
| `LOGIN:<username>` | Effettua il login |
| `REGISTER:<username>` | Registra un nuovo utente |
| `GUESS:<numero>` | Invia un tentativo |
| `LEADERBOARD` | Richiede la classifica |
| `PLAYAGAIN` | Inizia una nuova partita |
| `QUIT` | Chiude la connessione |



## Componenti Principali

### 1. **GameClient.java**
Il client implementa un'interfaccia grafica con Swing e permette ai giocatori di:
- Effettuare login e registrazione
- Giocare tentando di indovinare un numero
- Visualizzare la classifica

### 2. **GameServer.java**
Il server gestisce le connessioni client tramite socket e offre funzionalità come:
- Autenticazione utenti
- Gestione delle partite
- Generazione e validazione dei tentativi
- Salvataggio e aggiornamento dei punteggi nel file `users.csv`

### 3. **users.csv**
Archivio degli utenti registrati con i loro punteggi e partite giocate.

### 4. **Script di gestione**
- `run.bat`: Avvia il server e il client

## Avvio del Progetto

### **Avvio del Server**
```sh
run.bat server
```

### **Avvio del Client**
```sh
run.bat client
```
