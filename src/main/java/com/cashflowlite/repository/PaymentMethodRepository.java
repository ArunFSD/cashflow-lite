package com.cashflowlite.repository;

import com.cashflowlite.model.entity.PaymentMethod;
import com.cashflowlite.model.entity.PaymentMode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, Integer> {

	Optional<PaymentMethod> findByCode(PaymentMode code);
}
