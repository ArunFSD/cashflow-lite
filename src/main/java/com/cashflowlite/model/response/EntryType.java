package com.cashflowlite.model.response;

/**
 * Which kind of rows a list shows. ALL is the combined transactions list.
 */
public enum EntryType {

	ALL("All"),
	EXPENSE("Expenses"),
	DEPOSIT("Deposits");

	private final String label;

	EntryType(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}
}
