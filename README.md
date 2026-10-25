# Cashflow Lite

A personal cashflow tracker. Record what you spend and what you receive, see every transaction in one list, and read a monthly report.

Built with Spring Boot 4, Java 25, Thymeleaf, PostgreSQL 16 and Flyway.

## Features

- **Expenses:** add, edit and delete money paid out. Record how you paid (Cash, GPay, other UPI, Credit card, Debit card, Net banking or Other) and what it was for (Purchase, Spend, Bills, Sent to friend, Transfer to customer or Other).
- **Deposits:** add, edit and delete money received, with the source and how it came in.
- **All transactions:** expenses and deposits in one list, with money in, money out and net for the filter.
- **Filters on every list:** From and To dates (both inclusive), a Show filter, a multi-select "Search in" for the columns to match, and a search box.
- **Monthly report:** totals by payment method, by card or app, by purpose and by deposit source, with each item's share of the month.
- **Add and edit in a dialog:** the list stays in view. The dialog closes only with Esc, the × icon or Cancel.
- **REST API and Swagger UI:** every feature above is also available as JSON. See [API](#api).

## Pages

| URL | What it shows |
| --- | --- |
| `/expenses` | Expense list with the + Add button |
| `/deposits` | Deposit list with the + Add button |
| `/transactions` | Expenses and deposits together, with totals |
| `/monthly?month=2026-10` | Monthly report for one month |

Lists accept `from`, `to`, `kind`, `field` (repeat for several) and `q`. Dates are `YYYY-MM-DD` in the URL and `DD/MM/YYYY` in the forms.

## Database

The schema is PostgreSQL in third normal form, with indexes for search on large tables. Flyway applies it at startup. Migrations live in `src/main/resources/db/migration`.

- Never edit a migration that has already run. Add a new one (`V2__...sql`, `V3__...sql`) for every change. Flyway stops the app if an applied file changes.
- Hibernate only checks that the entities match the schema (`ddl-auto=validate`). It does not create or change tables.

The app connects with these environment variables. The defaults match the Docker command below:

| Variable | Default |
| --- | --- |
| `DB_URL` | `jdbc:postgresql://localhost:5432/cashflow-lite` |
| `DB_USERNAME` | `postgres` |
| `DB_PASSWORD` | `root@123` |
| `PORT` | `8080` |

### Run PostgreSQL in Docker

The container is named `cashflow-lite`. Its data lives in `data/postgres` inside the project, so it survives container removal. The folder is ignored by Git.

```bash
docker run -d --name cashflow-lite \
  -e POSTGRES_PASSWORD=root@123 \
  -p 5432:5432 \
  -v "$HOME/Projects/cashflow-lite/data/postgres:/var/lib/postgresql/data" \
  postgres:16
```

If the container already exists, stop and remove it first (the data stays in the folder):

```bash
docker stop expense-postgres && docker rm expense-postgres
```

### Back up

```bash
mkdir -p backups
docker exec cashflow-lite pg_dumpall -U postgres > "backups/cashflow-lite-$(date +%F).sql"
```

Restore with:

```bash
docker exec -i cashflow-lite psql -U postgres < backups/cashflow-lite-YYYY-MM-DD.sql
```

## Run

```bash
./mvnw spring-boot:run
```

Then open:

- Pages: http://localhost:8080/expenses
- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI document: http://localhost:8080/v3/api-docs

## API

Base path `/api/v1`. Dates are `YYYY-MM-DD`, amounts are rupees with up to two decimals. Errors use the RFC 9457 problem format and list field problems under `errors`.

| Method | Path | What it does |
| --- | --- | --- |
| GET | `/api/v1/expenses` | List expenses (paged, with totals) |
| POST | `/api/v1/expenses` | Add an expense (201) |
| GET | `/api/v1/expenses/{id}` | Get one expense |
| PUT | `/api/v1/expenses/{id}` | Update an expense (send back `version`) |
| DELETE | `/api/v1/expenses/{id}` | Delete an expense (204) |
| GET | `/api/v1/deposits` | List deposits (paged, with totals) |
| POST | `/api/v1/deposits` | Add a deposit (201) |
| GET | `/api/v1/deposits/{id}` | Get one deposit |
| PUT | `/api/v1/deposits/{id}` | Update a deposit (send back `version`) |
| DELETE | `/api/v1/deposits/{id}` | Delete a deposit (204) |
| GET | `/api/v1/transactions` | All transactions, or one kind with `kind=ALL\|EXPENSE\|DEPOSIT` |
| GET | `/api/v1/monthly?month=2026-10` | Monthly report |

List parameters: `from`, `to` (inclusive), `field` (repeat for several of `ALL`, `TITLE`, `TYPE`, `PAYMENT`, `DETAIL`, `PAYEE`, `NOTES`), `q`, `page` (default 0), `size` (default 50, 10 to 200).

Example:

```bash
curl -X POST http://localhost:8080/api/v1/expenses \
  -H "Content-Type: application/json" \
  -d '{"expenseDate":"2026-10-25","name":"Lunch","amount":190,"paymentMode":"CASH","expenseType":"SPEND"}'
```

Full request and response shapes are in Swagger UI.

## Test

Tests use an in-memory H2 database, so PostgreSQL is not needed:

```bash
./mvnw test
```
