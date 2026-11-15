package com.erp.brazil.common.domain.model;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Transient;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.Locale;
import java.util.Objects;

/**
 * Value object representing a monetary amount in Brazilian Real (BRL).
 * Uses BigDecimal for precise arithmetic operations.
 * Immutable by design - all operations return new instances.
 */
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Embeddable
public class Money implements Serializable, Comparable<Money> {

    private static final long serialVersionUID = 1L;
    public static final Currency BRL = Currency.getInstance("BRL");
    public static final int DEFAULT_SCALE = 2;
    public static final RoundingMode DEFAULT_ROUNDING = RoundingMode.HALF_EVEN;

    private BigDecimal amount;
    private String currencyCode;

    /**
     * Creates a new Money instance with the given amount in BRL.
     *
     * @param amount the monetary amount
     * @return a new Money instance
     */
    public static Money of(BigDecimal amount) {
        return of(amount, BRL);
    }

    /**
     * Creates a new Money instance with the given amount and currency.
     *
     * @param amount the monetary amount
     * @param currency the currency
     * @return a new Money instance
     */
    public static Money of(BigDecimal amount, Currency currency) {
        if (amount == null) {
            throw new IllegalArgumentException("Amount cannot be null");
        }
        if (currency == null) {
            throw new IllegalArgumentException("Currency cannot be null");
        }
        return new Money(amount.setScale(DEFAULT_SCALE, DEFAULT_ROUNDING), currency.getCurrencyCode());
    }

    /**
     * Creates a new Money instance from a double value in BRL.
     *
     * @param amount the monetary amount
     * @return a new Money instance
     */
    public static Money of(double amount) {
        return of(BigDecimal.valueOf(amount));
    }

    /**
     * Creates a new Money instance from a long value representing cents in BRL.
     *
     * @param cents the amount in cents
     * @return a new Money instance
     */
    public static Money ofCents(long cents) {
        return of(BigDecimal.valueOf(cents).divide(BigDecimal.valueOf(100), DEFAULT_SCALE, DEFAULT_ROUNDING));
    }

    /**
     * Creates a zero Money instance in BRL.
     *
     * @return zero Money
     */
    public static Money zero() {
        return of(BigDecimal.ZERO);
    }

    /**
     * Creates a Money instance from a string representation.
     *
     * @param amount the amount as string
     * @return a new Money instance
     */
    public static Money parse(String amount) {
        return of(new BigDecimal(amount));
    }

    /**
     * Returns the currency for this money.
     *
     * @return the currency
     */
    @Transient
    public Currency getCurrency() {
        return currencyCode != null ? Currency.getInstance(currencyCode) : BRL;
    }

    /**
     * Checks if this money is zero.
     *
     * @return true if amount is zero
     */
    public boolean isZero() {
        return amount != null && amount.compareTo(BigDecimal.ZERO) == 0;
    }

    /**
     * Checks if this money is positive.
     *
     * @return true if amount is greater than zero
     */
    public boolean isPositive() {
        return amount != null && amount.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * Checks if this money is negative.
     *
     * @return true if amount is less than zero
     */
    public boolean isNegative() {
        return amount != null && amount.compareTo(BigDecimal.ZERO) < 0;
    }

    /**
     * Adds another money to this one.
     *
     * @param other the money to add
     * @return a new Money instance with the sum
     */
    public Money add(Money other) {
        validateSameCurrency(other);
        return new Money(this.amount.add(other.amount), this.currencyCode);
    }

    /**
     * Subtracts another money from this one.
     *
     * @param other the money to subtract
     * @return a new Money instance with the difference
     */
    public Money subtract(Money other) {
        validateSameCurrency(other);
        return new Money(this.amount.subtract(other.amount), this.currencyCode);
    }

    /**
     * Multiplies this money by a factor.
     *
     * @param factor the multiplication factor
     * @return a new Money instance with the product
     */
    public Money multiply(BigDecimal factor) {
        return new Money(this.amount.multiply(factor).setScale(DEFAULT_SCALE, DEFAULT_ROUNDING), this.currencyCode);
    }

    /**
     * Multiplies this money by a factor.
     *
     * @param factor the multiplication factor
     * @return a new Money instance with the product
     */
    public Money multiply(double factor) {
        return multiply(BigDecimal.valueOf(factor));
    }

    /**
     * Divides this money by a divisor.
     *
     * @param divisor the divisor
     * @return a new Money instance with the quotient
     */
    public Money divide(BigDecimal divisor) {
        if (divisor.compareTo(BigDecimal.ZERO) == 0) {
            throw new ArithmeticException("Division by zero");
        }
        return new Money(this.amount.divide(divisor, DEFAULT_SCALE, DEFAULT_ROUNDING), this.currencyCode);
    }

    /**
     * Divides this money by a divisor.
     *
     * @param divisor the divisor
     * @return a new Money instance with the quotient
     */
    public Money divide(double divisor) {
        return divide(BigDecimal.valueOf(divisor));
    }

    /**
     * Returns the absolute value of this money.
     *
     * @return a new Money instance with the absolute value
     */
    public Money abs() {
        return new Money(this.amount.abs(), this.currencyCode);
    }

    /**
     * Returns the negative of this money.
     *
     * @return a new Money instance with the negated value
     */
    public Money negate() {
        return new Money(this.amount.negate(), this.currencyCode);
    }

    /**
     * Returns the amount in cents (as a long).
     *
     * @return the amount in cents
     */
    public long toCents() {
        return amount.multiply(BigDecimal.valueOf(100)).longValueExact();
    }

    /**
     * Validates that both money instances have the same currency.
     *
     * @param other the other money
     */
    private void validateSameCurrency(Money other) {
        if (other == null) {
            throw new IllegalArgumentException("Other money cannot be null");
        }
        if (!this.currencyCode.equals(other.currencyCode)) {
            throw new IllegalArgumentException(
                    "Currency mismatch: " + this.currencyCode + " vs " + other.currencyCode);
        }
    }

    @Override
    public int compareTo(Money other) {
        if (other == null) {
            return 1;
        }
        validateSameCurrency(other);
        return this.amount.compareTo(other.amount);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Money money = (Money) o;
        return Objects.equals(amount, money.amount) && Objects.equals(currencyCode, money.currencyCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount, currencyCode);
    }

    @Override
    public String toString() {
        return format(Locale.forLanguageTag("pt-BR"));
    }

    /**
     * Formats the money value for the given locale.
     *
     * @param locale the locale
     * @return formatted string
     */
    public String format(Locale locale) {
        return java.text.NumberFormat.getCurrencyInstance(locale).format(amount);
    }

    /**
     * Formats the money value in Brazilian Portuguese locale.
     *
     * @return formatted string in pt-BR
     */
    public String formatBr() {
        return format(new Locale("pt", "BR"));
    }
}