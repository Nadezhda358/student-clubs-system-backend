UPDATE clubs
SET main_image_url = NULL
WHERE main_image_url LIKE 'https://assets.school-clubs.bg/%';

DELETE FROM club_media
WHERE url LIKE 'https://assets.school-clubs.bg/%';

UPDATE events
SET main_image_url = NULL
WHERE main_image_url LIKE 'https://assets.school-clubs.bg/%';
