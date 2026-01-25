CREATE TABLE club_teachers (
    club_id BIGINT NOT NULL,
    teacher_user_id BIGINT NOT NULL,

    is_primary TINYINT(1) NOT NULL DEFAULT 1,

    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    deleted_at DATETIME(6) NULL,

    PRIMARY KEY (club_id, teacher_user_id),

    CONSTRAINT fk_club_teachers_club
        FOREIGN KEY (club_id) REFERENCES clubs(id),

    CONSTRAINT fk_club_teachers_teacher_user
        FOREIGN KEY (teacher_user_id) REFERENCES users(id)
) ENGINE=InnoDB;

CREATE INDEX ix_club_teachers_teacher ON club_teachers (teacher_user_id);
CREATE INDEX ix_club_teachers_deleted_at ON club_teachers (deleted_at);
