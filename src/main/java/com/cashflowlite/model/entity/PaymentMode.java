package com.cashflowlite.model.entity;

import java.util.Arrays;
import java.util.List;

/**
 * Payment methods the application understands. The payment_methods table holds one row
 * for each value, created at startup.
 */
public enum PaymentMode {

	CASH("Cash", null, true),
	GPAY("GPay", null, true),
	UPI("Other UPI app", "Which UPI app", true),
	CREDIT_CARD("Credit card", "Which credit card", false),
	DEBIT_CARD("Debit card", "Which debit card", false),
	NET_BANKING("Net banking", "Which netbanking", true),
	OTHER("Other", "Mention the other", true);

	private final String label;
	private final String detailLabel;
	private final boolean receivable;

	PaymentMode(String label, String detailLabel, boolean receivable) {
		this.label = label;
		this.detailLabel = detailLabel;
		this.receivable = receivable;
	}

	public String getLabel() {
		return label;
	}

	/** Label of the extra detail field, or null when the method needs no detail. */
	public String getDetailLabel() {
		return detailLabel;
	}

	public boolean needsDetail() {
		return detailLabel != null;
	}

	/** Cards cannot receive money, so they are not offered for deposits. */
	public boolean canReceive() {
		return receivable;
	}

	public static List<PaymentMode> receivable() {
		return Arrays.stream(values()).filter(PaymentMode::canReceive).toList();
	}
}
