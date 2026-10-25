package com.cashflowlite.service;

import com.cashflowlite.model.response.MonthlySummary;
import com.cashflowlite.model.response.MonthlySummary.Breakdown;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

/**
 * Builds the monthly report with one aggregate query per table. Read-only.
 */
@Service
@Transactional(readOnly = true)
public class MonthlyExpenseService {

	private final JdbcClient jdbc;

	public MonthlyExpenseService(JdbcClient jdbc) {
		this.jdbc = jdbc;
	}

	public MonthlySummary summarize(YearMonth month) {
		LocalDate from = month.atDay(1);
		LocalDate to = month.plusMonths(1).atDay(1);

		Count expenseCount = jdbc.sql("""
						SELECT COUNT(*) AS n, COALESCE(SUM(amount), 0) AS total FROM expenses
						WHERE expense_date >= :fromDate AND expense_date < :toDate
						""")
				.param("fromDate", from).param("toDate", to)
				.query((rs, rowNum) -> new Count(rs.getLong("n"), rs.getBigDecimal("total")))
				.single();

		Count depositCount = jdbc.sql("""
						SELECT COUNT(*) AS n, COALESCE(SUM(amount), 0) AS total FROM deposits
						WHERE deposit_date >= :fromDate AND deposit_date < :toDate
						""")
				.param("fromDate", from).param("toDate", to)
				.query((rs, rowNum) -> new Count(rs.getLong("n"), rs.getBigDecimal("total")))
				.single();

		List<Breakdown> byMethod = breakdown("""
				SELECT m.label AS label, SUM(e.amount) AS total FROM expenses e
				JOIN payment_methods m ON m.id = e.payment_method_id
				WHERE e.expense_date >= :fromDate AND e.expense_date < :toDate
				GROUP BY m.label ORDER BY total DESC
				""", from, to, expenseCount.total());

		List<Breakdown> byCardOrApp = breakdown("""
				SELECT i.label AS label, SUM(e.amount) AS total FROM expenses e
				JOIN payment_instruments i ON i.id = e.payment_instrument_id
				WHERE e.expense_date >= :fromDate AND e.expense_date < :toDate
				GROUP BY i.label ORDER BY total DESC
				""", from, to, expenseCount.total());

		List<Breakdown> byPurpose = breakdown("""
				SELECT p.label AS label, SUM(e.amount) AS total FROM expenses e
				JOIN purposes p ON p.id = e.purpose_id
				WHERE e.expense_date >= :fromDate AND e.expense_date < :toDate
				GROUP BY p.label ORDER BY total DESC
				""", from, to, expenseCount.total());

		List<Breakdown> depositsBySource = breakdown("""
				SELECT pa.name AS label, SUM(d.amount) AS total FROM deposits d
				JOIN parties pa ON pa.id = d.party_id
				WHERE d.deposit_date >= :fromDate AND d.deposit_date < :toDate
				GROUP BY pa.name ORDER BY total DESC
				""", from, to, depositCount.total());

		return new MonthlySummary(month,
				expenseCount.count(), expenseCount.total(),
				depositCount.count(), depositCount.total(),
				byMethod, byCardOrApp, byPurpose, depositsBySource);
	}

	private List<Breakdown> breakdown(String sql, LocalDate from, LocalDate to, BigDecimal grandTotal) {
		return jdbc.sql(sql)
				.param("fromDate", from).param("toDate", to)
				.query((rs, rowNum) -> {
					BigDecimal amount = rs.getBigDecimal("total");
					return new Breakdown(rs.getString("label"), amount, percent(amount, grandTotal));
				})
				.list();
	}

	private static double percent(BigDecimal part, BigDecimal total) {
		if (total.signum() == 0) {
			return 0;
		}
		return part.multiply(BigDecimal.valueOf(100)).divide(total, 1, RoundingMode.HALF_UP).doubleValue();
	}

	private record Count(long count, BigDecimal total) {
	}
}
