-- ============================================================
--  V1__create_initial_schema.sql
-- ============================================================

CREATE TABLE `user` (
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    name       VARCHAR(150) NOT NULL,
    email      VARCHAR(255) NOT NULL,
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_user       PRIMARY KEY (id),
    CONSTRAINT uq_user_email UNIQUE (email)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE room (
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    name       VARCHAR(100) NOT NULL,
    capacity   INT          NOT NULL,
    active     TINYINT(1)   NOT NULL DEFAULT 1,
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_room      PRIMARY KEY (id),
    CONSTRAINT uq_room_name UNIQUE (name),
    CONSTRAINT chk_room_capacity CHECK (capacity > 0)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE booking (
    id         BIGINT      NOT NULL AUTO_INCREMENT,
    room_id    BIGINT      NOT NULL,
    user_id    BIGINT      NOT NULL,
    start_time DATETIME    NOT NULL,
    end_time   DATETIME    NOT NULL,
    status     VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_booking        PRIMARY KEY (id),
    CONSTRAINT fk_booking_room   FOREIGN KEY (room_id) REFERENCES room (id),
    CONSTRAINT fk_booking_user   FOREIGN KEY (user_id) REFERENCES `user` (id),
    CONSTRAINT chk_booking_dates CHECK (end_time > start_time)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE INDEX idx_booking_room_status_period
    ON booking (room_id, status, start_time, end_time);