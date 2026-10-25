package com.cashflowlite;

import com.cashflowlite.model.entity.PaymentMode;
import com.cashflowlite.service.TransactionService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.YearMonth;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
class CashflowLiteApplicationTests {

	@Test
	void contextLoads() {
	}

	@Test
	void monthTextIsParsed() {
		assertEquals(YearMonth.of(2026, 10), TransactionService.parseMonth("2026-10"));
	}

	@Test
	void blankMonthMeansAllMonths() {
		assertNull(TransactionService.parseMonth(""));
	}

	@Test
	void cardsCannotReceiveMoney() {
		assertFalse(PaymentMode.receivable().contains(PaymentMode.CREDIT_CARD));
		assertFalse(PaymentMode.receivable().contains(PaymentMode.DEBIT_CARD));
	}
}
