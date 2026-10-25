package com.cashflowlite.api;

import java.util.Map;

/**
 * Thrown when a request passes the bean checks but breaks a rule that depends on other fields,
 * for example a card is needed for the chosen payment mode. Carries one message per field.
 */
public class InvalidEntryException extends RuntimeException {

	private final Map<String, String> problems;

	public InvalidEntryException(Map<String, String> problems) {
		super("The entry has invalid fields");
		this.problems = problems;
	}

	public Map<String, String> problems() {
		return problems;
	}
}
