package com.cashflowlite.model.response;

import java.math.BigDecimal;
import java.util.List;

/**
 * One page of transactions plus the totals for everything that matches the filter.
 */
public record TransactionPage(
		List<TransactionRow> rows,
		int page,
		int size,
		long totalRows,
		BigDecimal moneyIn,
		BigDecimal moneyOut) {

	public int totalPages() {
		return (int) Math.max(1L, Math.ceilDiv(totalRows, (long) size));
	}

	public boolean hasPrevious() {
		return page > 0;
	}

	public boolean hasNext() {
		return page + 1 < totalPages();
	}

	public BigDecimal net() {
		return moneyIn.subtract(moneyOut);
	}
}
