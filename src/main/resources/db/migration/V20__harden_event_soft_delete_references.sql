UPDATE event_registrations er
LEFT JOIN events e
    ON e.id = er.event_id
SET er.deleted_at = COALESCE(e.deleted_at, CURRENT_TIMESTAMP(6))
WHERE er.deleted_at IS NULL
  AND (e.id IS NULL OR e.deleted_at IS NOT NULL);

UPDATE event_registrations er
LEFT JOIN users u
    ON u.id = er.student_user_id
SET er.deleted_at = COALESCE(u.deleted_at, CURRENT_TIMESTAMP(6))
WHERE er.deleted_at IS NULL
  AND (u.id IS NULL OR u.deleted_at IS NOT NULL);

UPDATE events e
JOIN clubs c
    ON c.id = e.club_id
SET e.status = 'CANCELLED',
    e.updated_at = CURRENT_TIMESTAMP(6)
WHERE e.deleted_at IS NULL
  AND c.deleted_at IS NOT NULL
  AND e.start_at >= c.deleted_at
  AND e.status <> 'CANCELLED';
