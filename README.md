# IBAN-Validierung

Full-Stack-Anwendung zur Prüfung von IBAN-Eingaben, gebaut als Aufgabe für das Multibanking-Team von Lexware.

**Live-Demo:** http://3.75.184.115:8080

## Funktionen

- Freie Eingabe inklusive Trennzeichen (Leerzeichen, `-`, `.`, `/`), Groß- und Kleinschreibung
- Prüfung im Backend: Länge (DE genau 22 Zeichen, sonst 15 bis 34) und Prüfziffer nach ISO 13616 (Modulo 97)
- Bankname für drei bekannte Banken über die Bankleitzahl
- Anderer Weg: Namensauflösung über die externe API [openiban.com](https://openiban.com), mit Timeout und Fallback
- Lernendes Bankverzeichnis: Banken aus der externen API werden in der Tabelle `bank` gespeichert, beim nächsten Mal ist kein externer Aufruf mehr nötig
- Anzeige von IBAN, Bankleitzahl und Bankname im Ergebnis und in der Liste
- Jede Prüfung wird in PostgreSQL gespeichert, die letzten 20 werden angezeigt

## Stack

| Bereich | Technik |
|---|---|
| Backend | Java 21, Spring Boot 3.5, Spring Web, Spring Data JPA |
| Frontend | React 19, TypeScript, Vite |
| Datenbank | PostgreSQL 18 (AWS RDS) |
| Betrieb | AWS EC2 (Amazon Linux 2023, t3.micro) |
| Tests | JUnit 5, Mockito |
| API-Doku | springdoc-openapi (Swagger UI) |

## Architektur

```
Browser
   │  HTTP
   ▼
EC2: iban-service.jar (Port 8080)
   ├── React-Oberfläche (statische Dateien)
   └── REST-API
         IbanController → IbanService ─┬─ IbanValidator      (Normalisierung, Modulo 97)
                                       ├─ BankDirectory      (drei bekannte Banken)
                                       ├─ ExternalBankClient (openiban.com, Fallback)
                                       ├─ BankRepository     (gelernte Banken)
                                       └─ IbanCheckRepository
                                                │
                                                ▼
                                        RDS PostgreSQL
```

## REST-API

| Methode | Pfad | Beschreibung |
|---|---|---|
| `POST` | `/api/iban/validate` | Prüft eine IBAN und speichert das Ergebnis |
| `GET` | `/api/iban` | Liefert die letzten 20 Prüfungen |

Beispiel:

```bash
curl -X POST http://localhost:8080/api/iban/validate \
  -H "Content-Type: application/json" \
  -d '{"iban":"de89 3704-0044 0532 0130 00"}'
```

```json
{"valid": true, "iban": "DE89370400440532013000", "blz": "37040044", "bankName": "Commerzbank"}
```

Swagger UI: `http://localhost:8080/swagger-ui.html`

## Datenmodell

```sql
CREATE TABLE bank (
    blz   VARCHAR(8)   PRIMARY KEY,
    name  VARCHAR(100) NOT NULL,
    bic   VARCHAR(11)
);

CREATE TABLE iban_check (
    id          BIGSERIAL   PRIMARY KEY,
    iban        VARCHAR(34) NOT NULL,
    blz         VARCHAR(8)  REFERENCES bank(blz),
    valid       BOOLEAN     NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);
```

Findet die externe API eine Bank, die noch nicht in `bank` steht, wird sie dort gespeichert. Die BLZ in `iban_check` ist ein Fremdschlüssel (`@ManyToOne`), der Bankname steht nur einmal in `bank`. Hibernate läuft mit `ddl-auto=validate` und prüft beim Start, ob Entities und Tabellen zusammenpassen.

## Lokal starten

Voraussetzungen: Java 21, Maven, Node.js, eine PostgreSQL-Datenbank mit den Tabellen oben.

**Backend** (Zugangsdaten kommen aus Umgebungsvariablen, nicht aus dem Code):

```bash
cd backend
export DB_URL="jdbc:postgresql://<host>:5432/iban?sslmode=require"
export DB_USER="<user>"
read -s DB_PASSWORD && export DB_PASSWORD
mvn spring-boot:run
```

**Frontend** (Entwicklungsserver mit Proxy auf das Backend):

```bash
cd frontend
npm install
npm run dev
```

Dann http://localhost:5173 öffnen.

## Tests

```bash
cd backend
mvn test
```

18 Unit-Tests für Validator, Bankverzeichnis und Service. Datenbank und externe API werden mit Mockito ersetzt, die Tests brauchen also weder Datenbank noch Internet.

## Build und Deployment

Frontend und Backend werden zu einem JAR zusammengefasst:

```bash
cd frontend && npm run build
rm -rf ../backend/src/main/resources/static && mkdir -p ../backend/src/main/resources/static
cp -r dist/* ../backend/src/main/resources/static/
cd ../backend && mvn package
```

Auf EC2 (Java 21 installiert, Umgebungsvariablen wie oben gesetzt):

```bash
scp -i <key>.pem target/iban-service-0.1.0.jar ec2-user@<ec2-ip>:~
nohup java -jar iban-service-0.1.0.jar > app.log 2>&1 &
```

Security Groups: Port 8080 offen, SSH nur von der eigenen IP, die Datenbank nur aus der Security Group des Servers.

## Entscheidungen

- **POST statt GET für die Prüfung:** Die IBAN ist ein personenbezogenes Datum und soll nicht in URLs, Browser-Verlauf oder Server-Logs landen.
- **Eigene Prüfung zuerst, externe API als Ergänzung:** Die Prüfziffer wird immer lokal berechnet. Die externe API wird nur für unbekannte Banken gefragt, mit 2 Sekunden Timeout. Fällt sie aus, funktioniert die Anwendung weiter.
- **Gefundene Banken speichern:** Jede Bank wird höchstens einmal extern abgefragt. Das ist schneller und schickt weniger IBANs an Dritte.
- **Jede Prüfung wird gespeichert**, auch ungültige, damit eine Historie entsteht.
- **Keine IBAN in Logs.**

## Nächste Schritte

- HTTPS über einen Load Balancer, Betrieb in ECS/Fargate statt einer einzelnen EC2-Instanz
- Datenbank nur im privaten Subnetz, Zugangsdaten im AWS Secrets Manager
- CI/CD mit GitHub Actions (Tests, Build, Deployment)
- Vollständige BLZ-Liste der Bundesbank statt drei fester Banken
- Validierung der Anfrage mit `@NotBlank` und Antwort `400` bei fehlendem Feld
- Login und maskierte IBANs in der Liste (z. B. `DE89 **** 3000`)
- Abgleich von Kontoinhaber und IBAN (Verification of Payee)
