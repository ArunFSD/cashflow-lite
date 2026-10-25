package com.cashflowlite.api;

import com.cashflowlite.model.request.ExpenseForm;
import com.cashflowlite.model.response.EntryType;
import com.cashflowlite.model.response.SearchField;
import com.cashflowlite.model.response.TransactionFilter;
import com.cashflowlite.model.response.TransactionPage;
import com.cashflowlite.service.EntryValidator;
import com.cashflowlite.service.ExpenseService;
import com.cashflowlite.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/expenses")
@Tag(name = "Expenses", description = "Money paid out")
public class ExpenseApiController {

	private final ExpenseService expenses;
	private final EntryValidator validator;
	private final TransactionService transactions;

	public ExpenseApiController(ExpenseService expenses, EntryValidator validator, TransactionService transactions) {
		this.expenses = expenses;
		this.validator = validator;
		this.transactions = transactions;
	}

	@GetMapping
	@Operation(summary = "List expenses",
			description = "Newest first, paged. from and to are inclusive. field narrows the search columns; "
					+ "omit it to search all fields. Totals cover every row that matches, not just this page.")
	public TransactionPage list(
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
			@RequestParam(name = "field", required = false) List<SearchField> fields,
			@RequestParam(name = "q", required = false) String q,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "50") int size) {
		return transactions.page(
				new TransactionFilter(from, to, EntryType.EXPENSE, fields == null ? List.of() : fields, q),
				page, size);
	}

	@GetMapping("/{id}")
	@Operation(summary = "Get one expense")
	public ExpenseView get(@PathVariable Long id) {
		return ExpenseView.from(expenses.get(id));
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "Add an expense")
	public ExpenseView create(@Valid @RequestBody ExpenseForm form) {
		requireValid(validator.checkExpense(form));
		return ExpenseView.from(expenses.create(form));
	}

	@PutMapping("/{id}")
	@Operation(summary = "Update an expense",
			description = "Send back the version you read so that a change made by someone else is not overwritten.")
	public ExpenseView update(@PathVariable Long id, @Valid @RequestBody ExpenseForm form) {
		requireValid(validator.checkExpense(form));
		return ExpenseView.from(expenses.update(id, form));
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "Delete an expense")
	public void delete(@PathVariable Long id) {
		expenses.delete(id);
	}

	private static void requireValid(Map<String, String> problems) {
		if (!problems.isEmpty()) {
			throw new InvalidEntryException(problems);
		}
	}
}
