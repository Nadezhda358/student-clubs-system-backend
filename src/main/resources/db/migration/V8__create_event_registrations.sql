CREATE TABLE event_registrations (
    event_id BIGINT NOT NULL,
    student_user_id BIGINT NOT NULL,

    status VARCHAR(20) NOT NULL DEFAULT 'REGISTERED',
    registered_at DATETIME(6) NOT NULL,
    cancelled_at DATETIME(6) NULL,

    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    deleted_at DATETIME(6) NULL,

    PRIMARY KEY (event_id, student_user_id),

    CONSTRAINT fk_er_event
        FOREIGN KEY (event_id) REFERENCES events(id),

    CONSTRAINT fk_er_student_user
        FOREIGN KEY (student_user_id) REFERENCES users(id)
) ENGINE=InnoDB;

CREATE INDEX ix_er_student_status
    ON event_registrations (student_user_id, status);

CREATE INDEX ix_er_event_status
    ON event_registrations (event_id, status);

CREATE INDEX ix_er_deleted_at
    ON event_registrations (deleted_at);
