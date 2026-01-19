CREATE TABLE events (
    id BIGINT NOT NULL AUTO_INCREMENT,

    club_id BIGINT NOT NULL,

    title VARCHAR(200) NOT NULL,
    description VARCHAR(5000) NOT NULL,

    start_at DATETIME(6) NOT NULL,
    end_at DATETIME(6) NULL,

    location VARCHAR(200) NULL,

    capacity INT NULL,
    registration_deadline DATETIME(6) NULL,

    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',

    created_by BIGINT NOT NULL,

    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    deleted_at DATETIME(6) NULL,

    PRIMARY KEY (id),

    CONSTRAINT fk_events_club
        FOREIGN KEY (club_id) REFERENCES clubs(id),

    CONSTRAINT fk_events_created_by_users
        FOREIGN KEY (created_by) REFERENCES users(id),

    CONSTRAINT chk_events_capacity_non_negative
        CHECK (capacity IS NULL OR capacity >= 0),

    CONSTRAINT chk_events_end_after_start
        CHECK (end_at IS NULL OR end_at >= start_at),

    CONSTRAINT chk_events_deadline_before_start
        CHECK (registration_deadline IS NULL OR registration_deadline <= start_at)
) ENGINE=InnoDB;

CREATE INDEX ix_events_club ON events (club_id);
CREATE INDEX ix_events_start_at ON events (start_at);
CREATE INDEX ix_events_status ON events (status);
CREATE INDEX ix_events_deleted_at ON events (deleted_at);
