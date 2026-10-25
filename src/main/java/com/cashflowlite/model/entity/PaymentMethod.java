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
 * Lookup row for a payment method (table payment_methods).
 */
@Entity
@Table(name = "payment_methods")
public class PaymentMethod {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, unique = true, length = 20)
	private PaymentMode code;

	@Column(nullable = false, length = 40)
	private String label;

	@Column(name = "detail_label", length = 40)
	private String detailLabel;

	protected PaymentMethod() {
	}

	public PaymentMethod(PaymentMode code) {
		this.code = code;
		this.label = code.getLabel();
		this.detailLabel = code.getDetailLabel();
	}

	public Integer getId() {
		return id;
	}

	public PaymentMode getCode() {
		return code;
	}

	public String getLabel() {
		return label;
	}

	public String getDetailLabel() {
		return detailLabel;
	}

	public boolean needsDetail() {
		return detailLabel != null;
	}
}
