package com.cashflowlite.service;

import com.cashflowlite.model.entity.ExpenseType;
import com.cashflowlite.model.entity.Party;
import com.cashflowlite.model.entity.PaymentInstrument;
import com.cashflowlite.model.entity.PaymentMethod;
import com.cashflowlite.model.entity.PaymentMode;
import com.cashflowlite.model.entity.Purpose;
import com.cashflowlite.repository.PartyRepository;
import com.cashflowlite.repository.PaymentInstrumentRepository;
import com.cashflowlite.repository.PaymentMethodRepository;
import com.cashflowlite.repository.PurposeRepository;
import org.springframework.stereotype.Component;

/**
 * Turns the names typed on a form into references to the lookup rows. A name that does not
 * exist yet is created once, and every later use points to that same row.
 * Runs inside the caller's transaction, so a failed save leaves no new names behind.
 */
@Component
public class EntryReferences {

	private final PaymentMethodRepository paymentMethods;
	private final PurposeRepository purposes;
	private final PartyRepository parties;
	private final PaymentInstrumentRepository instruments;

	public EntryReferences(PaymentMethodRepository paymentMethods, PurposeRepository purposes,
			PartyRepository parties, PaymentInstrumentRepository instruments) {
		this.paymentMethods = paymentMethods;
		this.purposes = purposes;
		this.parties = parties;
		this.instruments = instruments;
	}

	public PaymentMethod paymentMethod(PaymentMode code) {
		return paymentMethods.findByCode(code)
				.orElseThrow(() -> new IllegalStateException("Payment method row missing: " + code));
	}

	public Purpose purpose(ExpenseType code) {
		return purposes.findByCode(code)
				.orElseThrow(() -> new IllegalStateException("Purpose row missing: " + code));
	}

	public Party party(String rawName) {
		String name = rawName.trim();
		return parties.findFirstByNameIgnoreCase(name)
				.orElseGet(() -> parties.save(new Party(name)));
	}

	/** Returns null when the label is blank, for methods that need no card, app or bank. */
	public PaymentInstrument instrument(PaymentMethod method, String rawLabel) {
		if (rawLabel == null || rawLabel.isBlank()) {
			return null;
		}
		String label = rawLabel.trim();
		return instruments.findFirstByPaymentMethodAndLabelIgnoreCase(method, label)
				.orElseGet(() -> instruments.save(new PaymentInstrument(method, label)));
	}
}
