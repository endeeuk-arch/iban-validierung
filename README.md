# IBAN-Validierung

Kleine Full-Stack-Anwendung zur Prüfung von IBAN-Eingaben.

- **Backend:** Java 21, Spring Boot 3.5, REST-API
- **Frontend:** React + TypeScript (folgt)
- **Datenbank:** PostgreSQL (AWS RDS)
- **Betrieb:** AWS

## Funktionen

- Freie Eingabe inkl. Trennzeichen (Leerzeichen, `-`, `.`)
- Prüfung im Backend (Länge + Prüfziffer Modulo 97)
- Bankname für drei bekannte Banken
- Speicherung der geprüften IBANs
- Alternativer Weg: externe API zur Validierung/Namensauflösung

## Starten (Backend)

```bash
cd backend
mvn test
mvn spring-boot:run
```
