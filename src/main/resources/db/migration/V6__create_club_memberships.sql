CREATE TABLE club_memberships (
    club_id BIGINT NOT NULL,
    student_user_id BIGINT NOT NULL,

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    joined_at DATETIME(6) NOT NULL,
    left_at DATETIME(6) NULL,

    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    deleted_at DATETIME(6) NULL,

    PRIMARY KEY (club_id, student_user_id),

    CONSTRAINT fk_cm_club
        FOREIGN KEY (club_id) REFERENCES clubs(id),

    CONSTRAINT fk_cm_student_user
        FOREIGN KEY (student_user_id) REFERENCES users(id)
) ENGINE=InnoDB;

CREATE INDEX ix_cm_student_status
    ON club_memberships (student_user_id, status);

CREATE INDEX ix_cm_club_status
    ON club_memberships (club_id, status);

CREATE INDEX ix_cm_deleted_at
    ON club_memberships (deleted_at);
