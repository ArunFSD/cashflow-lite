package com.cashflowlite.service;

import com.cashflowlite.model.request.DepositForm;
import com.cashflowlite.model.request.ExpenseForm;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Checks the rules that depend on the chosen options. Each check returns field name to message,
 * so the controller can show the message under the right field. The database enforces the same
 * rules again through triggers, so bad data cannot be saved even by another route.
 */
@Component
public class EntryValidator {

	public Map<String, String> checkExpense(ExpenseForm form) {
		Map<String, String> problems = new LinkedHashMap<>();
		if (form.getPaymentMode() != null && form.getPaymentMode().needsDetail() && isBlank(form.getPaymentDetail())) {
			problems.put("paymentDetail", "Please fill in: " + form.getPaymentMode().getDetailLabel());
		}
		if (form.getExpenseType() != null && form.getExpenseType().requiresParty() && isBlank(form.getPartyName())) {
			problems.put("partyName", "Enter the name of the friend or customer");
		}
		if (form.getExpenseType() != null && form.getExpenseType().requiresNote() && isBlank(form.getPurposeNote())) {
			problems.put("purposeNote", "Mention what it was for");
		}
		return problems;
	}

	public Map<String, String> checkDeposit(DepositForm form) {
		Map<String, String> problems = new LinkedHashMap<>();
		if (form.getReceivedVia() != null && !form.getReceivedVia().canReceive()) {
			problems.put("receivedVia", "Cards cannot receive money");
		}
		if (form.getReceivedVia() != null && form.getReceivedVia().needsDetail() && isBlank(form.getPaymentDetail())) {
			problems.put("paymentDetail", "Please fill in: " + form.getReceivedVia().getDetailLabel());
		}
		return problems;
	}

	private static boolean isBlank(String value) {
		return value == null || value.isBlank();
	}
}
