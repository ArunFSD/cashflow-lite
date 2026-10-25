package com.cashflowlite.api;

import com.cashflowlite.model.response.EntryType;
import com.cashflowlite.model.response.SearchField;
import com.cashflowlite.model.response.TransactionFilter;
import com.cashflowlite.model.response.TransactionPage;
import com.cashflowlite.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/transactions")
@Tag(name = "Transactions", description = "Expenses and deposits in one list")
public class TransactionApiController {

	private final TransactionService transactions;

	public TransactionApiController(TransactionService transactions) {
		this.transactions = transactions;
	}

	@GetMapping
	@Operation(summary = "List all transactions",
			description = "Expenses and deposits together, newest first. kind limits it to one type (ALL, EXPENSE, DEPOSIT). "
					+ "Totals cover every row that matches: moneyIn is deposits, moneyOut is expenses.")
	public TransactionPage list(
			@RequestParam(defaultValue = "ALL") EntryType kind,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
			@RequestParam(name = "field", required = false) List<SearchField> fields,
			@RequestParam(name = "q", required = false) String q,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "50") int size) {
		return transactions.page(
				new TransactionFilter(from, to, kind, fields == null ? List.of() : fields, q),
				page, size);
	}
}
