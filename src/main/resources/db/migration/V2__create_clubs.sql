CREATE TABLE clubs (
    id BIGINT NOT NULL AUTO_INCREMENT,

    name VARCHAR(160) NOT NULL,
    description VARCHAR(5000) NOT NULL,
    schedule_text VARCHAR(2000) NULL,
    room VARCHAR(80) NULL,
    contact_email VARCHAR(255) NULL,
    contact_phone VARCHAR(40) NULL,

    is_active TINYINT(1) NOT NULL DEFAULT 1,

    created_by BIGINT NOT NULL,

    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    deleted_at DATETIME(6) NULL,

    PRIMARY KEY (id),

    CONSTRAINT uq_clubs_name UNIQUE (name),
    CONSTRAINT fk_clubs_created_by_users
        FOREIGN KEY (created_by) REFERENCES users(id)
) ENGINE=InnoDB;

CREATE INDEX ix_clubs_created_by ON clubs (created_by);
CREATE INDEX ix_clubs_deleted_at ON clubs (deleted_at);
