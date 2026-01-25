CREATE TABLE club_media (
    id BIGINT NOT NULL AUTO_INCREMENT,

    club_id BIGINT NOT NULL,
    type VARCHAR(20) NOT NULL,
    url VARCHAR(2048) NOT NULL,
    caption VARCHAR(255) NULL,
    sort_order INT NOT NULL DEFAULT 0,

    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    deleted_at DATETIME(6) NULL,

    PRIMARY KEY (id),

    CONSTRAINT fk_club_media_club
        FOREIGN KEY (club_id) REFERENCES clubs(id)
) ENGINE=InnoDB;

CREATE INDEX ix_club_media_club ON club_media (club_id);
CREATE INDEX ix_club_media_deleted_at ON club_media (deleted_at);
CREATE INDEX ix_club_media_club_sort ON club_media (club_id, sort_order);
