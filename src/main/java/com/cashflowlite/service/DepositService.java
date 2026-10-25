package com.cashflowlite.service;

import com.cashflowlite.model.entity.Deposit;
import com.cashflowlite.model.request.DepositForm;
import com.cashflowlite.repository.DepositRepository;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.RoundingMode;

/**
 * Use cases for deposits. Every public method is one transaction.
 */
@Service
public class DepositService {

	private final DepositRepository deposits;
	private final EntryReferences references;

	public DepositService(DepositRepository deposits, EntryReferences references) {
		this.deposits = deposits;
		this.references = references;
	}

	@Transactional(readOnly = true)
	public Deposit get(Long id) {
		return deposits.findById(id).orElseThrow(() -> new DepositNotFoundException(id));
	}

	@Transactional
	public Deposit create(DepositForm form) {
		Deposit deposit = new Deposit();
		apply(deposit, form);
		return deposits.save(deposit);
	}

	@Transactional
	public Deposit update(Long id, DepositForm form) {
		Deposit deposit = get(id);
		if (form.getVersion() != null && !form.getVersion().equals(deposit.getVersion())) {
			throw new OptimisticLockingFailureException("Deposit " + id + " was changed by someone else");
		}
		apply(deposit, form);
		return deposit;
	}

	@Transactional
	public Deposit delete(Long id) {
		Deposit deposit = get(id);
		deposits.delete(deposit);
		return deposit;
	}

	private void apply(Deposit deposit, DepositForm form) {
		var method = references.paymentMethod(form.getReceivedVia());
		deposit.setDepositDate(form.getDepositDate());
		deposit.setAmount(form.getAmount().setScale(2, RoundingMode.HALF_UP));
		deposit.setParty(references.party(form.getReceivedFrom()));
		deposit.setPaymentMethod(method);
		deposit.setPaymentInstrument(method.needsDetail()
				? references.instrument(method, form.getPaymentDetail())
				: null);
		deposit.setNotes(trimToNull(form.getNotes()));
	}

	private static String trimToNull(String value) {
		if (value == null) {
			return null;
		}
		String trimmed = value.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}
}
