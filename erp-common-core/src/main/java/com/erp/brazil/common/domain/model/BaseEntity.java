package com.erp.brazil.common.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Base entity class providing common fields for all domain entities.
 * Includes ID, audit timestamps, and version for optimistic locking.
 */
@Getter
@Setter
@MappedSuperclass
public abstract class BaseEntity<ID extends Serializable> implements Serializable {

    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "UUID")
    private ID id;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Column(name = "created_by", length = 100, updatable = false)
    private String createdBy;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @Column(name = "version", nullable = false)
    private Long version = 0L;

    @Column(name = "deleted", nullable = false)
    private Boolean deleted = false;

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;

    @Column(name = "deleted_by", length = 100)
    private String deletedBy;

    @PrePersist
    protected void onCreate() {
        if (this.id == null) {
            this.id = generateId();
        }
        if (this.createdAt == null) {
            this.createdAt = OffsetDateTime.now();
        }
        if (this.version == null) {
            this.version = 0L;
        }
        if (this.deleted == null) {
            this.deleted = false;
        }
        populateAuditFields();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = OffsetDateTime.now();
        this.version = (this.version == null ? 0L : this.version) + 1;
        populateAuditFields();
    }

    /**
     * Generates a new ID for the entity.
     * Override in subclasses for custom ID generation.
     *
     * @return a new ID
     */
    @SuppressWarnings("unchecked")
    protected ID generateId() {
        return (ID) UUID.randomUUID();
    }

    /**
     * Populates audit fields from the current tenant context.
     */
    protected void populateAuditFields() {
        String userId = TenantContext.getUserId();
        if (userId != null) {
            if (this.createdBy == null) {
                this.createdBy = userId;
            }
            this.updatedBy = userId;
        }
    }

    /**
     * Soft deletes the entity.
     *
     * @param deletedBy the user performing the deletion
     */
    public void softDelete(String deletedBy) {
        this.deleted = true;
        this.deletedAt = OffsetDateTime.now();
        this.deletedBy = deletedBy;
    }

    /**
     * Checks if the entity is soft deleted.
     *
     * @return true if deleted, false otherwise
     */
    public boolean isDeleted() {
        return Boolean.TRUE.equals(this.deleted);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BaseEntity<?> that = (BaseEntity<?>) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "id=" + id +
                ", version=" + version +
                ", deleted=" + deleted +
                '}';
    }
}