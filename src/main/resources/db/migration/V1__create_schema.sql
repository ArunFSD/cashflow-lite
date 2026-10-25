-- =====================================================================
-- V1: Expense Book Log schema (normalised, 3NF)
--
-- Flyway runs this file automatically the first time the application starts
-- against an empty schema, and never runs it again. Later changes go in new
-- files named V2__..., V3__... so no data is lost.
-- =====================================================================

-- Fast "contains" search (ILIKE '%text%') on names and notes
CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- ---------------------------------------------------------------------
-- Lookup tables (one row per code, no repeated text in the transactions)
-- ---------------------------------------------------------------------
CREATE TABLE payment_methods (
    id           INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    code         VARCHAR(20) NOT NULL UNIQUE,
    label        VARCHAR(40) NOT NULL,
    detail_label VARCHAR(40)                       -- NULL when the method needs no extra detail
);

CREATE TABLE purposes (
    id             INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    code           VARCHAR(30) NOT NULL UNIQUE,
    label          VARCHAR(40) NOT NULL,
    requires_party BOOLEAN NOT NULL DEFAULT FALSE,
    requires_note  BOOLEAN NOT NULL DEFAULT FALSE
);

-- ---------------------------------------------------------------------
-- Entities that were repeated as free text before
-- ---------------------------------------------------------------------
-- A friend, customer, sender or company. One row per person, however often it is used.
CREATE TABLE parties (
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name       VARCHAR(150) NOT NULL,
    version    INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT parties_name_not_blank CHECK (btrim(name) <> '')
);
CREATE UNIQUE INDEX ux_parties_name ON parties (lower(name));
CREATE INDEX ix_parties_name_trgm ON parties USING gin (name gin_trgm_ops);

-- The card, UPI app or bank used for a payment. It belongs to one payment method.
CREATE TABLE payment_instruments (
    id                BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    payment_method_id INTEGER NOT NULL REFERENCES payment_methods (id),
    label             VARCHAR(100) NOT NULL,
    CONSTRAINT payment_instruments_label_not_blank CHECK (btrim(label) <> '')
);
CREATE UNIQUE INDEX ux_payment_instruments_method_label
    ON payment_instruments (payment_method_id, lower(label));

-- ---------------------------------------------------------------------
-- Transactions
-- ---------------------------------------------------------------------
CREATE TABLE expenses (
    id                    BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    expense_date          DATE NOT NULL,
    name                  VARCHAR(60) NOT NULL,
    amount                NUMERIC(12, 2) NOT NULL,
    payment_method_id     INTEGER NOT NULL REFERENCES payment_methods (id),
    payment_instrument_id BIGINT REFERENCES payment_instruments (id),
    purpose_id            INTEGER NOT NULL REFERENCES purposes (id),
    party_id              BIGINT REFERENCES parties (id),
    purpose_note          VARCHAR(100),
    notes                 VARCHAR(500),
    version               INTEGER NOT NULL DEFAULT 0,
    created_at            TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at            TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT expenses_amount_positive CHECK (amount > 0),
    CONSTRAINT expenses_name_not_blank CHECK (btrim(name) <> '')
);

CREATE TABLE deposits (
    id                    BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    deposit_date          DATE NOT NULL,
    amount                NUMERIC(12, 2) NOT NULL,
    party_id              BIGINT NOT NULL REFERENCES parties (id),
    payment_method_id     INTEGER NOT NULL REFERENCES payment_methods (id),
    payment_instrument_id BIGINT REFERENCES payment_instruments (id),
    notes                 VARCHAR(500),
    version               INTEGER NOT NULL DEFAULT 0,
    created_at            TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at            TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT deposits_amount_positive CHECK (amount > 0)
);

