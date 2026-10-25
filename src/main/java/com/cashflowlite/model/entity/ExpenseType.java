package com.cashflowlite.model.entity;

/**
 * What an outgoing payment was for. The purposes table holds one row for each value.
 */
public enum ExpenseType {

	PURCHASE("Purchase", false, false),
	SPEND("Spend", false, false),
	BILLS("Bills", false, false),
	SENT_TO_PERSON("Sent to friend / person", true, false),
	TRANSFER_TO_CUSTOMER("Transfer to customer", true, false),
	OTHER("Other", false, true);

	private final String label;
	private final boolean requiresParty;
	private final boolean requiresNote;

	ExpenseType(String label, boolean requiresParty, boolean requiresNote) {
		this.label = label;
		this.requiresParty = requiresParty;
		this.requiresNote = requiresNote;
	}

	public String getLabel() {
		return label;
	}

	/** True when the payment goes to a named friend or customer. */
	public boolean requiresParty() {
		return requiresParty;
	}

	/** True when the user must say what the payment was for. */
	public boolean requiresNote() {
		return requiresNote;
	}
}
