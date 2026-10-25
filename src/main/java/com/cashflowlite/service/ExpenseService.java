package com.cashflowlite.service;

import com.cashflowlite.model.entity.Expense;
import com.cashflowlite.model.request.ExpenseForm;
import com.cashflowlite.repository.ExpenseRepository;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.RoundingMode;

/**
 * Use cases for expenses. Every public method is one transaction: it commits completely or not at all.
 */
@Service
public class ExpenseService {

	private final ExpenseRepository expenses;
	private final EntryReferences references;

	public ExpenseService(ExpenseRepository expenses, EntryReferences references) {
		this.expenses = expenses;
		this.references = references;
	}

	@Transactional(readOnly = true)
	public Expense get(Long id) {
		return expenses.findById(id).orElseThrow(() -> new ExpenseNotFoundException(id));
	}

	@Transactional
	public Expense create(ExpenseForm form) {
		Expense expense = new Expense();
		apply(expense, form);
		return expenses.save(expense);
	}

	@Transactional
	public Expense update(Long id, ExpenseForm form) {
		Expense expense = get(id);
		if (form.getVersion() != null && !form.getVersion().equals(expense.getVersion())) {
			throw new OptimisticLockingFailureException("Expense " + id + " was changed by someone else");
		}
		apply(expense, form);
		return expense;
	}

	@Transactional
	public Expense delete(Long id) {
		Expense expense = get(id);
		expenses.delete(expense);
		return expense;
	}

	private void apply(Expense expense, ExpenseForm form) {
		var method = references.paymentMethod(form.getPaymentMode());
		expense.setExpenseDate(form.getExpenseDate());
		expense.setName(form.getName().trim());
		expense.setAmount(form.getAmount().setScale(2, RoundingMode.HALF_UP));
		expense.setPaymentMethod(method);
		expense.setPaymentInstrument(method.needsDetail()
				? references.instrument(method, form.getPaymentDetail())
				: null);
		expense.setPurpose(references.purpose(form.getExpenseType()));
		expense.setParty(form.getExpenseType().requiresParty()
				? references.party(form.getPartyName())
				: null);
		expense.setPurposeNote(form.getExpenseType().requiresNote() ? trimToNull(form.getPurposeNote()) : null);
		expense.setNotes(trimToNull(form.getNotes()));
	}

	private static String trimToNull(String value) {
		if (value == null) {
			return null;
		}
		String trimmed = value.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}
}