-- ---------------------------------------------------------------------
-- Indexes for large lists: date order, filters by method/purpose/party, and text search
-- ---------------------------------------------------------------------
CREATE INDEX ix_expenses_date_id      ON expenses (expense_date DESC, id DESC);
CREATE INDEX ix_expenses_method       ON expenses (payment_method_id);
CREATE INDEX ix_expenses_instrument   ON expenses (payment_instrument_id);
CREATE INDEX ix_expenses_purpose      ON expenses (purpose_id);
CREATE INDEX ix_expenses_party        ON expenses (party_id);
CREATE INDEX ix_expenses_name_trgm    ON expenses USING gin (name gin_trgm_ops);
CREATE INDEX ix_expenses_notes_trgm   ON expenses USING gin (notes gin_trgm_ops);

CREATE INDEX ix_deposits_date_id      ON deposits (deposit_date DESC, id DESC);
CREATE INDEX ix_deposits_method       ON deposits (payment_method_id);
CREATE INDEX ix_deposits_instrument   ON deposits (payment_instrument_id);
CREATE INDEX ix_deposits_party        ON deposits (party_id);
CREATE INDEX ix_deposits_notes_trgm   ON deposits USING gin (notes gin_trgm_ops);

-- ---------------------------------------------------------------------
-- Consistency rules enforced by the database itself (the rule holds for every writer)
-- ---------------------------------------------------------------------
CREATE OR REPLACE FUNCTION check_expense_rules() RETURNS trigger LANGUAGE plpgsql AS $$
DECLARE
    p purposes%ROWTYPE;
    m payment_methods%ROWTYPE;
BEGIN
    SELECT * INTO p FROM purposes WHERE id = NEW.purpose_id;
    SELECT * INTO m FROM payment_methods WHERE id = NEW.payment_method_id;

    IF p.requires_party AND NEW.party_id IS NULL THEN
        RAISE EXCEPTION 'this kind of expense needs a friend or customer name' USING ERRCODE = 'check_violation';
    END IF;
    IF p.requires_note AND NEW.purpose_note IS NULL THEN
        RAISE EXCEPTION 'this kind of expense needs a short mention' USING ERRCODE = 'check_violation';
    END IF;
    IF m.detail_label IS NOT NULL AND NEW.payment_instrument_id IS NULL THEN
        RAISE EXCEPTION 'this payment method needs its card, app or bank' USING ERRCODE = 'check_violation';
    END IF;
    IF NEW.payment_instrument_id IS NOT NULL AND NOT EXISTS (
        SELECT 1 FROM payment_instruments i
        WHERE i.id = NEW.payment_instrument_id AND i.payment_method_id = NEW.payment_method_id) THEN
        RAISE EXCEPTION 'the card, app or bank does not belong to this payment method' USING ERRCODE = 'check_violation';
    END IF;
    RETURN NEW;
END;
$$;

CREATE TRIGGER trg_expenses_rules
    BEFORE INSERT OR UPDATE ON expenses
    FOR EACH ROW EXECUTE FUNCTION check_expense_rules();

CREATE OR REPLACE FUNCTION check_deposit_rules() RETURNS trigger LANGUAGE plpgsql AS $$
DECLARE
    m payment_methods%ROWTYPE;
BEGIN
    SELECT * INTO m FROM payment_methods WHERE id = NEW.payment_method_id;

    IF m.code IN ('CREDIT_CARD', 'DEBIT_CARD') THEN
        RAISE EXCEPTION 'cards cannot receive money' USING ERRCODE = 'check_violation';
    END IF;
    IF m.detail_label IS NOT NULL AND NEW.payment_instrument_id IS NULL THEN
        RAISE EXCEPTION 'this payment method needs its card, app or bank' USING ERRCODE = 'check_violation';
    END IF;
    IF NEW.payment_instrument_id IS NOT NULL AND NOT EXISTS (
        SELECT 1 FROM payment_instruments i
        WHERE i.id = NEW.payment_instrument_id AND i.payment_method_id = NEW.payment_method_id) THEN
        RAISE EXCEPTION 'the card, app or bank does not belong to this payment method' USING ERRCODE = 'check_violation';
    END IF;
    RETURN NEW;
END;
$$;

CREATE TRIGGER trg_deposits_rules
    BEFORE INSERT OR UPDATE ON deposits
    FOR EACH ROW EXECUTE FUNCTION check_deposit_rules();

