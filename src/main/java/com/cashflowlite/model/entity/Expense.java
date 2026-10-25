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
 * Money paid out (table expenses). Repeated facts are stored as references to lookup tables.
 */
@Entity
@Table(name = "expenses")
public class Expense {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "expense_date", nullable = false)
	private LocalDate expenseDate;

	@Column(nullable = false, length = 60)
	private String name;

	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal amount;

	@ManyToOne(optional = false)
	@JoinColumn(name = "payment_method_id", nullable = false)
	private PaymentMethod paymentMethod;

	@ManyToOne
	@JoinColumn(name = "payment_instrument_id")
	private PaymentInstrument paymentInstrument;

	@ManyToOne(optional = false)
	@JoinColumn(name = "purpose_id", nullable = false)
	private Purpose purpose;

	@ManyToOne
	@JoinColumn(name = "party_id")
	private Party party;

	@Column(name = "purpose_note", length = 100)
	private String purposeNote;

	@Column(length = 500)
	private String notes;

	/** Optimistic lock: two edits of the same row cannot silently overwrite each other. */
	@Version
	@Column(nullable = false)
	private Integer version;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt = Instant.now();

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt = Instant.now();

	public Expense() {
	}

	@PreUpdate
	void touch() {
		updatedAt = Instant.now();
	}

	public Long getId() {
		return id;
	}

	public LocalDate getExpenseDate() {
		return expenseDate;
	}

	public void setExpenseDate(LocalDate expenseDate) {
		this.expenseDate = expenseDate;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
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

	public Purpose getPurpose() {
		return purpose;
	}

	public void setPurpose(Purpose purpose) {
		this.purpose = purpose;
	}

	public Party getParty() {
		return party;
	}

	public void setParty(Party party) {
		this.party = party;
	}

	public String getPurposeNote() {
		return purposeNote;
	}

	public void setPurposeNote(String purposeNote) {
		this.purposeNote = purposeNote;
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
