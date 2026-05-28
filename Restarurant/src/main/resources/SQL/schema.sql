-- ============================================================
--  Hotel Booking System — Database Schema
--  Run this once against hotel_db before starting the app.
--  Command: psql -U postgres -d hotel_db -f schema.sql
-- ============================================================

-- ─────────────────────────────────────────────
--  Clean slate (useful during development)
-- ─────────────────────────────────────────────
DROP TABLE IF EXISTS api_usage_logs CASCADE;
DROP TABLE IF EXISTS api_keys       CASCADE;
DROP TABLE IF EXISTS payments       CASCADE;
DROP TABLE IF EXISTS bookings       CASCADE;
DROP TABLE IF EXISTS rooms          CASCADE;
DROP TABLE IF EXISTS users          CASCADE;
DROP TABLE IF EXISTS roles          CASCADE;

-- ─────────────────────────────────────────────
--  ROLES
-- ─────────────────────────────────────────────
CREATE TABLE roles (
    id   BIGSERIAL    PRIMARY KEY,
    name VARCHAR(50)  UNIQUE NOT NULL
    -- Stored values: ROLE_ADMIN | ROLE_STAFF | ROLE_CUSTOMER
);

-- ─────────────────────────────────────────────
--  USERS
-- ─────────────────────────────────────────────
CREATE TABLE users (
    id         BIGSERIAL     PRIMARY KEY,
    full_name  VARCHAR(100)  NOT NULL,
    email      VARCHAR(150)  UNIQUE NOT NULL,
    password   VARCHAR(255)  NOT NULL,          -- BCrypt hash
    phone      VARCHAR(20),
    role_id    BIGINT        NOT NULL REFERENCES roles(id),
    is_active  BOOLEAN       DEFAULT TRUE,
    created_at TIMESTAMP     DEFAULT NOW()
);

-- ─────────────────────────────────────────────
--  ROOMS
-- ─────────────────────────────────────────────
CREATE TABLE rooms (
    id               BIGSERIAL       PRIMARY KEY,
    room_number      VARCHAR(10)     UNIQUE NOT NULL,
    type             VARCHAR(20)     NOT NULL,           -- SINGLE | DOUBLE | SUITE | DELUXE
    price_per_night  NUMERIC(10, 2)  NOT NULL,
    capacity         INT             NOT NULL,
    description      TEXT,
    image_url        VARCHAR(500),                       -- path to uploaded image or external URL
    is_available     BOOLEAN         DEFAULT TRUE
);

-- ─────────────────────────────────────────────
--  BOOKINGS
-- ─────────────────────────────────────────────
CREATE TABLE bookings (
    id              BIGSERIAL       PRIMARY KEY,
    customer_id     BIGINT          NOT NULL REFERENCES users(id),
    room_id         BIGINT          NOT NULL REFERENCES rooms(id),
    check_in_date   DATE            NOT NULL,
    check_out_date  DATE            NOT NULL,
    total_price     NUMERIC(10, 2)  NOT NULL,
    status          VARCHAR(20)     NOT NULL DEFAULT 'PENDING',
    -- PENDING | CONFIRMED | CANCELLED | CHECKED_IN | CHECKED_OUT
    assigned_by     BIGINT          REFERENCES users(id),   -- staff who confirmed
    source_partner  VARCHAR(100),                           -- set when created via /api/v1
    created_at      TIMESTAMP       DEFAULT NOW(),
    updated_at      TIMESTAMP       DEFAULT NOW(),

    CONSTRAINT chk_dates CHECK (check_out_date > check_in_date)
);

-- ─────────────────────────────────────────────
--  PAYMENTS
-- ─────────────────────────────────────────────
CREATE TABLE payments (
    id               BIGSERIAL       PRIMARY KEY,
    booking_id       BIGINT          NOT NULL REFERENCES bookings(id),
    amount           NUMERIC(10, 2)  NOT NULL,
    method           VARCHAR(30),    -- CREDIT_CARD | CASH | RAZORPAY | UPI
    status           VARCHAR(20),    -- PAID | PENDING | REFUNDED
    razorpay_order_id   VARCHAR(100),
    razorpay_payment_id VARCHAR(100),
    paid_at          TIMESTAMP,

    CONSTRAINT uq_booking_payment UNIQUE (booking_id)   -- one payment per booking
);

-- ─────────────────────────────────────────────
--  INDEXES  (improves query performance)
-- ─────────────────────────────────────────────
CREATE INDEX idx_bookings_customer   ON bookings(customer_id);
CREATE INDEX idx_bookings_room       ON bookings(room_id);
CREATE INDEX idx_bookings_status     ON bookings(status);
CREATE INDEX idx_bookings_dates      ON bookings(check_in_date, check_out_date);
CREATE INDEX idx_users_email         ON users(email);

-- ─────────────────────────────────────────────
--  API KEYS (partner integrations)
-- ─────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS api_keys (
    id            BIGSERIAL    PRIMARY KEY,
    key_value     VARCHAR(255) UNIQUE NOT NULL,
    partner_name  VARCHAR(100),
    customer_id   BIGINT       REFERENCES users(id),
    active        BOOLEAN      DEFAULT TRUE,
    created_at    TIMESTAMP,
    last_used_at  TIMESTAMP
);

CREATE TABLE IF NOT EXISTS api_usage_logs (
    id            BIGSERIAL    PRIMARY KEY,
    partner_name  VARCHAR(100),
    endpoint      VARCHAR(500),
    method        VARCHAR(10),
    status_code   INT,
    called_at     TIMESTAMP
);

CREATE TABLE IF NOT EXISTS room_orders (
    id            BIGSERIAL PRIMARY KEY,
    booking_id    BIGINT REFERENCES bookings(id),
    customer_id   BIGINT REFERENCES users(id),
    walk_in_name  VARCHAR(100),
    venue_slug    VARCHAR(100) NOT NULL,
    venue_name    VARCHAR(150) NOT NULL,
    billing_type  VARCHAR(30) NOT NULL,
    status        VARCHAR(20) NOT NULL,
    total_amount  NUMERIC(12, 2) NOT NULL,
    created_at    TIMESTAMP,
    updated_at    TIMESTAMP
);

CREATE TABLE IF NOT EXISTS room_order_items (
    id                 BIGSERIAL PRIMARY KEY,
    room_order_id      BIGINT REFERENCES room_orders(id),
    item_name          VARCHAR(150) NOT NULL,
    item_description   VARCHAR(400),
    quantity           INT NOT NULL,
    unit_price         NUMERIC(10, 2) NOT NULL,
    line_total         NUMERIC(10, 2) NOT NULL
);