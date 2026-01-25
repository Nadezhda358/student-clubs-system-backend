package com.school.ppmg.student_clubs_system_api.entities.base;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@MappedSuperclass
public abstract class AuditableEntity {

    @Column(name = "created_at", nullable = false, updatable = false)
    protected OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    protected OffsetDateTime updatedAt;

    @Column(name = "deleted_at")
    protected OffsetDateTime deletedAt;

    @PrePersist
    protected void onCreate() {
        OffsetDateTime now = OffsetDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }
}