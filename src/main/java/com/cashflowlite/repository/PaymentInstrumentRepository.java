package com.cashflowlite.repository;

import com.cashflowlite.model.entity.PaymentInstrument;
import com.cashflowlite.model.entity.PaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentInstrumentRepository extends JpaRepository<PaymentInstrument, Long> {

	Optional<PaymentInstrument> findFirstByPaymentMethodAndLabelIgnoreCase(PaymentMethod paymentMethod, String label);
}
