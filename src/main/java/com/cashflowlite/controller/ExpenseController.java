package com.cashflowlite.controller;

import com.cashflowlite.model.entity.Expense;
import com.cashflowlite.model.entity.ExpenseType;
import com.cashflowlite.model.entity.PaymentMode;
import com.cashflowlite.model.request.ExpenseForm;
import com.cashflowlite.model.response.EntryType;
import com.cashflowlite.model.response.SearchField;
import com.cashflowlite.service.EntryValidator;
import com.cashflowlite.service.ExpenseService;
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
 * The expenses list (the main screen). Add and edit happen in a dialog over the list.
 */
@Controller
@RequestMapping("/expenses")
public class ExpenseController {

	private static final String BASE = "/expenses";

	private final ExpenseService expenses;
	private final EntryValidator validator;
	private final TransactionListPresenter list;
	private final LookupService lookups;

	public ExpenseController(ExpenseService expenses, EntryValidator validator,
			TransactionListPresenter list, LookupService lookups) {
		this.expenses = expenses;
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
		list.show(model, new ListRequest(BASE, EntryType.EXPENSE, from, to, fields, q, page));
		addForm(model, new ExpenseForm(), null, false);
		return "expenses";
	}

	@GetMapping("/{id}/edit")
	public String edit(@PathVariable Long id, Model model) {
		Expense expense = expenses.get(id);
		list.show(model, new ListRequest(BASE, EntryType.EXPENSE, null, null, null, null, 0));
		addForm(model, ExpenseForm.from(expense), id, true);
		return "expenses";
	}

	@PostMapping
	public String create(@Valid @ModelAttribute("form") ExpenseForm form, BindingResult result,
			Model model, RedirectAttributes redirect) {
		if (!validate(form, result)) {
			return redisplay(model, form, null);
		}
		Expense saved = expenses.create(form);
		redirect.addFlashAttribute("message", "Saved " + saved.getName() + " for ₹" + saved.getAmount());
		return TransactionListPresenter.redirectToMonthOf(BASE, saved.getExpenseDate());
	}

	@PostMapping("/{id}")
	public String update(@PathVariable Long id, @Valid @ModelAttribute("form") ExpenseForm form,
			BindingResult result, Model model, RedirectAttributes redirect) {
		if (!validate(form, result)) {
			return redisplay(model, form, id);
		}
		Expense saved = expenses.update(id, form);
		redirect.addFlashAttribute("message", "Updated " + saved.getName());
		return TransactionListPresenter.redirectToMonthOf(BASE, saved.getExpenseDate());
	}

	@PostMapping("/{id}/delete")
	public String delete(@PathVariable Long id, RedirectAttributes redirect) {
		Expense deleted = expenses.delete(id);
		redirect.addFlashAttribute("message", "Deleted " + deleted.getName());
		return TransactionListPresenter.redirectToMonthOf(BASE, deleted.getExpenseDate());
	}

	private boolean validate(ExpenseForm form, BindingResult result) {
		Map<String, String> problems = validator.checkExpense(form);
		problems.forEach((field, message) -> result.rejectValue(field, "invalid", message));
		return !result.hasErrors();
	}

	/** Shows the list again with the dialog open and the entered values kept. */
	private String redisplay(Model model, ExpenseForm form, Long editingId) {
		list.show(model, new ListRequest(BASE, EntryType.EXPENSE, null, null, null, null, 0));
		addForm(model, form, editingId, true);
		return "expenses";
	}

	private void addForm(Model model, ExpenseForm form, Long editingId, boolean open) {
		model.addAttribute("form", form);
		model.addAttribute("editingId", editingId);
		model.addAttribute("formOpen", open);
		model.addAttribute("paymentModes", PaymentMode.values());
		model.addAttribute("expenseTypes", ExpenseType.values());
		lookups.addSuggestions(model);
	}
}
