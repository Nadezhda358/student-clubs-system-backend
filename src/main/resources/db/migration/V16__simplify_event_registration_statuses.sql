UPDATE event_registrations
SET status = 'REGISTERED',
    updated_at = CURRENT_TIMESTAMP(6)
WHERE status IN ('ATTENDED', 'NO_SHOW');
