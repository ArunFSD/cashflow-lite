package com.cashflowlite.api;

import com.cashflowlite.model.entity.Expense;
import com.cashflowlite.model.entity.ExpenseType;
import com.cashflowlite.model.entity.PaymentMode;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/**
 * One expense as returned by the API.
 */
@Schema(description = "An expense (money paid out)")
public record ExpenseView(
		@Schema(example = "101") Long id,
		@Schema(example = "2026-10-25") LocalDate expenseDate,
		@Schema(example = "Lunch") String name,
		@Schema(example = "190.00") BigDecimal amount,
		PaymentMode paymentMode,
		@Schema(description = "Card, UPI app or bank, when the payment mode needs one") String paymentDetail,
		ExpenseType expenseType,
		@Schema(description = "Friend or customer, for the types that need one") String partyName,
		@Schema(description = "What it was for, when the type needs a note") String purposeNote,
		String notes,
		@Schema(description = "Version for optimistic locking; send it back when updating") Integer version,
		Instant createdAt,
		Instant updatedAt) {

	public static ExpenseView from(Expense e) {
		return new ExpenseView(
				e.getId(),
				e.getExpenseDate(),
				e.getName(),
				e.getAmount(),
				e.getPaymentMethod().getCode(),
				e.getPaymentInstrument() == null ? null : e.getPaymentInstrument().getLabel(),
				e.getPurpose().getCode(),
				e.getParty() == null ? null : e.getParty().getName(),
				e.getPurposeNote(),
				e.getNotes(),
				e.getVersion(),
				e.getCreatedAt(),
				e.getUpdatedAt());
	}
}
