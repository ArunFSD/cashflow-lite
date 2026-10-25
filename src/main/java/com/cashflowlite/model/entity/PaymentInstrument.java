package com.cashflowlite.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * A card, UPI app or bank used for a payment (table payment_instruments).
 */
@Entity
@Table(name = "payment_instruments")
public class PaymentInstrument {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(optional = false)
	@JoinColumn(name = "payment_method_id", nullable = false)
	private PaymentMethod paymentMethod;

	@Column(nullable = false, length = 100)
	private String label;

	protected PaymentInstrument() {
	}

	public PaymentInstrument(PaymentMethod paymentMethod, String label) {
		this.paymentMethod = paymentMethod;
		this.label = label;
	}

	public Long getId() {
		return id;
	}

	public PaymentMethod getPaymentMethod() {
		return paymentMethod;
	}

	public String getLabel() {
		return label;
	}
}
