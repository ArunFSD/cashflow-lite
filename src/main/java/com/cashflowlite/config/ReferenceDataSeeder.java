package com.cashflowlite.config;

import com.cashflowlite.model.entity.ExpenseType;
import com.cashflowlite.model.entity.PaymentMethod;
import com.cashflowlite.model.entity.PaymentMode;
import com.cashflowlite.model.entity.Purpose;
import com.cashflowlite.repository.PaymentMethodRepository;
import com.cashflowlite.repository.PurposeRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Adds any missing payment method and purpose rows at startup. It only inserts,
 * so the rows are never changed or removed after they exist.
 */
@Component
public class ReferenceDataSeeder implements ApplicationRunner {

	private final PaymentMethodRepository paymentMethods;
	private final PurposeRepository purposes;

	public ReferenceDataSeeder(PaymentMethodRepository paymentMethods, PurposeRepository purposes) {
		this.paymentMethods = paymentMethods;
		this.purposes = purposes;
	}

	@Override
	@Transactional
	public void run(ApplicationArguments args) {
		for (PaymentMode mode : PaymentMode.values()) {
			if (paymentMethods.findByCode(mode).isEmpty()) {
				paymentMethods.save(new PaymentMethod(mode));
			}
		}
		for (ExpenseType type : ExpenseType.values()) {
			if (purposes.findByCode(type).isEmpty()) {
				purposes.save(new Purpose(type));
			}
		}
	}
}
