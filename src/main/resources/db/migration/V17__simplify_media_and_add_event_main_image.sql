ALTER TABLE events
    ADD COLUMN main_image_url VARCHAR(2048) NULL AFTER location;

ALTER TABLE club_media
    DROP COLUMN type;

DROP TABLE event_media;
