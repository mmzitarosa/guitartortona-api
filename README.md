In locale, da terminale:
- "docker compose up"

3️⃣ Ambiente di produzione
Qui hai due opzioni principali:

Opzione A – Database installato sulla macchina di produzione
Devi installare MariaDB sulla macchina (o usare un servizio gestito tipo AWS RDS, Azure Database, ecc.).

Non includere le credenziali nel codice!
Usa variabili d'ambiente o un file application-prod.yml non tracciato dal repository.

Esempio application-prod.yml (solo sul server):

yaml
Copia
Modifica
spring:
datasource:
url: jdbc:mariadb://prod-db-server:3306/mydb
username: ${DB_USER}
password: ${DB_PASS}
E lanci il jar con:

bash
Copia
Modifica
java -jar -Dspring.profiles.active=prod myapp.jar
Le variabili d’ambiente le setti con:

bash
Copia
Modifica
export DB_USER=myuser
export DB_PASS=mypass
Opzione B – Database in Docker anche in produzione
Puoi replicare il docker-compose.yml sul server, con password più sicure e backup programmati.

È molto comodo, ma in produzione di solito si preferisce un database gestito per non dover curare aggiornamenti e backup manualmente.

4️⃣ Deploy del JAR in produzione
Metodo semplice e sicuro:

Costruisci il jar:

bash
Copia
Modifica
./mvnw clean package
Troverai il file in target/myapp.jar.

Copialo sul server (es. con scp):

bash
Copia
Modifica
scp target/myapp.jar user@server:/opt/myapp/
Avvialo:

bash
Copia
Modifica
java -jar -Dspring.profiles.active=prod /opt/myapp/myapp.jar
Se vuoi tenerlo sempre attivo, usa systemd:

/etc/systemd/system/myapp.service:

ini
Copia
Modifica
[Unit]
Description=Spring Boot App
After=syslog.target

[Service]
User=myuser
ExecStart=/usr/bin/java -jar -Dspring.profiles.active=prod /opt/myapp/myapp.jar
SuccessExitStatus=143
Restart=on-failure

[Install]
WantedBy=multi-user.target
Poi:

bash
Copia
Modifica
sudo systemctl enable myapp
sudo systemctl start myapp
