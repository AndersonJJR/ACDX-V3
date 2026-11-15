package com.erp.brazil.common.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * Entity for managing numbering series for documents (invoices, orders, etc.).
 * Supports per-tenant, per-company, per-branch series with automatic sequencing.
 */
@Getter
@Setter
@ToString(callSuper = true)
@Entity
@Table(name = "numbering_series", uniqueConstraints = {
        @UniqueConstraint(name = "uk_numbering_series_tenant_code", columnNames = {"tenant_id", "company_id", "branch_id", "code"})
}, indexes = {
        @Index(name = "idx_numbering_series_tenant_company", columnList = "tenant_id, company_id"),
        @Index(name = "idx_numbering_series_code", columnList = "code")
})
public class NumberingSeries extends BaseEntity<String> implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name = "tenant_id", length = 100, nullable = false, updatable = false)
    private String tenantId;

    @Column(name = "company_id", length = 100, nullable = false, updatable = false)
    private String companyId;

    @Column(name = "branch_id", length = 100)
    private String branchId;

    @Column(name = "code", length = 50, nullable = false, updatable = false)
    private String code;

    @Column(name = "description", length = 200)
    private String description;

    @Column(name = "prefix", length = 20)
    private String prefix = "";

    @Column(name = "suffix", length = 20)
    private String suffix = "";

    @Column(name = "current_number", nullable = false)
    private Long currentNumber = 0L;

    @Column(name = "min_number", nullable = false)
    private Long minNumber = 1L;

    @Column(name = "max_number")
    private Long maxNumber;

    @Column(name = "padding_length", nullable = false)
    private Integer paddingLength = 6;

    @Column(name = "format_template", length = 100)
    private String formatTemplate = "{prefix}{number:000000}{suffix}";

    @Enumerated(EnumType.STRING)
    @Column(name = "reset_policy", length = 20, nullable = false)
    private ResetPolicy resetPolicy = ResetPolicy.NEVER;

    @Column(name = "last_reset_at")
    private java.time.OffsetDateTime lastResetAt;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "allows_manual", nullable = false)
    private Boolean allowsManual = false;

    public NumberingSeries() {
        super();
    }

    public enum ResetPolicy {
        NEVER,
        DAILY,
        MONTHLY,
        YEARLY
    }

    /**
     * Generates the next number in the series.
     *
     * @return the formatted next number
     */
    public String generateNext() {
        long next = this.currentNumber + 1;
        if (this.maxNumber != null && next > this.maxNumber) {
            if (this.resetPolicy == ResetPolicy.NEVER) {
                throw new IllegalStateException("Numbering series exhausted: " + this.code);
            }
            // Reset logic would be handled by a scheduled job
            next = this.minNumber;
        }
        this.currentNumber = next;
        return formatNumber(next);
    }

    /**
     * Formats a number according to the series configuration.
     *
     * @param number the number to format
     * @return formatted string
     */
    public String formatNumber(long number) {
        String paddedNumber = String.format("%0" + this.paddingLength + "d", number);
        return this.formatTemplate
                .replace("{prefix}", this.prefix != null ? this.prefix : "")
                .replace("{suffix}", this.suffix != null ? this.suffix : "")
                .replace("{number:000000}", paddedNumber)
                .replace("{number}", String.valueOf(number));
    }

    /**
     * Resets the series to the minimum number.
     */
    public void reset() {
        this.currentNumber = this.minNumber - 1;
        this.lastResetAt = java.time.OffsetDateTime.now();
    }

    /**
     * Checks if a manual number can be used.
     *
     * @param number the manual number to check
     * @return true if allowed and not used
     */
    public boolean canUseManual(String number) {
        if (!this.allowsManual) {
            return false;
        }
        // Additional logic to check if number is already used would go here
        return true;
    }

    /**
     * Uses a manual number (for corrections, imports, etc.).
     *
     * @param number the manual number to use
     */
    public void useManual(String number) {
        if (!canUseManual(number)) {
            throw new IllegalArgumentException("Manual number not allowed or already used: " + number);
        }
        // Parse the number and update current if higher
        try {
            long num = Long.parseLong(number.replaceAll("[^0-9]", ""));
            if (num > this.currentNumber) {
                this.currentNumber = num;
            }
        } catch (NumberFormatException e) {
            // Non-numeric manual number, just allow it
        }
    }
}