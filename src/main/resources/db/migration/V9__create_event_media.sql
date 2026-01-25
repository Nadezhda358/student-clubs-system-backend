CREATE TABLE event_media (
    id BIGINT NOT NULL AUTO_INCREMENT,

    event_id BIGINT NOT NULL,
    type VARCHAR(20) NOT NULL,
    url VARCHAR(2048) NOT NULL,
    caption VARCHAR(255) NULL,
    sort_order INT NOT NULL DEFAULT 0,

    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    deleted_at DATETIME(6) NULL,

    PRIMARY KEY (id),

    CONSTRAINT fk_event_media_event
        FOREIGN KEY (event_id) REFERENCES events(id)
) ENGINE=InnoDB;

CREATE INDEX ix_event_media_event ON event_media (event_id);
CREATE INDEX ix_event_media_deleted_at ON event_media (deleted_at);
CREATE INDEX ix_event_media_event_sort ON event_media (event_id, sort_order);
