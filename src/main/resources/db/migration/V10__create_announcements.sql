CREATE TABLE announcements (
    id BIGINT NOT NULL AUTO_INCREMENT,

    club_id BIGINT NOT NULL,

    title VARCHAR(200) NOT NULL,
    body VARCHAR(8000) NOT NULL,

    is_published TINYINT(1) NOT NULL DEFAULT 0,
    published_at DATETIME(6) NULL,

    author_id BIGINT NOT NULL,

    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    deleted_at DATETIME(6) NULL,

    PRIMARY KEY (id),

    CONSTRAINT fk_announcements_club
        FOREIGN KEY (club_id) REFERENCES clubs(id),

    CONSTRAINT fk_announcements_author
        FOREIGN KEY (author_id) REFERENCES users(id),

    CONSTRAINT chk_announcements_published_at
        CHECK (is_published = 0 OR published_at IS NOT NULL)
) ENGINE=InnoDB;

CREATE INDEX ix_announcements_club
    ON announcements (club_id);

CREATE INDEX ix_announcements_published
    ON announcements (club_id, is_published, published_at);

CREATE INDEX ix_announcements_deleted_at
    ON announcements (deleted_at);
