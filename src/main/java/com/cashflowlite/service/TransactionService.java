package com.cashflowlite.service;

import com.cashflowlite.model.response.EntryType;
import com.cashflowlite.model.response.SearchField;
import com.cashflowlite.model.response.TransactionFilter;
import com.cashflowlite.model.response.TransactionPage;
import com.cashflowlite.model.response.TransactionRow;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Read side for the transaction lists. Filters and paging run in the database, using the indexes,
 * so a list with millions of rows only fetches the page on screen.
 * Only fixed SQL fragments are built here; user input is always passed as a bound parameter.
 */
@Service
@Transactional(readOnly = true)
public class TransactionService {

	public static final int MIN_PAGE_SIZE = 10;
	public static final int MAX_PAGE_SIZE = 200;

	/** Expenses and deposits as one set of rows with the same columns. */
	private static final String ENTRIES_SQL = """
			WITH entries AS (
			    SELECT 'EXPENSE' AS kind, e.id AS id, e.expense_date AS entry_date, e.name AS title,
			           p.label AS type_label, m.label AS payment_label, i.label AS payment_detail,
			           pa.name AS payee, e.notes AS notes, e.amount AS amount
			    FROM expenses e
			    JOIN purposes p ON p.id = e.purpose_id
			    JOIN payment_methods m ON m.id = e.payment_method_id
			    LEFT JOIN payment_instruments i ON i.id = e.payment_instrument_id
			    LEFT JOIN parties pa ON pa.id = e.party_id
			    UNION ALL
			    SELECT 'DEPOSIT', d.id, d.deposit_date, pa.name, 'Deposit', m.label, i.label,
			           pa.name, d.notes, d.amount
			    FROM deposits d
			    JOIN payment_methods m ON m.id = d.payment_method_id
			    LEFT JOIN payment_instruments i ON i.id = d.payment_instrument_id
			    JOIN parties pa ON pa.id = d.party_id
			)
			""";

	private static final String ALL_FIELDS_CONDITION = "(title ILIKE :pattern OR type_label ILIKE :pattern"
			+ " OR payment_label ILIKE :pattern OR payment_detail ILIKE :pattern OR payee ILIKE :pattern"
			+ " OR notes ILIKE :pattern OR CAST(amount AS VARCHAR(20)) ILIKE :pattern)";

	private final JdbcClient jdbc;

	public TransactionService(JdbcClient jdbc) {
		this.jdbc = jdbc;
	}

	/**
	 * Parses "yyyy-MM" for the monthly report. A missing value means the current month,
	 * a blank value means all months, and an invalid value falls back to the current month.
	 */
	public static YearMonth parseMonth(String raw) {
		if (raw == null) {
			return YearMonth.now(com.cashflowlite.config.AppTime.ZONE);
		}
		if (raw.isBlank()) {
			return null;
		}
		try {
			return YearMonth.parse(raw.trim());
		} catch (DateTimeParseException ex) {
			return YearMonth.now(com.cashflowlite.config.AppTime.ZONE);
		}
	}

	public TransactionPage page(TransactionFilter filter, int page, int size) {
		int safeSize = Math.clamp(size, MIN_PAGE_SIZE, MAX_PAGE_SIZE);
		int safePage = Math.max(page, 0);

		Map<String, Object> params = new HashMap<>();
		String where = whereClause(filter, params);

		List<Totals> totals = jdbc.sql(ENTRIES_SQL
						+ "SELECT kind, COUNT(*) AS n, COALESCE(SUM(amount), 0) AS total FROM entries"
						+ where + " GROUP BY kind")
				.params(params)
				.query((rs, rowNum) -> new Totals(rs.getString("kind"), rs.getLong("n"), rs.getBigDecimal("total")))
				.list();

		params.put("limit", safeSize);
		params.put("offset", (long) safePage * safeSize);
		List<TransactionRow> rows = jdbc.sql(ENTRIES_SQL
						+ "SELECT * FROM entries" + where
						+ " ORDER BY entry_date DESC, id DESC LIMIT :limit OFFSET :offset")
				.params(params)
				.query(TransactionService::mapRow)
				.list();

		long totalRows = 0;
		BigDecimal moneyIn = BigDecimal.ZERO;
		BigDecimal moneyOut = BigDecimal.ZERO;
		for (Totals t : totals) {
			totalRows += t.count();
			if ("DEPOSIT".equals(t.kind())) {
				moneyIn = moneyIn.add(t.total());
			} else {
				moneyOut = moneyOut.add(t.total());
			}
		}
		return new TransactionPage(rows, safePage, safeSize, totalRows, moneyIn, moneyOut);
	}

	private static String whereClause(TransactionFilter filter, Map<String, Object> params) {
		List<String> conditions = new ArrayList<>();
		if (filter.from() != null) {
			conditions.add("entry_date >= :fromDate");
			params.put("fromDate", filter.from());
		}
		if (filter.to() != null) {
			// the "to" date is included, so the bound is the day after it
			conditions.add("entry_date < :toDate");
			params.put("toDate", filter.to().plusDays(1));
		}
		if (filter.kind() != null && filter.kind() != EntryType.ALL) {
			conditions.add("kind = :kind");
			params.put("kind", filter.kind().name());
		}
		if (filter.query() != null && !filter.query().isBlank()) {
			params.put("pattern", "%" + escapeLike(filter.query().trim()) + "%");
			conditions.add(searchCondition(filter.fields()));
		}
		return conditions.isEmpty() ? "" : " WHERE " + String.join(" AND ", conditions);
	}

	/** Matches the text in any of the chosen columns. */
	private static String searchCondition(List<SearchField> fields) {
		if (fields == null || fields.isEmpty() || fields.contains(SearchField.ALL)) {
			return ALL_FIELDS_CONDITION;
		}
		List<String> parts = new ArrayList<>();
		for (SearchField field : fields) {
			parts.add(columnCondition(field));
		}
		return "(" + String.join(" OR ", parts) + ")";
	}

	private static String columnCondition(SearchField field) {
		return switch (field) {
			case ALL -> ALL_FIELDS_CONDITION;
			case TITLE -> "title ILIKE :pattern";
			case TYPE -> "type_label ILIKE :pattern";
			case PAYMENT -> "payment_label ILIKE :pattern";
			case DETAIL -> "payment_detail ILIKE :pattern";
			case PAYEE -> "payee ILIKE :pattern";
			case NOTES -> "notes ILIKE :pattern";
		};
	}

	/** Escapes the LIKE wildcards so the user's text is matched literally. */
	private static String escapeLike(String text) {
		return text.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
	}

	private static TransactionRow mapRow(ResultSet rs, int rowNum) throws SQLException {
		return new TransactionRow(
				rs.getLong("id"),
				EntryType.valueOf(rs.getString("kind")),
				rs.getObject("entry_date", LocalDate.class),
				rs.getString("type_label"),
				rs.getString("title"),
				rs.getString("payment_label"),
				rs.getString("payment_detail"),
				rs.getString("payee"),
				rs.getString("notes"),
				rs.getBigDecimal("amount"));
	}

	private record Totals(String kind, long count, BigDecimal total) {
	}
}
