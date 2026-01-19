CREATE TABLE club_membership_requests (
    id BIGINT NOT NULL AUTO_INCREMENT,

    club_id BIGINT NOT NULL,
    student_user_id BIGINT NOT NULL,

    status VARCHAR(20) NOT NULL,
    message VARCHAR(2000) NULL,

    decided_by BIGINT NULL,
    decided_at DATETIME(6) NULL,
    decision_note VARCHAR(2000) NULL,

    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    deleted_at DATETIME(6) NULL,

    PRIMARY KEY (id),

    CONSTRAINT fk_cmr_club
        FOREIGN KEY (club_id) REFERENCES clubs(id),

    CONSTRAINT fk_cmr_student_user
        FOREIGN KEY (student_user_id) REFERENCES users(id),

    CONSTRAINT fk_cmr_decided_by_user
        FOREIGN KEY (decided_by) REFERENCES users(id),

    CONSTRAINT uq_cmr_club_student_status
        UNIQUE (club_id, student_user_id, status)
) ENGINE=InnoDB;

CREATE INDEX ix_cmr_club_status
    ON club_membership_requests (club_id, status);

CREATE INDEX ix_cmr_student_status
    ON club_membership_requests (student_user_id, status);

CREATE INDEX ix_cmr_deleted_at
    ON club_membership_requests (deleted_at);
