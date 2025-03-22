@echo off
if "%1"=="server" (
    echo Avvio del server...
    java src/GameServer.java
    javac -d bin src/GameServer.java

) else if "%1"=="client" (
    echo Avvio del client...
    java src/GameClient.java
    javac -d bin src/GameClient.java
) else (
    echo Comando non valido. Usa uno dei seguenti comandi:
    echo run.bat server - Avvia il server
    echo run.bat client - Avvia il client
)
