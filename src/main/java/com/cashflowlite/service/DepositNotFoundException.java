package com.cashflowlite.service;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when a deposit id does not exist.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class DepositNotFoundException extends RuntimeException {

	public DepositNotFoundException(Long id) {
		super("Deposit " + id + " not found");
	}
}
