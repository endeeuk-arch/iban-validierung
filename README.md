# IBAN Validation

Full-stack application that validates IBAN input, built as a coding task for the Lexware Multibanking team.

**Live demo:** http://3.75.184.115:8080

## Features

- Free input including separators (spaces, `-`, `.`, `/`), upper and lower case
- Validation in the backend: length (DE exactly 22 characters, other countries 15 to 34) and check digits according to ISO 13616 (modulo 97)
- Reason for invalid IBANs (empty, wrong length, wrong check digits) and the country code
- Bank name and BIC for German IBANs via the bank code (BLZ)
- Three known banks are loaded as start data into the table `bank` (`data.sql`)
- Another way: name and BIC lookup via the external API [openiban.com](https://openiban.com), with timeout and fallback
- Learning bank directory: banks found via the external API are saved in `bank`, so the next lookup needs no external call
- Every check is stored in PostgreSQL, the latest 20 are shown with IBAN, BLZ and bank name

## Stack

| Area | Technology |
|---|---|
| Backend | Java 21, Spring Boot 3.5, Spring Web, Spring Data JPA |
| Frontend | React 19, TypeScript, Vite |
| Database | PostgreSQL 18 (AWS RDS) |
| Hosting | AWS EC2 (Amazon Linux 2023, t3.micro) |
| Tests | JUnit 5, Mockito |
| API docs | springdoc-openapi (Swagger UI) |

## Architecture

```
Browser
   │  HTTP
   ▼
EC2: iban-service.jar (port 8080)
   ├── React UI (static files)
   └── REST API
         IbanController → IbanService ─┬─ IbanValidator      (normalization, modulo 97)
                                       ├─ BankRepository     (known and learned banks)
                                       ├─ ExternalBankClient (openiban.com, fallback)
                                       └─ IbanCheckRepository
                                                │
                                                ▼
                                        RDS PostgreSQL
```

Bank lookup order: table `bank` (name and BIC) → openiban.com → save the new bank in `bank`.

## REST API

| Method | Path | Description |
|---|---|---|
| `POST` | `/api/iban/validate` | Validates an IBAN and stores the result |
| `GET` | `/api/iban` | Returns the latest 20 checks |

Example:

```bash
curl -X POST http://localhost:8080/api/iban/validate \
  -H "Content-Type: application/json" \
  -d '{"iban":"de89 3704-0044 0532 0130 00"}'
```

```json
{"valid": true, "iban": "DE89370400440532013000", "country": "DE", "blz": "37040044",
 "bankName": "Commerzbank", "bic": "COBADEFFXXX", "error": null}
```

Invalid IBAN:

```json
{"valid": false, "iban": "DE89370400440532013001", "country": "DE", "blz": null,
 "bankName": null, "bic": null, "error": "Check digits do not match (typo?)"}
```

Swagger UI: `http://localhost:8080/swagger-ui.html`

## Data model

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

At startup, `data.sql` inserts the three known banks with BIC (`ON CONFLICT DO NOTHING`, so it can run on every start). If the external API finds a bank that is not yet in `bank`, it is saved there. The BLZ in `iban_check` is a foreign key (`@ManyToOne`), the bank name is stored only once in `bank`. Hibernate runs with `ddl-auto=validate` and checks at startup that entities and tables match.

## Run locally

Requirements: Java 21, Maven, Node.js, a PostgreSQL database with the tables above.

**Backend** (credentials come from environment variables, not from the code):

```bash
cd backend
export DB_URL="jdbc:postgresql://<host>:5432/iban?sslmode=require"
export DB_USER="<user>"
read -s DB_PASSWORD && export DB_PASSWORD
mvn spring-boot:run
```

**Frontend** (dev server with a proxy to the backend):

```bash
cd frontend
npm install
npm run dev
```

Then open http://localhost:5173.

## Tests

```bash
cd backend
mvn test
```

22 unit tests for the validator and the service. The database and the external API are replaced with Mockito, so the tests need neither a database nor the internet.

## Build and deployment

Frontend and backend are packaged into one JAR:

```bash
cd frontend && npm run build
rm -rf ../backend/src/main/resources/static && mkdir -p ../backend/src/main/resources/static
cp -r dist/* ../backend/src/main/resources/static/
cd ../backend && mvn package
```

On EC2 (Java 21 installed, environment variables set as above):

```bash
scp -i <key>.pem target/iban-service-0.1.0.jar ec2-user@<ec2-ip>:~
nohup java -jar iban-service-0.1.0.jar > app.log 2>&1 &
```

Security groups: port 8080 open, SSH only from my own IP, the database only from the server's security group.

## Decisions

- **POST instead of GET for the check:** an IBAN is personal data and should not end up in URLs, browser history or server logs.
- **Own validation first, external API as an addition:** the check digits are always calculated locally. The external API is only asked for unknown banks, with a 2-second timeout. If it fails, the application keeps working.
- **One source of truth for banks:** the three known banks are start data in the database instead of a second list in the code.
- **Save found banks:** each bank is looked up externally at most once. This is faster and sends fewer IBANs to third parties.
- **Every check is stored**, including invalid ones, to build a history.
- **No IBANs in logs.**

## Next steps

- HTTPS via a load balancer, run on ECS/Fargate instead of a single EC2 instance
- Database only in a private subnet, credentials in AWS Secrets Manager
- CI/CD with GitHub Actions (tests, build, deployment)
- Schema changes and start data with Flyway migrations
- Integration tests with Testcontainers and MockMvc
- Full Bundesbank BLZ list instead of the external API
- Request validation with `@NotBlank` and a `400` response for a missing field, `@Transactional` for saving
- Login and masked IBANs in the list (e.g. `DE89 **** 3000`)
- Matching account holder and IBAN (Verification of Payee)
