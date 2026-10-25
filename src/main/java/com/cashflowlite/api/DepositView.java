package com.cashflowlite.api;

import com.cashflowlite.model.entity.Deposit;
import com.cashflowlite.model.entity.PaymentMode;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/**
 * One deposit (money received) as returned by the API.
 */
@Schema(description = "A deposit (money received)")
public record DepositView(
		@Schema(example = "7") Long id,
		@Schema(example = "2026-10-25") LocalDate depositDate,
		@Schema(example = "Ravi") String receivedFrom,
		@Schema(example = "5000.00") BigDecimal amount,
		PaymentMode receivedVia,
		@Schema(description = "Card, UPI app or bank, when the mode needs one") String paymentDetail,
		String notes,
		@Schema(description = "Version for optimistic locking; send it back when updating") Integer version,
		Instant createdAt,
		Instant updatedAt) {

	public static DepositView from(Deposit d) {
		return new DepositView(
				d.getId(),
				d.getDepositDate(),
				d.getParty().getName(),
				d.getAmount(),
				d.getPaymentMethod().getCode(),
				d.getPaymentInstrument() == null ? null : d.getPaymentInstrument().getLabel(),
				d.getNotes(),
				d.getVersion(),
				d.getCreatedAt(),
				d.getUpdatedAt());
	}
}
