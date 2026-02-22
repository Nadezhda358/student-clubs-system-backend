CREATE TABLE teacher_invites (
    id BIGINT NOT NULL AUTO_INCREMENT,
    email VARCHAR(255) NOT NULL,
    token_hash VARCHAR(64) NOT NULL,
    expires_at DATETIME(6) NOT NULL,
    used_at DATETIME(6) NULL,

    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    deleted_at DATETIME(6) NULL,

    PRIMARY KEY (id),
    CONSTRAINT uq_teacher_invites_token_hash UNIQUE (token_hash)
) ENGINE=InnoDB;

CREATE INDEX ix_teacher_invites_email ON teacher_invites (email);
CREATE INDEX ix_teacher_invites_expires_at ON teacher_invites (expires_at);
CREATE INDEX ix_teacher_invites_used_at ON teacher_invites (used_at);
