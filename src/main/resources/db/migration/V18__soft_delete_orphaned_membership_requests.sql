UPDATE club_membership_requests cmr
LEFT JOIN clubs c
    ON c.id = cmr.club_id
SET cmr.deleted_at = COALESCE(c.deleted_at, CURRENT_TIMESTAMP(6))
WHERE cmr.deleted_at IS NULL
  AND (c.id IS NULL OR c.deleted_at IS NOT NULL);

UPDATE club_membership_requests cmr
LEFT JOIN users u
    ON u.id = cmr.student_user_id
SET cmr.deleted_at = COALESCE(u.deleted_at, CURRENT_TIMESTAMP(6))
WHERE cmr.deleted_at IS NULL
  AND (u.id IS NULL OR u.deleted_at IS NOT NULL);
