package com.cashflowlite.api;

import com.cashflowlite.model.request.DepositForm;
import com.cashflowlite.model.response.EntryType;
import com.cashflowlite.model.response.SearchField;
import com.cashflowlite.model.response.TransactionFilter;
import com.cashflowlite.model.response.TransactionPage;
import com.cashflowlite.service.DepositService;
import com.cashflowlite.service.EntryValidator;
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
@RequestMapping("/api/v1/deposits")
@Tag(name = "Deposits", description = "Money received")
public class DepositApiController {

	private final DepositService deposits;
	private final EntryValidator validator;
	private final TransactionService transactions;

	public DepositApiController(DepositService deposits, EntryValidator validator, TransactionService transactions) {
		this.deposits = deposits;
		this.validator = validator;
		this.transactions = transactions;
	}

	@GetMapping
	@Operation(summary = "List deposits",
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
				new TransactionFilter(from, to, EntryType.DEPOSIT, fields == null ? List.of() : fields, q),
				page, size);
	}

	@GetMapping("/{id}")
	@Operation(summary = "Get one deposit")
	public DepositView get(@PathVariable Long id) {
		return DepositView.from(deposits.get(id));
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "Add a deposit")
	public DepositView create(@Valid @RequestBody DepositForm form) {
		requireValid(validator.checkDeposit(form));
		return DepositView.from(deposits.create(form));
	}

	@PutMapping("/{id}")
	@Operation(summary = "Update a deposit",
			description = "Send back the version you read so that a change made by someone else is not overwritten.")
	public DepositView update(@PathVariable Long id, @Valid @RequestBody DepositForm form) {
		requireValid(validator.checkDeposit(form));
		return DepositView.from(deposits.update(id, form));
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "Delete a deposit")
	public void delete(@PathVariable Long id) {
		deposits.delete(id);
	}

	private static void requireValid(Map<String, String> problems) {
		if (!problems.isEmpty()) {
			throw new InvalidEntryException(problems);
		}
	}
}
