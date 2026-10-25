package com.cashflowlite.model.response;

/**
 * Which column the search box looks in.
 */
public enum SearchField {

	ALL("All fields"),
	TITLE("Name / from"),
	TYPE("For"),
	PAYMENT("Paid by / received via"),
	DETAIL("Card / UPI app / bank"),
	PAYEE("Friend / customer"),
	NOTES("Notes");

	private final String label;

	SearchField(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}
}
