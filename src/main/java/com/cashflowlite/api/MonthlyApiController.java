package com.cashflowlite.api;

import com.cashflowlite.model.response.MonthlySummary;
import com.cashflowlite.service.MonthlyExpenseService;
import com.cashflowlite.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/monthly")
@Tag(name = "Monthly report", description = "Totals and breakdowns for one calendar month")
public class MonthlyApiController {

	private final MonthlyExpenseService monthly;

	public MonthlyApiController(MonthlyExpenseService monthly) {
		this.monthly = monthly;
	}

	@GetMapping
	@Operation(summary = "Monthly summary",
			description = "Expense and deposit totals for the month, with breakdowns by payment method, card or app, purpose and source.")
	public MonthlySummary summary(
			@Parameter(description = "Month as YYYY-MM. Defaults to the current month.", example = "2026-10")
			@RequestParam(name = "month", required = false) String month) {
		return monthly.summarize(TransactionService.parseMonth(month));
	}
}
