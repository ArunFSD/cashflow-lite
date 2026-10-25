package com.cashflowlite.service;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when an expense id does not exist.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ExpenseNotFoundException extends RuntimeException {

	public ExpenseNotFoundException(Long id) {
		super("Expense " + id + " not found");
	}
}
