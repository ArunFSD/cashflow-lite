package com.cashflowlite.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/**
 * Money received (table deposits).
 */
@Entity
@Table(name = "deposits")
public class Deposit {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "deposit_date", nullable = false)
	private LocalDate depositDate;

	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal amount;

	@ManyToOne(optional = false)
	@JoinColumn(name = "party_id", nullable = false)
	private Party party;

	@ManyToOne(optional = false)
	@JoinColumn(name = "payment_method_id", nullable = false)
	private PaymentMethod paymentMethod;

	@ManyToOne
	@JoinColumn(name = "payment_instrument_id")
	private PaymentInstrument paymentInstrument;

	@Column(length = 500)
	private String notes;

	@Version
	@Column(nullable = false)
	private Integer version;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt = Instant.now();

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt = Instant.now();

	public Deposit() {
	}

	@PreUpdate
	void touch() {
		updatedAt = Instant.now();
	}

	public Long getId() {
		return id;
	}

	public LocalDate getDepositDate() {
		return depositDate;
	}

	public void setDepositDate(LocalDate depositDate) {
		this.depositDate = depositDate;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public Party getParty() {
		return party;
	}

	public void setParty(Party party) {
		this.party = party;
	}

	public PaymentMethod getPaymentMethod() {
		return paymentMethod;
	}

	public void setPaymentMethod(PaymentMethod paymentMethod) {
		this.paymentMethod = paymentMethod;
	}

	public PaymentInstrument getPaymentInstrument() {
		return paymentInstrument;
	}

	public void setPaymentInstrument(PaymentInstrument paymentInstrument) {
		this.paymentInstrument = paymentInstrument;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public Integer getVersion() {
		return version;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}
}
