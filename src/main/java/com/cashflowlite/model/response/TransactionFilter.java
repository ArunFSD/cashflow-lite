package com.cashflowlite.model.response;

import java.time.LocalDate;
import java.util.List;

/**
 * What the transaction list should show. Both dates are inclusive. A null date means no limit on that side.
 * fields lists the columns to search in. Empty, or containing ALL, means every column.
 */
public record TransactionFilter(LocalDate from, LocalDate to, EntryType kind, List<SearchField> fields, String query) {
}
