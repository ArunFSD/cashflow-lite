package com.cashflowlite.controller;

import com.cashflowlite.model.response.EntryType;
import com.cashflowlite.model.response.SearchField;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Expenses and deposits in one list. The "Show" filter narrows it to one kind.
 */
@Controller
@RequestMapping("/transactions")
public class TransactionController {

	private final TransactionListPresenter list;

	public TransactionController(TransactionListPresenter list) {
		this.list = list;
	}

	@GetMapping
	public String list(@RequestParam(name = "kind", required = false) EntryType kind,
			@RequestParam(name = "from", required = false) String from,
			@RequestParam(name = "to", required = false) String to,
			@RequestParam(name = "field", required = false) List<SearchField> fields,
			@RequestParam(name = "q", required = false) String q,
			@RequestParam(name = "page", defaultValue = "0") int page,
			Model model) {
		list.show(model, new ListRequest("/transactions", kind, from, to, fields, q, page));
		return "transactions";
	}
}
