package com.cashflowlite.model.response;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * One line in a transaction list. Expenses and deposits are both shown as rows.
 */
public record TransactionRow(
		Long id,
		EntryType kind,
		LocalDate date,
		String typeLabel,
		String title,
		String paymentLabel,
		String paymentDetail,
		String payee,
		String notes,
		BigDecimal amount) {

	public boolean deposit() {
		return kind == EntryType.DEPOSIT;
	}
}
