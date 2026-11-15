-- =====================================================================
--  RunEase - Running Event Management System
--  Relational schema (3NF) - MySQL export
--  Target: MySQL 8.0+ (InnoDB, utf8mb4)
-- =====================================================================

SET NAMES utf8mb4;
SET time_zone = '+08:00';

CREATE DATABASE IF NOT EXISTS runease
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE runease;

SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS audit_logs;
DROP TABLE IF EXISTS announcements;
DROP TABLE IF EXISTS results;
DROP TABLE IF EXISTS payments;
DROP TABLE IF EXISTS registrations;
DROP TABLE IF EXISTS event_categories;
DROP TABLE IF EXISTS events;
DROP TABLE IF EXISTS organizers;
DROP TABLE IF EXISTS users;
SET FOREIGN_KEY_CHECKS = 1;

-- 1. USERS ------------------------------------------------------------
CREATE TABLE users (
    user_id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name       VARCHAR(120) NOT NULL,
    email           VARCHAR(160) NOT NULL,
    password_hash   VARCHAR(100) NOT NULL,
    phone           VARCHAR(20),
    role            VARCHAR(20)  NOT NULL,
    status          VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    gender          VARCHAR(10),
    date_of_birth   DATE,
    city            VARCHAR(80),
    last_login_at   TIMESTAMP,
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP,
    deleted_at      TIMESTAMP,
    CONSTRAINT uq_users_email UNIQUE (email),
    CONSTRAINT ck_users_role CHECK (role IN ('ADMIN', 'ORGANIZER', 'PARTICIPANT')),
    CONSTRAINT ck_users_status CHECK (status IN ('ACTIVE', 'INACTIVE', 'SUSPENDED')),
    CONSTRAINT ck_users_gender CHECK (gender IS NULL OR gender IN ('MALE', 'FEMALE'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. ORGANIZERS (profile of a user with the ORGANIZER role) -------------
CREATE TABLE organizers (
    organizer_id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id             BIGINT       NOT NULL,
    organization_name   VARCHAR(150) NOT NULL,
    registration_no     VARCHAR(50),
    contact_person      VARCHAR(120),
    website             VARCHAR(160),
    verification_status VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    verified_at         TIMESTAMP,
    verified_by         BIGINT,
    created_at          TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_organizers_user UNIQUE (user_id),
    CONSTRAINT fk_organizers_user FOREIGN KEY (user_id) REFERENCES users (user_id),
    CONSTRAINT fk_organizers_verifier FOREIGN KEY (verified_by) REFERENCES users (user_id),
    CONSTRAINT ck_organizers_status CHECK (verification_status IN ('PENDING', 'VERIFIED', 'REJECTED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. EVENTS ------------------------------------------------------------
CREATE TABLE events (
    event_id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    organizer_id        BIGINT       NOT NULL,
    title               VARCHAR(150) NOT NULL,
    slug                VARCHAR(170) NOT NULL,
    description         TEXT,
    event_type          VARCHAR(20)  NOT NULL DEFAULT 'ROAD',
    venue               VARCHAR(150) NOT NULL,
    city                VARCHAR(80),
    state               VARCHAR(80),
    event_date          DATE         NOT NULL,
    flag_off_time       TIME,
    registration_open   DATE         NOT NULL,
    registration_close  DATE         NOT NULL,
    status              VARCHAR(20)  NOT NULL DEFAULT 'DRAFT',
    rejection_reason    VARCHAR(255),
    created_at          TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP,
    deleted_at          TIMESTAMP,
    CONSTRAINT uq_events_slug UNIQUE (slug),
    CONSTRAINT fk_events_organizer FOREIGN KEY (organizer_id) REFERENCES organizers (organizer_id),
    CONSTRAINT ck_events_status CHECK (status IN ('DRAFT', 'PENDING', 'APPROVED', 'REJECTED', 'COMPLETED', 'CANCELLED')),
    CONSTRAINT ck_events_type CHECK (event_type IN ('ROAD', 'TRAIL', 'FUN_RUN', 'VIRTUAL')),
    CONSTRAINT ck_events_dates CHECK (registration_close <= event_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. EVENT CATEGORIES --------------------------------------------------
CREATE TABLE event_categories (
    category_id     BIGINT AUTO_INCREMENT PRIMARY KEY,
    event_id        BIGINT        NOT NULL,
    name            VARCHAR(60)   NOT NULL,
    distance_km     DECIMAL(5,1)  NOT NULL,
    price           DECIMAL(10,2) NOT NULL,
    quota           INT           NOT NULL,
    start_time      TIME,
    created_at      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP,
    CONSTRAINT uq_categories_event_name UNIQUE (event_id, name),
    CONSTRAINT fk_categories_event FOREIGN KEY (event_id) REFERENCES events (event_id) ON DELETE CASCADE,
    CONSTRAINT ck_categories_price CHECK (price >= 0),
    CONSTRAINT ck_categories_quota CHECK (quota > 0),
    CONSTRAINT ck_categories_distance CHECK (distance_km > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. REGISTRATIONS -----------------------------------------------------
CREATE TABLE registrations (
    registration_id     BIGINT AUTO_INCREMENT PRIMARY KEY,
    registration_code   VARCHAR(20)   NOT NULL,
    user_id             BIGINT        NOT NULL,
    category_id         BIGINT        NOT NULL,
    bib_number          VARCHAR(15),
    jersey_size         VARCHAR(5),
    emergency_name      VARCHAR(120)  NOT NULL,
    emergency_phone     VARCHAR(20)   NOT NULL,
    medical_notes       VARCHAR(255),
    status              VARCHAR(20)   NOT NULL DEFAULT 'PENDING_PAYMENT',
    total_amount        DECIMAL(10,2) NOT NULL,
    registered_at       TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    checked_in_at       TIMESTAMP,
    cancelled_at        TIMESTAMP,
    deleted_at          TIMESTAMP,
    CONSTRAINT uq_registrations_code UNIQUE (registration_code),
    CONSTRAINT uq_registrations_bib UNIQUE (bib_number),
    CONSTRAINT fk_registrations_user FOREIGN KEY (user_id) REFERENCES users (user_id),
    CONSTRAINT fk_registrations_category FOREIGN KEY (category_id) REFERENCES event_categories (category_id),
    CONSTRAINT ck_registrations_status CHECK (status IN
        ('PENDING_PAYMENT', 'CONFIRMED', 'CANCELLED', 'CHECKED_IN', 'FINISHED', 'DISQUALIFIED')),
    CONSTRAINT ck_registrations_jersey CHECK (jersey_size IS NULL OR jersey_size IN ('XS', 'S', 'M', 'L', 'XL', 'XXL'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. PAYMENTS (one-to-one with registration) ---------------------------
CREATE TABLE payments (
    payment_id      BIGINT AUTO_INCREMENT PRIMARY KEY,
    registration_id BIGINT        NOT NULL,
    reference_no    VARCHAR(40)   NOT NULL,
    method          VARCHAR(20)   NOT NULL,
    amount          DECIMAL(10,2) NOT NULL,
    status          VARCHAR(20)   NOT NULL DEFAULT 'PENDING',
    paid_at         TIMESTAMP,
    created_at      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_payments_registration UNIQUE (registration_id),
    CONSTRAINT uq_payments_reference UNIQUE (reference_no),
    CONSTRAINT fk_payments_registration FOREIGN KEY (registration_id) REFERENCES registrations (registration_id),
    CONSTRAINT ck_payments_method CHECK (method IN ('FPX', 'CARD', 'EWALLET')),
    CONSTRAINT ck_payments_status CHECK (status IN ('PENDING', 'PAID', 'REFUNDED', 'FAILED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7. RESULTS -----------------------------------------------------------
CREATE TABLE results (
    result_id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    registration_id     BIGINT NOT NULL,
    finish_time_seconds INT    NOT NULL,
    overall_position    INT,
    category_position   INT,
    pace_seconds        INT,
    recorded_by         BIGINT,
    recorded_at         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_results_registration UNIQUE (registration_id),
    CONSTRAINT fk_results_registration FOREIGN KEY (registration_id) REFERENCES registrations (registration_id),
    CONSTRAINT fk_results_recorder FOREIGN KEY (recorded_by) REFERENCES users (user_id),
    CONSTRAINT ck_results_time CHECK (finish_time_seconds > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 8. ANNOUNCEMENTS -----------------------------------------------------
CREATE TABLE announcements (
    announcement_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    event_id        BIGINT       NOT NULL,
    title           VARCHAR(150) NOT NULL,
    body            TEXT         NOT NULL,
    audience        VARCHAR(20)  NOT NULL DEFAULT 'ALL',
    created_by      BIGINT       NOT NULL,
    published_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP,
    deleted_at      TIMESTAMP,
    CONSTRAINT fk_announcements_event FOREIGN KEY (event_id) REFERENCES events (event_id) ON DELETE CASCADE,
    CONSTRAINT fk_announcements_author FOREIGN KEY (created_by) REFERENCES users (user_id),
    CONSTRAINT ck_announcements_audience CHECK (audience IN ('ALL', 'CONFIRMED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 9. AUDIT LOGS --------------------------------------------------------
CREATE TABLE audit_logs (
    log_id      BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT,
    action      VARCHAR(30)  NOT NULL,
    entity_type VARCHAR(40),
    entity_id   VARCHAR(40),
    description VARCHAR(255),
    ip_address  VARCHAR(45),
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_audit_user FOREIGN KEY (user_id) REFERENCES users (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================================
--  Indexes (query paths used by listing, filtering and reporting)
-- =====================================================================
CREATE INDEX idx_users_role_status ON users (role, status);
CREATE INDEX idx_organizers_status ON organizers (verification_status);
CREATE INDEX idx_events_organizer ON events (organizer_id);
CREATE INDEX idx_events_status_date ON events (status, event_date);
CREATE INDEX idx_categories_event ON event_categories (event_id);
CREATE INDEX idx_registrations_user ON registrations (user_id);
CREATE INDEX idx_registrations_category_status ON registrations (category_id, status);
CREATE INDEX idx_registrations_registered ON registrations (registered_at);
CREATE INDEX idx_payments_status ON payments (status);
CREATE INDEX idx_results_time ON results (finish_time_seconds);
CREATE INDEX idx_announcements_event ON announcements (event_id);
CREATE INDEX idx_audit_created ON audit_logs (created_at);
