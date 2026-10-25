package com.cashflowlite.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Lookup row for what an expense was for (table purposes).
 */
@Entity
@Table(name = "purposes")
public class Purpose {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, unique = true, length = 30)
	private ExpenseType code;

	@Column(nullable = false, length = 40)
	private String label;

	@Column(name = "requires_party", nullable = false)
	private boolean requiresParty;

	@Column(name = "requires_note", nullable = false)
	private boolean requiresNote;

	protected Purpose() {
	}

	public Purpose(ExpenseType code) {
		this.code = code;
		this.label = code.getLabel();
		this.requiresParty = code.requiresParty();
		this.requiresNote = code.requiresNote();
	}

	public Integer getId() {
		return id;
	}

	public ExpenseType getCode() {
		return code;
	}

	public String getLabel() {
		return label;
	}

	public boolean isRequiresParty() {
		return requiresParty;
	}

	public boolean isRequiresNote() {
		return requiresNote;
	}
}
