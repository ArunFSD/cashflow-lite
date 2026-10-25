package com.cashflowlite.model.response;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

/**
 * Totals and breakdowns for one month.
 */
public record MonthlySummary(
		YearMonth month,
		long expenseCount,
		BigDecimal expenseTotal,
		long depositCount,
		BigDecimal depositTotal,
		List<Breakdown> byPaymentMethod,
		List<Breakdown> byCardOrApp,
		List<Breakdown> byPurpose,
		List<Breakdown> depositsBySource) {

	public BigDecimal net() {
		return depositTotal.subtract(expenseTotal);
	}

	/** One line of a breakdown: label, amount and its share of the total in percent. */
	public record Breakdown(String label, BigDecimal amount, double percent) {
	}
}
