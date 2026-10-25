package com.cashflowlite.controller;

import com.cashflowlite.model.entity.Deposit;
import com.cashflowlite.model.entity.PaymentMode;
import com.cashflowlite.model.request.DepositForm;
import com.cashflowlite.model.response.EntryType;
import com.cashflowlite.model.response.SearchField;
import com.cashflowlite.service.DepositService;
import com.cashflowlite.service.EntryValidator;
import com.cashflowlite.service.LookupService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

/**
 * The deposits list. Add and edit happen in a dialog over the list.
 */
@Controller
@RequestMapping("/deposits")
public class DepositController {

	private static final String BASE = "/deposits";

	private final DepositService deposits;
	private final EntryValidator validator;
	private final TransactionListPresenter list;
	private final LookupService lookups;

	public DepositController(DepositService deposits, EntryValidator validator,
			TransactionListPresenter list, LookupService lookups) {
		this.deposits = deposits;
		this.validator = validator;
		this.list = list;
		this.lookups = lookups;
	}

	@GetMapping
	public String list(@RequestParam(name = "from", required = false) String from,
			@RequestParam(name = "to", required = false) String to,
			@RequestParam(name = "field", required = false) List<SearchField> fields,
			@RequestParam(name = "q", required = false) String q,
			@RequestParam(name = "page", defaultValue = "0") int page,
			Model model) {
		list.show(model, new ListRequest(BASE, EntryType.DEPOSIT, from, to, fields, q, page));
		addForm(model, new DepositForm(), null, false);
		return "deposits";
	}

	@GetMapping("/{id}/edit")
	public String edit(@PathVariable Long id, Model model) {
		Deposit deposit = deposits.get(id);
		list.show(model, new ListRequest(BASE, EntryType.DEPOSIT, null, null, null, null, 0));
		addForm(model, DepositForm.from(deposit), id, true);
		return "deposits";
	}

	@PostMapping
	public String create(@Valid @ModelAttribute("form") DepositForm form, BindingResult result,
			Model model, RedirectAttributes redirect) {
		if (!validate(form, result)) {
			return redisplay(model, form, null);
		}
		Deposit saved = deposits.create(form);
		redirect.addFlashAttribute("message", "Saved deposit from " + saved.getParty().getName()
				+ " for ₹" + saved.getAmount());
		return TransactionListPresenter.redirectToMonthOf(BASE, saved.getDepositDate());
	}

	@PostMapping("/{id}")
	public String update(@PathVariable Long id, @Valid @ModelAttribute("form") DepositForm form,
			BindingResult result, Model model, RedirectAttributes redirect) {
		if (!validate(form, result)) {
			return redisplay(model, form, id);
		}
		Deposit saved = deposits.update(id, form);
		redirect.addFlashAttribute("message", "Updated deposit from " + saved.getParty().getName());
		return TransactionListPresenter.redirectToMonthOf(BASE, saved.getDepositDate());
	}

	@PostMapping("/{id}/delete")
	public String delete(@PathVariable Long id, RedirectAttributes redirect) {
		Deposit deleted = deposits.delete(id);
		redirect.addFlashAttribute("message", "Deleted deposit from " + deleted.getParty().getName());
		return TransactionListPresenter.redirectToMonthOf(BASE, deleted.getDepositDate());
	}

	private boolean validate(DepositForm form, BindingResult result) {
		Map<String, String> problems = validator.checkDeposit(form);
		problems.forEach((field, message) -> result.rejectValue(field, "invalid", message));
		return !result.hasErrors();
	}

	private String redisplay(Model model, DepositForm form, Long editingId) {
		list.show(model, new ListRequest(BASE, EntryType.DEPOSIT, null, null, null, null, 0));
		addForm(model, form, editingId, true);
		return "deposits";
	}

	private void addForm(Model model, DepositForm form, Long editingId, boolean open) {
		model.addAttribute("form", form);
		model.addAttribute("editingId", editingId);
		model.addAttribute("formOpen", open);
		model.addAttribute("paymentModes", PaymentMode.receivable());
		lookups.addSuggestions(model);
	}
}
