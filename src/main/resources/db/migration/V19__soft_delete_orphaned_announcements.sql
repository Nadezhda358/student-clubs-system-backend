UPDATE announcements a
LEFT JOIN clubs c
    ON c.id = a.club_id
SET a.deleted_at = COALESCE(c.deleted_at, CURRENT_TIMESTAMP(6))
WHERE a.deleted_at IS NULL
  AND (c.id IS NULL OR c.deleted_at IS NOT NULL);

UPDATE announcements a
LEFT JOIN users u
    ON u.id = a.author_id
SET a.deleted_at = COALESCE(u.deleted_at, CURRENT_TIMESTAMP(6))
WHERE a.deleted_at IS NULL
  AND (u.id IS NULL OR u.deleted_at IS NOT NULL);
